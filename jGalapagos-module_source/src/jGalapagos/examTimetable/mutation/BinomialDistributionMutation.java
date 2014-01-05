package jGalapagos.examTimetable.mutation;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.util.FastMath;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

/**
 *	Class that implements <code>Mutation</code> interface with <code>mutate(VariableTimetable timetable)</code>
 *method that performs mutation on genotype based on binomial distribution.
 */
public class BinomialDistributionMutation implements Mutation {
	
	private final RandomGenerator random;
	private final ConstantData constantData;
	private double[] rouletteWheel;
	private int mostProbableIndex;
	private double probability;
	
	/**
	 * Binomial distribution mutation constructor.
	 * @param constantData Constant problem data.
	 * @param random	Instance of a random generator.
	 * @param configuration	Algorithm configuration.
	 */
	public BinomialDistributionMutation(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		this.constantData = constantData;
		this.random = random;
		this.probability = configuration.getDouble("probability");
		rouletteWheel = new double[constantData.getCourseCount()];
		setProbability(probability);
	}
	
	public int nextInteger() {
		double randomNumber = random.nextDouble();
		if (rouletteWheel[mostProbableIndex] > randomNumber) {
			for (int i = mostProbableIndex - 1; i >= 0; i--) {
				if (randomNumber > rouletteWheel[i]) {
					return i + 1;
				}
			}
			return 0;
		} else {
			for (int i = mostProbableIndex + 1; i < constantData.getCourseCount(); i++) {
				if (randomNumber < rouletteWheel[i]) {
					return i;
				}
			}
			return constantData.getCourseCount();
		}
	}

	@Override
	public void mutate(VariableTimetable timetable) {
		int coursesToMutate = nextInteger();
		for (int i = 0; i < coursesToMutate; i++) {
			final int courseIndex = random.nextInt(constantData.getCourseCount());
			int newTerm = timetable.getOneOfPossibleTransfers(courseIndex, random);
			if (newTerm >= 0) {
				timetable.setTermIndex(courseIndex, newTerm);
			}
		}
	}

	@Override
	public void setProbability(double newProbability) {
		rouletteWheel[0] = Math.pow(1 - probability, constantData.getCourseCount());
		double maxProbability = rouletteWheel[0];
		int mostProbableIndex = 0;
		for(int i = 1; i < constantData.getCourseCount(); i++) {
			double probabilityForI = FastMath.binomial(constantData.getCourseCount(), i) * Math.pow(probability, i) * Math.pow(1 - probability, constantData.getCourseCount() - i);
			if (probabilityForI > maxProbability) {
				mostProbableIndex = i;
			}
			rouletteWheel[i] = rouletteWheel[i - 1] + probabilityForI;
		}
		this.mostProbableIndex = mostProbableIndex;
	}

}
