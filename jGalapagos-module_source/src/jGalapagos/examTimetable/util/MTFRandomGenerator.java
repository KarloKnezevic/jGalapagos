package jGalapagos.examTimetable.util;

import ec.util.MersenneTwisterFast;

public class MTFRandomGenerator implements RandomGenerator {

	private MersenneTwisterFast rnd = new MersenneTwisterFast();
	
	@Override
	public boolean nextBoolean() {
		return rnd.nextBoolean();
	}

	@Override
	public double nextDouble() {
		return rnd.nextDouble();
	}

	@Override
	public int nextInt() {
		return rnd.nextInt();
	}

	@Override
	public int nextInt(int value) {
		return rnd.nextInt(value);
	}

	@Override
	public boolean nextBoolean(double value) {
		return rnd.nextBoolean(value);
	}
}
