package jGalapagos.util;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Algorithm;
import jGalapagos.core.Module;
import jGalapagos.core.statistics.Statistics;
import jGalapagos.master.Connection;
import jGalapagos.master.NodeContainer;
import jGalapagos.worker.ObjectUtilities;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

public class ExperimentConfigHelper {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int workerCount = 3;
		int algorithmsPerWorker = 4;
		String[] algorithmNames = new String[] { "GaGeneration1", "GaSteadyState1", "Mmas1", "Sia1", "Hs1" };
		
//		List<NodeContainer> nodeContainerList = NetworkUtils.createCompleteTopology(algorithmNames, workerCount * algorithmsPerWorker, 5000);
//		List<NodeContainer> nodeContainerList = NetworkUtils.createToroidTopology(algorithmNames, 3, 4, 5000);
//		List<NodeContainer> nodeContainerList = NetworkUtils.createRingTopology(algorithmNames, workerCount * algorithmsPerWorker, 5000);
		List<NodeContainer> nodeContainerList = NetworkUtils.createIslandTopology(algorithmNames, 3, 4, 5000, 25000);
		
		String experiment = createExperiment(
				"file:implementations/examTimetable/distributedAlgorithmsCoreImpl.jar",
				"data/experiments/komar/problemDescription_ljetni-zi.dat",
				"data/experiments/komar/algorithm.conf",
				0,
				30,
				30,
				"D:/Eksperiment_komar/exp24",
				"161.53.77.161:10006",
				workerCount,
				algorithmsPerWorker,
				algorithmNames,
				nodeContainerList,
				false);
		
		FileUtils.writeStringToFile(new File("data/experiments/komar/exp24"), experiment);
	}
	
	public static enum Topology {
		TREE, COMPLETE, RING, ISLAND, TOROID, UNCONNECTED
	}
	
	public static final String createExperiment(String implementation, String problemDescription, String algorithmConfigurations, int maxInactivity, int maxDuration, int maxRounds, 
			String saveResultsTo, String listenOn, int workerCount, int algorithmsPerWorker, String[] algorithmNames, List<NodeContainer> nodeContainerList, boolean test) throws Exception {
		if (workerCount < 1) throw new IllegalArgumentException("Worker count must be greater then 0");
		if (algorithmsPerWorker < 1) throw new IllegalArgumentException("Algorithms per worker must be greater then 0");
		
		StringBuilder out = new StringBuilder();
		out.append("# putanje do drugih potrebnih datoteka\n");
		out.append("implementation = ").append(implementation).append("\n");
		out.append("problemDescription = ").append(problemDescription).append("\n");
		out.append("algorithmConfigurations = ").append(algorithmConfigurations).append("\n");
		out.append("\n");
		
		out.append("# postavke zaustavljanja\n");
		out.append("maxInactivity = ").append(maxInactivity).append("\n");
		out.append("maxDuration = ").append(maxDuration).append("\n");
		out.append("maxRounds = ").append(maxRounds).append("\n");
		out.append("saveResultsTo = ").append(saveResultsTo).append("\n");
		out.append("\n");
		
		out.append("# slušanje radnika\n");
		out.append("listenOn = ").append(listenOn).append("\n");
		out.append("\n");
		
		out.append("# lista cvorova i radnika\n");
		out.append("nodes = node1");
		for (int i = 2; i <= workerCount * algorithmsPerWorker; i++) {
			out.append(",node").append(i);
		}
		out.append("\n");
		out.append("workers = worker1");
		for (int i = 2; i <= workerCount; i++) {
			out.append(",worker").append(i);
		}
		out.append("\n\n");
		
		out.append("# raspodjela cvorova po radnicima\n");
		for (int i = 0; i < workerCount; i++) {
			out.append("workers.worker").append(i + 1).append(" = node").append(i * algorithmsPerWorker + 1);
			for (int j = 2; j <= algorithmsPerWorker; j++) {
				out.append(",node").append(i * algorithmsPerWorker + j);
			}
			out.append("\n");
		}
		out.append("\n");
		
		out.append("# postavljanje algoritama po cvorovima\n");
		int connectionCount = 0;
		for (int i = 0; i < nodeContainerList.size(); i++) {
			NodeContainer nodeContainer = nodeContainerList.get(i);
			connectionCount += nodeContainer.getConnectionList().size();
			out.append("nodes.node").append(i + 1).append(".algorithm = ").append(nodeContainer.getAlgorithmName()).append("\n");
		}
		out.append("\n");
		
		if (connectionCount > 0) {
			out.append("# migracijski parametri\n");
			out.append("connections = conn1");
			for (int i = 2; i <= connectionCount; i++) {
				out.append(",conn").append(i);
			}
			out.append("\n");
			
			int currentConnection = 1;
			for (NodeContainer nodeContainer : nodeContainerList) {
				for (Connection connection : nodeContainer.getConnectionList()) {
					out.append("connections.conn").append(currentConnection++).append(" = ");
					out.append(nodeContainer.getNodeName()).append(",").append(connection.getNodeContainer().getNodeName());
					out.append(",").append(connection.getInterval()).append("\n");
				}
			}
		}
		
		if (test) {
			URL url = new URL(implementation);
			URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url });
			Class<?> classApp = Class.forName("hr.fer.zemris.distributedAlgorithms.CoreImpl", true, urlClassLoader);
			Object object = classApp.newInstance();
			Module core = (Module) object;
			byte[] problemDescriptionData = FileUtils.readFileToByteArray(new File(problemDescription));
			Serializable problemDescriptionObject = ObjectUtilities.toObject(urlClassLoader, problemDescriptionData);
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(algorithmConfigurations);
			for (final String algorithmName : algorithmNames) {
				Configuration algorithmConfiguration = propertiesConfiguration.subset(algorithmName);
				AtomicBoolean stopRequested = new AtomicBoolean();
				final Algorithm algorithm = core.createAlgorithm(algorithmConfiguration, problemDescriptionObject, new Statistics(TopologyNode.getMaster()), stopRequested);
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						algorithm.runAlgorithm();
					}
				});
				System.out.println("Starting algorithm " + algorithmName + " for 5s...");
				Thread.sleep(5000);
				System.out.println("Waiting for 2s to stop...");
				stopRequested.set(true);
				Thread.sleep(2000);
				if (thread.isAlive()) {
					System.out.println("WARNING: algorithm not stopped!!");
					thread.interrupt();
				} else {
					System.out.println("Algorithm stopped");
				}
			}
		}
		
		return out.toString();
	}

}
