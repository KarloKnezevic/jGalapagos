package jGalapagos.examTimetable.mutation;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

/**
 *	Class that implements <code>Mutation</code> interface with <code>mutate(VariableTimetable timetable)</code>
 *method that performs mutation by rolling dice on every gene of the genotype.
 */
public final class MultiplePermutations implements Mutation {
	
	private final ConstantData constantData;
	private final RandomGenerator random;
	
	private double probability;
	
	/**
	 * Multiple permutation mutation constructor.
	 * @param constantData Constant problem data.
	 * @param random	Instance of a random generator.
	 * @param configuration	Algorithm configuration.
	 */
	public MultiplePermutations(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		this.constantData = constantData;
		this.random = random;
		probability = configuration.getDouble("probability");
	}
	
	@Override
	public void mutate(final VariableTimetable timetable) {
		int courseCount = constantData.getCourseCount();
		for (int courseIndex = 0; courseIndex < courseCount; courseIndex++) {
			if (random.nextDouble() < probability) {
				int newTermIndex = timetable.getOneOfPossibleTransfers(courseIndex, random);
				if (newTermIndex != -1) {
					timetable.setTermIndex(courseIndex, newTermIndex);
				}
			}
		}
	}
	
	@Override
	public void setProbability(double newProbability) {
		this.probability = newProbability;
	}

}
