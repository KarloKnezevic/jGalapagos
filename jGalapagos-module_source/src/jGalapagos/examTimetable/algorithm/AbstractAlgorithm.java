package jGalapagos.examTimetable.algorithm;

import jGalapagos.core.Algorithm;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.Population;
import jGalapagos.examTimetable.util.ArrayUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author Mihej Komar
 * @author Đorđe Grbić
 * 
 * Implements Algorithm interface and defines an abstraction class
 * to Exam timetabling algorithm solvers. 
 *
 */
public abstract class AbstractAlgorithm implements Algorithm{
	
	/**Queue of solutions that will be injected in the next iteration of algorithm*/
	protected final ConcurrentLinkedQueue<Solution> solutionQueue;
	/**Reference to best solution found in the population so far*/
	private final AtomicReference<ConstantTimetable> bestTimetable = new AtomicReference<ConstantTimetable>();
	/**Keeps algorithm population statistics informations*/
	private final AlgorithmStatistics statistics;
	/**Defines population statistics update interval in milliseconds */
	private long updateStatsInterval;
	/**Time of last statistics update in milliseconds*/
	private long lastUpdate;
	
	public AbstractAlgorithm(AlgorithmStatistics statistics) {
		solutionQueue = new ConcurrentLinkedQueue<Solution>();
		this.statistics = statistics;
		updateStatsInterval = 10000;
		lastUpdate = 0;
	}
	
	@Override
	public void receiveForeignSolutions(List<Solution> solutionList) {
		for(int i = 0; i < solutionList.size(); i++){
			Solution solution = solutionList.get(i);
			if (solution != null) {
				solutionQueue.add(solution);
			}
		}
	}

	@Override
	public List<Solution> getSolutionsToSend() {
		List<Solution> solutionsToSend = new ArrayList<Solution>();
		Solution timetableToSend = bestTimetable.get();
		if (timetableToSend != null) {
			solutionsToSend.add(timetableToSend);
		}
		return solutionsToSend;
	}
	/**
	 * Starts statistics update timer
	 */
	protected void startStatistic() {
		statistics.startStatistic();
	}
	/**
	 * Sets best timetable found so far and informs the master about it.
	 * @param timetable	timetable reference that will be checked if it's best found so far
	 */
	protected void setBestTimetable(ConstantTimetable timetable) {
		if(timetable == null) return;
		if (bestTimetable.get() == null || bestTimetable.get().isWorseThan(timetable.getFitness())) {			
			bestTimetable.set(timetable);
			statistics.addBestSolution(timetable);
			
		}
	}
	/**
	 * Generates statistics out of <code>population</code> and <code>iterationCount</code> if the timer allows it
	 * and sends it to the master. Statistics informations are: best fitness, worst fitness, average fitness,
	 * standard deviation of fitness in population.
	 * @param population	population from which statistics will be generated 
	 * @param iterationCount	number of iterations passed during algorithm run
	 */
	protected void generateStatistics(Population<ConstantTimetable> population, int iterationCount){
		if(System.currentTimeMillis() - lastUpdate < updateStatsInterval ){
			return;
		}
		double[] bestFitness = population.getBestFitness();
		double[] worstFitness = population.getWorstFitness();
		double[] averageFitness = population.getAverageFitness();
		double standardDeviation = 0;
		for(int i = 0; i < population.getPopulationCount(); i++){
			double diff = population.get(i).getFitness()[0] - averageFitness[0];
			standardDeviation += diff*diff;
		}
		standardDeviation /= (population.getPopulationCount()-1);
		standardDeviation = Math.pow(standardDeviation, 0.5);
		statistics.addPopulationStat(bestFitness, worstFitness, averageFitness, ArrayUtilities.toArray(standardDeviation), iterationCount);
		lastUpdate = System.currentTimeMillis();
	}
	
	/**
	 * Creates statistics based only on iterations passed during algorithm run.
	 * @param iterationCount	number of iterations passed during algorithm run
	 */
	protected void generateStatistics(int iterationCount){
		if(System.currentTimeMillis() - lastUpdate < updateStatsInterval ){
			return;
		}
		statistics.addPopulationStat(ArrayUtilities.toArray(0), ArrayUtilities.toArray(0), ArrayUtilities.toArray(0), ArrayUtilities.toArray(0), iterationCount);
		lastUpdate = System.currentTimeMillis();
	}
}
