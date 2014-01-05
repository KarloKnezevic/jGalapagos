package jGalapagos.master;

import jGalapagos.WorkDescriptionForWorker;
import jGalapagos.WorkingMessageType;
import jGalapagos.communication.DisconnectedListener;
import jGalapagos.communication.Message;
import jGalapagos.communication.PreparingMessageType;
import jGalapagos.communication.ReceiveMessageListener;
import jGalapagos.communication.TcpConnection;
import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.Statistics;
import jGalapagos.worker.ArrayUtils;
import jGalapagos.worker.Worker;
import jGalapagos.worker.WorkerDetails;
import jGalapagos.worker.WorkerLibrary;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Mihej Komar
 *
 */
public class MasterComunicator {
	
	private final Log log = LogFactory.getLog(MasterComunicator.class);
	private final ModuleContainer moduleContainer;
	private final Map<String, Long> workerLibaryChecksum = new HashMap<String, Long>();
	private final Map<String, File> workerLibraryFiles = new HashMap<String, File>();
	private final WorkDescription workDescription;
	private final ReceiveMessageListener receiveMessageListener;
	private final DisconnectedListener disconnectedListener;
	private final List<StatisticsListener> statisticsListenerList = Collections.synchronizedList(new ArrayList<StatisticsListener>());
	private final List<RoundCompletionListener> roundCompletionListenerList = Collections.synchronizedList(new ArrayList<RoundCompletionListener>());
	private final List<MaxRoundCompletedListener> maxRoundCompletedListenerList = Collections.synchronizedList(new ArrayList<MaxRoundCompletedListener>());
	private final List<WorkerExceptionListener> workerExceptionListenerList = Collections.synchronizedList(new ArrayList<WorkerExceptionListener>());
	private WaitingForConnections waitingForConnections;
	
	private final AtomicReference<Solution> bestSolution = new AtomicReference<Solution>();
	private final AtomicLong start = new AtomicLong();
	private final AtomicLong lastUpdateOfBestSolution = new AtomicLong();
	private final AtomicInteger finishedRounds = new AtomicInteger();
	private final AtomicBoolean working = new AtomicBoolean(false);
	
