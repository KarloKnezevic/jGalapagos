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
/**
 * 
 * @author Đorđe Grbić
 * @author Mihej Komar
 * 
 * Implementation of Min-Max ant system that searches for suitable exam timetables. 
 * This class extends the <code>AbstractAlgorithm</code> class and can be instanced.
 */
public final class Mmas extends AbstractAlgorithm {
	/**Writes informations on screen and text file.*/
	private final Log log = LogFactory.getLog(Mmas.class);
	/**
	 * see http://www.scholarpedia.org/article/Ant_colony_optimization#MAX-MIN_ant_system
	 * for parameters description
	 */
	private final double rho;
	private final double alpha;
	private final double beta;
	private final double a;
	private final int antCount;
	private final Selection selection;
	/**Reference to a fast local search.*/
	private final LocalSearch localSearchOfAll;
	/**Reference to a slow but better local search.*/
	private final LocalSearch localSearchOfBest;
	private final ConstantData constantData;
	private final RandomGenerator random;
	private final AtomicBoolean stopRequested;
	
	/**
	 * Min-Max ant system constructor.
	 * @param configuration	Algorithm configuration data
	 * @param constantData	Constant data about problem
	 * @param statistics	Implementation of methods that calculate statistics data
	 * @param stopRequested	Boolean variable that signals if algorithm has to stop
	 */
	
	public Mmas(Configuration configuration, ConstantData constantData, AlgorithmStatistics statistics, AtomicBoolean stopRequested) {
		super(statistics);
		this.constantData = constantData;
		this.random = new MTFRandomGenerator();
		this.stopRequested = stopRequested;
		
		rho = configuration.getDouble("rho");
		alpha = configuration.getDouble("alpha");
		beta = configuration.getDouble("beta");
		a = configuration.getDouble("a");
		antCount = configuration.getInt("antCount");
		selection = SelectionFactory.getInstance(random, configuration.subset("selection"));
		localSearchOfAll = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfAll"));
		localSearchOfBest = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfBest"));
		
		// check data
		if (rho < 0 || rho > 1) {
			throw new IllegalArgumentException("Rho must be between 0 and 1.");
		} else if (antCount < 1 || antCount > 10000) {
			throw new IllegalArgumentException("Ant count must be between 1 and 10000.");
		}
	}
	
	@Override
	public void runAlgorithm() {
		// initialize help data
		final double oneMinusRho = 1.0 - rho;
		final int courseLength = constantData.getCourseCount();
		final int termLength = constantData.getTermCount();
		int lastUpdate = 0;
		ConstantTimetable bestTimetable = null;
		final VariableTimetable timetable = new VariableTimetable(constantData);
		for (int courseIndex = 0; courseIndex < constantData.getCourseCount(); courseIndex++) {
			int termIndex = random.nextInt(constantData.getTermCount());
			timetable.setTermIndex(courseIndex, termIndex);
		}
		double tauMax = 1.0 / (rho * timetable.getFitness()[0]);
		double tauMin = tauMax / a;
		final VariableTimetable iterationBest = new VariableTimetable(constantData);
		
		// initialize pheromone
		final double[][] pheromone = new double[courseLength][termLength];
		for (int i = 0; i < courseLength; i++) {
			for (int j = 0; j < termLength; j++) {
				pheromone[i][j] = tauMax;
			}
		}
		
		int iterationCount = 0;
		startStatistic();
		while(!stopRequested.get()) {
			
			// update statistics
			iterationCount++;
			this.generateStatistics(iterationCount);

			if (iterationCount - lastUpdate == 1000) {
				for (int i = 0; i < courseLength; i++) {
					for (int j = 0; j < termLength; j++) {
						pheromone[i][j] = tauMax;
					}
				}
				lastUpdate = iterationCount;
				log.info("<MMAS> Pheromone reset");
			}
			
			while(!solutionQueue.isEmpty()) {
				ConstantTimetable constantTimetable = (ConstantTimetable) solutionQueue.remove();
				if (bestTimetable == null || bestTimetable.isWorseThan(constantTimetable.getFitness())) {
					bestTimetable = constantTimetable;
					log.info("Foreign timetable (" + constantTimetable + ") added to population");
				}
			}
			
			// run ants
			int n = antCount;
			boolean setIterationBest = false;
			while (n > 0) {
				if (runAnt(pheromone, alpha, beta, selection, timetable) == false) {
					continue;
				}			
				localSearchOfAll.startSearch(timetable);
				
				if (!setIterationBest || timetable.isBetterThan(iterationBest.getFitness())) {
					iterationBest.makeMeEqualAs(timetable);
					setIterationBest = true;
				}
				
				n--;
			}
				
			localSearchOfBest.startSearch(iterationBest);
			
			// set best timetable
			if (bestTimetable == null || iterationBest.isBetterThan(bestTimetable.getFitness())) {
				bestTimetable = iterationBest.getConstantTimetable();
				tauMax = 1.0 / (rho * iterationBest.getFitness()[0]);
				tauMin = tauMax / a;
				lastUpdate = iterationCount;
			}
			
			// update pheromone
			double newPheromone;
			for (int i = 0; i < courseLength; i++) {
				for (int j = 0; j < termLength; j++) {
					newPheromone = oneMinusRho * pheromone[i][j];
					pheromone[i][j] = (newPheromone < tauMin) ? tauMin : newPheromone;
				}
			}
			double delta = 1.0 / bestTimetable.getFitness()[0];
			int termIndex;
			for (int i = 0; i < courseLength; i++) {
				termIndex = bestTimetable.getTermIndex(i);
				newPheromone = pheromone[i][termIndex] + delta;
				pheromone[i][termIndex] = (newPheromone < tauMin) ? tauMin : newPheromone;
			}
			
			setBestTimetable(bestTimetable);
		}
	}

	/**
	 * Method that runs virtual ant and creates new solution. 
	 * @param pheromone	Field of pheromone values between courses and terms.
	 * @param alpha see http://www.scholarpedia.org/article/Ant_colony_optimization#MAX-MIN_ant_system
	 * @param beta	see http://www.scholarpedia.org/article/Ant_colony_optimization#MAX-MIN_ant_system
	 * @param selection object containing selection methods
	 * @param timetable Object containing newly created solution after method returns true.
	 * @return	returns true if creation is successful, false otherwise.
	 */
	private boolean runAnt(double[][] pheromone, double alpha, double beta, Selection selection, VariableTimetable timetable) {
		
		// initialize help data
		final int termNumber = constantData.getTermCount();
		final int courseLength = constantData.getCourseCount();
		final int termNumberMinusOne = termNumber - 1;
		double sumOfProbabilities;
		final double[] cumulativeProbability = new double[termNumber];
		timetable.deleteAllTerms();

		for (int i = 0; i < courseLength; i++) {
			sumOfProbabilities = 0;
			for (int j = 0; j < termNumber; j++) {
				double probability = timetable.isTransferPossible(i, j) ? FastMath.pow(pheromone[i][j], alpha) : 0;
				cumulativeProbability[j] = probability;
				sumOfProbabilities += probability;
			}
			if (sumOfProbabilities == 0) {
				return false;
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
		
		return true;
	}
	
}
