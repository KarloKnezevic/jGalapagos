package jGalapagos.examTimetable.parentSelection;

import jGalapagos.examTimetable.model.Population;
import jGalapagos.examTimetable.model.Timetable;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;
/**
 * Implementation of roulette wheel selection based on <code>ParentSelection</code> interface.
 */
// TODO: SLOW!!
public final class RouletteWheelParentSelection implements ParentSelection {
	
	private final RandomGenerator random;
	/**
	 * Roulette wheel selection constructor.
	 * @param random	Instance of a random generator.
	 * @param configuration	ALgorithm configuration.
	 */
	public RouletteWheelParentSelection(RandomGenerator random, Configuration configuration) {
		this.random = random;
	}

	@Override
	public <T extends Timetable> void selectTwoParents(Population<T> population, T[] parents) {
		final int populationCount = population.getPopulationCount();
		
		final double[] cumulativeProbability = new double[populationCount];
		double sumOfProbabilities = 0;
		for (int i = populationCount; --i >= 0;) {
			final T timetable = population.get(i);
			final double probability = 1 / timetable.getFitness()[0];
			cumulativeProbability[i] = probability;
			sumOfProbabilities += probability;
		}
		cumulativeProbability[0] = cumulativeProbability[0] / sumOfProbabilities;
		for (int i = 1; i < populationCount; i++) {
			cumulativeProbability[i] = cumulativeProbability[i - 1] + cumulativeProbability[i] / sumOfProbabilities;
		}
	
		// select parents
		int[] selected = new int[2];
		do {
			final double rand1 = random.nextDouble();
			for (int j = 0; j < populationCount; j++) {
				if (cumulativeProbability[j] > rand1) {
					selected[0] = j;
					break;
				}
			}
			
			final double rand2 = random.nextDouble();
			for (int j = 0; j < populationCount; j++) {
				if (cumulativeProbability[j] > rand2) {
					selected[1] = j;
					break;
				}
			}
		} while (selected[0] == selected[1]);
		
		parents[0] = population.get(selected[0]);
		parents[1] = population.get(selected[1]);
	}
	


	
}
