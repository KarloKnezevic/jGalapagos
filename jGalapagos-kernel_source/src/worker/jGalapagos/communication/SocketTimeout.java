package jGalapagos.communication;

import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketTimeout {
	
	private final Log log = LogFactory.getLog(SocketTimeout.class);
	public static final int TIMEOUT = 1000;
	private final Socket socket;
	
	public SocketTimeout(Socket socket) {
		this.socket = socket;
	}
	
	public void setTimeout() {
		try {
			socket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			log.error("Error setting socket timeout", e);
		}
	}
	
	public void removeTimeout() {
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e) {
			log.error("Error setting socket timeout to 0", e);
		}
	}

}
