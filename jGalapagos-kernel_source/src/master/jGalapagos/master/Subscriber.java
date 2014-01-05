package jGalapagos.master;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.statistics.StatDataType;

import org.jfree.data.time.TimeSeries;

public class Subscriber {
	
	private TimeSeries timeSeries;
	private TopologyNode toplogyNode;
	private StatDataType intrestData;
	private int dimension = 0;
	
	public Subscriber(TimeSeries timeSeries, TopologyNode node, StatDataType dataType, int dimension){
		this.timeSeries = timeSeries;
		this.toplogyNode = node;
		this.intrestData = dataType;
		this.dimension = dimension;
	}
	
	public TimeSeries getTimeSeries(){
		return timeSeries;
	}
	public TopologyNode getNode() {
		return toplogyNode;
	}
	public StatDataType getDataType() {
		return intrestData;
	}
	public int getFitnessInterestDimension(){
		return dimension;
	}

}
