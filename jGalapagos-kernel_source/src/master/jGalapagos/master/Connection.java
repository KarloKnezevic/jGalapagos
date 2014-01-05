package jGalapagos.master;

/**
 * 
 * @author Mihej Komar
 *
 */
public class Connection {

	private final NodeContainer nodeContainer;
	private int interval;

	public Connection(NodeContainer nodeContainer) {
		this.nodeContainer = nodeContainer;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public NodeContainer getNodeContainer() {
		return nodeContainer;
	}

}
