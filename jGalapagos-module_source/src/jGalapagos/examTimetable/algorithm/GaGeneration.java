package jGalapagos.examTimetable.algorithm;

import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.crossover.Crossover;
import jGalapagos.examTimetable.crossover.CrossoverFactory;
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
import jGalapagos.examTimetable.parentSelection.ParentSelection;
import jGalapagos.examTimetable.parentSelection.ParentSelectionFactory;
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
 * Implementation of generation genetic algorithm that searches for suitable exam timetables. 
 * This class extends the <code>AbstractAlgorithm</code> class and can be instanced.
 */
public class GaGeneration extends AbstractAlgorithm {
	/**Writes informations on screen and text file.*/
	private final Log log = LogFactory.getLog(GaGeneration.class);
	/**Initial population of solutions that is created in class constructor and meets hard constraints.*/
	private final InitPopulation initPopulation;
	/**Reference to a parent selection method that is used in algorithm.*/
	private final ParentSelection parentSelection;
	/**Reference to a crossover method that is used in algorithm.*/
	private final Crossover crossover;
	/**Reference to a mutation method that is used in algorithm.*/
	private final Mutation mutation;
	/**Reference to a fast local search.*/
	private final LocalSearch localSearchOfAll;
	/**Reference to a slow but better local search.*/
	private final LocalSearch localSearchOfBest;
	private final ConstantData constantData;
	private final RandomGenerator random;
	private final AtomicBoolean stopRequested;
	
	/**
	 * Generation genetic algorithm constructor.
	 * @param configuration	Algorithm configuration data
	 * @param constantData	Constant data about problem
	 * @param statistics	Implementation of methods that calculate statistics data
	 * @param stopRequested	Boolean variable that signals if algorithm has to stop
	 */
	public GaGeneration(Configuration configuration, ConstantData constantData, AlgorithmStatistics statistics, AtomicBoolean stopRequested) {
		super(statistics);
		this.constantData = constantData;
		this.random = new MTFRandomGenerator();
		this.stopRequested = stopRequested;
		
		initPopulation = InitPopulationFactory.getInstance(constantData, random, configuration.subset("initPopulation"), stopRequested);
		parentSelection = ParentSelectionFactory.getInstance(random, configuration.subset("parentSelection"));
		crossover = CrossoverFactory.getInstance(constantData, random, configuration.subset("crossover"));
		mutation = MutationFactory.getInstance(constantData, random, configuration.subset("mutation"));
		localSearchOfAll = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfAll"));
		localSearchOfBest = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfBest"));
	}
	
	@Override
	public void runAlgorithm() {
		// init data
		final UnsortedPopulation<ConstantTimetable> population = new UnsortedPopulation<ConstantTimetable>(initPopulation, constantData, ConstantTimetable.class);
		final UnsortedPopulation<ConstantTimetable> nextPopulation = new UnsortedPopulation<ConstantTimetable>(initPopulation.getPopulationSize(), ConstantTimetable.class);
		final VariableTimetable child = new VariableTimetable(constantData);
		final ConstantTimetable[] parents = new ConstantTimetable[2];
		int iterationCount = 0;
		
		// start algorithm
		solutionQueue.clear();
		startStatistic();
		while(!stopRequested.get()) {
			
			nextPopulation.removeAll();
			
			// update statistics
			iterationCount++;
			this.generateStatistics(population, iterationCount);
			
			// elitism
			nextPopulation.add(population.getBest(), Replace.ADD);
			
			// update population with foreign timetables
			while(!solutionQueue.isEmpty()) {
				ConstantTimetable timetable = (ConstantTimetable) solutionQueue.remove();
				if (nextPopulation.getPopulationCount() < initPopulation.getPopulationSize() && !nextPopulation.contains(timetable)) {
					nextPopulation.add(timetable, Replace.ADD);
					log.info("Foreign timetable (" + timetable + ") added to population");
				}
			}
			
			// create new generation
			while(nextPopulation.getPopulationCount() < population.getPopulationCount()) {

				// select parents
				 parentSelection.selectTwoParents(population, parents);
					
				// create child
				crossover.crossover(parents[0], parents[1], child);
				
				// mutate child
				mutation.mutate(child);
				
				// already exists?
				if (nextPopulation.contains(child)) {
					continue;
				}
				
				// local search of all children
				localSearchOfAll.startSearch(child);		
				
				// again, already exists?
				if (nextPopulation.contains(child)) {
					continue;
				}
				
				// add to population
				ConstantTimetable constantChild = child.getConstantTimetable();
				nextPopulation.add(constantChild, Replace.ADD);
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
			
			// report best timetable in current generation
			setBestTimetable(population.getBest());
		}
	}
}


