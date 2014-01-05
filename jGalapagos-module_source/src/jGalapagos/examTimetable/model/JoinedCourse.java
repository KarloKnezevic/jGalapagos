package jGalapagos.examTimetable.model;

import java.util.Arrays;

public class JoinedCourse extends Course {

	private static final long serialVersionUID = 1L;
	private final Course course1;
	private final Course course2;
	
	public JoinedCourse(Course course1, Course course2) {
		super(course1.getName() + "#" + course2.getName(), course1.getCode() + "#" + course2.getCode(), course1.getYear(), joinCourses(course1, course2));
		this.course1 = course1;
		this.course2 = course2;
	}
	
	public Course getCourse1() {
		return course1;
	}

	public Course getCourse2() {
		return course2;
	}

	public static String[] joinCourses(Course course1, Course course2){
		String[] jmbags1 = course1.getJmbags();
		String[] jmbags2 = course2.getJmbags();
		
		String[] newJmbags = new String[jmbags1.length + jmbags2.length];
		System.arraycopy(jmbags1, 0, newJmbags, 0, jmbags1.length);
		System.arraycopy(jmbags2, 0, newJmbags, jmbags1.length, jmbags2.length);
		Arrays.sort(newJmbags);
		return newJmbags;
	}

}
