package jGalapagos.examTimetable.mutation;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

public class MutationFactory {
	
	/**
	 * Factory that returns mutation instance based on configuration data.
	 * @param constantData Constant problem data.
	 * @param random	Instance of a random generator.
	 * @param configuration	Algorithm configuration.
	 * @return Returns mutation class instance.
	 * @see Mutation
	 */
	public static Mutation getInstance(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		String className = configuration.getString("class");
		if (className.equals(SinglePermutation.class.getName())) {
			return new SinglePermutation(constantData, random, configuration);
		} else if (className.equals(MultiplePermutations.class.getName())) {
			return new MultiplePermutations(constantData, random, configuration);
		} else if (className.equals(BinomialDistributionMutation.class.getName())) {
			return new BinomialDistributionMutation(constantData, random, configuration);
		} else {
			throw new IllegalArgumentException("Unknown Mutation: " + className);
		}
	}

}
