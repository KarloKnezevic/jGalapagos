package jGalapagos.examTimetable.algorithm;

import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.initPopulation.InitPopulation;
import jGalapagos.examTimetable.initPopulation.InitPopulationFactory;
import jGalapagos.examTimetable.localSearch.LocalSearch;
import jGalapagos.examTimetable.localSearch.LocalSearchFactory;
import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.UnsortedPopulation;
import jGalapagos.examTimetable.model.UnsortedPopulation.Replace;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.mutation.Mutation;
import jGalapagos.examTimetable.mutation.MutationFactory;
import jGalapagos.examTimetable.util.MTFRandomGenerator;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 
 * @author Đorđe Grbić
 * @author Mihej Komar
 * 
 * Implementation of simple immune algorithm that searches for suitable exam timetables. 
 * This class extends the <code>AbstractAlgorithm</code> class and can be instanced.
 */
public final class Sia extends AbstractAlgorithm {
	/**Writes informations on screen and text file.*/
	private final Log log = LogFactory.getLog(Sia.class);
	/**Initial population of solutions that is created in class
	 * constructor and meets hard constraints.*/
	private final InitPopulation initPopulation;
	/**Reference to a mutation method that is used in algorithm.*/
	private final Mutation mutation;
	private final int cloneMultiplication;
	/**Reference to a fast local search.*/
	private final LocalSearch localSearchOfAll;
	/**Reference to a slow but better local search.*/
	private final LocalSearch localSearchOfBest;
	private final ConstantData constantData;
	private final RandomGenerator random;
	private final AtomicBoolean stopRequested;
	
	
	/**
	 * Simple immune algorithm algorithm constructor.
	 * @param configuration	Algorithm configuration data
	 * @param constantData	Constant data about problem
	 * @param statistics	Implementation of methods that calculate statistics data
	 * @param stopRequested	Boolean variable that signals if algorithm has to stop
	 */
	public Sia(Configuration configuration, ConstantData constantData, AlgorithmStatistics statistics, AtomicBoolean stopRequested) {
		super(statistics);
		this.constantData = constantData;
		this.random = new MTFRandomGenerator();
		this.stopRequested = stopRequested;
		
		initPopulation = InitPopulationFactory.getInstance(constantData, random, configuration.subset("initPopulation"), stopRequested);
		localSearchOfAll = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfAll"));
		localSearchOfBest = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfBest"));
		cloneMultiplication = configuration.getInt("cloneMultiplication");
		mutation = MutationFactory.getInstance(constantData, random, configuration.subset("mutation"));
	}
	
	@Override
	public void runAlgorithm() {
		// init data
		final UnsortedPopulation<ConstantTimetable> population = new UnsortedPopulation<ConstantTimetable>(initPopulation, constantData, ConstantTimetable.class);
		final UnsortedPopulation<ConstantTimetable> nextPopulation = new UnsortedPopulation<ConstantTimetable>(initPopulation.getPopulationSize(), ConstantTimetable.class);
		final VariableTimetable child = new VariableTimetable(constantData);
		int iterationCount = 0;
		
		// start algorithm
		solutionQueue.clear();
		startStatistic();
		while (!stopRequested.get()) {

			nextPopulation.removeAll();
			
			// update statistics
			this.generateStatistics(population, iterationCount);
			iterationCount++;
			
			nextPopulation.makeEqual(population);
			
			// update population with foreign timetables
			while (!solutionQueue.isEmpty()) {
				ConstantTimetable timetable = (ConstantTimetable) solutionQueue.remove();
				if (!nextPopulation.contains(timetable)) {
					nextPopulation.add(timetable, Replace.WORST);
					log.info("Foreign timetable (" + timetable + ") added to population");
				}
			}
			
			for (int i = 0; i < initPopulation.getPopulationSize(); i++) {
				for (int j = 0; j < cloneMultiplication; j++) {
					child.makeMeEqualAs(population.get(i));

					// mutate child
					mutation.mutate(child);

					// already exists?
					if (nextPopulation.contains(child)) {
						continue;
					}

					localSearchOfAll.startSearch(child);

					// again, already exists?
					if (nextPopulation.contains(child)) {
						continue;
					}

					// add to population
					ConstantTimetable constantChild = child.getConstantTimetable();
					nextPopulation.add(constantChild, Replace.WORST);
				}
			}
			
			// local search of best
			ConstantTimetable bestTimetable = nextPopulation.getBest();
			if (!bestTimetable.equals(population.getBest())) {
				child.makeMeEqualAs(bestTimetable);
				localSearchOfBest.startSearch(child);
				bestTimetable = child.getConstantTimetable();
				nextPopulation.add(bestTimetable, Replace.BEST);
			}
			
			population.makeEqual(nextPopulation);

			// found new best
			setBestTimetable(population.getBest());
			generateStatistics(population, iterationCount);
		}
	}
}