package jGalapagos.examTimetable.initPopulation;


import jGalapagos.examTimetable.localSearch.LocalSearch;
import jGalapagos.examTimetable.localSearch.LocalSearchFactory;
import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.model.Course.ComparatorByDependence;
import jGalapagos.examTimetable.util.ArrayUtilities;
import jGalapagos.examTimetable.util.MTFRandomGenerator;
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
 * Class that implements method that creates initial population of solutions by backtracking algorithm. 
 *
 */
public class RecursiveInitPopulation implements InitPopulation {

	private static final Log log = LogFactory.getLog(RecursiveInitPopulation.class);
	
	private final ConstantData constantData;
	private final AtomicBoolean stopRequested;
	private final int populationCount;
	private final LocalSearch localSearch;
	private final int timeLimit = 1000;
	
	// help objects
	private final RandomGenerator random = new MTFRandomGenerator();
	private final VariableTimetable timetable;
	private final Integer[] courseIndexes;
	private final int[] buffer;
	private long begin;

	/**
	 * Initial population creator by backtracking algorithm constructor.
	 * @param constantData	Constant data object.
	 * @param random	Instance of a random generator.
	 * @param configuration	Configuration object.
	 * @param stopRequested	Boolean that indicates if algorithm stop is requested.
	 */
	public RecursiveInitPopulation(ConstantData constantData, RandomGenerator random, Configuration configuration, AtomicBoolean stopRequested) {
		this.constantData = constantData;
		this.stopRequested = stopRequested;
		populationCount = configuration.getInt("populationCount");
		localSearch = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearch"));
		buffer = new int[constantData.getTermCount()];
		timetable = new VariableTimetable(constantData);
		
		final int courseLength = constantData.getCourseCount();
		courseIndexes = new Integer[courseLength];
		for (int i = 0; i < courseLength; i++) {
			courseIndexes[i] = i;
		}
		Arrays.sort(courseIndexes, new ComparatorByDependence(constantData));
	}

	@Override
	public ConstantTimetable[] initPopulation() {
		log.info("Creating population of " + populationCount + " solutions");
		ConstantTimetable[] population = new ConstantTimetable[populationCount];
		for (int i = 0; i < populationCount; i++) {
			ConstantTimetable constantTimetable = createNewTimetable();
			if (constantTimetable == null) return null;
			population[i] = constantTimetable;
		}

		Arrays.sort(population);
		return population;
	}
	
	public ConstantTimetable createNewTimetable() {
		while(true) {
			if (stopRequested.get()) return null;
			
			timetable.deleteAllTerms();
			begin = System.currentTimeMillis();
			if (!set(timetable, 0)) {
				continue;
			}
			localSearch.startSearch(timetable);
			return timetable.getConstantTimetable();
		}
	}
	
	private boolean set(final VariableTimetable timetable, final int index) {
		if (index == constantData.getCourseCount()) {
			return true;
		}
		
		int realCourseIndex = courseIndexes[index];
		int possibleTermNumber = timetable.getPossibleTransfers(realCourseIndex, buffer, false);
		
		if (possibleTermNumber == 0) {
			return false;
		}
		
		ArrayUtilities.shuffle(buffer, possibleTermNumber, random);
		
		for (int i = 0; i < possibleTermNumber; i++) {
			if (System.currentTimeMillis() - begin > timeLimit) {
				return false;
			}
			
			timetable.setTermIndex(realCourseIndex, buffer[i]);
			if (set(timetable, index + 1)) {
				return true;
			}
		}
		return false;
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
