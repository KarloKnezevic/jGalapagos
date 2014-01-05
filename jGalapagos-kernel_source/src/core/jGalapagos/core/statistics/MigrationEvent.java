package jGalapagos.core.statistics;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Solution;

public class MigrationEvent extends AbstractEvent {
	
	private static final long serialVersionUID = 1L;
	public double[] migratedSolutionFitness;
	
	public MigrationEvent(long time, TopologyNode node, Solution solution){
		super(time, node);
		this.migratedSolutionFitness = solution.getFitness();
	}
	
	public double[] getSolutionFitness(){
		return this.migratedSolutionFitness;
	}

}
