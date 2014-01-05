package jGalapagos.examTimetable.model;

import jGalapagos.core.Solution;

/**
 * Class that contains informations about timetable and implements <code>Solution</code> interface.
 */
public abstract class Timetable implements Solution {
	
	private static final long serialVersionUID = 1L;

	/**Gets term index of the input course index.*/
	public abstract int getTermIndex(int courseIndex);
	
	/**Gets amount of courses contained within term labeled with term index.*/
	public abstract int getCourseCount(int termIndex);
	
	/**Gets Course index contained within input term on index.
	 *  @param termIndex	Term index.
	 *  @param	index	Index within a term.
	*/
	public abstract int getCourseInTermIndex(int termIndex, int index);
	
	/**
	 * 
	 * @return	Returns amount of different courses within timetable.
	 */
	protected abstract int getCourseCount();
	
	/**
	 * Returns measure of similarity between this timetable and input timetable.
	 * Measure of similarity is increased by 1 for every course that is assigned to the same
	 * term within both timetables.
	 * @param timetable	Input timetable
	 * @return	Measure of similarity.
	 */
	public final int similarity(Timetable timetable) {
		int similarity = 0;
		for (int i = getCourseCount(); --i >= 0;) {
			if (getTermIndex(i) == timetable.getTermIndex(i)) {
				similarity++;
			}
		}
		return similarity;
	}
	
	/**
	 * Return if this timetable fitness is better (smaller) than input fitness.
	 * @param fitness Input fitness.
	 * @return	If this timetable fitness is better.
	 */
	public boolean isBetterThan(double[] fitness) {
		return getFitness()[0] < fitness[0];
	}
	
	/**
	 * Return if this timetable fitness is worse (greater) than input fitness.
	 * @param fitness Input fitness.
	 * @return	If this timetable fitness is worse.
	 */
	public boolean isWorseThan(double[] fitness) {
		return getFitness()[0] > fitness[0];
	}
	
	@Override
	public final int compareTo(Solution o) {
		if (getFitness() == o.getFitness()) {
			return 0;
		}
		return (getFitness()[0] - o.getFitness()[0] > 0) ? 1 : -1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Timetable)) {
			return false;
		}
		int objHashCode = obj.hashCode();
		if (objHashCode != this.hashCode()) {
			return false;
		}
		Timetable timetable = (Timetable) obj;
		for (int i = getCourseCount(); --i >= 0;) {
			if (getTermIndex(i) != timetable.getTermIndex(i)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return Double.toString(getFitness()[0]);
	}

}
