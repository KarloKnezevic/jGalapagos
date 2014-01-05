package jGalapagos.master;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

public class WorkDescription {
	
	private final MasterComunicator masterComunicator;
	private Serializable problemDescription;
	private final Map<String, PropertiesConfiguration> algorithmConfiguration = new HashMap<String, PropertiesConfiguration>();
	private final List<WorkerInformation> workerInformationList = new ArrayList<WorkerInformation>();
	private final List<NodeContainer> nodeContainerList = new ArrayList<NodeContainer>();
	private int maxInactivityMinutes = 0;
	private int maxDuration = 0;
	private int maxRounds = 30;

	private final List<AlgorithmListener> algorithmListenerList = new ArrayList<AlgorithmListener>();
	private final List<WorkerInformationListener> workerInformationListenerList = new ArrayList<WorkerInformationListener>();
	private final List<NodeContainerListener> nodeContainerListenerList = new ArrayList<NodeContainerListener>();
	
	public WorkDescription(ModuleContainer module) throws IOException {
		masterComunicator = new MasterComunicator(module, this);
	}

	public MasterComunicator getMasterComunicator() {
		return masterComunicator;
	}

	public Serializable getProblemDescription() {
		return problemDescription;
	}

	public void setProblemDescription(Serializable problemDescription) {
		this.problemDescription = problemDescription;
	}

	public Map<String, PropertiesConfiguration> getAlgorithmConfiguration() {
		return algorithmConfiguration;
	}

	public List<WorkerInformation> getWorkerInformationList() {
		return workerInformationList;
	}

	public List<NodeContainer> getNodeContainerList() {
		return nodeContainerList;
	}
	
	public void addAlgoithmListener(AlgorithmListener algorithmListener) {
		algorithmListenerList.add(algorithmListener);
	}
	
	public void addWorkerInformationListener(WorkerInformationListener workerInformationListener) {
		workerInformationListenerList.add(workerInformationListener);
	}
	
	public void addNodeContainerListener(NodeContainerListener nodeContainerListener) {
		nodeContainerListenerList.add(nodeContainerListener);
	}
	
	public int getMaxInactivityMinutes() {
		return maxInactivityMinutes;
	}

	public void setMaxInactivityMinutes(int maxInactivityMinutes) {
		this.maxInactivityMinutes = maxInactivityMinutes;
	}

	public int getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(int maxDuration) {
		this.maxDuration = maxDuration;
	}

	public int getMaxRounds() {
		return maxRounds;
	}

	public void setMaxRounds(int maxRounds) {
		this.maxRounds = maxRounds;
	}
	
	public boolean nodeNameExists(String name){
		for (NodeContainer nodeContainer : nodeContainerList) {
			if(nodeContainer.getNodeName().contentEquals(name)){
				return true;
			}
		}
		return false;
	}

	public void fireAlgorithmChanged() {
		for (AlgorithmListener algorithmListener : algorithmListenerList) {
			algorithmListener.changed();
		}
	}
	
	public void fireWorkerInformationChanged() {
		for (WorkerInformationListener workerInformationListener : workerInformationListenerList) {
			workerInformationListener.changed();
		}
	}
	
	public void fireNodeContainerChanged() {
		for (NodeContainerListener nodeContainerListener : nodeContainerListenerList) {
			nodeContainerListener.changed();
		}
	}
	
	public static interface AlgorithmListener {
		
		public void changed();
		
	}
	
	public static interface WorkerInformationListener {
		
		public void changed();
		
	}
	
	public static interface NodeContainerListener {
		
		public void changed();
		
	}

}
