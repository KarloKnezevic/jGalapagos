package jGalapagos.master;


import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.AbstractEvent;
import jGalapagos.core.statistics.NewBestEvent;
import jGalapagos.core.statistics.PopulationStat;
import jGalapagos.core.statistics.StatDataType;
import jGalapagos.core.statistics.Statistics;
import jGalapagos.master.MasterComunicator.StatisticsListener;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;


public class StatisticsCollector implements StatisticsListener {

	private List<AbstractEvent> statistics;
/*
 * Redni broj je identifikator pretplatnika događaja.
 * Pretplatnik rednog broja "i" sluša TopologyNode rednog broja "i"
 */
	private List<Subscriber> subscribers;
	
	private Map<TopologyNode, Solution> bestSolutionsByTopologyNode;
	
	private String logEncoding;
	private long fileWritingInterval;
	private Writer eventLogFile;
	private Timer statWriteTimer;
	
	private final Log log = LogFactory.getLog(StatisticsCollector.class);

	
	public StatisticsCollector(String logFilename){
		subscribers = new ArrayList<Subscriber>();
		this.statistics = Collections.synchronizedList(new ArrayList<AbstractEvent>());
		bestSolutionsByTopologyNode = new HashMap<TopologyNode, Solution>();

		logEncoding = "UTF-8";
		fileWritingInterval = 10000;
		try{
			this.eventLogFile = new OutputStreamWriter(new FileOutputStream(logFilename), logEncoding);
		}catch (Exception e) {
			log.warn("cannot open log file @ StatisticsCollector", e);
			e.printStackTrace();
		}
		
		statWriteTimer = new Timer();
		statWriteTimer.schedule(new TimerTask() {		
			@Override
			public void run() {
				log();	
			}
		},0, fileWritingInterval);

	}
	
	@Override
	public void receiveStatistics(Statistics statistics) {
		update(statistics.getEventList());
		updateBest(statistics.getNode(), statistics.getBestSolution());
	}
	
	public void log(){
		ArrayList<AbstractEvent> copyOfList = new ArrayList<AbstractEvent>(statistics);
		int eventNum = copyOfList.size();
		try{
			for(int i = 0; i < eventNum; i++){
				AbstractEvent event = copyOfList.get(i);
				if(event instanceof PopulationStat){
					PopulationStat popStat = (PopulationStat) event;
					String outputData = "populationStat@"+popStat.getNode().getName()
					+ ". BEST = " + Arrays.toString(popStat.getBestFitness())
					+ "; AVERAGE = " + Arrays.toString(popStat.getAverageFitness())
					+ "; WORST = " + Arrays.toString(popStat.getWorstFitness())
					+ "; DEVIATION = " + Arrays.toString(popStat.getStandardDeviation())
					+ "; ITERATION = " + popStat.getIterationCount()
					+ "; TIME = " + popStat.getTime()
					+ System.getProperty("line.separator");
					eventLogFile.write(outputData);
				}else if(event instanceof NewBestEvent){
					NewBestEvent newBest = (NewBestEvent) event;
					String outputData = "newBest@"+newBest.getNode().getName() + 
					". FITNESS = " + Arrays.toString(newBest.getSolutionFitness())
					+ "; TIME = " + newBest.getTime()
					+ System.getProperty("line.separator");
					eventLogFile.write(outputData);
				}
			}
			eventLogFile.flush();
			statistics.removeAll(copyOfList);
		}catch (Exception e) {
			log.warn("Cannot write data into log file @ StatisticsCollector", e);
		}
	}
	
	public void close() {
		try{
			
			eventLogFile.close();
		}catch (Exception e) {
			log.warn("Cannot close log file @ StatisticsCollector", e);
		}
		statWriteTimer.cancel();
	}
	
	public void update(List<AbstractEvent> eventList){
		statistics.addAll(eventList);
		for(int i = 0; i < eventList.size(); i++){
			for(int j = 0; j < subscribers.size();j++){
				String nodeNameOfIntrest = subscribers.get(j).getNode().getName();
				String nodeName = eventList.get(i).getNode().getName();
				
				int dimensionInterest = subscribers.get(j).getFitnessInterestDimension();
				Subscriber subscriber = subscribers.get(j);
				if(nodeNameOfIntrest.contentEquals(nodeName)){
					Millisecond time = new Millisecond(new Time(eventList.get(i).getTime()));
					if(eventList.get(i) instanceof PopulationStat){
						PopulationStat statEvent = (PopulationStat)eventList.get(i);
						if(subscribers.get(j).getDataType() == StatDataType.AVERAGE){
							subscriber.getTimeSeries().add(time,statEvent.getAverageFitness()[dimensionInterest]);
						}else if(subscribers.get(j).getDataType() == StatDataType.WORST){
							subscriber.getTimeSeries().add(time,statEvent.getWorstFitness()[dimensionInterest]);
						}else if(subscribers.get(j).getDataType() == StatDataType.DEVIATION){
							subscriber.getTimeSeries().add(time,statEvent.getStandardDeviation()[dimensionInterest]);
						}else if(subscribers.get(j).getDataType() == StatDataType.ITERATION){
							subscribers.get(j).getTimeSeries().add(time, statEvent.getIterationCount());
						}
					}else if (eventList.get(i) instanceof NewBestEvent){
						NewBestEvent statEvent = (NewBestEvent) eventList.get(i);
						if(subscribers.get(j).getDataType() == StatDataType.BEST){
							subscriber.getTimeSeries().add(time,statEvent.getSolutionFitness()[dimensionInterest]);
						}
					}
				}
			}
		}
		
	}
	
	public void updateBest(TopologyNode node, Solution solution){
		bestSolutionsByTopologyNode.put(node, solution);
	}
	
	public Solution getBestSolution(TopologyNode node){
		Solution solution;
		solution = this.bestSolutionsByTopologyNode.get(node);
		return solution;
		
	}
	
	public int getFitnessDimensionality(){
		//TODO: kako dobiti dimenzionalnost kazne bez inicijalnog rješenja?
		return 1;
	}
	
	public void subscribe(TimeSeries timeSeries, TopologyNode nodeOfInterest, StatDataType dataOfIntrest, int dimensionInterest){
		Subscriber subscriber = new Subscriber(timeSeries, nodeOfInterest, dataOfIntrest, dimensionInterest);
		this.subscribers.add(subscriber);
	}
	
	public void unsubscribe(TopologyNode unsubscriber, StatDataType intrest){
		String unNodeName = unsubscriber.getName();
		for(int i = 0; i < this.subscribers.size(); i++){
			String nodeName = subscribers.get(i).getNode().getName();
			if(unNodeName.contentEquals(nodeName) &&(subscribers.get(i).getDataType() == intrest)){
				subscribers.remove(i);
				break;
			}
		}
	}
	
	public ArrayList<AbstractEvent> getAllEvents(){
		ArrayList<AbstractEvent> copyOfList = new ArrayList<AbstractEvent>();
		copyOfList.addAll(this.statistics);
		return copyOfList;
	}

}
