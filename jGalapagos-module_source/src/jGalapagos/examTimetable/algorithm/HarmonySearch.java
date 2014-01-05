package jGalapagos.examTimetable.algorithm;
import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.initPopulation.InitPopulation;
import jGalapagos.examTimetable.initPopulation.InitPopulationFactory;
import jGalapagos.examTimetable.localSearch.LocalSearch;
import jGalapagos.examTimetable.localSearch.LocalSearchFactory;
import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.Course.ComparatorByDependence;
import jGalapagos.examTimetable.model.UnsortedPopulation;
import jGalapagos.examTimetable.model.UnsortedPopulation.Replace;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.util.AlgorithmUtilities;
import jGalapagos.examTimetable.util.MTFRandomGenerator;
import jGalapagos.examTimetable.util.RandomGenerator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Đorđe Grbić
 * @author Mihej Komar
 * 
 * Implementation of harmony search algorithm that searches for suitable exam timetables. 
 * This class extends the <code>AbstractAlgorithm</code> class and can be instanced.
 */
public final class HarmonySearch extends AbstractAlgorithm {
	/**Writes informations on screen and text file.*/
	private final Log log = LogFactory.getLog(HarmonySearch.class);
	/**Initial population of solutions that is created in class
	 * constructor and meets hard constraints.*/
	private final InitPopulation initPopulation;
	/**Probability that component in new solution will be
	 * created using existing solutions in algorithm population.*/
	private final double harmonyProbability;
	/**
	 * Probability that randomly created component in new solution will be "adjusted"
	 * to a new value neighboring the old one
	 */
	private final double pitchProbability;
	/**
	 * Distance in days that defines neighborhood of term. Used in pitch
	 * adjustment step.
	 */
	private final int dayDistance;
	/**
	 * Course indexes sorted by number of shared students with other courses
	 */
	private final Integer[] courseIndexesByDependance;
	/**Reference to a fast local search.*/
	private final LocalSearch localSearchOfAll;
	/**Reference to a slow but better local search.*/
	private final LocalSearch localSearchOfBest;	
	private final ConstantData constantData;	
	private final RandomGenerator random;
	private final AtomicBoolean stopRequested;

	//new harmony building
	private final ReverseComparatorByHistoryAvailab historyComparator;
	private final int[] historyOfAvailab;
	private final Integer[] indexesByHistoryAvailab;
	
	/**
	 * Harmony search algorithm constructor.
	 * @param configuration	Algorithm configuration data
	 * @param constantData	Constant data about problem
	 * @param statistics	Implementation of methods that calculate statistics data
	 * @param stopRequested	Boolean variable that signals if algorithm has to stop
	 */
	public HarmonySearch(Configuration configuration, ConstantData constantData, AlgorithmStatistics statistics, AtomicBoolean stopRequested) {
		super(statistics);
		this.constantData = constantData;
		this.random = new MTFRandomGenerator();
		this.stopRequested = stopRequested;
		
		final int courseLength = constantData.getCourseCount();
		courseIndexesByDependance = new Integer[courseLength];
		
		
		for (int i = courseLength; --i >= 0;) {
			courseIndexesByDependance[i] = i;
		}
		historyOfAvailab = new int[constantData.getCourseCount()];
		indexesByHistoryAvailab = new Integer[constantData.getCourseCount()];
		for(int i = 0; i < indexesByHistoryAvailab.length; i++)	indexesByHistoryAvailab[i] = i;
		Arrays.sort(courseIndexesByDependance, new ComparatorByDependence(constantData));

		historyComparator = new ReverseComparatorByHistoryAvailab(constantData, historyOfAvailab);
		
		harmonyProbability = configuration.getDouble("harmonyProbability");
		pitchProbability = configuration.getDouble("pitchProbability");
		dayDistance = configuration.getInt("dayDistance");
		initPopulation = InitPopulationFactory.getInstance(constantData, random, configuration.subset("initPopulation"), stopRequested);
		//hillClimbingOfAll isključuje hillclimbingOfBest da uštedi posao
		//TODO: Kad dođe spori i brzi hillClimbing ovaj dio isključiti
		localSearchOfAll = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfAll"));
		localSearchOfBest = LocalSearchFactory.getInstance(constantData, random, configuration.subset("localSearchOfBest"));
	}
	
