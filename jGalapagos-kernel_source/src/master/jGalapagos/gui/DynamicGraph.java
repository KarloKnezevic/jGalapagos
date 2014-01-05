package jGalapagos.gui;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.statistics.AbstractEvent;
import jGalapagos.core.statistics.NewBestEvent;
import jGalapagos.core.statistics.PopulationStat;
import jGalapagos.core.statistics.StatDataType;
import jGalapagos.master.StatisticsCollector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Time;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class DynamicGraph extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private StatisticsCollector collector;
	private String name;
	private TimeSeriesCollection dataset;

	public DynamicGraph(StatisticsCollector collector, String graphName) {
		super(new BorderLayout());
		this.collector = collector;
		this.name = graphName;
		this.dataset = new TimeSeriesCollection();
		DateAxis domain = new DateAxis("Time");
		NumberAxis range = new NumberAxis("Fitness");
		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN,14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN,14));
		
		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);

		XYPlot plot = new XYPlot(dataset, domain, range, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		domain.setAutoRange(true);
		domain.setLowerMargin(0.0);
		domain.setUpperMargin(0.0);
		domain.setTickLabelsVisible(true);
		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		JFreeChart chart = new JFreeChart(this.name, new Font("SansSerif", Font.BOLD, 24), plot, true);
		chart.setBackgroundPaint(Color.white);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		add(chartPanel);
	}
	
	public void plot(TopologyNode node, StatDataType type, int plotDimension){
		TimeSeries timeseries = new TimeSeries(node.getName() + "." +type.toString());
		ArrayList<AbstractEvent> listOfEvents = collector.getAllEvents();
//		System.out.println("plot - listOfEvents.size = " + listOfEvents.size());
		for(int i = 0; i < listOfEvents.size(); i++){
			AbstractEvent event = listOfEvents.get(i);
			Millisecond time = new Millisecond(new Time(event.getTime()));
			if(event instanceof PopulationStat){
				PopulationStat popStat = (PopulationStat) event;
				if(event.getNode().getName().contentEquals(node.getName())){
					switch(type){
					case AVERAGE:
						timeseries.add(time, popStat.getAverageFitness()[plotDimension]);
						break;
					case DEVIATION:
						timeseries.add(time, popStat.getStandardDeviation()[plotDimension]);
						break;
					case ITERATION:
						timeseries.add(time, popStat.getIterationCount());
						break;
					case WORST:
						timeseries.add(time, popStat.getWorstFitness()[plotDimension]);
						break;
					default:
//						System.err.println("DynamicGraph.plot() - invalid input argument \"type\"");
						break;
					}
				}
			}else if(event instanceof NewBestEvent){
//				System.out.println("event IS NewBestEvent");
				NewBestEvent best = (NewBestEvent) event;
				if(event.getNode().getName().contentEquals(node.getName()) && type == StatDataType.BEST){
//					System.out.println("zapisivanje u TimeSeries");
					timeseries.add(time, best.getSolutionFitness()[plotDimension]);
				}
				
			}
		}
		dataset.addSeries(timeseries);
		collector.subscribe(timeseries, node, type, plotDimension);
	}

}
