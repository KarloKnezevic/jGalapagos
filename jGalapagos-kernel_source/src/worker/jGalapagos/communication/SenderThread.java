package jGalapagos.communication;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SenderThread extends Thread {
	
	private final Log log = LogFactory.getLog(ReceiverThread.class);
	private final DataOutputStream output;
	private final TcpConnection tcpConnection;
	private final BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
	
	public SenderThread(DataOutputStream output, TcpConnection tcpConnection) {
		this.output = output;
		this.tcpConnection = tcpConnection;
	}

	@Override
	public void run() {
		log.info("Sender thread is started");
		while(tcpConnection.isAlive() || !queue.isEmpty()) {
			try {
				final Message message = queue.poll(1, TimeUnit.SECONDS);
				if (message == null) { continue; }
				byte[] messageByte = SerializationUtils.serialize(message);
				int length = messageByte.length;
				output.writeInt(length);
				output.write(messageByte);
				output.flush();
			} catch (EOFException e) {
				// disconnected
				tcpConnection.disconnect();
			} catch (InterruptedException e) {
				log.warn("Error while waiting for messages", e);
				tcpConnection.disconnect();
			} catch (IOException e) {
				if (tcpConnection.isAlive()) {
					log.warn("Error sending message", e);
					tcpConnection.disconnect();
				}
			}
		}
		log.info("Sender thread is over.");
	}
	
	public void sendMessage(Message message) {
		queue.add(message);
	}

}
