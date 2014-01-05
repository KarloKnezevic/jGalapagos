package jGalapagos.examTimetable.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Class that contains informations about course. Name, year, code and enrolled students.
 * @author Mihej Komar
 * @author Đorđe Grbić
 *
 */
public class Course implements Serializable {

	private static final long serialVersionUID = 1L;
	/**Name of the course*/
	private final String name;
	/**Unique code of the course*/
	private final String code;
	/**Year of the course lecturing*/
	private final int year;
	
	/** Sorted array of JMBAGs. (Unique student number). */
	private final String[] jmbags;
	private final int studentCount;
	/**Penalty factor for the course used in fitness calculation*/
	private double penaltyFactor = 1.0;

	/**
	 * Course object constructor.
	 * @param name	Name of the course.
	 * @param code	Unique code of the course.
	 * @param year	Year of the course.
	 * @param jmbags	Array of JMBAGs of the enrolled students.
	 */
	public Course(String name, String code, int year, String[] jmbags) {
		this.name = name;
		this.code = code;
		this.year = year;
		this.jmbags = jmbags;
		studentCount = jmbags.length;
	}
	/**
	 * @return	Returns the name of the course.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return Returns the code of the course.
	 */
	public final String getCode() {
		return code;
	}

	/**
	 * @return	Returns the year in which course is teached.
	 */
	public int getYear() {
		return year;
	}
	
	/**
	 * Sets penalty factor for fitness calculation for the course.
	 * @param penaltyFactor
	 */
	public void setPenaltyFactor(double penaltyFactor) {
		this.penaltyFactor = penaltyFactor;
	}

	/**
	 * @return	Returns penalty factor for fitness calculation for the course.
	 */
	public double getPenaltyFactor() {
		return penaltyFactor;
	}

	/**
	 * @return	 Returns JMBAGs of the enrolled students
	 */
	public final String[] getJmbags() {
		return jmbags;
	}

	/**
	 * @return	Returns the count of the enrolled students
	 */
	public final int getStudentCount() {
		return studentCount;
	}

	/**
	 * Class that implements dependence comparator between courses.
	 * The course which has higher number of shared students has higher dependence.
	 * @see ConstantData
	 */
	public static final class ComparatorByDependence implements Comparator<Integer> {
		
		private final int[] dependences;

		public ComparatorByDependence(final ConstantData constantData) {
			int courseCount = constantData.getCourseCount();
			dependences = new int[courseCount];
			
			for (int i = 0; i < courseCount; i++) {
				int dependence = 0;
				for (int j = 0; j < courseCount; j++) {
					if (i == j) continue;
					dependence += constantData.getPenaltyBetweenCourses(i, j);
				}
				dependences[i] = dependence;
			}
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return dependences[o2] - dependences[o1];
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (!(obj instanceof Course)) { return false; }
		Course course = (Course) obj;
		return name.equals(course.name) && course.equals(course.code);
	}

}
