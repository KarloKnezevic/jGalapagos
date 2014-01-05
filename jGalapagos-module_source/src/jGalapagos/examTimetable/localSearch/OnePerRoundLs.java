package jGalapagos.examTimetable.localSearch;


import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.model.Course.ComparatorByDependence;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
/**
 * Class that implements "One Per Round Local Search" method.
 * Iteratively finds all possible term transfers for each course in timetable and transfers course
 * to a term that makes the best improvement on the timetable fitness.  
 * @author Đorđe Grbić
 *
 */
public class OnePerRoundLs implements LocalSearch{
	
	private RandomGenerator random;
	private ConstantData constantData;
	private Integer[] courseIndexes;
	
	public OnePerRoundLs(ConstantData constantData, RandomGenerator random, Configuration configuration){
		this.random = random;
		this.constantData = constantData;
		courseIndexes = new Integer[this.constantData.getCourseCount()];
		for(int i = 0; i < this.constantData.getCourseCount(); i++){
			courseIndexes[i] = i;
		}
		Arrays.sort(courseIndexes, new ComparatorByDependence(this.constantData));
	}
	
	@Override
	public void startSearch(VariableTimetable inputTimetable){
		int[] possibleTerms = new int[constantData.getTermCount()];
		
		while(true){
			double bestMoveValue = 10000;
			int bestCourseToMove = -1;
			int bestTermToMove = -1;
			
			for(int courseIndex = 0; courseIndex < constantData.getCourseCount(); courseIndex++){
				
				int numOfPossibleTransitions = inputTimetable.getPossibleTransfers(courseIndex, possibleTerms, false);
				double bestPartialFitness = inputTimetable.getPartialFitness(courseIndex);
				double originalPartialFitness = bestPartialFitness;
				int oldTerm = inputTimetable.getTermIndex(courseIndex);
				int bestTerm = oldTerm;
				for(int possTermIndex = 0; possTermIndex < numOfPossibleTransitions; possTermIndex++){
					int newTerm = possibleTerms[possTermIndex];
					inputTimetable.setTermIndex(courseIndex, newTerm);
					double newPartialFitness = inputTimetable.getPartialFitness(courseIndex);
					if(newPartialFitness < bestPartialFitness){
						bestPartialFitness = newPartialFitness;
						bestTerm = newTerm;
					}
					inputTimetable.setTermIndex(courseIndex, oldTerm);
					
				}
				double moveValue = bestPartialFitness - originalPartialFitness;
				if(moveValue < bestMoveValue){
					bestCourseToMove = courseIndex;
					bestTermToMove = bestTerm;
					bestMoveValue = moveValue;
				}
			}
			if(bestMoveValue == 0){
				break;
			}
			inputTimetable.setTermIndex(bestCourseToMove, bestTermToMove);
		}
	}

}
