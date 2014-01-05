package jGalapagos.communication;


import jGalapagos.worker.ObjectUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final TopologyNode sender;
	private final List<TopologyNode> receivers = new ArrayList<TopologyNode>();
	private final int messageType;
	private byte[] data;
	
	public Message(TopologyNode sender, int messageType) {
		this.sender = sender;
		this.messageType = messageType;
	}

	public TopologyNode getSender() {
		return sender;
	}

	public List<TopologyNode> getReceivers() {
		return receivers;
	}

	/**
	 * Dohvaća vrstu poruke.
	 * 
	 * @return Vrsta poruke
	 */
	public int getMessageType() {
		return messageType;
	}
	
	/**
	 * Postavlja podatke koji se šalju preko poruke.
	 * 
	 * @param object
	 *            Objekt koj se šalje preko poruke
	 */
	public void setData(Serializable object) {
		data = SerializationUtils.serialize(object);
	}

	/**
	 * Dohvaća podatke poslane preko poruke.
	 * 
	 * @param <T>
	 *            Razred koji se očekuje unutar poruke
	 * @return Objekt poslan preko poruke
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T getData() {
		return (T) ObjectUtilities.toObject(data);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T getData(ClassLoader classLoader) {
		return (T) ObjectUtilities.toObject(classLoader, data);
	}

}