package jGalapagos;

import jGalapagos.communication.Message;
import jGalapagos.communication.ReceiveMessageListener;
import jGalapagos.communication.TcpConnection;
import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Algorithm;
import jGalapagos.core.Module;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.Statistics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Mihej Komar
 *
 */
public class WorkerController {
	
	private final Log log = LogFactory.getLog(WorkerController.class);
	private final TcpConnection tcpConnection;
	private final Module module;
	private final WorkDescriptionForWorker workDescriptionForWorker;
	private final AtomicBoolean disconnected;
	private final AtomicBoolean algorithmStopRequested = new AtomicBoolean();
	private final Map<TopologyNode, Algorithm> algorithmByTopologyNode = new HashMap<TopologyNode, Algorithm>();
	private final List<Algorithm> algorithmList = new ArrayList<Algorithm>();
	private final List<Statistics> statisticList = new ArrayList<Statistics>();
	private final List<Thread> threadList = new ArrayList<Thread>();
	private final AtomicReference<Timer> timerReference = new AtomicReference<Timer>();
	
	private int threadCount = 1;
	
	public WorkerController(final TcpConnection tcpConnection, byte[] workDescriptionByteArray, final URLClassLoader urlClassLoader, final AtomicBoolean disconnected) throws Exception {
		
		// init data
		this.tcpConnection = tcpConnection;

		workDescriptionForWorker = toObject(urlClassLoader, workDescriptionByteArray);
		module = (Module) urlClassLoader.loadClass(workDescriptionForWorker.getModuleClass()).newInstance();
		this.disconnected = disconnected;
		
		// create receive listener
		ReceiveMessageListener receiveMessageListener = new ReceiveMessageListener() {
			
			@Override
			public void receiveMessage(TcpConnection tcpConnection, Message message) {
				if (message.getMessageType() == WorkingMessageType.SOLUTION) {

					ArrayList<Solution> solutionList = message.getData(urlClassLoader);
					if (solutionList == null) {
						log.info("Received solutions from " + message.getSender() + " but unable to deserialize");
						return;
					} else {
						log.info("Received solutions from " + message.getSender());
					}
					for (TopologyNode receiver : message.getReceivers()) {
						Algorithm algorithm = algorithmByTopologyNode.get(receiver);
						if (algorithm == null) {
							continue;
						}
						algorithm.receiveForeignSolutions(solutionList);
					}
				} else if (message.getMessageType() == WorkingMessageType.START) {
					startAlgorithms();
				} else if (message.getMessageType() == WorkingMessageType.RESTART) {
					restartAlgorithms();
				} else if (message.getMessageType() == WorkingMessageType.DISCONNECT) {
					disconnected.set(true);
					synchronized (disconnected) {
						disconnected.notifyAll();
					}
				}
			}
		};
		tcpConnection.setRecieveListener(receiveMessageListener);
		initAlgorithms();
	}
	
	private void initAlgorithms() {
		algorithmByTopologyNode.clear();
		algorithmList.clear();
		statisticList.clear();
		algorithmStopRequested.set(false);
		Serializable problemDescription = workDescriptionForWorker.getProblemDescription();
		
		for (TopologyNode topologyNode : workDescriptionForWorker.getAlgorithmConfigurationsByNode().keySet()) {
			byte[] configurationData = workDescriptionForWorker.getAlgorithmConfigurationsByNode().get(topologyNode);
			Configuration configuration = getConfigurationFromByteArray(configurationData);
			Statistics statistics = new Statistics(topologyNode);
			Algorithm algorithm = null;
			try {
				algorithm = module.createAlgorithm(configuration, problemDescription, statistics, algorithmStopRequested);
			} catch (Exception e) {
				Message message = new Message(topologyNode, WorkingMessageType.EXCEPTION);
				message.getReceivers().add(TopologyNode.getMaster());
				message.setData(e);
				tcpConnection.sendMessage(message);
				continue;
			}
			algorithmList.add(algorithm);
			statisticList.add(statistics);
			algorithmByTopologyNode.put(topologyNode, algorithm);
		}
		
		sendMessageReadyForStart();
	}
	
