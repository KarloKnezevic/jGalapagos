package jGalapagos.examTimetable.parentSelection;

import jGalapagos.examTimetable.model.Population;
import jGalapagos.examTimetable.model.Timetable;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

/**
 * Implementation of tournament selection based on <code>ParentSelection</code> interface.
 */
public final class TournamentParentSelection implements ParentSelection {

	private final RandomGenerator random;
	
	/**
	 * Tournament selection constructor.
	 * @param random	Instance of a random generator.
	 * @param configuration	ALgorithm configuration.
	 */
	public TournamentParentSelection(RandomGenerator random, Configuration configuration) {
		this.random = random;
	}

	@Override
	public <T extends Timetable> void selectTwoParents(Population<T> population, T[] parents) {
		final int populationCount = population.getPopulationCount();
		
		final int selected1 = random.nextInt(populationCount);
		int selected2, selected3;
		
		do {
			selected2 = random.nextInt(populationCount);
		} while (selected1 == selected2);
		
		do {
			selected3 = random.nextInt(populationCount);
		} while (selected1 == selected3 || selected2 == selected3);
		
		final T timetable1 = population.get(selected1);
		final T timetable2 = population.get(selected2);
		final T timetable3 = population.get(selected3);
		final double fitness1 = timetable1.getFitness()[0];
		final double fitness2 = timetable2.getFitness()[0];
		final double fitness3 = timetable3.getFitness()[0];
		
		if (fitness1 < fitness3) {
			if (fitness2 < fitness3) {
				parents[0] = timetable1;
				parents[1] = timetable2;
			} else {
				parents[0] = timetable1;
				parents[1] = timetable3;
			}
		} else {
			if (fitness1 < fitness2) {
				parents[0] = timetable1;
				parents[1] = timetable3;
			} else {
				parents[0] = timetable2;
				parents[1] = timetable3;
			}
		}
	}

}