	public MasterComunicator(final ModuleContainer moduleContainer, final WorkDescription workDescription) throws IOException {
		this.moduleContainer = moduleContainer;
		this.workDescription = workDescription;
		updateWorkerLibaryChecksum();

		receiveMessageListener = new ReceiveMessageListener() {
			
			@Override
			public void receiveMessage(TcpConnection tcpConnection, Message message) {
				if (message.getMessageType() == PreparingMessageType.RESPONSE_DETAILS) {
					WorkerDetails workerDetails = message.getData();
					int processorCount = workerDetails.getProcessorCount();
					WorkerStatus workerStatus = (hasWorkerNewLibrary(workerDetails)) ? WorkerStatus.READY_NEW_IMPL : WorkerStatus.READY_OLD_IMPL;
					for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
						if (workerInformation.getTcpConnection().equals(tcpConnection)) {
							workerInformation.setAvailableProcessors(processorCount);
							updateComputerStatus(workerInformation, workerStatus);
						}
					}
				} else if (message.getMessageType() == WorkingMessageType.SOLUTION && working.get()) {
					for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
						if (message.getReceivers().contains(nodeContainer.getTopologyNode())) {
							nodeContainer.getWorkerInformation().getTcpConnection().sendMessage(message);
						}
					}
				} else if (message.getMessageType() == WorkingMessageType.STATISTICS && working.get()) {
					Statistics statistics = message.getData(moduleContainer.getUrlClassLoader());
					
					// check if algorithms must be restarted (duration)
					if (workDescription.getMaxDuration() != 0) {
						int duration = (int) (System.currentTimeMillis() - start.get()) / 60000;
						if (duration >= workDescription.getMaxDuration()) {
							restartAlgorithms();
							return;
						}
					}
					
					
					Solution bestSolutionFromStatistics = statistics.getBestSolution();
					if (bestSolutionFromStatistics != null && (bestSolution.get() == null || bestSolutionFromStatistics.compareTo(bestSolution.get()) < 0)) {
						bestSolution.set(bestSolutionFromStatistics);
						lastUpdateOfBestSolution.set(System.currentTimeMillis());
					} else {
						
						// check if algorithms must be restarted (inactivity)
						if (workDescription.getMaxInactivityMinutes() != 0) {
							int lastUpdate = (int) (System.currentTimeMillis() - lastUpdateOfBestSolution.get()) / 60000;
							if (lastUpdate > workDescription.getMaxInactivityMinutes()) {
								restartAlgorithms();
								return;
							}
						}
					}
					
					for (StatisticsListener statisticsListener : statisticsListenerList) {
						statisticsListener.receiveStatistics(statistics);
					}
				} else if (message.getMessageType() == WorkingMessageType.READY_FOR_START) {
					SocketAddress socketAddress = message.getData();
					for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
						if (workerInformation.getTcpConnection().getRemoteSocketAddress().equals(socketAddress)) {
							updateComputerStatus(workerInformation, WorkerStatus.READY_FOR_START);
						}
					}
				} else if (message.getMessageType() == WorkingMessageType.EXCEPTION) {
					for (WorkerExceptionListener workerExceptionListener : workerExceptionListenerList) {
						TopologyNode topologyNode = message.getSender();
						Exception exception = message.getData();
						workerExceptionListener.exceptionThrown(topologyNode, exception);
					}
				}
			}
		};
		disconnectedListener = new DisconnectedListener() {
			
			@Override
			public void disconnected(TcpConnection tcpConnection) {
				for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
					if (workerInformation.getTcpConnection().equals(tcpConnection)) {
						updateComputerStatus(workerInformation, WorkerStatus.DISCONNECTED);
					}
				}
			}
		};
	}
	
	private boolean hasWorkerNewLibrary(WorkerDetails workerDetails) {
		if (workerDetails.getLibaryChecksum().size() != workerLibaryChecksum.size()) {
			return false;
		}
		for (String filename : workerLibaryChecksum.keySet()) {
			Long workerChecksum = workerDetails.getLibaryChecksum().get(filename);
			if (workerChecksum == null || !workerChecksum.equals(workerLibaryChecksum.get(filename))) {
				return false;
			}
		}
		return true;
	}
	
	private WorkerLibrary createWorkerLibrary() {
		WorkerLibrary workerLibrary = new WorkerLibrary();
		for (String filename : workerLibaryChecksum.keySet()) {
			try {
				byte[] data = FileUtils.readFileToByteArray(workerLibraryFiles.get(filename));
				workerLibrary.getImplementationMap().put(filename, data);
			} catch (IOException e) {
				log.warn(e, e);
			}
		}
		return workerLibrary;
	}
	
	public void setWaitingForConnections(int port, InetAddress localInetAddress) throws IOException {
		stopWaitingForConnections();
		waitingForConnections = new WaitingForConnections(port, localInetAddress);
		waitingForConnections.start();
	}
	
	public void stopWaitingForConnections() {
		if (waitingForConnections != null && waitingForConnections.isConnected()) {
			waitingForConnections.disconnect();
		}
	}
	
	private void updateComputerStatus(WorkerInformation workerInformation, WorkerStatus workerStatus) {
		workerInformation.setWorkerStatus(workerStatus);
		workDescription.fireWorkerInformationChanged();
	}
	
	public void connectToWorkers(List<InetSocketAddress> workerAddressList) throws IOException {
		if (!waitingForConnections.isConnected()) {
			throw new IOException("Not listening on any port");
		}
		int maxLength = 4 + 2 + 16;
		byte[] buffer = new byte[maxLength];
		DatagramPacket sendPacket = new DatagramPacket(buffer, maxLength);
		InetAddress localInetAddress = waitingForConnections.getLocalInetAddress();
		DatagramSocket datagramSocket = null;
		datagramSocket = new DatagramSocket(0, localInetAddress);
		
		if (localInetAddress instanceof Inet4Address) {
			ArrayUtils.writeIntToByteArray(Worker.HEADER_IPv4, buffer, 0, 4);
			ArrayUtils.writeIntToByteArray(waitingForConnections.getLocalPort(), buffer, 4, 2);
			System.arraycopy(localInetAddress.getAddress(), 0, buffer, 6, 4);
		} else if (localInetAddress instanceof Inet6Address) {
			ArrayUtils.writeIntToByteArray(Worker.HEADER_IPv6, buffer, 0, 4);
			ArrayUtils.writeIntToByteArray(waitingForConnections.getLocalPort(), buffer, 4, 2);
			System.arraycopy(localInetAddress.getAddress(), 0, buffer, 6, 16);
		} else {
			throw new SocketException("Unsupported type of InetAddress");
		}
		
		try {
			for (InetSocketAddress worker : workerAddressList) {
				sendPacket.setSocketAddress(worker);
				datagramSocket.send(sendPacket);
			}
		} finally {
			datagramSocket.close();
		}
	}
	
	public void updateImplStatus() {
		for (WorkerInformation workerInformation : new ArrayList<WorkerInformation>(workDescription.getWorkerInformationList())) {
			if (!workerInformation.getWorkerStatus().equals(WorkerStatus.DISCONNECTED)) {
				Message message = new Message(TopologyNode.getMaster(), PreparingMessageType.REQUEST_DETAILS);
				workerInformation.getTcpConnection().sendMessage(message);
			}
		}
	}
	
	public void updateImpl() {
		WorkerLibrary workerLibrary = createWorkerLibrary();
		for (WorkerInformation workerInformation : new ArrayList<WorkerInformation>(workDescription.getWorkerInformationList())) {
			if (workerInformation.getWorkerStatus().equals(WorkerStatus.READY_OLD_IMPL)) {
				updateComputerStatus(workerInformation, WorkerStatus.UPDATING);
				Message message = new Message(TopologyNode.getMaster(), PreparingMessageType.IMPLEMENTATION);
				message.setData(workerLibrary);
				workerInformation.getTcpConnection().sendMessage(message);
			}
		}
	}
	
	public void startImpl(Map<WorkerInformation, WorkDescriptionForWorker> workDescriptionsByWorkers) {
		for (WorkerInformation workerInformation : new ArrayList<WorkerInformation>(workDescription.getWorkerInformationList())) {
			if (workerInformation.getWorkerStatus().equals(WorkerStatus.READY_NEW_IMPL)) {
				WorkDescriptionForWorker workDescriptionForWorker = workDescriptionsByWorkers.get(workerInformation);
				if (workDescriptionForWorker == null) {
					continue;
				}
				byte[] data = SerializationUtils.serialize(workDescriptionForWorker);
				updateComputerStatus(workerInformation, WorkerStatus.STARTING_IMPL);
				Message message = new Message(TopologyNode.getMaster(), PreparingMessageType.START_IMPL);
				message.setData(data);
				workerInformation.getTcpConnection().sendMessage(message);
			}
		}
		
		new StartWhenReady().start();
	}
	
	private class StartWhenReady extends Thread {
		
		@Override
		public void run() {
			while (areWorkersReady() == false) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
			
			bestSolution.set(null);
			lastUpdateOfBestSolution.set(System.currentTimeMillis());
			start.set(System.currentTimeMillis());
			
			Set<WorkerInformation> workerSet = new HashSet<WorkerInformation>();
			for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
				workerSet.add(nodeContainer.getWorkerInformation());
			}

			for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
				if (workerSet.contains(nodeContainer.getWorkerInformation())) {
					workerSet.remove(nodeContainer.getWorkerInformation());
				} else {
					continue;
				}
				WorkerInformation workerInformation = nodeContainer.getWorkerInformation();
				Message message = new Message(TopologyNode.getMaster(), WorkingMessageType.START);
				message.getReceivers().add(nodeContainer.getTopologyNode());
				workerInformation.getTcpConnection().sendMessage(message);
				updateComputerStatus(workerInformation, WorkerStatus.WORKING);
			}
			working.set(true);
			log.info("All workers started to work");
		}
		
		private boolean areWorkersReady() {
			for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
				if (nodeContainer.getWorkerInformation().getWorkerStatus() != WorkerStatus.READY_FOR_START) {
					return false;
				}
			}
			return true;
		}
		
	}

	public void disconnect() {
		fireRoundComplete(true);
		for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
			Message message = new Message(TopologyNode.getMaster(), WorkingMessageType.DISCONNECT);
			workerInformation.getTcpConnection().sendMessage(message);
			updateComputerStatus(workerInformation, WorkerStatus.DISCONNECTING);
		}
	}
	
	public void restartAlgorithms() {
		working.set(false);
		if (finishedRounds.incrementAndGet() >= workDescription.getMaxRounds()) {
			fireRoundComplete(true);
			for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
				Message message = new Message(TopologyNode.getMaster(), WorkingMessageType.DISCONNECT);
				workerInformation.getTcpConnection().sendMessage(message);
				updateComputerStatus(workerInformation, WorkerStatus.DISCONNECTING);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (MaxRoundCompletedListener maxRoundCompletedListener : maxRoundCompletedListenerList) {
				maxRoundCompletedListener.maxRoundCompleted();
			}
		} else {
			fireRoundComplete(false);
			for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
				Message message = new Message(TopologyNode.getMaster(), WorkingMessageType.RESTART);
				workerInformation.getTcpConnection().sendMessage(message);
				updateComputerStatus(workerInformation, WorkerStatus.STARTING_IMPL);
			}
			new StartWhenReady().start();
		}
	}
	
	private void fireRoundComplete(boolean noMoreRounds) {
		Solution finalBest = bestSolution.get();
		for (RoundCompletionListener listener : roundCompletionListenerList) {
			listener.roundCompleted(finalBest, noMoreRounds);
		}
	}
	
	public void addStatisticsListener(StatisticsListener statisticsListener) {
		statisticsListenerList.add(statisticsListener);
	}
	
	public void removeStatisticsListener(StatisticsListener statisticsListener) {
		 statisticsListenerList.remove(statisticsListener);
	}
	
	public void addRoundCompletionListener(RoundCompletionListener roundCompletionListener) {
		roundCompletionListenerList.add(roundCompletionListener);
	}
	
	public void addMaxRoundCompletedListener(MaxRoundCompletedListener maxRoundCompletedListener) {
		maxRoundCompletedListenerList.add(maxRoundCompletedListener);
	}
	
	public void addWorkerExceptionListener(WorkerExceptionListener workerExceptionListener) {
		workerExceptionListenerList.add(workerExceptionListener);
	}
	
	private void updateWorkerLibaryChecksum() {
		List<File> files = new ArrayList<File>();
		for (String filename : moduleContainer.getConfig().getStringArray("jars")) {
			files.add(new File(moduleContainer.getDirectory(), filename));
		}
		files.add(new File("lib/distributedAlgorithmsCore.jar"));
		CRC32 crc32 = new CRC32();
		workerLibraryFiles.clear();
		for (File file : files) {
			String filename = file.getName();
			crc32.reset();
			Long checksum;
			try {
				checksum = FileUtils.checksum(file, crc32).getValue();
			} catch (IOException e) {
				log.warn(e, e);
				continue;
			}
			workerLibaryChecksum.put(filename, checksum);
			workerLibraryFiles.put(filename, file);
		}
	}
	
	private class WaitingForConnections extends Thread {
		
		private final Log log = LogFactory.getLog(WaitingForConnections.class);
		private final ServerSocket serverSocket;
		private final AtomicBoolean stop = new AtomicBoolean(false);
		
		public WaitingForConnections(int port, InetAddress localInetAddress) throws IOException {
			serverSocket = new ServerSocket(port, 0, localInetAddress);
		}
		
		public int getLocalPort() {
			return serverSocket.getLocalPort();
		}
		
		public boolean isConnected() {
			return serverSocket.isBound();
		}
		
		public InetAddress getLocalInetAddress() {
			return serverSocket.getInetAddress();
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Socket socket = serverSocket.accept();
					TcpConnection connection = new TcpConnection(socket, receiveMessageListener, disconnectedListener);
					WorkerInformation workerInformation = new WorkerInformation(connection);
					workDescription.getWorkerInformationList().add(workerInformation);
					updateComputerStatus(workerInformation, WorkerStatus.READY_UNKNOWN_IMPL);
					Message message = new Message(TopologyNode.getMaster(), PreparingMessageType.REQUEST_DETAILS);
					workerInformation.getTcpConnection().sendMessage(message);
				}
			} catch (IOException e) {
				if (stop.get() == false) {
					log.warn(e, e);
				}
			}
		}
		
		public void disconnect() {
			try {
				stop.set(true);
				serverSocket.close();
			} catch (IOException e) { }
		}
	}
	
	public static interface StatisticsListener {
		
		public void receiveStatistics(Statistics statistics);
		
	}
	
	public static interface RoundCompletionListener {
		
		public void roundCompleted(Solution finalSolution, boolean noMoreRounds);
		
	}
	
	public static interface MaxRoundCompletedListener {
		
		public void maxRoundCompleted();
		
	}
	
	public static interface WorkerExceptionListener {
		
		public void exceptionThrown(TopologyNode worker, Exception e);
		
	}

}
