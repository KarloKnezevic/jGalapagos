package jGalapagos.examTimetable.crossover;

import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.VariableTimetable;
/**
 * @author jGalapagos team
 * 
 * Interface that defines crossover method and input data
 */
public interface Crossover {
	/**
	 * Method that makes child solution out of two parents
	 * @param parent1	first parent for crossover
	 * @param parent2	second parent for crossover
	 * @param child	object that contains solution created from two
	 * parents
	 */
	public void crossover(final ConstantTimetable parent1, final ConstantTimetable parent2, final VariableTimetable child);

}
