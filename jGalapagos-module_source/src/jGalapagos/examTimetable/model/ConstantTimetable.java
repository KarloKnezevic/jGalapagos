package jGalapagos.examTimetable.model;

/**
 * Class that implements <code>Timetable</code> abstract class and is used as timetable thet is never
 * changed when is once created.
 * @author Mihej Komar
 *
 */
public class ConstantTimetable extends Timetable {
	
	private static final long serialVersionUID = 1L;
	private final int[] termsInCourses;
	private final int[][] coursesInTerm;
	private final double[] fitness;
	private final int hashCode;
	
	public ConstantTimetable(int[] termsInCourses, int[][] coursesInTerm, double fitness, int hashCode) {
		this.termsInCourses = termsInCourses;
		this.coursesInTerm = coursesInTerm;
		this.fitness = new double[] { fitness };
		this.hashCode = hashCode;
	}
	
	@Override
	public int getCourseCount(int termIndex) {
		return coursesInTerm[termIndex].length;
	}
	
	@Override
	public int getCourseInTermIndex(int termIndex, int index) {
		return coursesInTerm[termIndex][index];
	}
	
	@Override
	public int getTermIndex(int courseIndex) {
		return termsInCourses[courseIndex];
	}
	
	@Override
	public double[] getFitness() {
		return fitness;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	protected int getCourseCount() {
		return termsInCourses.length;
	}
	
	protected int[] getCourses(int termIndex) {
		return coursesInTerm[termIndex];
	}

	protected int[] getTermsInCourses() {
		return termsInCourses;
	}

}
