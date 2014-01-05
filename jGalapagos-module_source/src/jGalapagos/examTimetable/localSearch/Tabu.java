package jGalapagos.examTimetable.localSearch;


import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.configuration.Configuration;

/**
 * Class that implements Tabu search method for local search.
 * @author Đorđe Grbić
 *
 */
public class Tabu implements LocalSearch{
	
	private final ConstantData constantData;
	private final RandomGenerator random;
	
	public Tabu(ConstantData constantData, RandomGenerator random, Configuration configuration){
		this.random = random;
		this.constantData = constantData;
	}
	public void startSearch(VariableTimetable inputTimetable){
		
		VariableTimetable tempTimetable = new VariableTimetable(constantData);
		double currentFitness = inputTimetable.getFitness()[0];
		int[] buffer = new int[constantData.getTermCount()];
		int possibleTermNum = 0;
		tempTimetable.makeMeEqualAs(inputTimetable);
		int[] tabuCounter = new int[constantData.getCourseCount()];
		double[] frequencies = new double[constantData.getCourseCount()];
		double initialFitness = currentFitness;
		double bestFitness = currentFitness;
		
		int basicTenure = 8;

		for(int k = 0; k < 5; k++){ // TODO ovo promijeniti
			
			int[] bestTermsFound = new int[constantData.getCourseCount()];
			double[] moveValues = new double[constantData.getCourseCount()];
			Integer[] courseIndexes = new Integer[constantData.getCourseCount()];
			for(int i = 0; i < courseIndexes.length; i++){
				courseIndexes[i] = i;
			}
			
			
			// iteracija po svim ispitima (i)
			for(int i = 0; i < constantData.getCourseCount(); i++	){
				double minFitnessPenaltyI = 1000000;
				int bestTermFoundI = -1;
				
				possibleTermNum = tempTimetable.getPossibleTransfers(i, buffer, false);

				int tempTerm = tempTimetable.getTermIndex(i);//orig. termin
				// iteracija po dozvoljenim terminima za pojedini ispit (j)
				for(int j = 0; j < possibleTermNum; j++){
					tempTimetable.setTermIndex(i, buffer[j]);
					if(tempTimetable.getFitness()[0] < minFitnessPenaltyI){
						bestTermFoundI = buffer[j];
						minFitnessPenaltyI = tempTimetable.getFitness()[0];
					}
				}
				bestTermsFound[i] = bestTermFoundI;
				moveValues[i] = minFitnessPenaltyI - currentFitness;
				if(possibleTermNum != 0){
					tempTimetable.setTermIndex(i, tempTerm);	
				}
				
			}
			Arrays.sort(courseIndexes, new MoveValueComparator(constantData, moveValues, frequencies));
			
			
			if(moveValues[courseIndexes[0]] < 0){
				tempTimetable.setTermIndex(courseIndexes[0], bestTermsFound[courseIndexes[0]]);
				currentFitness = tempTimetable.getFitness()[0];
			}else{
				for(int i = 0; i < courseIndexes.length; i++){
					if(random.nextBoolean()&& (tabuCounter[i]==0)){
						tempTimetable.setTermIndex(courseIndexes[i], bestTermsFound[courseIndexes[i]]);
						currentFitness = tempTimetable.getFitness()[0];
						tabuCounter[courseIndexes[i]] = (int)Math.min(2*basicTenure, basicTenure+((30*(bestFitness+currentFitness))/(initialFitness-bestFitness+1))) + random.nextInt(4); // <==  ovdje tabu ocijena
						frequencies[courseIndexes[i]] += 0.001;
						int dayOfMovedCourse = constantData.getTerm(tempTimetable.getTermIndex(courseIndexes[i])).getDayIndex();//vadi dan termina kolegija :)
						for(int j = 0; j < tabuCounter.length; j++){
							//tabuCounter[j]
							int dayOfUpdateCourse = constantData.getTerm(tempTimetable.getTermIndex(j)).getDayIndex();
							if((dayOfUpdateCourse == dayOfMovedCourse)&&(courseIndexes[i] != j)){
								if(tabuCounter[j] > 0){
									tabuCounter[j]--;
								}
							}
						}
						break;
					}
				}
			}
			

			if(currentFitness < bestFitness){
				bestFitness = currentFitness;
				inputTimetable.makeMeEqualAs(tempTimetable);
			}
		}
		
		
	}
	
	
	public static final class MoveValueComparator implements Comparator<Integer> {
		
		private double[] moveVals;
		private double[] frequencyVals;
		public MoveValueComparator (final ConstantData constantData, final double[] moveValues, final double[] frequencyValues){
			moveVals = moveValues;
			frequencyVals = frequencyValues;
		}
		
		@Override
		public int compare(Integer o1, Integer o2){
			if(o1 != o2){
				return (int)(moveVals[o1] - moveVals[o2]);
			}else{
				return (int)(frequencyVals[o1] - frequencyVals[o2]);
			}
			
		}
	}


}
