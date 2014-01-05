package jGalapagos.core;

import java.io.Serializable;

/**
 * 
 * @author Mihej Komar
 *
 */
public class TopologyNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final TopologyNode MASTER = new TopologyNode("master");
	
	private String name;

	public TopologyNode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static TopologyNode getMaster() {
		return MASTER;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopologyNode other = (TopologyNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
