package jGalapagos.examTimetable.model;


import jGalapagos.examTimetable.util.RandomGenerator;

import java.sql.Time;
import java.util.Arrays;

/**
 * Class that implements <code>Timetable</code> class and is used for 
 * timetables that can be (and often are) changed. 
 * @author Mihej Komar
 * @author Đorđe Grbić
 * @see Timetable
 */
public final class VariableTimetable extends Timetable {
	
	private static final long serialVersionUID = 1L;
	/** Terms assigned to courses. Array index is course index and number contained within each array cell 
	 * is assigned term index
	*/
	private final int[] termsInCourses;
	/**
	 * Array of course containers assigned to a term. Within array cells are course containers that can
	 * contain more than 0 courses. Array index represents term index in which courses are contained.
	 */
	private final CourseIndexContainer[] coursesInTerm;
	
	/**
	 * Timetable fitness. This class is implemented on a problem with one dimensional fitness vector.
	 */
	private double[] fitness = new double[1];
	private int hashCode;
	private boolean fitnessHasChanged = true;
	private boolean hashCodeHasChanged = true;
	private transient ConstantData constantData;
	
	private final int[] tempArray;
	
	/**
	 * Variable timetable constructor.
	 * @param constantData	Constant problem data.
	 */
	public VariableTimetable(ConstantData constantData) {
		this(constantData, true);
	}
	
	/**
	 * Private variable timetable constructor that is used to fill data with dummy values and initializes
	 * class arrays. <code>termsInCourses</code> array is filled with -1, <code>tempArray</code> is filled with rising 
	 * indexes (1,2,3,...,n).
	 * @param constantData Constant problem data
	 * @param fillTermsInCourses	If initial <code>termsInCourses</code> array will be filled with -1.
	 */
	private VariableTimetable(ConstantData constantData, boolean fillTermsInCourses) {
		this.constantData = constantData;
		this.termsInCourses = new int[constantData.getCourseCount()];
		if (fillTermsInCourses) {
			Arrays.fill(termsInCourses, -1);
		}
		coursesInTerm = new CourseIndexContainer[constantData.getTermCount()];
		for (int i = 0; i < constantData.getTermCount(); i++) {
			// TODO: napraviti da automatski provjeri koliko mora bit veličina polja
			coursesInTerm[i] = new CourseIndexContainer(25);
		}
		tempArray = new int[constantData.getTermCount()];
		for (int i = 0; i < constantData.getTermCount(); i++) {
			tempArray[i] = i;
		}
	}
	
	@Override
	public int getTermIndex(int courseIndex) {
		return termsInCourses[courseIndex];
	}
	
	@Override
	public double[] getFitness() {
		if (fitnessHasChanged) {
			updateFitness();
			fitnessHasChanged = false;
		}
		return fitness;
	}
	
	@Override
	public int getCourseCount(int termIndex) {
		return coursesInTerm[termIndex].getCourseIndexCount();
	}
	
	@Override
	public int getCourseInTermIndex(int termIndex, int index) {
		return coursesInTerm[termIndex].getCourseIndex(index);
	}
	
	@Override
	protected int getCourseCount() {
		return termsInCourses.length;
	}
	
	/**
	 * Assigns term to a course. 
	 * @param courseIndex
	 * @param termIndex
	 */
	public void setTermIndex(int courseIndex, int termIndex) {
		fitnessHasChanged = true;
		hashCodeHasChanged = true;
		int oldTermIndex = termsInCourses[courseIndex];
		if (oldTermIndex != -1) {
			coursesInTerm[oldTermIndex].remove(Integer.valueOf(courseIndex));
		}
		termsInCourses[courseIndex] = termIndex;
		coursesInTerm[termIndex].add(courseIndex);
	}
	
	/**
	 * Sets new constant problem data.
	 * @param constantData Constant problem data.
	 */
	public void setConstantData(ConstantData constantData) {
		this.constantData = constantData;
	}
	
