package jGalapagos.gui.experiments;

import jGalapagos.WorkDescriptionForWorker;
import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Solution;
import jGalapagos.master.Connection;
import jGalapagos.master.MasterComunicator;
import jGalapagos.master.ModuleContainer;
import jGalapagos.master.NodeContainer;
import jGalapagos.master.StatisticsCollector;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkerInformation;
import jGalapagos.master.WorkerStatus;
import jGalapagos.worker.ObjectUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Mihej Komar
 *
 */
public class Experiment {
	
	private final PropertiesConfiguration experimentConfiguration;
	private final String path;
	private final ModuleContainer moduleContainer;
	private final Serializable problemDescription;
	
	public Experiment(ModuleContainer moduleContainer, File experimentFile) throws Exception {
		this.moduleContainer = moduleContainer;
		path = experimentFile.getAbsolutePath();
		experimentConfiguration = new PropertiesConfiguration(experimentFile);
		byte[] problemDescriptionData = FileUtils.readFileToByteArray(new File(experimentConfiguration.getString("problemDescription")));
		problemDescription = ObjectUtilities.toObject(moduleContainer.getUrlClassLoader(), problemDescriptionData);
	}

	public Map<WorkerInformation, WorkDescriptionForWorker> createWorkDescriptionsByWorkers(WorkDescription workDescription) {
		Map<WorkerInformation, WorkDescriptionForWorker> workDescriptionsByWorkers = new HashMap<WorkerInformation, WorkDescriptionForWorker>();
		for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
			WorkDescriptionForWorker workDescriptionForWorker = new WorkDescriptionForWorker(problemDescription, moduleContainer.getConfig().getString("coreImplementationClass"));
			for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
				if (nodeContainer.getWorkerInformation().equals(workerInformation)) {
					TopologyNode topologyNode = nodeContainer.getTopologyNode();
					String algorithmName = nodeContainer.getAlgorithmName(); 
					PropertiesConfiguration configuration = workDescription.getAlgorithmConfiguration().get(algorithmName);
					ByteArrayOutputStream configurationByteStream = new ByteArrayOutputStream();
					try {
						configuration.save(configurationByteStream);
					} catch (ConfigurationException e1) {
						e1.printStackTrace();
					}
					byte[] configurationData = configurationByteStream.toByteArray();
					workDescriptionForWorker.getAlgorithmConfigurationsByNode().put(topologyNode, configurationData);
					Map<TopologyNode, Integer> connectionMap = new HashMap<TopologyNode, Integer>();
					for (Connection connection : nodeContainer.getConnectionList()) {
						TopologyNode toNode = connection.getNodeContainer().getTopologyNode();
						Integer interval = connection.getInterval();
						connectionMap.put(toNode, interval);
					}
					workDescriptionForWorker.getConnectionsMap().put(topologyNode, connectionMap);
				}
			}
			workDescriptionsByWorkers.put(workerInformation, workDescriptionForWorker);
		}
		return workDescriptionsByWorkers;
	}
	
	public WorkDescription createWorkDescription() throws Exception {
		String[] workers = experimentConfiguration.getStringArray("workers");
		int workerCount = workers.length;
		
		// create work description
		System.out.println("Creating work description... ");
		final WorkDescription workDescription = new WorkDescription(moduleContainer);
		workDescription.setMaxDuration(experimentConfiguration.getInt("maxDuration"));
		workDescription.setMaxInactivityMinutes(experimentConfiguration.getInt("maxInactivity"));
		workDescription.setMaxRounds(experimentConfiguration.getInt("maxRounds"));
		final MasterComunicator masterComunicator = workDescription.getMasterComunicator();
		workDescription.addWorkerInformationListener(new WorkDescription.WorkerInformationListener() {
			
			Map<WorkerInformation, WorkerStatus> workerStatusMap = new HashMap<WorkerInformation, WorkerStatus>();
			
			@Override
			public void changed() {
				for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
					WorkerStatus oldWorkerStatus = workerStatusMap.get(workerInformation);
					
					if (oldWorkerStatus == null || oldWorkerStatus != workerInformation.getWorkerStatus()) {
						workerStatusMap.put(workerInformation, workerInformation.getWorkerStatus());
						System.out.println(workerInformation.getTcpConnection().getRemoteSocketAddress() + ": " + workerInformation.getWorkerStatus());
					}
				}
			}
		});

		// load problem description
		System.out.println("Loading problem description... ");
		byte[] problemDescriptionData = FileUtils.readFileToByteArray(new File(experimentConfiguration.getString("problemDescription")));
		Serializable problemDescription = ObjectUtilities.toObject(moduleContainer.getUrlClassLoader(), problemDescriptionData);
		workDescription.setProblemDescription(problemDescription);
		
		// load algorithm configurations
		System.out.println("Loading algorithm configurations... ");
		PropertiesConfiguration algorithmConfiguration = new PropertiesConfiguration(experimentConfiguration.getString("algorithmConfigurations"));
		String[] algorithms = algorithmConfiguration.getStringArray("algorithms");
		for (String algorithm : algorithms) {
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
			propertiesConfiguration.append(algorithmConfiguration.subset(algorithm));
			workDescription.getAlgorithmConfiguration().put(algorithm, propertiesConfiguration);
		}
		
		// waiting for connections
		System.out.println("Waiting for " + workers.length + " workers... ");
		String listenOn = experimentConfiguration.getString("listenOn");
		workDescription.addWorkerInformationListener(new WorkDescription.WorkerInformationListener() {
			
			@Override
			public void changed() {
				synchronized (workDescription) {
					workDescription.notifyAll();
				}
			}
		});
		masterComunicator.setWaitingForConnections(Integer.parseInt(listenOn.split(":")[1]), InetAddress.getByName(listenOn.split(":")[0]));
		while(workDescription.getWorkerInformationList().size() < workerCount) {
			synchronized (workDescription) {
				workDescription.wait();
			}
		}
		masterComunicator.stopWaitingForConnections();
		
		// update workers
		System.out.println("Updating workers... ");
		List<WorkerInformation> preparedWorkers = new ArrayList<WorkerInformation>();
		while(true) {
			masterComunicator.updateImplStatus();
			Thread.sleep(1000);
			preparedWorkers.clear();
			for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
				if (workerInformation.getWorkerStatus() == WorkerStatus.READY_NEW_IMPL) {
					preparedWorkers.add(workerInformation);
				}
			}
			if (preparedWorkers.size() >= workerCount) break;
			masterComunicator.updateImpl();
		}
		
		
		
		// prepare work
		System.out.println("Preparing work for workers... ");
		Map<String, NodeContainer> nodeContainers = new HashMap<String, NodeContainer>();
		for (int i = 0; i < workerCount; i++) {
			String workerName = workers[i];
			String[] nodeNames = experimentConfiguration.subset("workers").getStringArray(workerName);
			for (int j = 0; j < nodeNames.length; j++) {
				String nodeName = nodeNames[j];
				if (nodeContainers.containsKey(nodeName)) {
					System.out.println("Duplicate name: " + nodeName);
					System.exit(-1);
				} else {
					NodeContainer nodeContainer = new NodeContainer(nodeName);
					nodeContainer.setAlgorithmName(experimentConfiguration.getString("nodes." + nodeName + ".algorithm"));
					nodeContainer.setWorkerInformation(preparedWorkers.get(i));
					nodeContainers.put(nodeName, nodeContainer);
				}
			}
		}
		for (String connectionName : experimentConfiguration.getStringArray("connections")) {
			String[] connectionData = experimentConfiguration.subset("connections").getStringArray(connectionName);
			NodeContainer destination = nodeContainers.get(connectionData[1]);
			if (destination == null) {
				System.out.println("Cannot find destination node " + connectionData[1] + " at connection " + connectionName);
			}
			Connection connection = new Connection(destination);
			connection.setInterval(Integer.parseInt(connectionData[2]));
			
			NodeContainer source = nodeContainers.get(connectionData[0]);
			if (destination == null) {
				System.out.println("Cannot find source node " + connectionData[0] + " at connection " + connectionName);
			}
			source.getConnectionList().add(connection);
		}
		workDescription.getNodeContainerList().addAll(nodeContainers.values());
		
		// create statistics collector
		createStatisticsCollector(masterComunicator, experimentConfiguration);
		masterComunicator.addRoundCompletionListener(new MasterComunicator.RoundCompletionListener() {
			
			@Override
			public void roundCompleted(Solution finalSolution, boolean noMoreRounds) {
				System.out.println("Round complete");
				// save data and stop old statistics collector
				StatisticsCollector oldStatisticsCollector = statisticsCollectorRef.get();
				oldStatisticsCollector.log();
				oldStatisticsCollector.close();
				masterComunicator.removeStatisticsListener(oldStatisticsCollector);
				
				if (noMoreRounds == false) {
					// create new statistics collector
					createStatisticsCollector(masterComunicator, experimentConfiguration);
				}
			}
		});
		
		masterComunicator.addWorkerExceptionListener(new MasterComunicator.WorkerExceptionListener() {
			
			@Override
			public void exceptionThrown(TopologyNode worker, Exception e) {
				System.out.println("Exception at worker " + worker.getName());
				e.printStackTrace();
			}
		});
		
		return workDescription;
	}
	
	private final AtomicReference<StatisticsCollector> statisticsCollectorRef = new AtomicReference<StatisticsCollector>();

	private final void createStatisticsCollector(MasterComunicator masterComunicator, PropertiesConfiguration experimentConfiguration) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		File directoryToSave = new File(experimentConfiguration.getString("saveResultsTo"));
		if (!directoryToSave.exists()) directoryToSave.mkdirs();
		String path = experimentConfiguration.getString("saveResultsTo") + "/" + dateFormat.format(new Date()) + ".txt";
		StatisticsCollector statisticsCollector = new StatisticsCollector(path);
		masterComunicator.addStatisticsListener(statisticsCollector);
		statisticsCollectorRef.set(statisticsCollector);
	}
	
	@Override
	public String toString() {
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Experiment other = (Experiment) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	
}