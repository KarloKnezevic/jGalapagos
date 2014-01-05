package jGalapagos.examTimetable.localSearch;


import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;
/**
 * Local search instance factory
 * @author Đorđe Grbić
 *
 */
public class LocalSearchFactory {
	/**
	 * Local search instance factory method
	 * @param constantData	Constant problem data
	 * @param random	Instance of a random generator
	 * @param configuration	Algorithm configuration
	 * @return	Instance of a local search algorithm
	 */
	public static LocalSearch getInstance(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		String className = configuration.getString("class");
		if (className.equals(Tabu.class.getName())) {
			return new Tabu(constantData, random, configuration);
		}else if(className.equals(DependLs.class.getName())){
			return new DependLs(constantData, random, configuration);
		}else if(className.equals(OnePerRoundLs.class.getName())){
			return new OnePerRoundLs(constantData, random, configuration);
		}else if(className.equals(NoLocalSearch.class.getName())){
			return new NoLocalSearch();
		}else {
			throw new IllegalArgumentException("Unknown LocalSearch: " + className);
		}
	}
}
