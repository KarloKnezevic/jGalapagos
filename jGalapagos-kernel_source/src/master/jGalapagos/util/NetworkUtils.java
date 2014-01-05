package jGalapagos.util;

import jGalapagos.master.Connection;
import jGalapagos.master.NodeContainer;
import jGalapagos.master.WorkerInformation;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {

	public static InetAddress[] getInterfaceAddresses() {
		List<InetAddress> inetAddressList = new ArrayList<InetAddress>();
		for (NetworkInterface n : getNetworkInterfaces()) {
			Enumeration<InetAddress> interfaceAddr = n.getInetAddresses();
			while (interfaceAddr.hasMoreElements()) {
				InetAddress address = interfaceAddr.nextElement();
				inetAddressList.add(address);
			}
		}
		return inetAddressList.toArray(new InetAddress[inetAddressList.size()]);
	}

	public static List<NetworkInterface> getNetworkInterfaces() {
		Enumeration<NetworkInterface> networkInterface;
		List<NetworkInterface> networkInterfaceList = new ArrayList<NetworkInterface>();
		try {
			networkInterface = NetworkInterface.getNetworkInterfaces();
			while (networkInterface.hasMoreElements()) {
				NetworkInterface n = networkInterface.nextElement();
				networkInterfaceList.add(n);
			}
		} catch (SocketException e) {
		}
		return networkInterfaceList;
	}
	
	public static List<NodeContainer> createTreeTopology(String[] names, int nodeNum, int interval){
		List<NodeContainer> nodes = new ArrayList<NodeContainer>();
		
		if(nodeNum < 1){
			return nodes;
		}
		
		for (int i = 0; i < nodeNum; i++) {
			NodeContainer node = new NodeContainer("Node" + i);
			node.setAlgorithmName(names[i%names.length]);
			nodes.add(node);
			if(i > 0){	
				List<Connection> connectionList = node.getConnectionList();
				Connection connection = new Connection(nodes.get((int)((i-1)/2)));
				connection.setInterval(interval);
				connectionList.add(connection);
			}
		}
		
		
		return nodes;
	}
	
	public static List<NodeContainer> createCompleteTopology(String[] names, int nodeNum, int interval){
		List<NodeContainer> nodes = new ArrayList<NodeContainer>();
		
		if(nodeNum < 1){
			return nodes;
		}
		
		for (int i = 0; i < nodeNum	; i++) {
			NodeContainer node = new NodeContainer("Node" + i);
			node.setAlgorithmName(names[i%names.length]);
			nodes.add(node);
		}
		
		for (int i = 0; i < nodeNum; i++) {
			List<Connection> connectionList = nodes.get(i).getConnectionList();
			for (int j = 0; j < nodeNum; j++) {
				if(i == j) continue;
				Connection connection = new Connection(nodes.get(j));
				connection.setInterval(interval);
				connectionList.add(connection);
			}
		}
		return nodes;
	}
	
	public static List<NodeContainer> createRingTopology(String[] names, int nodeNum, int interval){
		List<NodeContainer> nodes = new ArrayList<NodeContainer>();
		
		if(nodeNum < 1){
			return nodes;
		}
		
		for (int i = 0; i < nodeNum; i++) {
			NodeContainer node = new NodeContainer("Node" + i);
			node.setAlgorithmName(names[i%names.length]);
			nodes.add(node);
			if(i > 0){
				List<Connection> connectionList = nodes.get(i).getConnectionList();
				Connection connection = new Connection(nodes.get(i-1));
				connection.setInterval(interval);
				connectionList.add(connection);
			}
		}
		
		List<Connection> connectionList = nodes.get(0).getConnectionList();
		Connection connection = new Connection(nodes.get(nodes.size()-1));
		connection.setInterval(interval);
		connectionList.add(connection);
		
		return nodes;
	}
	
	public static List<NodeContainer> createIslandTopology(String[] names, int islandNum, 
			int nodesPerIsland, int innerInterval, int outerInterval){
		List<NodeContainer> nodes = new ArrayList<NodeContainer>();
		
		if(islandNum < 1 || nodesPerIsland < 1){
			return nodes;
		}
		
		for (int i = 0; i < islandNum * nodesPerIsland; i++) {
			NodeContainer node = new NodeContainer("Node" + i);
			node.setAlgorithmName(names[i%names.length]);
			nodes.add(node);
		}
		
		// Spajam unutrasnjost otoka
		for (int i = 0; i < islandNum; i++) {
			for (int j = 0; j < nodesPerIsland; j++) {
				if(j > 0){	
					List<Connection> list = nodes.get(i*nodesPerIsland + j).getConnectionList();
					Connection connection = new Connection(nodes.get(i*nodesPerIsland + j -1));
					connection.setInterval(innerInterval);
					list.add(connection);
				}
			}
			List<Connection> connectionList = nodes.get(i*nodesPerIsland).getConnectionList();
			Connection connection = new Connection(nodes.get(i*nodesPerIsland + nodesPerIsland-1));
			connection.setInterval(innerInterval);
			connectionList.add(connection);
		}
		
		// Spajam otoke medjusobno
		for (int i = 1; i < islandNum; i++) {
			List<Connection> connectionList = nodes.get(i*nodesPerIsland).getConnectionList();
			Connection connection = new Connection(nodes.get(i*nodesPerIsland-1));
			connection.setInterval(outerInterval);
			connectionList.add(connection);
		}
		List<Connection> connectionList = nodes.get(0).getConnectionList();
		Connection connection = new Connection(nodes.get(nodes.size()-1));
		connection.setInterval(outerInterval);
		connectionList.add(connection);
		
		
		return nodes;
	}

	public static List<NodeContainer> createToroidTopology(String[] names, int rowNum, int columnNum, int interval){
		List<NodeContainer> nodes = new ArrayList<NodeContainer>();
		
		if(rowNum < 1 || columnNum < 1){
			return nodes;
		}
		
		for (int i = 0; i < columnNum*rowNum; i++) {
			NodeContainer node = new NodeContainer("Node" + i);
			node.setAlgorithmName(names[i%names.length]);
			nodes.add(node);
		}
		
		for (int i = 0; i < rowNum*columnNum; i++) {
			int left = i - 1;
			int right = i + 1;
			int up = i - columnNum;
			int down = (i + columnNum)%(columnNum*rowNum);
			
			if(up < 0) up = (rowNum * columnNum) - (columnNum - i);
			if(i%columnNum == 0) left = i + columnNum - 1;
			if(i%columnNum == columnNum - 1) right = i - columnNum + 1;

			
			List<Connection> connectionList = nodes.get(i).getConnectionList();
			Connection connection = new Connection(nodes.get(left));
			connection.setInterval(interval);
			connectionList.add(connection);
			
			connection = new Connection(nodes.get(right));
			connection.setInterval(interval);
			connectionList.add(connection);
			
			connection = new Connection(nodes.get(up));
			connection.setInterval(interval);
			connectionList.add(connection);
			
			connection = new Connection(nodes.get(down));
			connection.setInterval(interval);
			connectionList.add(connection);
		}
		
		
		return nodes;
	}
	
	public static List<NodeContainer> createUnconnectedTopology(String[] names, int nodeNum){
		List<NodeContainer> nodes = new ArrayList<NodeContainer>();
		
		if(nodeNum < 1){
			return nodes;
		}
		
		for (int i = 0; i < nodeNum; i++) {
			NodeContainer node = new NodeContainer("Node" + i);
			node.setAlgorithmName(names[i%names.length]);
			nodes.add(node);
		}
		
		return nodes;
	}
	
	public static void assignProcessors(List<NodeContainer> nodeList, List<WorkerInformation> workerInfoList){
		int nodeIterator = 0;
		boolean isEnough = false;
		while(nodeIterator < nodeList.size()){			
			for (int i = 0; i < workerInfoList.size(); i++) {
				WorkerInformation worker = workerInfoList.get(i);
				for (int j = 0; j < worker.getAvailableProcessors(); j++) {
					if(nodeIterator >= nodeList.size()){
						isEnough = true;
						break;
					}
					nodeList.get(nodeIterator).setWorkerInformation(worker);
					nodeIterator++;
				}
				if(isEnough){
					break;
				}
			}
		}
	}
}
