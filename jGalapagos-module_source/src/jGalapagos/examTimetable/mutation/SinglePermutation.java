package jGalapagos.examTimetable.mutation;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

/**
 *	Class that implements <code>Mutation</code> interface with <code>mutate(VariableTimetable timetable)</code>
 *method that performs mutation on one and only one random gene. Throws exception if <code>setProbability(double newProbability)</code>
 *method is called since it doesn't use mutation probability.
 */
public final class SinglePermutation implements Mutation {
	
	private final ConstantData constantData;
	private final RandomGenerator random;
	
	/**
	 * Single permutation mutation constructor.
	 * @param constantData Constant problem data.
	 * @param random	Instance of a random generator.
	 * @param configuration	Algorithm configuration.
	 */
	public SinglePermutation(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		this.constantData = constantData;
		this.random = random;
	}
	
	@Override
	public void mutate(VariableTimetable timetable) {
		final int courseCount = constantData.getCourseCount();
		while (true) {
			int mutationPosition = random.nextInt(courseCount);
			int newTermIndex =  timetable.getOneOfPossibleTransfers(mutationPosition, random);
			if (newTermIndex != -1) {
				timetable.setTermIndex(mutationPosition, newTermIndex);
				break;
			}
		}
	}

	@Override
	public void setProbability(double newProbability) {
		throw new IllegalStateException("SinglePermutation doesn't use probability.");
	}

}
