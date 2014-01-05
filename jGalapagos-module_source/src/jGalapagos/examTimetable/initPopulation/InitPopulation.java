package jGalapagos.examTimetable.initPopulation;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;

/**
 * 
 * @author Mihej Komar
 * 
 * Interface that defines functions that need to be implemented
 * for class that produces initial population of solutions.
 *
 */
public interface InitPopulation {
	
	/**
	 * Method that creates initial population of timetables.
	 * @return Array that contains constant timetables (<code>ConstantTimetable</code>). 
	 */
	public ConstantTimetable[] initPopulation();
	/**
	 * 
	 * @return	Returns <code>ConstantData</code> object.
	 */
	public ConstantData getConstantData();
	/**
	 * 
	 * @return	Returns size of the population
	 */
	public int getPopulationSize();
	
}
