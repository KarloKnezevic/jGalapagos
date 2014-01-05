package jGalapagos.examTimetable.parentSelection;

import jGalapagos.examTimetable.model.Population;
import jGalapagos.examTimetable.model.Timetable;

/**
 * Interface that defines prototype of method that selects a set of parents from population.
 */
public interface ParentSelection {
	/**
	 * Method that assigns a set of parents to array selected form a population of parents.
	 * @param <T> Type of class that extends <code>Timetable</code>
	 * @param population Population on which selection is performed
	 * @param parents	Array of selected parents.
	 */
	public <T extends Timetable> void selectTwoParents(Population<T> population, T[] parents);

}
