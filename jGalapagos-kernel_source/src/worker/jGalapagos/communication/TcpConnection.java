package jGalapagos.communication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpConnection {
	
	private final ReceiverThread receiverThread;
	private final SenderThread senderThread;
	private final AtomicBoolean alive = new AtomicBoolean(true);
	private final Socket socket;
	private final SocketTimeout socketTimeout;
	private ReceiveMessageListener recieveListener;
	private DisconnectedListener disconnectedListener;
	
	public TcpConnection(Socket socket, ReceiveMessageListener recieveListener, DisconnectedListener disconnectedListener) throws IOException {
		this.socket = socket;
		this.recieveListener = recieveListener;
		this.disconnectedListener = disconnectedListener;
		this.socketTimeout = new SocketTimeout(socket);
		
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		
		receiverThread = new ReceiverThread(input, this);
		senderThread = new SenderThread(output, this);
		
		receiverThread.start();
		senderThread.start();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		disconnect();
	}
	
	public void sendMessage(Message message) {
		senderThread.sendMessage(message);
	}

	public void disconnect() {
		alive.set(false);
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// ignore
		}
		disconnectedListener.disconnected(this);
	}

	public boolean isAlive() {
		return alive.get();
	}

	public ReceiveMessageListener getRecieveListener() {
		return recieveListener;
	}

	public void setRecieveListener(ReceiveMessageListener recieveListener) {
		this.recieveListener = recieveListener;
	}

	public DisconnectedListener getDisconnectedListener() {
		return disconnectedListener;
	}

	public void setDisconnectedListener(DisconnectedListener disconnectedListener) {
		this.disconnectedListener = disconnectedListener;
	}

	public SocketTimeout getSocketTimeout() {
		return socketTimeout;
	}
	
	public SocketAddress getLocalSocketAddress() {
		return socket.getLocalSocketAddress();
	}
	
	public SocketAddress getRemoteSocketAddress() {
		return socket.getRemoteSocketAddress();
	}
	
	@Override
	public String toString() {
		return (socket == null || socket.getInetAddress() == null)  ? "N/A" : socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
	}

}
