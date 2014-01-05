package jGalapagos.examTimetable.localSearch;


import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.model.Course.ComparatorByDependence;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;

/**
 * Class that implements "Depend Local Search" method. Method sorts courses by its amount of shared students with
 * other courses. Iteratively finds all possible term transfers for every course respective to sorted list and assign every
 * course to a term that improves fitness the most.  
 * @author Đorđe Grbić
 *
 */
public class DependLs implements LocalSearch{
	
	private RandomGenerator random;
	private ConstantData constantData;
	private Integer[] courseIndexes;
	
	private final double[] progress = new double[20];
	private final int[] availableTerms;
	
	public DependLs(ConstantData constantData, RandomGenerator random, Configuration configuration){
		this.random = random;
		this.constantData = constantData;
		availableTerms = new int[constantData.getCourseCount()];
		courseIndexes = new Integer[this.constantData.getCourseCount()];
		for(int i = 0; i < this.constantData.getCourseCount(); i++){
			courseIndexes[i] = i;
		}
		Arrays.sort(courseIndexes, new ComparatorByDependence(this.constantData));
	}
	
	public void startSearch(VariableTimetable inputTimetable){
		
		int availableTermNum = 0;
		int cycle = 0;
		double bestFitness = inputTimetable.getFitness()[0];
		while(true){
			
			boolean isFitnessChanged = false;
			for(int i = 0; i < constantData.getCourseCount(); i++){
				int courseIndex = courseIndexes[i];
				double bestPartialFitness = inputTimetable.getPartialFitness(courseIndex);
				availableTermNum = inputTimetable.getPossibleTransfers(courseIndex, availableTerms, false);
				int oldTermIndex = inputTimetable.getTermIndex(courseIndex);
				for(int j = 0; j < availableTermNum; j++){
					int newTermIndex = availableTerms[j];
					inputTimetable.setTermIndex(courseIndex, newTermIndex);
					double newPartialFitness = inputTimetable.getPartialFitness(courseIndex); 
					if(bestPartialFitness > newPartialFitness){
						bestPartialFitness = newPartialFitness;
						isFitnessChanged = true;
					}else{
						inputTimetable.setTermIndex(courseIndex, oldTermIndex);
					}
				}
			}
			if(isFitnessChanged){
				bestFitness = inputTimetable.getFitness()[0]; 
			}
			progress[cycle] = bestFitness;
			if(cycle > 0){
				if(progress[cycle] == progress[cycle-1]){
					break;
				}
			}
		cycle++;
		}
	}

}
