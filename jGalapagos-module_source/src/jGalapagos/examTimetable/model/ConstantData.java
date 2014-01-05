package jGalapagos.examTimetable.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class ConstantData implements Serializable {

	private static final Log log = LogFactory.getLog(ConstantData.class);
	private static final long serialVersionUID = 1L;
	
	/** Array of courses. Indexes of this array are used in program. */
	private final Course[] courses;
	
	/** Array of terms. Indexes of this array are used in program. */
	private final Term[] terms;
	
	/**
	 * Value is <code>true</code> if certain term is acceptable for certain
	 * course. The first index of the array is the index of a course that is being checked
	 * and a the second index is the index of a term. 
	 */
	private final boolean[][] acceptableTerms;
	
	/** Penalty between two courses. */
	private final double[][] penaltyBetweenCourses;
	
	/** Array of indexes of terms sorted by days. The first index is the index of a day. */
	private final int[][] termsInDays;
	
	/** Array which contains a set of course indexes for certain group. Courses are sorted.*/
	private final int[][] courseGroups;
	
	/**Array which contains course group label. */
	private final String[] courseGroupLabel;
	
	/** Sorted array of students. */
	private final String[] jmbags;
	
	/**Array that contains all courses enrolled by student. Courses are sorted.*/
	private final int[][] coursesForStudent;
	
	/** Array containing the group for a student. */
	private final int[] groupForStudent;
	
	/**Array containing penalty between terms.*/
	private final double[][] penaltyBetweenTerms;
	
	/**Array containing penalty between days.*/
	private final double[] penaltyBetweenDays;

	/**
	 * Array that defines pairs of courses that are penalized no matter how many students share that
	 * pair of courses. The first index is the index of a pair and the other index is either 1 or 0, if
	 * pair is unconditionally penalized or not respectively. 
	 */
	private final int[][] coursesPairsToPenalize;
	
	private static final double COURSE_NOT_IN_GROUP = 0.25;
	
	/**
	 * Constant data constructor.
	 * @param courses Array of all courses.
	 * @param terms	Array of all terms.
	 * @param acceptableTerms	Array of acceptable course-term pairs.
	 * @param courseGroups	Array of course groups.
	 * @param jmbags	Array of all JMBAGs.
	 * @param coursesForStudent	Array of courses enrolled by each students.
	 * @param groupForStudent	Array of student groups.
	 * @param coursesPairsToPenalize	Array of unconditionally penalized pairs of courses.  
	 * @param courseGroupLabel	Course group labels.
	 */
	public ConstantData(Course[] courses, Term[] terms, boolean[][] acceptableTerms, 
			int[][] courseGroups, String[] jmbags, int[][] coursesForStudent, int[] groupForStudent, int[][] coursesPairsToPenalize, String[] courseGroupLabel) {
		this.courses = courses;
		this.terms = terms;
		this.acceptableTerms = acceptableTerms;
		this.jmbags = jmbags;
		this.groupForStudent = groupForStudent;
		this.courseGroups = courseGroups;
		this.coursesForStudent = coursesForStudent;
		this.coursesPairsToPenalize = coursesPairsToPenalize;
		this.courseGroupLabel = courseGroupLabel;
		
		termsInDays = createTermsInDays();
		penaltyBetweenCourses = createPenaltyBetweenCourses();
		penaltyBetweenTerms = createPenaltyBetweenTerms();
		penaltyBetweenDays = createPenaltyBetweenDays();
	}

	private int[][] createTermsInDays() {
		int maxDayIndex = -1;
		for (int i = terms.length; --i >= 0;) {
			if (terms[i].getDayIndex() > maxDayIndex) {
				maxDayIndex = terms[i].getDayIndex();
			}
		}
		final int[][] termsInDays = new int[maxDayIndex][];
		for (int i = 0; i < terms.length; i++) {
			final Term term = terms[i];
			final int dayIndex = term.getDayIndex() - 1;
			int[] termsInDay = termsInDays[dayIndex];
			if (termsInDay == null) {
				termsInDay = new int[1];
				termsInDay[0] = i;
			} else {
				final int[] oldTermsInDay = termsInDay;
				termsInDay = new int[termsInDay.length + 1];
				System.arraycopy(oldTermsInDay, 0, termsInDay, 0, oldTermsInDay.length);
				termsInDay[oldTermsInDay.length] = i;
			}
			termsInDays[dayIndex] = termsInDay;
		}
		return termsInDays;
	}
	
	private double[][] createPenaltyBetweenCourses() {
		final double[][] penaltyBetweenCourses = new double[courses.length][courses.length];
		for (int studentIndex = 0; studentIndex < jmbags.length; studentIndex++) {
			int[] coursesForStudent = this.coursesForStudent[studentIndex];
			int[] coursesForGroupForStudent = courseGroups[groupForStudent[studentIndex]];
			int studentYear = courses[coursesForGroupForStudent[0]].getYear();
			for (int i = 0; i < coursesForStudent.length - 1; i++) {
				int courseIndex1 = coursesForStudent[i];
				int courseYear1 = courses[courseIndex1].getYear();
				boolean courseInGroup1 = Arrays.binarySearch(coursesForGroupForStudent, courseIndex1) >= 0;
				for (int j = i + 1; j < coursesForStudent.length; j++) {
					int courseIndex2 = coursesForStudent[j];
					int courseYear2 = courses[courseIndex2].getYear();
					boolean courseInGroup2 = Arrays.binarySearch(coursesForGroupForStudent, courseIndex2) >= 0;
					if (courseInGroup1 && courseInGroup2) {
						penaltyBetweenCourses[courseIndex1][courseIndex2] += 1.0;
						penaltyBetweenCourses[courseIndex2][courseIndex1] += 1.0;
					} else if (!courseInGroup1 && courseInGroup2) {
						if (courseYear1 == studentYear) {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.5;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.5;
						} else {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.25;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.25;
						}
					} else if (courseInGroup1 && !courseInGroup2) {
						if (courseYear2 == studentYear) {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.5;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.5;
						} else {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.25;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.25;
						}
					} else {
						if (courseYear1 != studentYear && courseYear2 != studentYear) {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.125;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.125;
						} else if (courseYear1 == studentYear && courseYear2 == studentYear) {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.5;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.5;
						} else {
							penaltyBetweenCourses[courseIndex1][courseIndex2] += 0.25;
							penaltyBetweenCourses[courseIndex2][courseIndex1] += 0.25;
						}
					}
				}
			}
		}
		for (int i = 0; i < courses.length; i++) {
			double penalty1 = courses[i].getPenaltyFactor();
			for (int j = 0; j < courses.length; j++) {
				double penalty2 = courses[j].getPenaltyFactor();
				penaltyBetweenCourses[i][j] *= Math.min(penalty1, penalty2);
			}
		}
		return penaltyBetweenCourses;
	}
	
	private double[][] createPenaltyBetweenTerms() {
		double[][] penaltyBetweenTerms = new double[terms.length][terms.length];
		for (int termIndex1 = 0; termIndex1 < terms.length; termIndex1++) {
			for (int termIndex2 = 0; termIndex2 < terms.length; termIndex2++) {
				Term term1 = terms[termIndex1];
				Term term2 = terms[termIndex2];
				long termTime1 = term1.getDate().getTime();
				long termTime2 = term2.getDate().getTime();
				double dayDistance = Math.abs(termTime1 - termTime2) / 86400000.0;
				if (dayDistance >= 4.0) continue;
				double penalty = Math.pow(7.0, 1.0 - dayDistance);
				if (term1.getDayIndex() == term2.getDayIndex()) penalty *= 10;
				penaltyBetweenTerms[termIndex1][termIndex2] = penalty;
			}
		}
		return penaltyBetweenTerms;
	}
	
	private double[] createPenaltyBetweenDays() {
		double[] penaltyBetweenDays = new double[termsInDays.length];
		penaltyBetweenDays[0] = 200;
		penaltyBetweenDays[1] = 100;
		for (int dayDistance = 2; dayDistance < termsInDays.length; dayDistance++) {
			double penalty = 30 * 2.0/(1+Math.exp(Math.pow(Math.pow(0.2 * dayDistance, 2), 1.5)));
			penaltyBetweenDays[dayDistance] = penalty;
		}
		return penaltyBetweenDays;
	}
	
	/**
	 * Returns course count.
	 * @return	Course count
	 */
	public int getCourseCount() {
		return courses.length;
	}
	
	/**
	 * Returns term count.
	 * @return	Term count
	 */
	public int getTermCount() {
		return terms.length;
	}
	
	/**
	 * Returns day count.
	 * @return	Day count
	 */
	public int getDayCount() {
		return termsInDays.length;
	}
	
	/**
	 * Returns all terms contained in the day.
	 * @param dayIndex	Index of the day.
	 * @return	Array of terms.
	 */
	public int[] getTermsForDay(int dayIndex) {
		return termsInDays[dayIndex];
	}
	
	/**
	 * Returns <code>Term</code> object for the term index.
	 * @param termIndex Term index.
	 * @return	<code>Term</code> object
	 */
	public Term getTerm(int termIndex) {
		return terms[termIndex];
	}
	
	/**
	 * Returns <code>Course</code> object for the Course index.
	 * @param termIndex Course index.
	 * @return	<code>Course</code> object
	 */
	public Course getCourse(int courseIndex) {
		return courses[courseIndex];
	}
	
	/**
	 * Gets penalty between courses.
	 * @param courseIndex1	First course index.
	 * @param courseIndex2	Second course index.
	 * @return	Penalty between courses
	 */
	public double getPenaltyBetweenCourses(int courseIndex1, int courseIndex2) {
		return penaltyBetweenCourses[courseIndex1][courseIndex2];
	}
	
	/**
	 * Returns if a course can be assigned to a term.
	 * @param courseIndex	Course index.
	 * @param termIndex	Term index.
	 * @return	is course term assignment is acceptable.
	 */
	public boolean isTermAcceptable(int courseIndex, int termIndex) {
		return acceptableTerms[courseIndex][termIndex];
	}
	
	/**
	 * Returns count of course pairs.
	 * @return	Count of course pairs.
	 */
	public int getCountOfCoursesPairs() {
		return coursesPairsToPenalize.length;
	}
	
	/**
	 * Returns pair of courses labeled by course pair index.
	 * @param coursesPairIndex	courses pair index.
	 * @return	Pair of course indexes.
	 */
	public int[] getCoursesPair(int coursesPairIndex) {
		return coursesPairsToPenalize[coursesPairIndex];
	}
	
	public double getPenaltyBetweenTerms(int termIndex1, int termIndex2) {
		return penaltyBetweenTerms[termIndex1][termIndex2];
	}
	
	/**
	 * Day penalty between two terms 
	 * @param termIndex1	Term index 1.
	 * @param termIndex2	Term index 2.
	 * @return	Penalty.
	 */
	public double getPenaltyBetweenDays(int termIndex1, int termIndex2) {
		Term term1 = terms[termIndex1];
		Term term2 = terms[termIndex2];
		int dayDistance = Math.abs(term1.getDayIndex() - term2.getDayIndex());
		return penaltyBetweenDays[dayDistance];
	}
	
	/**
	 * @return	Gets student count
	 */
	public int getStudentCount() {
		return jmbags.length;
	}
	
	/**
	 * Course indexes enrolled by a student.
	 * @param studentIndex	Student index.
	 * @return	Array of courses.
	 */
	public int[] getCourseIndexes(int studentIndex) {
		return coursesForStudent[studentIndex];
	}
	
	/**
	 * Gets JMBAG of a student.
	 * @param studentIndex	Student index.
	 * @return	JMBAG.
	 */
	public String getJmbag(int studentIndex) {
		return jmbags[studentIndex];
	}
	
	/**
	 * @return	Course group count.
	 */
	public int getCourseGroupCount() {
		return courseGroups.length;
	}
	
	/**
	 * Gets course indexes contained in a group.
	 * @param groupIndex Group index.
	 * @return	Course indexes.
	 */
	public int[] getCourseGroupIndexes(int groupIndex) {
		return courseGroups[groupIndex];
	}
	
	/**
	 * Returns course group label for the group index.
	 * @param groupIndex	Group index.
	 * @return	Course group label.
	 */
	public String getCourseGroupLabel(int groupIndex) {
		return courseGroupLabel[groupIndex];
	}
	
	/**
	 * Returns group code for the student.
	 * @param studentIndex Student index.
	 * @return Group code.
	 */
	public int getGroupIndexForStudent(int studentIndex) {
		return groupForStudent[studentIndex];
	}
	
	/**
	 * Method returns part of fitness added by the student in the certain timetable. 
	 * @param studentIndex	Index of the student.
	 * @param timetable	Timetable for which fitness part will be calculated.
	 * @return	Part of fitness added by <code>studentIndex</code>.
	 */
	public double getFitnessForStudent(int studentIndex, ConstantTimetable timetable) {
		double fitness = 0.0;
		int[] coursesForStudent = this.coursesForStudent[studentIndex];
		int[] coursesForGroupForStudent = courseGroups[groupForStudent[studentIndex]];
		for (int i = 0; i < coursesForStudent.length - 1; i++) {
			int courseIndex1 = coursesForStudent[i];
			boolean courseInGroup1 = Arrays.binarySearch(coursesForGroupForStudent, courseIndex1) >= 0;
			double penalty1 = courses[courseIndex1].getPenaltyFactor();
			long termTime1 = getTerm(timetable.getTermIndex(courseIndex1)).getDate().getTime();
			for (int j = i + 1; j < coursesForStudent.length; j++) {
				int courseIndex2 = coursesForStudent[j];
				long termTime2 = getTerm(timetable.getTermIndex(courseIndex2)).getDate().getTime();
				double dayDistance = Math.abs(termTime1 - termTime2) / 86400000.0;
				if (dayDistance >= 4.0) continue;
				double penaltyBetweenTerms = Math.pow(7.0, 1.0 - dayDistance);
				double penalty2 = courses[courseIndex2].getPenaltyFactor();
				if (!courseInGroup1 || (Arrays.binarySearch(coursesForGroupForStudent, courseIndex2) < 0)) {
					fitness += COURSE_NOT_IN_GROUP * Math.min(penalty1, penalty2) * penaltyBetweenTerms;
				} else {
					fitness += 1.0 * Math.min(penalty1, penalty2) * penaltyBetweenTerms;
				}
			}
		}
		return fitness;
	}
	
	/**
	 * Method that loads constant data from formated text file and returns ConstantData object containing
	 * loaded data.
	 * @param file	Path to a file containing main problem data.
	 * @param byYearsFile	Path to a text file containing courses assigned to a year.
	 * @param courseGroupsFile	Path to a text file containing courses assigned to a group.
	 * @param studentGroupFile	Path to a text file containing students assigned to a group.
	 * @param coursePairsFile	Path to a text file containing course pairs. 
	 * @return	Returns ConstantData object containing loaded data.
	 */
	public static ConstantData loadTextData(final String file, String byYearsFile, String courseGroupsFile, String studentGroupFile, String coursePairsFile) {

		// help data
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		BufferedReader reader = null;
		int currentLine = 0;
		
		// load by year data
		Map<String, Integer> courseYears = new HashMap<String, Integer>();
		try {
			log.info("Loading file " + byYearsFile + ".");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(byYearsFile), "UTF8"));
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] lineData = line.split("\t");
				if (lineData.length != 2) {
					log.warn("File " + byYearsFile + " in wrong format, error in line " + line);
					continue;
				}
				int year;
				try {
					year = Integer.parseInt(lineData[1]);
				} catch (NumberFormatException e) {
					log.warn("File " + byYearsFile + " in wrong format, error in line " + line, e);
					continue;
				}
				courseYears.put(lineData[0], year);
			}
		} catch (IOException e) {
			log.warn("Error reading file " + byYearsFile, e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		// load data file
		Course[] courses;
		Term[] terms;
		boolean[][] acceptableTerms;
		try {
			// opening file
			log.info("Loading file " + file + ".");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			
			// reading courses and students
			int courseCount = Integer.parseInt(reader.readLine());
			currentLine++;
			courses = new Course[courseCount];
			for (int i = 0; i < courseCount; i++) {
				String[] lineData = reader.readLine().split("#", -1);
				currentLine++;
				String code = lineData[0];
				Integer year = courseYears.get(code);
				if (year == null) {
					log.warn("Course " + code + " not defined in " + byYearsFile + ".");
					year = 0;
				}
				String[] jmbags = lineData[2].split(",");
				Arrays.sort(jmbags);
				Course course = new Course(lineData[1], code, year, jmbags);
				courses[i] = course;
			}
			
			// reading terms
			int termNumber = Integer.parseInt(reader.readLine());
			currentLine++;
			terms = new Term[termNumber];
			for (int i = 0; i < termNumber; i++) {
				String[] lineData = reader.readLine().split("#", -1);
				currentLine++;
				Term term = new Term(dateTimeFormat.parse(lineData[0]), Integer.parseInt(lineData[1]), Integer.parseInt(lineData[2]), Integer.parseInt(lineData[3]), Integer.parseInt(lineData[4]), lineData[5]);
				terms[i] = term;
			}
			
			// reading inseparable courses
			int inseparableCourseNumber = Integer.parseInt(reader.readLine());
			currentLine++;
			for (int i = 0; i < inseparableCourseNumber; i++) {
				String[] lineData = reader.readLine().split("#", -1);
				currentLine++;
				Course course1 = null;
				Course course2 = null;
				int courseIndex1 = -1;
				int courseIndex2 = -1;
				for (int j = 0; j < courses.length; j++) {
					Course course = courses[j];
					if (course.getCode().equals(lineData[0])) {
						course1 = course;
						courseIndex1 = j;
					} else if (course.getCode().equals(lineData[1])) {
						course2 = course;
						courseIndex2 = j;
					}
					if (course1 != null && course2 != null) {
						break;
					}
				}
				
				Course newCourse = new JoinedCourse(course1, course2);
				
				int firstIndex, lastIndex;
				if (courseIndex1 < courseIndex2) {
					firstIndex = courseIndex1;
					lastIndex = courseIndex2;
				} else {
					firstIndex = courseIndex2;
					lastIndex = courseIndex1;
				}
				
				courses[firstIndex] = newCourse;
				
				Course[] oldCourses = courses;
				courses = new Course[courses.length - 1];
				System.arraycopy(oldCourses, 0, courses, 0, lastIndex);
				System.arraycopy(oldCourses, lastIndex + 1, courses, lastIndex, oldCourses.length - lastIndex - 1);
			}
			
			// reading acceptable terms
			acceptableTerms = new boolean[courses.length][terms.length];
			Set<Integer> definedCourses = new HashSet<Integer>();
			int acceptableTermNumber = Integer.parseInt(reader.readLine());
			currentLine++;
			for (int i = 0; i < acceptableTermNumber; i++) {
				String[] lineData = reader.readLine().split("#", -1);
				currentLine++;
				int courseIndex = -1;
				for (int j = 0; j < courses.length; j++) {
					Course course = courses[j];
					if (course instanceof JoinedCourse) {
						JoinedCourse joinedCourse = (JoinedCourse) course;
						if (joinedCourse.getCourse1().getCode().equals(lineData[0]) || joinedCourse.getCourse2().getCode().equals(lineData[0])) {
							courseIndex = j;
							break;
						}
					} else {
						if (course.getCode().equals(lineData[0])) {
							courseIndex = j;
							break;
						}
					}
				}
				if (courseIndex == -1){
					log.error("Unknown course code " + lineData[0] + " in row " + currentLine);
					return null;
				}
				if (!definedCourses.contains(courseIndex)) {
					for (String termLabel : lineData[1].split(",", -1)) {
						int termIndex = -1;
						for (int j = 0; j < terms.length; j++) {
							if (terms[j].getLabel().equals(termLabel)) {
								termIndex = j;
								break;
							}
						}
						if (termIndex == -1){
							log.error("Unknown term label " + termLabel + " in row " + currentLine);
							return null;
						}
						acceptableTerms[courseIndex][termIndex] = true;
					}
					definedCourses.add(courseIndex);
				} else {
					boolean[] newAcceptableTerms = new boolean[terms.length];
					for (String termLabel : lineData[1].split(",", -1)) {
						int termIndex = -1;
						for (int j = 0; j < terms.length; j++) {
							if (terms[j].getLabel().equals(termLabel)) {
								termIndex = j;
								break;
							}
						}
						newAcceptableTerms[termIndex] = true;
					}
					for (int j = 0; j < terms.length; j++) {
						acceptableTerms[courseIndex][j] = acceptableTerms[courseIndex][j] && newAcceptableTerms[j];
					}
				}
			}
			
			// read penalty factor
			boolean[] isPenaltySet = new boolean[courses.length];
			int penaltyNumber = Integer.parseInt(reader.readLine());
			currentLine++;
			for (int i = 0; i < penaltyNumber; i++) {
				String[] lineData = reader.readLine().split("#", -1);
				currentLine++;
				for (int j = 0; j < courses.length; j++) {
					Course course = courses[j];
					if (course instanceof JoinedCourse) {
						JoinedCourse joinedCourse = (JoinedCourse) course;
						
						if (isPenaltySet[j]) {
							int alreadySetStudentCount, currentlySetStudentCount;
							if (joinedCourse.getCourse1().getCode().equals(lineData[0])) {
								alreadySetStudentCount = joinedCourse.getCourse2().getStudentCount();
								currentlySetStudentCount = joinedCourse.getCourse1().getStudentCount();
							} else if (joinedCourse.getCourse2().getCode().equals(lineData[0])) {
								alreadySetStudentCount = joinedCourse.getCourse1().getStudentCount();
								currentlySetStudentCount = joinedCourse.getCourse2().getStudentCount();
							} else {
								continue;
							}
							joinedCourse.setPenaltyFactor((joinedCourse.getPenaltyFactor() * alreadySetStudentCount + Double.parseDouble(lineData[1]) * currentlySetStudentCount) / (alreadySetStudentCount + currentlySetStudentCount));
							break;
						} else if (joinedCourse.getCourse1().getCode().equals(lineData[0]) || joinedCourse.getCourse2().getCode().equals(lineData[0])) {
							joinedCourse.setPenaltyFactor(Double.parseDouble(lineData[1]));
							isPenaltySet[j] = true;
							break;
						}
					} else {
						if (course.getCode().equals(lineData[0])) {
							course.setPenaltyFactor(Double.parseDouble(lineData[1]));
							isPenaltySet[j] = true;
							break;
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("File not found!", e);
			return null;
		} catch (IOException e) {
			log.warn("Error while reading file!", e);
			return null;
		} catch (NumberFormatException e) {
			log.warn("Error while parsing numbers!", e);
			return null;
		} catch (ParseException e) {
			log.warn("Error while parsing!", e);
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			log.warn("File in wrong format!", e);
			return null;
		} catch (Exception e) {
			log.warn("Unexpected error!", e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		// create course map
		Map<String, Integer> courseMap = new HashMap<String, Integer>();
		for (int i = 0; i < courses.length; i++) {
			Course course = courses[i];
			if (course instanceof JoinedCourse) {
				JoinedCourse joinedCourse = (JoinedCourse) course;
				courseMap.put(joinedCourse.getCourse1().getCode(), i);
				courseMap.put(joinedCourse.getCourse2().getCode(), i);
				courseMap.put(joinedCourse.getCode(), i);
			} else {
				courseMap.put(course.getCode(), i);
			}
		}
		
		// load course groups
		Map<String, Set<Course>> courseGroupsMap = new HashMap<String, Set<Course>>();
		try {
			log.info("Loading file " + courseGroupsFile + ".");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(courseGroupsFile), "UTF8"));
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] lineData = line.split("\t");
				Integer courseIndex = courseMap.get(lineData[0]);
				if (courseIndex == null) {
					continue;
				}
				Course course = courses[courseIndex];
				String groupLabel = lineData[1];
				Set<Course> courseSet = courseGroupsMap.get(groupLabel);
				if (courseSet == null) {
					courseSet = new HashSet<Course>();
					courseGroupsMap.put(groupLabel, courseSet);
				}
				courseSet.add(course);
			}
		} catch (IOException e) {
			log.warn("Error reading file " + courseGroupsFile, e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		// load student group
		Map<String, String> studentGroupsMap = new HashMap<String, String>();
		try {
			log.info("Loading file " + courseGroupsFile + ".");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(studentGroupFile), "UTF8"));
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] lineData = line.split("\t");
				studentGroupsMap.put(lineData[0], lineData[1]);
			}
		} catch (IOException e) {
			log.warn("Error reading file " + courseGroupsFile, e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		// load course pairs
		List<int[]> coursePairs = new ArrayList<int[]>();
		try {
			log.info("Loading file " + coursePairsFile + ".");
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(coursePairsFile), "UTF8"));
			while(true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] lineData = line.split("\t");
				int courseIndex1 = courseMap.get(lineData[0]);
				int courseIndex2 = courseMap.get(lineData[1]);
				coursePairs.add(new int[] { courseIndex1, courseIndex2 });
			}
		} catch (IOException e) {
			log.warn("Error reading file " + coursePairsFile, e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		int[][] coursesPairsToPenalize = new int[coursePairs.size()][];
		coursePairs.toArray(coursesPairsToPenalize);
		
		// create student map
		Map<String, List<Course>> courseSetForStudent = new HashMap<String, List<Course>>();
		for (int i = 0; i < courses.length; i++) {
			Course course = courses[i];
			String[] jmbags = course.getJmbags();
			for (int j = 0; j < jmbags.length; j++) {
				String jmbag = jmbags[j];
				List<Course> studentCourses = courseSetForStudent.get(jmbag);
				if (studentCourses == null) {
					studentCourses = new ArrayList<Course>();
					courseSetForStudent.put(jmbag, studentCourses);
				}
				studentCourses.add(course);
			}
		}
		
		String[] jmbags = new String[studentGroupsMap.size()];
		studentGroupsMap.keySet().toArray(jmbags);
		Arrays.sort(jmbags);
		
		String[] temp = new String[courseGroupsMap.size()];
		courseGroupsMap.keySet().toArray(temp);
		
		int[][] coursesForStudents = new int[jmbags.length][];
		for (int i = 0; i < jmbags.length; i++) {
			String jmbag = jmbags[i];
			int studentIndex = Arrays.binarySearch(jmbags, jmbag);
			List<Course> courseList = courseSetForStudent.get(jmbag);
			coursesForStudents[studentIndex] = new int[courseList.size()];
			int position = 0;
			for (Course course : courseList) {
				coursesForStudents[studentIndex][position] = courseMap.get(course.getCode());
				position++;
			}
			Arrays.sort(coursesForStudents[studentIndex]);
		}
		
		
		int[][] courseGroups = new int[courseGroupsMap.size()][];
		String[] courseGroupLabel = new String[courseGroupsMap.size()];
		for (int i = 0; i < courseGroupsMap.size(); i++) {
			Set<Course> courseSet = courseGroupsMap.get(temp[i]);
			courseGroupLabel[i] = temp[i];
			courseGroups[i] = new int[courseSet.size()];
			int position = 0;
			for (Course course : courseSet) {
				courseGroups[i][position] = courseMap.get(course.getCode());
				position++;
			}
			Arrays.sort(courseGroups[i]);
		}
		
		int[] groupForStudent = new int[jmbags.length];
		for (String jmbag : studentGroupsMap.keySet()) {
			int index = Arrays.binarySearch(jmbags, jmbag);
			int groupIndex = ArrayUtils.indexOf(temp, studentGroupsMap.get(jmbag));
			groupForStudent[index] = groupIndex;
		}

		return new ConstantData(courses, terms, acceptableTerms, courseGroups, jmbags, coursesForStudents, groupForStudent, coursesPairsToPenalize, courseGroupLabel);
	}

}
