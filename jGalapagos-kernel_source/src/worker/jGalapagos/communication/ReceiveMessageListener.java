package jGalapagos.communication;

public interface ReceiveMessageListener {
	
	public void receiveMessage(TcpConnection tcpConnection, Message message);

}