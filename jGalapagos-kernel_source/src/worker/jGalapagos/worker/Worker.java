package jGalapagos.worker;


import jGalapagos.communication.DisconnectedListener;
import jGalapagos.communication.Message;
import jGalapagos.communication.PreparingMessageType;
import jGalapagos.communication.ReceiveMessageListener;
import jGalapagos.communication.TcpConnection;
import jGalapagos.communication.TopologyNode;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Worker {
	
	private final Log log = LogFactory.getLog(Worker.class);
	public static final int HEADER_IPv4 = 165973070;
	public static final int HEADER_IPv6 = 1703128663; 
	private static final String PATH_TO_LIBDIR = "workerLib";
	private static final String WORKER_CONTROLLER_CLASS = "jGalapagos.WorkerController";
	private static final String WORKER_CONTROLLER_METHOD = "blockUntilFinished";
	
	public Worker(int port) {
		listeningForUDP(port);
	}
	
	public Worker(InetSocketAddress masterAddress, long attemptInterval) {
		connectingToMaster(masterAddress, attemptInterval);
	}
	
	private void connectingToMaster(InetSocketAddress masterAddress, long attemptInterval) {
		String message = "Trying to connect to " + masterAddress + " every " + (attemptInterval / 1000) + " seconds";
		log.info(message);
		while(true) {
			Socket socket = null;
			try {
				socket = new Socket(masterAddress.getAddress(), masterAddress.getPort());
			} catch (IOException e) {
				// expected if master is not online
				try {
					Thread.sleep(attemptInterval);
				} catch (InterruptedException e1) { }
				continue;
			}
			
			TcpConnection tcpConnection = null;
			try {
				AtomicReference<byte[]> workDescription = new AtomicReference<byte[]>();
				AtomicBoolean disconnected = new AtomicBoolean();
				tcpConnection = new TcpConnection(socket, createMessageListener(workDescription), createDisconnectedListener(disconnected, workDescription));
				log.info("Connected to " + masterAddress);
				connect(tcpConnection, disconnected, workDescription);
			} catch (Throwable e) {
				log.warn(e, e);
			} finally {
				if (tcpConnection != null) {
					tcpConnection.disconnect();
				}
			}
			log.info(message);
		}
	}
	
	private void listeningForUDP(int port) {
		int maxLength = 4 + 2 + 16;
		byte[] buffer = new byte[maxLength];
		DatagramPacket receivePacket = new DatagramPacket(buffer, maxLength);
		DatagramSocket datagramSocket = null;
		
		try {
			datagramSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			log.error(e, e);
			return;
		}
		
		while (true) {
			TcpConnection tcpConnection = null;
			try {
				log.info("Listening for messages on port " + port);
				datagramSocket.receive(receivePacket);
				int header = ArrayUtils.readIntFromByteArray(buffer, 0, 4);
				InetAddress address;
				if (header == HEADER_IPv4) {
					byte[] addr = new byte[4];
					System.arraycopy(buffer, 6, addr, 0, 4);
					address = Inet4Address.getByAddress(addr);
				} else if (header == HEADER_IPv6) {
					byte[] addr = new byte[16];
					System.arraycopy(buffer, 6, addr, 0, 16);
					address = Inet6Address.getByAddress(addr);
				} else {
					log.info("Ignoring unexpected UDP packet");
					continue;
				}
				int masterPort = ArrayUtils.readIntFromByteArray(buffer, 4, 2);
				InetSocketAddress socketAddress = new InetSocketAddress(address, masterPort);
				Socket socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
				AtomicReference<byte[]> workDescription = new AtomicReference<byte[]>();
				AtomicBoolean disconnected = new AtomicBoolean();
				tcpConnection = new TcpConnection(socket, createMessageListener(workDescription), createDisconnectedListener(disconnected, workDescription));
				log.info("Connected to " + socketAddress);
				connect(tcpConnection, disconnected, workDescription);
			} catch (Throwable e) {
				log.warn(e, e);
			} finally {
				if (tcpConnection != null) {
					tcpConnection.disconnect();
				}
			}
		}
	}
	
	private void connect(TcpConnection tcpConnection, AtomicBoolean disconnected, AtomicReference<byte[]> workDescriptionByteArray) throws Throwable {
		
		// block until workDescriptionByteArray is set
		while (workDescriptionByteArray.get() == null && disconnected.get() == false) {
			synchronized (workDescriptionByteArray) {
				workDescriptionByteArray.wait();
			}
		}
		
		// if disconnected, return
		if (disconnected.get() == true) {
			return;
		}
		
		// start implementation
		byte[] byteArray = workDescriptionByteArray.get();
		File[] jarList = getJarList();
		URL[] library = new URL[jarList.length];
		for (int i = 0; i < jarList.length; i++) {
			library[i] = new URL("file:" + jarList[i].getPath());
		}
		URLClassLoader urlClassLoader = new URLClassLoader(library);
		Class<?> classApp = urlClassLoader.loadClass(WORKER_CONTROLLER_CLASS);
		Constructor<?> constructor = classApp.getConstructor(TcpConnection.class, byte[].class, URLClassLoader.class, AtomicBoolean.class);
		Object object = constructor.newInstance(tcpConnection, byteArray, urlClassLoader, disconnected);
		Method startMethod = classApp.getMethod(WORKER_CONTROLLER_METHOD);
		startMethod.invoke(object);
	}
	
	private ReceiveMessageListener createMessageListener(final AtomicReference<byte[]> workDescriptionByteArray) {
		return new ReceiveMessageListener() {
			
			@Override
			public void receiveMessage(TcpConnection tcpConnection, Message message) {
				if (message.getMessageType() == PreparingMessageType.REQUEST_DETAILS) {
					Message response = new Message(null, PreparingMessageType.RESPONSE_DETAILS);
					response.getReceivers().add(TopologyNode.getMaster());
					response.setData(getWorkerDetails());
					tcpConnection.sendMessage(response);
				} else if (message.getMessageType() == PreparingMessageType.IMPLEMENTATION) {
					WorkerLibrary workerLibrary = message.getData();
					updateLibrary(workerLibrary);
					Message response = new Message(null, PreparingMessageType.RESPONSE_DETAILS);
					response.getReceivers().add(TopologyNode.getMaster());
					response.setData(getWorkerDetails());
					tcpConnection.sendMessage(response);
				} else if (message.getMessageType() == PreparingMessageType.START_IMPL) {
					byte[] byteArray = message.getData();
					
					// start impl
					workDescriptionByteArray.set(byteArray);
					synchronized (workDescriptionByteArray) {
						workDescriptionByteArray.notifyAll();
					}
				}
			}
		};
	}
	
	private DisconnectedListener createDisconnectedListener(final AtomicBoolean disconnected, final AtomicReference<byte[]> workDescriptionByteArray) {
		return new DisconnectedListener() {
			
			@Override
			public void disconnected(TcpConnection tcpConnection) {
				disconnected.set(true);
				
				// connection disconnected before work description is received, notify sleeping threads
				if (workDescriptionByteArray.get() == null) {
					synchronized (workDescriptionByteArray) {
						workDescriptionByteArray.notifyAll();
					}
				}
				
				// notify sleeping threads
				synchronized (disconnected) {
					disconnected.notifyAll();
				}
			}
		};
	}
	
	private WorkerDetails getWorkerDetails() {
		WorkerDetails workerDetails = new WorkerDetails(Runtime.getRuntime().availableProcessors());
		CRC32 crc32 = new CRC32();
		for (File file : getJarList()) {
			String filename = file.getName();
			crc32.reset();
			Long checksum;
			try {
				checksum = FileUtils.checksum(file, crc32).getValue();
			} catch (IOException e) {
				checksum = null;
			}
			workerDetails.getLibaryChecksum().put(filename, checksum);
		}
		
		return workerDetails;
	}
	
	private void updateLibrary(WorkerLibrary workerLibrary) {
		for (File file : getJarList()) {
			file.delete();
		}
		for (String filename : workerLibrary.getImplementationMap().keySet()) {
			byte[] data = workerLibrary.getImplementationMap().get(filename);
			File file = new File(PATH_TO_LIBDIR, filename);
			try {
				FileUtils.writeByteArrayToFile(file, data);
			} catch (IOException e) {
				log.warn(e, e);
			}
		}
	}
	
	private File[] getJarList() {
		FilenameFilter filenameFilter = new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
			
		};
		File directory = new File(PATH_TO_LIBDIR);
		if (!directory.exists()) {
			directory.mkdir();
		}
		return directory.listFiles(filenameFilter);
	}
	
	public static void main(String[] args) {
		if (args.length != 1 && args.length != 3) {
			System.out.println("Usage:\n" +
					"\t- port number to listen for UDP messages (example: java -jar worker.jar 5000)\n" +
					"\t- master socket address to connect and attempt interval is 10 s (example: java -jar worker.jar localhost:10000\n" +
					"\t- master socket and attept interval in seconds (example: java -jar worker.jar localhost:10000 -t 5)");
			return;
		}
		if (args[0].contains(":")) {
			String[] data = args[0].split(":");
			String host = data[0];
			int port;
			try {
				port = Integer.parseInt(data[1]);
			} catch (NumberFormatException e) {
				System.out.println("Error: port number in wrong format");
				return;
			}
			if (args.length == 1) {
				new Worker(new InetSocketAddress(host, port), 10000);
			} else if (args[1].equals("-t")) {
				int attemptInterval;
				try {
					attemptInterval = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					System.out.println("Error: attempt interval in wrong format");
					return;
				}
				new Worker(new InetSocketAddress(host, port), attemptInterval * 1000);
			} else {
				System.out.println("Error: arguments in wrong format");
			}
		} else {
			int port;
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Error: port number in wrong format");
				return;
			}
			if (port < 1 || port > 65535) {
				System.out.println("Error: port number in wrong format");
				return;
			}
			new Worker(port);
		}
	}
}