	@Override
	public void runAlgorithm() {
		
		UnsortedPopulation<ConstantTimetable> harmonyMemory = new UnsortedPopulation<ConstantTimetable>(initPopulation, constantData, ConstantTimetable.class);
		
		final int termCount = constantData.getTermCount();
		final int courseCount = constantData.getCourseCount();
		
		
		int iterationCount = 0;
		final VariableTimetable newTune = new VariableTimetable(constantData);
		startStatistic();
		while(!stopRequested.get()){
			
			this.generateStatistics(harmonyMemory, iterationCount);
			iterationCount++;
			while(!solutionQueue.isEmpty()) {
				ConstantTimetable timetable = (ConstantTimetable)solutionQueue.remove();
				if (!harmonyMemory.contains(timetable)) {
					harmonyMemory.add(timetable, Replace.WORST);
					//statistics.addedForeignTimetable(timetable);
					log.info("Foreign timetable (" + timetable + ") added to population");
				}
			}
			
			//Creating notes in newTune from components taken in HarmonyMemory
			//First newTune needs to be initialized from some allready feasable solution taken from HarmonyMemory
			newTune.deleteAllTerms();

			boolean[] randomCourses = new boolean[courseCount];
			for(int i = 0; i < courseCount; i++){//TODO Ubrzati
				randomCourses[i] = random.nextBoolean(1 - harmonyProbability);
			}

			this.BuildNewHarmony(newTune, harmonyMemory, randomCourses);
			//Pitch adjustment for notes from memory;
			for(int j = 0; j < courseCount; j++){
				if(randomCourses[j]) continue;
				if(random.nextBoolean(pitchProbability)){
					int[] buffer = new int[termCount];
					int length = AlgorithmUtilities.getCloseTerms(j, newTune, constantData, buffer, false, dayDistance);
					if(length != 0){
						newTune.setTermIndex(j, buffer[random.nextInt(length)]);
					}
				}
			}
			
			//if newTune allready exists in HarmonyMemory, don't put it in.
			if(harmonyMemory.contains(newTune)){
				continue;
			}
			
			//local search
			localSearchOfAll.startSearch(newTune);
			
			
			//add to memory
			if(newTune.isBetterThan(harmonyMemory.getWorstFitness())){
				
				//local search of best
				if((newTune.isBetterThan(harmonyMemory.getBestFitness()))){
					localSearchOfBest.startSearch(newTune);
				}
				ConstantTimetable newConstTune = newTune.getConstantTimetable();
				harmonyMemory.add(newConstTune, Replace.WORST);
//				setBestTimetable(newConstTune);
			}
			setBestTimetable(harmonyMemory.getBest());
//			generateStatistics(harmonyMemory, iterationCount);
		}
		
	}
	
	private int getAllAvailableTermsFormMemory(VariableTimetable timetable, int course, UnsortedPopulation<ConstantTimetable> memory, ConstantData constantData, final int[] buffer){
		int bufferPosition = 0;
		for(int j = 0; j < buffer.length; j++) buffer[j] = -1;
		int term = -1;
		for (int j = 0; j < memory.getPopulationCount(); j++) {
			term = memory.get(j).getTermIndex(course);
			if (timetable.isTransferPossible(course, term)) {
				if(!allreadyInBuffer(buffer, term)){
					buffer[bufferPosition] = term;
					bufferPosition++;
				}
			}
		}
		return bufferPosition;
	}
	
	private boolean allreadyInBuffer(int[] buffer, int term){
		for(int i = buffer.length; --i>=0;){
			if(buffer[i] == term) return true;
		}
		return false;
	}

	/**
	 * Method that build new solution
	 * @param timetable	Object that contains newly created solution	
	 * @param harmonyMemory	Population of solutions used to create new solution
	 * @param randomCourses	List of booleans that defines which course in new solution
	 * will be randomly generated and which will be taken from population solutions
	 * @return returns true if building new solution was successful and false otherwise
	 */
	private boolean BuildNewHarmony(final VariableTimetable timetable, UnsortedPopulation<ConstantTimetable> harmonyMemory, boolean[] randomCourses){
		
		int possibleTermNum = 0;
		int[] possibleTerms = new int[constantData.getTermCount()];
		int courseIndex = 0;
		int termIndx = 0;
		int term = -1;
		
		for(int j = 0; j < possibleTerms.length; j++) possibleTerms[j] = -1;

		for(int i = 0; i  < constantData.getCourseCount(); i++){
			
			Arrays.sort(indexesByHistoryAvailab, historyComparator);

			courseIndex = indexesByHistoryAvailab[i];
			if(randomCourses[courseIndex]){
				possibleTermNum = timetable.getPossibleTransfers(courseIndex, possibleTerms, false);
			}else{
				possibleTermNum = this.getAllAvailableTermsFormMemory(timetable, courseIndex, harmonyMemory, constantData, possibleTerms);
			}
			if(possibleTermNum == 0){
				historyOfAvailab[courseIndex]++;
				i = -1;
				timetable.deleteAllTerms();
				
				continue;
			}
			termIndx = random.nextInt(possibleTermNum);
			term = possibleTerms[termIndx];
			timetable.setTermIndex(courseIndex, term);
			
		}
		//boolean isRegular = AlgorithmUtilities.hasHardConstraints(timetable, constantData);
		return true;
	}
	
	public static final class ReverseComparatorByHistoryAvailab implements Comparator<Integer> {
		
		private final int[] history;
		private final ComparatorByDependence dependComparator;
		public ReverseComparatorByHistoryAvailab (final ConstantData constantData, final int[] historyOfAvailab){
			history = historyOfAvailab;
			dependComparator = new ComparatorByDependence(constantData);
		}
		
		@Override
		public int compare(Integer o1, Integer o2){
			if(history[o2] != history[o1]){				
				return history[o2] - history[o1];
			}else{
				
				return dependComparator.compare(o1, o2);
			}
		}
	}	
}
