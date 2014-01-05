package jGalapagos.master;

import jGalapagos.communication.TopologyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mihej Komar
 *
 */
public class NodeContainer {

	private final TopologyNode topologyNode;
	private String algorithmName;
	private WorkerInformation workerInformation;
	private final List<Connection> connectionList = new ArrayList<Connection>();
	
	public NodeContainer(String nodeName) {
		topologyNode = new TopologyNode(nodeName);
	}

	public String getNodeName() {
		return topologyNode.getName();
	}

	public void setNodeName(String nodeName) {
		topologyNode.setName(nodeName);
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public WorkerInformation getWorkerInformation() {
		return workerInformation;
	}

	public void setWorkerInformation(WorkerInformation workerInformation) {
		this.workerInformation = workerInformation;
	}

	public List<Connection> getConnectionList() {
		return connectionList;
	}

	public TopologyNode getTopologyNode() {
		return topologyNode;
	}

	@Override
	public String toString() {
		return getNodeName();
	}

}
