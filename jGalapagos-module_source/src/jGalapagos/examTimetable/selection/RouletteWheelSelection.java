package jGalapagos.examTimetable.selection;

import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

public final class RouletteWheelSelection implements Selection {
	
	private final RandomGenerator random;
	private final int count;
	
	public RouletteWheelSelection(RandomGenerator random, Configuration configuration) {
		this.random = random;
//		size = configuration.getInt("size");
		count = configuration.getInt("count");
	}

	@Override
	public int[] select(final double[] cumulativeProbability) {
		final int size = cumulativeProbability.length;
		final int[] selected = new int[count];
		for (int i = count; --i >= 0;) {
			final double rand = random.nextDouble();
			for (int j = 0; j < size; j++) {
				if (cumulativeProbability[j] > rand) {
					selected[i] = j;
					break;
				}
			}
		}
		return selected;
	}
	
}
