package jGalapagos.examTimetable.localSearch;

import jGalapagos.examTimetable.model.VariableTimetable;
/**
 * Class that doesn't perform local search and contains dummy method when no local search
 * is needed.
 * @author Đorđe Grbić
 *
 */
public class NoLocalSearch implements LocalSearch {

	/**
	 * method that does nothing
	 */
	@Override
	public void startSearch(VariableTimetable inputTimetable) {
		// nothing to do
	}

}
