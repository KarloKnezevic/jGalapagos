package jGalapagos.examTimetable.mutation;

import jGalapagos.examTimetable.model.VariableTimetable;

/**
 *	Interface defining methods that mutation classes must implement.
 */
public interface Mutation {
	/**
	 * Mutates input timetable.
	 * @param data Input timetable.
	 */
	public void mutate(final VariableTimetable data);
	
	/**
	 * Sets probability that mutation operates with.
	 * @param newProbability New operating mutation probability.
	 */
	public void setProbability(double newProbability);

}
