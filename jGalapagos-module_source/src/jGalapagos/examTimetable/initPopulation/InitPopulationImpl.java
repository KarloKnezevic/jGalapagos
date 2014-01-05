package jGalapagos.examTimetable.initPopulation;


import jGalapagos.examTimetable.localSearch.LocalSearch;
import jGalapagos.examTimetable.localSearch.LocalSearchFactory;
import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.model.Course.ComparatorByDependence;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Mihej Komar
 * @author Đorđe Grbić
 * 
 * Class that implements method that creates initial population of solutions randomly.
 *
 */
public final class InitPopulationImpl implements InitPopulation {
	
	private final Log log = LogFactory.getLog(InitPopulationImpl.class);
	private final ConstantData constantData;
	private final RandomGenerator random;
	private final int populationCount;
	private final LocalSearch localSearch;
	private final AtomicBoolean stopRequested;
	
	/**
	 * Initial population creator constructor.
	 * @param constantData	Constant data object.
	 * @param random	Instance of a random generator.
	 * @param configuration	Configuration object.
	 * @param stopRequested	Boolean that indicates if algorithm stop is requested.
	 */
	public InitPopulationImpl(ConstantData constantData, RandomGenerator random, Configuration configuration, AtomicBoolean stopRequested) {
		this.constantData = constantData;
		this.random = random;
		this.stopRequested = stopRequested;
		populationCount = configuration.getInt("populationCount");
		localSearch = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearch"));
	}

	@Override
	public ConstantTimetable[] initPopulation() {
		log.info("Creating population of " + populationCount + " solutions");
		ConstantTimetable[] population = new ConstantTimetable[populationCount];
		final int courseLength = constantData.getCourseCount();
		Integer[] courseIndexes = new Integer[courseLength];
		
		for (int i = courseLength; --i >= 0;) {
			courseIndexes[i] = i;
		}

		Arrays.sort(courseIndexes, new ComparatorByDependence(constantData));

		for (int i = 0; i < populationCount; i++) {
			
			if (stopRequested.get()) {
				return null;
			}
			
			VariableTimetable timetable = new VariableTimetable(constantData);

			while (true) {
				boolean tryAgain = false;
				timetable.deleteAllTerms();
				for (int j = 0; j < courseLength; j++) {
					int courseIndex = courseIndexes[j];
					int newTermIndex = timetable.getOneOfPossibleTransfers(courseIndex, random);
					if (newTermIndex == -1) {
						tryAgain = true;
						break;
					} else {
						timetable.setTermIndex(courseIndex, newTermIndex);
					}
				}
				if (!tryAgain) {
					break;
				}
			}
			
			localSearch.startSearch(timetable);
			population[i] = timetable.getConstantTimetable();
		}
		Arrays.sort(population);
		return population;
	}

	@Override
	public ConstantData getConstantData() {
		return constantData;
	}

	@Override
	public int getPopulationSize() {
		return populationCount;
	}

}
