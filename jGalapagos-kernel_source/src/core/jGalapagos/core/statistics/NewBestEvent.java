package jGalapagos.core.statistics;

import jGalapagos.communication.TopologyNode;


public class NewBestEvent extends AbstractEvent {
	private static final long serialVersionUID = 1L;
	private double[] fitness;
	
	public NewBestEvent(long time, double[] newBestFitness, TopologyNode node) {
		super(time, node);
		this.fitness = newBestFitness;
		
	}
	
	public double[] getSolutionFitness(){
		return fitness;
	}
}
