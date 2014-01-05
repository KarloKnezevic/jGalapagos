package jGalapagos.core.statistics;


import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Solution;

import java.util.ArrayList;

public class Statistics implements AlgorithmStatistics {
	
	private static final long serialVersionUID = 1L;
	private final ArrayList<AbstractEvent> eventList;
	private Solution bestSolution;
	private long start;
	private final TopologyNode node;

	
	public Statistics(TopologyNode topologyNode) {
		eventList = new ArrayList<AbstractEvent>();
		node = topologyNode;
	}
	
	@Override
	public void startStatistic(){
		start = System.currentTimeMillis();
	}
		
	public ArrayList<AbstractEvent> getEventList(){
		return eventList;
	}
	
	public void clearEventList(){
		eventList.clear();
	}
	
	//TODO: upozoriti o tome da se može umetati samo konstantno rješenje
	@Override
	public void addBestSolution(Solution bestSolution){
		this.bestSolution = bestSolution;
		NewBestEvent event = new NewBestEvent(System.currentTimeMillis() - start, bestSolution.getFitness(), node );
//		System.out.println("["+ event.getTime()+"]" + "New best solution event: fitness = " 
//				+ event.getSolutionFitness() + "@" + event.getNode().getName());
		eventList.add(event);
	}
	
	public Solution getBestSolution(){
		return bestSolution;
	}
	
	public TopologyNode getNode() {
		return node;
	}

	@Override
	public void addPopulationStat(double[] bestFitness, double[] worstFitness, double[] averageFitness,
			double[] standardDeviation, long iterationCount){
		PopulationStat popStat = new PopulationStat(System.currentTimeMillis() - start, bestFitness, worstFitness, averageFitness, standardDeviation, iterationCount, node);
//		System.out.println("Population Stat event: ");
//		System.out.println("========================");
//		System.out.println("Average = " + popStat.getAverageFitness());
//		System.out.println("Best = " + popStat.getBestFitness());
//		System.out.println("Worst = " + popStat.getWorstFitness());
//		System.out.println("Deviation = " + popStat.getStandardDeviation());
//		System.out.println("Iteration = " + popStat.getIterationCount());
//		System.out.println("Time = " + popStat.getTime());
//		System.out.println("========================");
		eventList.add(popStat);
	}
	
	public void addMigration(Solution solution){
		MigrationEvent event = new MigrationEvent(System.currentTimeMillis(), getNode(), solution);
		eventList.add(event);
	}
	
	@Override
	public Statistics clone() {
		Statistics statistics = new Statistics(node);
		statistics.eventList.addAll(eventList);
		statistics.bestSolution = bestSolution;
		statistics.start = start;
		return statistics;
	}
	

}
