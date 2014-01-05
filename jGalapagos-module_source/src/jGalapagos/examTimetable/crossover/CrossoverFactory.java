package jGalapagos.examTimetable.crossover;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

public class CrossoverFactory {
	/**
	 * Gets instance of crossover implementation depending on configuration file entry.
	 * @param constantData
	 * @param random
	 * @param configuration
	 * @return
	 */
	public static Crossover getInstance(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		String className = configuration.getString("class");
		if (className.equals(CrossoverImpl.class.getName())) {
			return new CrossoverImpl(constantData, random, configuration);
		} else {
			throw new IllegalArgumentException("Unknown Crossover: " + className);
		}
	}

}
