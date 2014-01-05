package jGalapagos.core.statistics;

import jGalapagos.communication.TopologyNode;


public class PopulationStat extends AbstractEvent {
	private static final long serialVersionUID = 1L;
	private final double[] bestFitness;
	private final double[] worstFitness;
	private final double[] averageFitness;
	private final double[] standardDeviation;
	private final long iterationCount;
	
	public PopulationStat(long time,	double[] bestFitness, 
			double[] worstFitness, double[] averageFitness, 
			double[] standardDeviation, long iterationCount, TopologyNode node) {
		super(time, node);
		this.bestFitness = bestFitness;
		this.worstFitness = worstFitness;
		this.averageFitness = averageFitness;
		this.standardDeviation = standardDeviation;
		this.iterationCount = iterationCount;
	}
	
	public double[] getBestFitness(){
		return this.bestFitness;
	}
	
	public double[] getWorstFitness(){
		return this.worstFitness;
	}
	
	public double[] getAverageFitness(){
		return this.averageFitness;
	}
	
	public double[] getStandardDeviation(){
		return this.standardDeviation;
	}
	
	public long getIterationCount(){
		return this.iterationCount;
	}
	
}
