package jGalapagos.examTimetable.localSearch;

import jGalapagos.examTimetable.model.VariableTimetable;
/**
 * Local search method interface 
 * @author Đorđe Grbić
 *
 */
public interface LocalSearch {
	/**
	 * Method that performs local search improvement on a solution
	 * @param inputTimetable	Timetable to improve by local search
	 */
	public void startSearch(VariableTimetable inputTimetable);
	

}
