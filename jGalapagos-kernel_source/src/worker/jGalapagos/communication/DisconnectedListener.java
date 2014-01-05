package jGalapagos.communication;

public interface DisconnectedListener {
	
	/**
	 * Metoda koja se poziva ako je TCP veza prekinuta.
	 * 
	 * @param name
	 *           TCP veza koja je prekinuta
	 */
	public void disconnected(TcpConnection tcpConnection);

}
