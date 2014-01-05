package jGalapagos.examTimetable.selection;

import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

public final class TournamentSelection implements Selection {

	private final RandomGenerator random;
	private final int size;
	private final int count;
	
	public TournamentSelection(RandomGenerator random, Configuration configuration) {
		this.random = random;
		size = configuration.getInt("size");
		count = configuration.getInt("count");
	}
	
	@Override
	public int[] select(final double[] cumulativeProbability) {
		final int[] selected = new int[count];
		for (int i = 0; i < count; i++) {
			selected[i] = random.nextInt(size);
		}
		return selected;
	}

}
