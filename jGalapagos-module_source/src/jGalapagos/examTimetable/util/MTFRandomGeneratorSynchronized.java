package jGalapagos.examTimetable.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ec.util.MersenneTwisterFast;

/**
 * Implementation of random generator that is thread safe.
 * @author Mihej Komar
 *
 */
public class MTFRandomGeneratorSynchronized implements RandomGenerator {

	private final Log log = LogFactory.getLog(MTFRandomGeneratorSynchronized.class);
	private MersenneTwisterFast rnd = new MersenneTwisterFast();
	private Thread user;
	
	@Override
	public synchronized boolean nextBoolean() {
		updateThread();
		return rnd.nextBoolean();
	}

	@Override
	public synchronized double nextDouble() {
		updateThread();
		return rnd.nextDouble();
	}

	@Override
	public synchronized int nextInt() {
		updateThread();
		return rnd.nextInt();
	}

	@Override
	public synchronized int nextInt(int value) {
		updateThread();
		return rnd.nextInt(value);
	}
	
	@Override
	public synchronized boolean nextBoolean(double value) {
		updateThread();
		return rnd.nextBoolean(value);
	}

	private void updateThread() {
		if(user==null) {
			user = Thread.currentThread();
			return;
		}
		Thread ct = Thread.currentThread();
		if(ct==user) return;
		
		log.info("Random generator je koristen od vise dretvi!!!");
		
	}

}
