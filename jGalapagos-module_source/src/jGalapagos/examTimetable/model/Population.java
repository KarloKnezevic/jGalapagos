package jGalapagos.examTimetable.model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Abstract class that defines basic functions that implemented population class needs to have.
 * @param <T> Class type that extends <code>Timetable</code> class.
 */
public abstract class Population<T extends Timetable> {
	
	/**Population of timetable objects*/
	private T[] population;
	private final HashSet<Integer> hashSet;
	private final Class<T> populationClass;
	private int populationCount = 0;
	/**Sum of all fitnesses contained within the population*/
	private double fitnessSum;
	protected final int maxSize;
	
	@SuppressWarnings("unchecked")
	/**
	 * Population class initialization constructor.
	 */
	public Population(int maxSize, Class<T> populationClass){
		this.maxSize = maxSize;
		this.populationClass = populationClass;
		population = (T[]) Array.newInstance(populationClass, maxSize);
		hashSet = new HashSet<Integer>(maxSize);
	}
	
	/**
	 * @return	Returns best timetable from population.
	 */
	public abstract T getBest();
	
	/**
	 * @return	Returns best fitness from population.
	 */
	public abstract double[] getBestFitness();
	
	/**
	 * @return returns worst timetable from population
	 */
	public abstract T getWorst();

	/**
	 * @return	Returns worst fitness from population.
	 */
	public abstract double[] getWorstFitness();
	
	/**
	 * @return Returns size of the population
	 */
	public int getPopulationCount() {
		return populationCount;
	}
	
	/**
	 * Returns Timetable from given index.
	 * @param index	Timetable index in the population.
	 * @return	Timetable
	 */
	public T get(int index) {
		return population[index];
	}
	
	/**
	 * @return	Returns population class type.
	 */
	public Class<T> getPopulationClass() {
		return populationClass;
	}

	/**
	 * @return Returns standard deviation of fitnesses contained within the population.
	 */
	public double[] getStandardDeviation() {
		double average = getAverageFitness()[0];
		double standardDeviation = 0.0;
		for (int i = 0; i < populationCount; i++) {
			double value = get(i).getFitness()[0] - average;
			standardDeviation += value * value;
		}
		standardDeviation = standardDeviation / populationCount;
		return new double[] { Math.sqrt(standardDeviation) };
	}
	
	/**
	 * @return Returns average fitness from the population.
	 */
	public double[] getAverageFitness() {
		return new double[] { fitnessSum / populationCount };
	}
	
	/**
	 * Returns <code>true</code> if timetable is contained within the population.
	 * @param timetable	<code>Timetable</code> object.
	 * @return Boolean suggesting if timetable is contained within the population.
	 */
	public boolean contains(Timetable timetable) {
		return contains(timetable, population.length);
	}
	
	/**
	 * Returns <code>true</code> if timetable is contained within the population until
	 * certain index.
	 * @param timetable	<code>Timetable</code> object.
	 * @param untilIndex	Until index.
	 * @return Boolean suggesting if timetable is contained within the population.
	 */
	public boolean contains(Timetable timetable, int untilIndex){
		if (!hashSet.contains(timetable.hashCode())) {
			return false;
		}
		for(int i = 0; i < untilIndex; i++){
			if(population[i] != null && population[i].equals(timetable)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Function that makes this population equal to a certain population.
	 * @param otherPopulation Population that this population will be equal to.
	 */
	protected void makeEqualInside(Population<T> otherPopulation) {
		hashSet.clear();
		hashSet.addAll(otherPopulation.hashSet);
		fitnessSum = otherPopulation.fitnessSum;
		populationCount = otherPopulation.populationCount;
		System.arraycopy(otherPopulation.population, 0, population, 0, population.length);
	}
	
	/**
	 * Clears all timetables from population.
	 */
	public void removeAll() {
		hashSet.clear();
		fitnessSum = 0;
		populationCount = 0;
		Arrays.fill(population, null);
	}
	
	/**
	 * Sets the timetable to a certain position and returns a timetable that was
	 * previously on that position. 
	 * 
	 * @param index
	 *            Timetable will be added to <code>index</code> position
	 *            within the population.
	 * @param timetable
	 *            Timetable which will be added.
	 */
	protected void set(int index, T timetable) {
		final T oldTimetable = get(index);
		if (oldTimetable != null) {
			hashSet.remove(oldTimetable.hashCode());
			fitnessSum -= oldTimetable.getFitness()[0];
		} else {
			populationCount++;
		}
		
		fitnessSum += timetable.getFitness()[0];
		hashSet.add(timetable.hashCode());
		population[index] = timetable;
	}
	
	/**
	 * Removes timetable from index. Unknown behavior if index is too large. 
	 * @param index	Index of removing timetable.
	 */
	protected void remove(int index) {
		final Timetable oldTimetable = get(index);
		if (oldTimetable != null) {
			hashSet.remove(oldTimetable.hashCode());
			fitnessSum -= oldTimetable.getFitness()[0];
			population[index] = null;
		}
	}
	
}