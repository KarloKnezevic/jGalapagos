package jGalapagos.communication;


import jGalapagos.worker.ObjectUtilities;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReceiverThread extends Thread {
	
	private final Log log = LogFactory.getLog(ReceiverThread.class);
	private final TcpConnection tcpConnection;
	private final DataInputStream input;
	
	public ReceiverThread(DataInputStream input, TcpConnection tcpConnection) {
		this.input = input;
		this.tcpConnection = tcpConnection;
	}

	@Override
	public void run() {
		tcpConnection.getSocketTimeout().setTimeout();
		log.info("Receiver thread is started");
		while(tcpConnection.isAlive()) {
			try {
				int length = input.readInt();
				tcpConnection.getSocketTimeout().removeTimeout();
				byte[] data = new byte[length];
				input.readFully(data);
				tcpConnection.getSocketTimeout().setTimeout();
				Message message = ObjectUtilities.toObject(data);
				tcpConnection.getRecieveListener().receiveMessage(tcpConnection, message);
			} catch (SocketTimeoutException e) {
				// ignore
			} catch (EOFException e) {
				// disconnected
				tcpConnection.disconnect();
			} catch (IOException e) {
				if (tcpConnection.isAlive()) {
					log.error("Error in communication. Disconnecting...", e);
					tcpConnection.disconnect();
				}
			}
		}
		log.info("Receiver thread is over.");
	}
	
}