	/**
	 * Deletes data in timetable. Timetable is empty after this method is called. 
	 */
	public void deleteAllTerms(){
		Arrays.fill(termsInCourses, -1);
		for (int i = 0; i < coursesInTerm.length; i++) {
			coursesInTerm[i].clear();
		}
	}

	/**
	 * Updates fitness is data has changed. This is called when <code>getFitness()</code> method is called.
	 * This way computer time is saved because there is no need to calculate fitness instantly if data is changed.
	 */
	private void updateFitness() {
		double fitness = 0;
		for (int termIndex1 = 0; termIndex1 < constantData.getTermCount() - 1; termIndex1++) {
			CourseIndexContainer courseIndexContainer1 = coursesInTerm[termIndex1];
			int courseIndexCount1 = courseIndexContainer1.getCourseIndexCount();
			for (int termIndex2 = termIndex1 + 1; termIndex2 < constantData.getTermCount(); termIndex2++) {
				double penaltyBetweenTerms = constantData.getPenaltyBetweenTerms(termIndex1, termIndex2);
				if (penaltyBetweenTerms == 0.0) continue;
				CourseIndexContainer courseIndexContainer2 = coursesInTerm[termIndex2];
				int courseIndexCount2 = courseIndexContainer2.getCourseIndexCount();
				double penaltyForCourses = 0;
				for (int i = 0; i < courseIndexCount1; i++) {
					int courseIndex1 = courseIndexContainer1.getCourseIndex(i);
					for (int j = 0; j < courseIndexCount2; j++) {
						int courseIndex2 = courseIndexContainer2.getCourseIndex(j);
						penaltyForCourses += constantData.getPenaltyBetweenCourses(courseIndex1, courseIndex2);
					}
				}
				fitness += penaltyForCourses * penaltyBetweenTerms;
			}
		}
		for (int coursesPairIndex = 0; coursesPairIndex < constantData.getCountOfCoursesPairs(); coursesPairIndex++) {
			int[] coursesPair = constantData.getCoursesPair(coursesPairIndex);
			int course1 = coursesPair[0];
			int course2 = coursesPair[1];
			int termIndex1 = termsInCourses[course1];
			int termIndex2 = termsInCourses[course2];
			fitness += constantData.getPenaltyBetweenDays(termIndex1, termIndex2);
		}
		this.fitness[0] = fitness;
	}
	
	/**
	 * Returns how much certain course adds to the fitness sum ie. partial fitness.
	 * @param courseIndex Course index.
	 * @return	Partial fitness.
	 */
	public double getPartialFitness(int courseIndex){
		double partialFitness = 0;
		int inputTermIndex = this.getTermIndex(courseIndex);
		for(int termIndex = 0; termIndex < constantData.getTermCount(); termIndex++){
			CourseIndexContainer courseIndexContainer = coursesInTerm[termIndex];
			int courseIndexcount = courseIndexContainer.getCourseIndexCount();
			double penaltyForCourses = 0;
			double penaltyBetweenTerms = constantData.getPenaltyBetweenTerms(inputTermIndex, termIndex);
			if (penaltyBetweenTerms == 0.0) continue;
			for(int i = 0; i < courseIndexcount; i++){
				int courseIndexIter = courseIndexContainer.getCourseIndex(i);
				penaltyForCourses += constantData.getPenaltyBetweenCourses(courseIndex, courseIndexIter);
				
			}
			partialFitness += penaltyForCourses * penaltyBetweenTerms;
		}
		
		for(int coursesPairIndex = 0; coursesPairIndex < constantData.getCountOfCoursesPairs(); coursesPairIndex++){
			int[] coursesPair = constantData.getCoursesPair(coursesPairIndex);
			int course1 = coursesPair[0];
			int course2 = coursesPair[1];
			if(course1 != courseIndex && course2 != courseIndex) continue;
			int termIndex1 = termsInCourses[course1];
			int termIndex2 = termsInCourses[course2];
			partialFitness += constantData.getPenaltyBetweenDays(termIndex1, termIndex2);
		
		}
		
		
		return partialFitness;
	}
	
