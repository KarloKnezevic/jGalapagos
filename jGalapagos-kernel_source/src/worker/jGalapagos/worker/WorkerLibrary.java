package jGalapagos.worker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorkerLibrary implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final Map<String, byte[]> implementationMap = new HashMap<String, byte[]>();

	public Map<String, byte[]> getImplementationMap() {
		return implementationMap;
	}

}
