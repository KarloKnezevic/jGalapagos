package jGalapagos.worker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorkerDetails implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int processorCount;
	private final Map<String, Long> libaryChecksum = new HashMap<String, Long>();

	public WorkerDetails(int processorCount) {
		this.processorCount = processorCount;
	}

	public int getProcessorCount() {
		return processorCount;
	}

	public Map<String, Long> getLibaryChecksum() {
		return libaryChecksum;
	}

}
