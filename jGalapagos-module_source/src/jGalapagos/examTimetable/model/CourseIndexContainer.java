package jGalapagos.examTimetable.model;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that contains a set of courses
 */
public class CourseIndexContainer {
	
	private static final Log log = LogFactory.getLog(CourseIndexContainer.class);
	
	private final int[] courseIndexes;
	private final int maxCourseCount;
	private int courseCount;
	
	public CourseIndexContainer(int maxCourseCount) {
		this.maxCourseCount = maxCourseCount;
		courseIndexes = new int[maxCourseCount];
		courseCount = 0;
	}
	
	/**
	 * Adds course index.
	 * 
	 * @param courseIndex
	 *            Course index that is added.
	 */
	public void add(int courseIndex) {
		int searchResult = Arrays.binarySearch(courseIndexes, 0, courseCount, courseIndex);
		if (searchResult < 0) {
			searchResult = - searchResult - 1;
		}
		try {
			System.arraycopy(courseIndexes, searchResult, courseIndexes, searchResult + 1, courseCount - searchResult);
			courseIndexes[searchResult] = courseIndex;
			courseCount++;
		} catch (IndexOutOfBoundsException e) {
			log.error("Unexpected error while adding course index in course index container.", e);
		}
	}
	
	/**
	 * Deletes course index.
	 * 
	 * @param courseIndex
	 *            Index of deleted course.
	 * @return Returns <code>true</code> if <code>courseIndex</code>
	 *         is found, <code>false</code> otherwise.
	 */
	public boolean remove(int courseIndex) {
		int searchResult = Arrays.binarySearch(courseIndexes, 0, courseCount, courseIndex);
		if (searchResult < 0) {
			return false;
		}
		System.arraycopy(courseIndexes, searchResult + 1, courseIndexes, searchResult, courseCount - searchResult);
		courseCount--;
		return true;
	}
	
	public void makeMeEqualAs(CourseIndexContainer courseIndexContainer) {
		if (courseIndexContainer.maxCourseCount != maxCourseCount) {
			throw new IllegalStateException("Max course count isn't equal.");
		}
		System.arraycopy(courseIndexContainer.courseIndexes, 0, courseIndexes, 0, maxCourseCount);
		courseCount = courseIndexContainer.courseCount;
	}
	
	/**
	 * Sets input set of course as local set of courses.
	 * @param courses	Array of input courses.
	 */
	public void makeMeEqualAs(int[] courses) {
		System.arraycopy(courses, 0, courseIndexes, 0, courses.length);
		courseCount = courses.length;
	}
	
	/**
	 * @return	Returns course count.
	 */
	public int getCourseIndexCount() {
		return courseCount;
	}
	
	/**
	 * Gets course index contained within on local index
	 * @param index	Local index
	 * @return	Course index.
	 */
	public int getCourseIndex(int index) {
		return courseIndexes[index];
	}
	
	/**
	 * Clears all courses.
	 */
	public void clear() {
		courseCount = 0;
	}
	
	/**
	 * @return returns array of course indexes.
	 */
	public int[] toArray() {
		int[] array = new int[courseCount];
		System.arraycopy(courseIndexes, 0, array, 0, courseCount);
		return array;
	}
	
}