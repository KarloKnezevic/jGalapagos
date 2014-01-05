package jGalapagos.core.statistics;

import jGalapagos.communication.TopologyNode;

import java.io.Serializable;



public abstract class AbstractEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	protected long time;
	private TopologyNode node;

	
	public AbstractEvent(long time, TopologyNode node) {
		this.time = time;
		this.node = node;
	}
	
	public long getTime(){
		return time;
	}
	
	public TopologyNode getNode(){
		return node;
	}
	
}