	private void startAlgorithms() {
		
		// for debugging only
		if (threadList != null) {
			for (Thread thread : threadList) {
				if (thread.isAlive()) {
					// should never be true
					log.warn("Thread is alive");
					thread.interrupt();
				}
			}
		}
		
		// create algorithm threads, save them to list and start
		threadList.clear();
		for (final TopologyNode topologyNode : algorithmByTopologyNode.keySet()) {
			final Algorithm algorithm = algorithmByTopologyNode.get(topologyNode);
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						algorithm.runAlgorithm();
					} catch (Exception e) {
						Message message = new Message(topologyNode, WorkingMessageType.EXCEPTION);
						message.getReceivers().add(TopologyNode.getMaster());
						message.setData(e);
						tcpConnection.sendMessage(message);
					}
				}
			}, "WorkerThread" + threadCount + "@" + topologyNode.getName());
			threadCount++;
			thread.start();
			threadList.add(thread);
		}
		
		// create timers for sending solutions to other algorithms
		Timer timer = new Timer();
		timerReference.set(timer);
		for (final TopologyNode sender : workDescriptionForWorker.getConnectionsMap().keySet()) {
			Algorithm algorithm = algorithmByTopologyNode.get(sender);
			int index = algorithmList.indexOf(algorithm);
			final Statistics statistics = statisticList.get(index);
			Map<TopologyNode, Integer> connectionMap = workDescriptionForWorker.getConnectionsMap().get(sender);
			for (final TopologyNode toNode : connectionMap.keySet()) {
				int interval = connectionMap.get(toNode);
				
				TimerTask timerTask;
				if (algorithmByTopologyNode.containsKey(toNode)) {
					final Algorithm toAlgorithm = algorithmByTopologyNode.get(toNode);
					timerTask = new TimerTask() {
						
						@Override
						public void run() {
							Solution bestSolution = statistics.getBestSolution();
							ArrayList<Solution> solutionList = new ArrayList<Solution>();
							solutionList.add(bestSolution);
							toAlgorithm.receiveForeignSolutions(solutionList);
						}
					};
				} else {
					timerTask = new TimerTask() {
						
						@Override
						public void run() {
							Solution bestSolution = statistics.getBestSolution();
							ArrayList<Solution> solutionList = new ArrayList<Solution>();
							solutionList.add(bestSolution);

							Message message = new Message(sender, WorkingMessageType.SOLUTION);
							message.getReceivers().add(toNode);
							message.setData(solutionList);
							tcpConnection.sendMessage(message);
						}
					};
				}
				timer.schedule(timerTask, (int) (interval * Math.random()), interval);
			}
		}
		
		// create timers for sending statistics
		int interval = 10000;
		for (final Statistics statistics : statisticList) {
			TimerTask timerTask = new TimerTask() {
				
				@Override
				public void run() {
					Message message = new Message(statistics.getNode(), WorkingMessageType.STATISTICS);
					message.getReceivers().add(TopologyNode.getMaster());
					message.setData(statistics.clone());
					tcpConnection.sendMessage(message);
					statistics.clearEventList();
				}
			};
			timer.schedule(timerTask, (int) (interval * Math.random()), interval);
		}
	}
	
	private void restartAlgorithms() {
		
		// stop current algorithms
		log.info("Restarting algorithms...");
		timerReference.get().cancel();
		log.info("Disconnected u restartAlgorithms");
		algorithmStopRequested.set(true);
		for (Thread thread : threadList) {
			try {
				log.info("Waiting thread " + thread.getName() + " to stop");
				thread.join();
			} catch (InterruptedException e) {
				// ignored
			}
		}
		
		// start new algorithms
		log.info("All algorithms are stopped. Starting to create new algorithms...");
		initAlgorithms();
	}
	
	public void blockUntilFinished() {
		while(disconnected.get() == false) {
			synchronized (disconnected) {
				try {
					disconnected.wait();
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		
		log.info("Disconnected u block until finished");
		
		// stop algorithm threads
		if (timerReference.get() != null) {
			timerReference.get().cancel();
		}
		algorithmStopRequested.set(true);
	}
	
	private void sendMessageReadyForStart() {
		Message message = new Message(null, WorkingMessageType.READY_FOR_START);
		message.getReceivers().add(TopologyNode.getMaster());
		message.setData(tcpConnection.getLocalSocketAddress());
		tcpConnection.sendMessage(message);
	}
	
	private Configuration getConfigurationFromByteArray(byte[] configurationData) {
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		try {
			configuration.load(new ByteArrayInputStream(configurationData));
		} catch (ConfigurationException e) {
			log.error(e, e);
		}
		
		return configuration;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T toObject(ClassLoader classLoader, byte[] byteArray) {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
		ClassLoaderObjectInputStream objectInputStream = null;
		T object = null;
		try {
			objectInputStream = new ClassLoaderObjectInputStream(classLoader, byteArrayInputStream);
			object = (T) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { objectInputStream.close(); } catch (Exception e) { }
		}
		return object;
	}
	
	private static class ClassLoaderObjectInputStream extends ObjectInputStream{
		 
		private final ClassLoader classLoader;
	 
		public ClassLoaderObjectInputStream(ClassLoader classLoader, InputStream in) throws IOException {
			super(in);
			this.classLoader = classLoader;
		}
		
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		
			try{
				String name = desc.getName();
				return Class.forName(name, false, classLoader);
			}
			catch(ClassNotFoundException e){
				return super.resolveClass(desc);
			}
		}
	}

}
