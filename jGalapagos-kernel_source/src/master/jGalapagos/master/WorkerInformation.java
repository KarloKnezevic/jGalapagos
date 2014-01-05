package jGalapagos.master;

import jGalapagos.communication.TcpConnection;

public class WorkerInformation {
	
	private final TcpConnection tcpConnection;
	private WorkerStatus workerStatus;
	private int availableProcessors;
	
	public WorkerInformation(TcpConnection tcpConnection) {
		this.tcpConnection = tcpConnection;
	}

	public WorkerStatus getWorkerStatus() {
		return workerStatus;
	}

	public void setWorkerStatus(WorkerStatus workerStatus) {
		this.workerStatus = workerStatus;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public TcpConnection getTcpConnection() {
		return tcpConnection;
	}
	
	@Override
	public String toString() {
		return tcpConnection == null ? "N/A" : tcpConnection.toString();
	}

}
