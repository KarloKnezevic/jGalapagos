package jGalapagos.examTimetable.algorithm;

import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.localSearch.LocalSearch;
import jGalapagos.examTimetable.localSearch.LocalSearchFactory;
import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.selection.Selection;
import jGalapagos.examTimetable.selection.SelectionFactory;
import jGalapagos.examTimetable.util.FastMath;
import jGalapagos.examTimetable.util.MTFRandomGenerator;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class MmasOld extends AbstractAlgorithm {
	
	private final Log log = LogFactory.getLog(MmasOld.class);
	private final double rho;
	private final double alpha;
	private final double beta;
	private final double tauMax;
	private final double tauMin;
	private final int antCount;
	private final Selection selection;
	private final LocalSearch localSearchOfAll;
	private final LocalSearch localSearchOfBest;
	private final ConstantData constantData;
	private final RandomGenerator random;
	private final AtomicBoolean stopRequested;
	
	public MmasOld(Configuration configuration, ConstantData constantData, AlgorithmStatistics statistics, AtomicBoolean stopRequested) {
		super(statistics);
		this.constantData = constantData;
		this.random = new MTFRandomGenerator();
		this.stopRequested = stopRequested;
		
		rho = configuration.getDouble("rho");
		alpha = configuration.getDouble("alpha");
		beta = configuration.getDouble("beta");
		tauMax = configuration.getDouble("tauMax");
		tauMin = configuration.getDouble("tauMin");
		antCount = configuration.getInt("antCount");
		selection = SelectionFactory.getInstance(random, configuration.subset("selection"));
		localSearchOfAll = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfAll"));
		localSearchOfBest = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfBest"));

		// check data
		if (rho < 0 || rho > 1) {
			throw new RuntimeException("Rho must be between 0 and 1.");
		} else if (tauMax <= tauMin) {
			throw new RuntimeException("tauMax must be greater then tauMin.");
		} else if (antCount < 1 || antCount > 10000) {
			throw new RuntimeException("Ant count must be between 1 and 10000.");
		}
	}
	
	@Override
	public void runAlgorithm() {
		// initialize help data
		final double oneMinusRho = 1.0 - rho;
		final int courseLength = constantData.getCourseCount();
		final int termLength = constantData.getTermCount();
		ConstantTimetable bestTimetable = null;
		
		// initialize pheromone
		final double[][] pheromone = new double[courseLength][termLength];
		final double pheromoneBeginValue = Math.max(Math.min(1.0 / rho, tauMax), tauMin);
		for (int i = 0; i < courseLength; i++) {
			for (int j = 0; j < termLength; j++) {
				pheromone[i][j] = pheromoneBeginValue;
			}
		}
		
		int iterationCount = 0;
		startStatistic();
		while(!stopRequested.get()) {
			// update statistics
			iterationCount++;
			this.generateStatistics(iterationCount);
			while(!solutionQueue.isEmpty()) {
				ConstantTimetable timetable = (ConstantTimetable)solutionQueue.remove();
				if (bestTimetable == null || bestTimetable.isWorseThan(timetable.getFitness())) {
					bestTimetable = timetable;
					log.info("Foreign timetable (" + timetable + ") added to population");
				}
			}
			
			// run ants
			int sum = 0;
			int n = antCount;
			VariableTimetable iterationBest = null;
			while (n > 0) {
				VariableTimetable timetable = runAnt(pheromone, alpha, beta, selection);
				if (timetable == null) {
					continue;
				}
				
				//hill climbing of all timetables
				localSearchOfAll.startSearch(timetable);
				
				// set iteration best
				if (iterationBest == null || timetable.isBetterThan(iterationBest.getFitness())) {
					iterationBest = timetable;
				}
				
				sum += timetable.getFitness()[0];
				n--;
			}
			
			localSearchOfBest.startSearch(iterationBest);
			
			if (bestTimetable == null || iterationBest.isBetterThan(bestTimetable.getFitness())) {
				bestTimetable = iterationBest.getConstantTimetable();
			}
			
			// no timetables found
			if (bestTimetable == null) {
				continue;
			}
			
			setBestTimetable(bestTimetable);
			
			// update pheromone
			double newPheromone;
			for (int i = courseLength; --i >= 0;) {
				int termIndex = bestTimetable.getTermIndex(i);
				for (int j = 0; j < termIndex; j++) {
					newPheromone = oneMinusRho * pheromone[i][j];
					pheromone[i][j] = (newPheromone < tauMin) ? tauMin : newPheromone;
				}
				
				newPheromone = oneMinusRho * pheromone[i][termIndex];
				newPheromone++;
				pheromone[i][termIndex] = newPheromone < tauMin
							? tauMin 
							: (newPheromone > tauMax ? tauMax : newPheromone);
				
				for (int j = termIndex + 1; j < termLength; j++) {
					newPheromone = oneMinusRho * pheromone[i][j];
					pheromone[i][j] = (newPheromone < tauMin) ? tauMin : newPheromone;
				}
			}
		}
	}

	private VariableTimetable runAnt(double[][] pheromone, double alpha, double beta, Selection selection) {
		
		// initialize help data
		final int termNumber = constantData.getTermCount();
		final int courseLength = constantData.getCourseCount();
		final int termNumberMinusOne = termNumber - 1;
		double sumOfProbabilities;
		final double[] cumulativeProbability = new double[courseLength];
		final VariableTimetable timetable = new VariableTimetable(constantData);
		

		for (int i = 0; i < courseLength; i++) {
			sumOfProbabilities = 0;
			for (int j = 0; j < termNumber; j++) {
				
				// pheromone component
				double probability = FastMath.pow(pheromone[i][j], alpha);
				
				// heuristic component is 1 if hard constraints are OK, or 0 if hard constrains are not OK
				if (!timetable.isTransferPossible(i, j)) {
					probability = 0;
				}
				
				cumulativeProbability[j] = probability;
				sumOfProbabilities += probability;
			}
			if (sumOfProbabilities == 0) {
				return null;
			}
			cumulativeProbability[0] /= sumOfProbabilities;
			double last = cumulativeProbability[0];
			for (int j = 1; j < termNumberMinusOne; j++) {
				last += cumulativeProbability[j] / sumOfProbabilities;
				cumulativeProbability[j] = last;
			}
			cumulativeProbability[termNumberMinusOne] = 1.0;
			timetable.setTermIndex(i, selection.select(cumulativeProbability)[0]);
		}
		
		return timetable;
	}
	
}
