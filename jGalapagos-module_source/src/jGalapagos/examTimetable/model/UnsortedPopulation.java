package jGalapagos.examTimetable.model;

import jGalapagos.examTimetable.initPopulation.InitPopulation;

/**
 * Class that implements <code>Population</code> abstract class and keeps timetables
 * unsorted.
 * @param <T>	T is of Timetable type.
 */
public class UnsortedPopulation<T extends Timetable> extends Population<T> {
	
	@SuppressWarnings("unchecked")
	/** Constructor that gets initial population creator and fills population array with timetables.
	 * @param	initPopulation	Object of <code>InitPopulation</code> type that contains population creator method.
	 * @param	constantData	Object that contains constant data about problem.
	 * @param	populationClass	Population class type.
	 */
	public UnsortedPopulation(InitPopulation initPopulation, ConstantData constantData, Class<T> populationClass) {
		super(initPopulation.getPopulationSize(), populationClass);
		ConstantTimetable[] initialPopulation = initPopulation.initPopulation();
		if (initialPopulation == null) return;
		if (populationClass.equals(ConstantTimetable.class)) {
			for (int i = 0; i < initialPopulation.length; i++) {
				add((T) initialPopulation[i], Replace.ADD);
			}
		} else if (populationClass.equals(VariableTimetable.class)) {
			for (int i = 0; i < initialPopulation.length; i++) {
				VariableTimetable variableTimetable = new VariableTimetable(constantData);
				variableTimetable.makeMeEqualAs(initialPopulation[i]);
				add((T) variableTimetable, Replace.ADD);
			}
		}
	}
	
	/**
	 * Constructor of an empty population.
	 * @param maxSize Maximum size of an emty population.
	 * @param populationClass	Population class type.
	 */
	public UnsortedPopulation(int maxSize, Class<T> populationClass) {
		super(maxSize, populationClass);
	}
	
	private int bestTimetableIndex = -1;
	private int worstTimetableIndex = -1;

	@Override
	public T getBest() {
		return get(bestTimetableIndex);
	}

	@Override
	public double[] getBestFitness() {
		return getBest().getFitness();
	}

	@Override
	public T getWorst() {
		return get(worstTimetableIndex);
	}

	@Override
	public double[] getWorstFitness() {
		return getWorst().getFitness();
	}
	
	/**
	 * Replaces timetable from population with new timetable decided by replace token. 
	 * @param timetable	Timetable to be inserted into population.
	 * @param replace	Decides which timetable will be thrown out of population. 
	 * @see Replace
	 */
	public void add(T timetable, Replace replace) {
		if (getPopulationCount() == 0) {
			if (replace == Replace.ADD) {
				bestTimetableIndex = 0;
				worstTimetableIndex = 0;
				set(0, timetable);
				return;
			} else {
				throw new IllegalStateException("Cannot replace " + replace + " from empty population.");
			}
		}
		
		switch (replace) {
		case BEST:
			if (timetable.isBetterThan(getBestFitness())) {
				set(bestTimetableIndex, timetable);
			} else {
				set(bestTimetableIndex, timetable);
				updateBestAndWorst();
			}
			break;
		case WORST:
			if (timetable.isWorseThan(getWorstFitness())) {
				set(worstTimetableIndex, timetable);
			} else {
				set(worstTimetableIndex, timetable);
				updateBestAndWorst();
			}
			break;
		case MOST_SIMILAR:
			int mostSimilarIndex = 0;
			for (int i = 0; i < getPopulationCount(); i++) {
				if (mostSimilarIndex < get(i).similarity(timetable)) {
					mostSimilarIndex = i;
				}
			}
			set(mostSimilarIndex, timetable);
			updateBestAndWorst();
			break;
		case ADD:
			if (timetable.isBetterThan(getBestFitness())) {
				bestTimetableIndex = getPopulationCount();
			}
			if (timetable.isWorseThan(getWorstFitness())) {
				worstTimetableIndex = getPopulationCount();
			}
			set(getPopulationCount(), timetable);
			break;
		default:
			throw new IllegalStateException("Unknown replace: " + replace);
		}
	}
	
	/**
	 * Function that makes this population equal to a certain population.
	 * @param otherPopulation Population that this population will be equal to.
	 */
	public void makeEqual(UnsortedPopulation<T> otherPopulation) {
		bestTimetableIndex = otherPopulation.bestTimetableIndex;
		worstTimetableIndex = otherPopulation.worstTimetableIndex;
		makeEqualInside(otherPopulation);
	}
	
	private void updateBestAndWorst() {
		bestTimetableIndex = 0;
		worstTimetableIndex = 0;
		for (int i = 1; i < getPopulationCount(); i++) {
			if (get(i).isBetterThan(getBestFitness())) {
				bestTimetableIndex = i;
			}
			if (get(i).isWorseThan(getWorstFitness())) {
				worstTimetableIndex = i;
			}
		}
	}

	public static enum Replace {
		BEST, WORST, MOST_SIMILAR, ADD
	}

}