	/**
	 * @return Returns if timetable violates any hard constraints.
	 */
	public boolean hasHardConstraints() {
		for (int termIndex = 0; termIndex < constantData.getTermCount(); termIndex++) {
			CourseIndexContainer courseIndexContainer = coursesInTerm[termIndex];
			int courseIndexCount = courseIndexContainer.getCourseIndexCount();
			Term term = constantData.getTerm(termIndex);
			if (courseIndexCount == 1) {
				if (constantData.getCourse(courseIndexContainer.getCourseIndex(0)).getStudentCount() > term.getHardCapacity()) {
					return true;
				}
			} else {
				int studentCount = 0;
				for (int i = 0; i < courseIndexCount - 1; i++) {
					int courseIndex1 = courseIndexContainer.getCourseIndex(i);
					studentCount += constantData.getCourse(courseIndex1).getStudentCount();
					for (int j = i + 1; j < courseIndexCount; j++) {
						int courseIndex2 = courseIndexContainer.getCourseIndex(j);
						if (courseIndex2 == -1) {
							break;
						}
						double penalty = constantData.getPenaltyBetweenCourses(courseIndex1, courseIndex2);
						if (penalty != 0) {
							return true;
						}
					}
				}
				if (studentCount > term.getSoftCapacity()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns if transfer of a certain course is possible to a certain term. 
	 * @param courseIndex Course index
	 * @param toTermIndex	Term index
	 * @return Is transfer possible?
	 */
	public boolean isTransferPossible(int courseIndex, final int toTermIndex) {
		if (!constantData.isTermAcceptable(courseIndex, toTermIndex)) {
			return false;
		}
		CourseIndexContainer courseIndexContainer = coursesInTerm[toTermIndex];
		int courseIndexCount = courseIndexContainer.getCourseIndexCount();
		Term term = constantData.getTerm(toTermIndex);
		int studentCount = constantData.getCourse(courseIndex).getJmbags().length;
		if (courseIndexCount == 0) {
			return studentCount <= term.getHardCapacity();
		}
		int studentCountInTerm = studentCount;
		for (int i = 0; i < courseIndexCount; i++) {
			int courseIndexInTerm = courseIndexContainer.getCourseIndex(i);
			double penalty = constantData.getPenaltyBetweenCourses(courseIndexInTerm, courseIndex);
			studentCountInTerm += constantData.getCourse(courseIndexInTerm).getStudentCount();
			if (penalty != 0 || studentCountInTerm > term.getSoftCapacity()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Fills buffer with all possible term transfers of a certain course. 
	 * @param courseIndex Course index
	 * @param buffer	Buffer that is filled with all possible term transfer indexes.
	 * @param includeCurrent	Will current term index be included in buffer.
	 * @return	Possible transfers count.
	 */
	public int getPossibleTransfers(int courseIndex, int[] buffer, boolean includeCurrent) {
		int position = 0;
		int currentTermIndex = termsInCourses[courseIndex];
		for (int i = 0; i < constantData.getTermCount(); i++) {
			if (includeCurrent && i == currentTermIndex) {
				buffer[position] = currentTermIndex;
			} else if (isTransferPossible(courseIndex, i)) {
				buffer[position] = i;
				position++;
			}
		}
		return position;
	}
	
	/**
	 * Gets one random possible term transfer if it exist.
	 * @param courseIndex	Course index.
	 * @param random	Random generator instance.
	 * @return	Random term index. -1 if term doesn't exist.
	 */
	public int getOneOfPossibleTransfers(int courseIndex, RandomGenerator random) {
		for (int i = tempArray.length; i > 1; i--) {
			final int positionInArray = random.nextInt(i);
			final int termIndex = tempArray[positionInArray];
			tempArray[positionInArray] = tempArray[i - 1];
			tempArray[i - 1] = termIndex;
			if (isTransferPossible(courseIndex, termIndex) && termsInCourses[courseIndex] != termIndex) {
				return termIndex;
			}
		}
		return -1;
	}
	
	/**
	 * Makes this timetable equal as input timetable.
	 * @param originalTimetable Original timetable of <code>VariableTimetable</code> type.
	 */
	public void makeMeEqualAs(VariableTimetable originalTimetable) {
		System.arraycopy(originalTimetable.termsInCourses, 0, termsInCourses, 0, termsInCourses.length);
		for (int i = 0; i < coursesInTerm.length; i++) {
			coursesInTerm[i].makeMeEqualAs(originalTimetable.coursesInTerm[i]);
		}
		this.fitness[0] = originalTimetable.getFitness()[0];
		this.hashCode = originalTimetable.hashCode();
		this.hashCodeHasChanged = false;
		this.fitnessHasChanged = false;
	}
	
	/**
	 * Makes this timetable equal as input timetable.
	 * @param originalTimetable Original timetable of <code>ConstantTimetable</code> type.
	 * @see ConstantTimetable
	 */
	public void makeMeEqualAs(ConstantTimetable originalTimetable) {
		System.arraycopy(originalTimetable.getTermsInCourses(), 0, termsInCourses, 0, termsInCourses.length);
		for (int termIndex = 0; termIndex < coursesInTerm.length; termIndex++) {
			coursesInTerm[termIndex].makeMeEqualAs(originalTimetable.getCourses(termIndex));
		}
		this.fitness[0] = originalTimetable.getFitness()[0];
		this.hashCode = originalTimetable.hashCode();
		this.hashCodeHasChanged = false;
		this.fitnessHasChanged = false;
	}
	
	/**
	 * @return Returns <code>ConstantTimetable</code> timetable that is equal to this timetable.
	 * @see ConstantTimetable
	 */
	public ConstantTimetable getConstantTimetable() {
		int[] termsInCourses = new int[this.termsInCourses.length];
		System.arraycopy(this.termsInCourses, 0, termsInCourses, 0, this.termsInCourses.length);
		int[][] coursesInTerm = new int[this.coursesInTerm.length][];
		for (int i = 0; i < this.coursesInTerm.length; i++) {
			CourseIndexContainer courseContainer = this.coursesInTerm[i];
			coursesInTerm[i] = courseContainer.toArray();
		}
		double fitness = getFitness()[0];
		int hashCode = hashCode();
		return new ConstantTimetable(termsInCourses, coursesInTerm, fitness, hashCode);
	}
	
	@Override
	public VariableTimetable clone() {
		VariableTimetable newTimetable = new VariableTimetable(constantData, false);
		System.arraycopy(termsInCourses, 0, newTimetable.termsInCourses, 0, termsInCourses.length);
		for (int i = 0; i < coursesInTerm.length; i++) {
			newTimetable.coursesInTerm[i].makeMeEqualAs(coursesInTerm[i]);
		}
		newTimetable.fitness = fitness;
		newTimetable.hashCode = hashCode;
		newTimetable.fitnessHasChanged = false;
		newTimetable.hashCodeHasChanged = false;
		return newTimetable;
	}
	
	@Override
	public int hashCode() {
		if (hashCodeHasChanged) {
			hashCode = Arrays.hashCode(termsInCourses);
			hashCodeHasChanged = false;
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof VariableTimetable)) {
			return false;
		}
		int objHashCode = obj.hashCode();
		if (objHashCode != this.hashCode) {
			return false;
		}
		return Arrays.equals(termsInCourses, ((VariableTimetable) obj).termsInCourses);
	}

	@Override
	public String toString() {
		return Arrays.toString(termsInCourses);
	}
	
}
