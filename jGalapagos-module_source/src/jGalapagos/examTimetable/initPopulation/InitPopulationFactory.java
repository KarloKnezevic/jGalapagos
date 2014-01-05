package jGalapagos.examTimetable.initPopulation;


import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
/**
 * Factory that creates instance of initial population creator depending on a configuration file. 
 * 
 * @author Mihej Komar
 * @author Đorđe Grbić 
 *
 */
public class InitPopulationFactory {
	/**
	 * Initial population creator instance factory method
	 * @param constantData	Constant problem data
	 * @param random	Instance of a random generator
	 * @param configuration	Algorithm configuration
	 * @param stopRequested	Boolean variable that signals if algorithm stop is requested 
	 * @return	Instance of initial population creator
	 */
	public static InitPopulation getInstance(ConstantData constantData, RandomGenerator random, Configuration configuration, AtomicBoolean stopRequested) {
		String className = configuration.getString("class");
		if (className.equals(InitPopulationImpl.class.getName())) {
			return new InitPopulationImpl(constantData, random, configuration, stopRequested);
		} else if (className.equals(RecursiveInitPopulation.class.getName())) {
			return new RecursiveInitPopulation(constantData, random, configuration, stopRequested);
		} else {
			throw new IllegalArgumentException("Unknown InitPopulation: " + className);
		}
	}

}
