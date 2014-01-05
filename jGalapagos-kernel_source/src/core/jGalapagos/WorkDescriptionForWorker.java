package jGalapagos;

import jGalapagos.communication.TopologyNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Mihej Komar
 *
 */
public class WorkDescriptionForWorker implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final Serializable problemDescription;
	private final Map<TopologyNode, byte[]> algorithmConfigurationsByNode = new HashMap<TopologyNode, byte[]>();
	private final Map<TopologyNode, Map<TopologyNode, Integer>> connectionsMap = new HashMap<TopologyNode, Map<TopologyNode,Integer>>();
	private final String moduleClass;
	
	public WorkDescriptionForWorker(Serializable problemDescription, String moduleClass) {
		this.problemDescription = problemDescription;
		this.moduleClass = moduleClass;
	}

	public Serializable getProblemDescription() {
		return problemDescription;
	}

	public Map<TopologyNode, byte[]> getAlgorithmConfigurationsByNode() {
		return algorithmConfigurationsByNode;
	}

	public Map<TopologyNode, Map<TopologyNode, Integer>> getConnectionsMap() {
		return connectionsMap;
	}

	public String getModuleClass() {
		return moduleClass;
	}

}
