package jGalapagos.gui.running;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.statistics.StatDataType;
import jGalapagos.gui.DynamicGraph;
import jGalapagos.gui.Tab;
import jGalapagos.master.StatisticsCollector;
import jGalapagos.master.WorkDescription;

import java.util.List;

import javax.swing.JComponent;

public class GraphTab implements Tab {
	
//	private final StatisticsCollector statisticsCollector;
//	private final WorkDescription workDescription;
	private final DynamicGraph graph;
	private int  plotDimension = 0;
	
	public GraphTab(StatisticsCollector statisticsCollector, WorkDescription workDescription, TopologyNode selectedTopologyNode, List<StatDataType> selectedStatDataTypes, int dimensionInterest) {
//		this.statisticsCollector = statisticsCollector;
//		this.workDescription = workDescription;
		plotDimension = dimensionInterest;
		this.graph = new DynamicGraph(statisticsCollector, getName());
		for(int i = 0; i < selectedStatDataTypes.size(); i++){
			graph.plot(selectedTopologyNode, selectedStatDataTypes.get(i), plotDimension);
		}
	}
	
	public GraphTab(StatisticsCollector statisticsCollector, WorkDescription workDescription, List<TopologyNode> selectedTopologyNodes, StatDataType selectedDataType, int dimensionInterest){
//		this.statisticsCollector = statisticsCollector;
//		this.workDescription = workDescription;
		plotDimension = dimensionInterest;
		this.graph = new DynamicGraph(statisticsCollector, getName());

		for(int i = 0; i < selectedTopologyNodes.size(); i++){
			graph.plot(selectedTopologyNodes.get(i), selectedDataType, plotDimension);
		}
	}
	
	@Override
	public JComponent getContent() throws Exception {
		return graph;
	}
	
	@Override
	public String getName() {
		return "Graph";
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}
	
	@Override
	public void closeAction() {
		
	}

}
