package jGalapagos.gui.running;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.Module;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.AbstractEvent;
import jGalapagos.core.statistics.NewBestEvent;
import jGalapagos.core.statistics.PopulationStat;
import jGalapagos.core.statistics.Statistics;
import jGalapagos.gui.ExceptionView;
import jGalapagos.gui.TabContainer;
import jGalapagos.gui.Tab;
import jGalapagos.gui.SolutionTab;
import jGalapagos.master.NodeContainer;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.MasterComunicator.RoundCompletionListener;
import jGalapagos.master.MasterComunicator.StatisticsListener;
import jGalapagos.master.MasterComunicator.WorkerExceptionListener;
import jGalapagos.worker.ArrayUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.time.DurationFormatUtils;

/**
 * 
 * @author Mihej Komar
 *
 */
public class TablePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final TabContainer tabContainer;
	private final WorkDescription workDescription;
	private final Module module;
	private final StatisticsTableModel statisticsTableModel;
	private final RoundCompletedTableModel roundCompletedTableModel;
	private final NumberFormat format = new DecimalFormat("000.00");
	
	public TablePanel(TabContainer tabContainer, final WorkDescription workDescription, Module module) {
		this.tabContainer = tabContainer;
		this.workDescription = workDescription;
		this.module = module;
		
		statisticsTableModel = new StatisticsTableModel();
		roundCompletedTableModel = new RoundCompletedTableModel();
		
		workDescription.getMasterComunicator().addStatisticsListener(statisticsTableModel);
		workDescription.getMasterComunicator().addRoundCompletionListener(roundCompletedTableModel);
		workDescription.getMasterComunicator().addWorkerExceptionListener(new WorkerExceptionListener() {
			
			@Override
			public void exceptionThrown(TopologyNode worker, Exception e) {
				new ExceptionView("Exception at " + worker, e);
			}
		});
		
		final JButton buttonDisconnect = new JButton("Disconnect");
		final JButton buttonRestart = new JButton("Restart");
		
		buttonDisconnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonDisconnect.setEnabled(false);
				buttonRestart.setEnabled(false);
				workDescription.getMasterComunicator().disconnect();
			}
		});
		

		buttonRestart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				workDescription.getMasterComunicator().restartAlgorithms();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonRestart);
		buttonPanel.add(buttonDisconnect);
		
		JTable tableByAlgorithms = new JTable(statisticsTableModel);
		tableByAlgorithms.getColumn("Best seen").setCellRenderer(new ButtonRenderer());
		tableByAlgorithms.getColumn("Best seen").setCellEditor(new ButtonEditor());
		
		JTable tableByRuns = new JTable(roundCompletedTableModel);
		tableByRuns.getColumn("Final solution").setCellRenderer(new ButtonRenderer());
		tableByRuns.getColumn("Final solution").setCellEditor(new ButtonEditor());
		
		JScrollPane leftScrollPane = new JScrollPane(tableByAlgorithms);
		JScrollPane rightScrollPane = new JScrollPane(tableByRuns);
		
		Dimension minimumSize = new Dimension(100, 50);
		leftScrollPane.setMinimumSize(minimumSize);
		rightScrollPane.setMinimumSize(minimumSize);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(leftScrollPane);
		splitPane.setRightComponent(rightScrollPane);
		splitPane.setResizeWeight(0.9);
		
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		tableByAlgorithms.getParent().setBackground(Color.WHITE);
		tableByRuns.getParent().setBackground(Color.WHITE);
	}
	
	private class ButtonRenderer extends JButton implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
			Solution solution = (Solution) value;
			setText((value == null) ? "N/A" : ArrayUtils.toString(solution.getFitness(), format));
			return this;
		}

	}
	
	private class ButtonEditor extends DefaultCellEditor {
		
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private Solution solution;
		private String label;
		private boolean isPushed;

		public ButtonEditor() {
			super(new JCheckBox());
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			solution = (Solution) value;
			label = (value == null) ? "N/A" : ArrayUtils.toString(solution.getFitness(), format);
			button.setText(label);
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed && solution != null) {
				Tab tabSolution = new SolutionTab(workDescription.getProblemDescription(), solution, module);
				tabContainer.addTab(tabSolution);
			}
			isPushed = false;
			return label;
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}
	
	private class RoundCompletedTableModel extends AbstractTableModel implements RoundCompletionListener {
		
		private static final long serialVersionUID = 1L;
		private final List<String> finishStringList = new ArrayList<String>();
		private final List<Solution> solutionList = new ArrayList<Solution>();
		
		@Override
		public int getRowCount() {
			return finishStringList.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return finishStringList.get(rowIndex);
			} else {
				return solutionList.get(rowIndex);
			}
		}

		@Override
		public void roundCompleted(Solution finalSolution, boolean noMoreRounds) {
			finishStringList.add(new Date().toString());
			solutionList.add(finalSolution);
			fireTableDataChanged();
			statisticsTableModel.clearData();
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Finished";
			case 1:
				return "Final solution";
			default:
				return null;
			}
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1) 
				return solutionList.get(rowIndex) != null;
			else 
				return false;
		}
		
	}
	
	private class StatisticsTableModel extends AbstractTableModel implements StatisticsListener {
		
		private static final long serialVersionUID = 1L;
		private final int nodeCount;
		private final String[][] data;
		private final List<Solution> bestSolutions;
	
		public StatisticsTableModel() {
			nodeCount = workDescription.getNodeContainerList().size();
			data = new String[nodeCount][6];
			bestSolutions = new ArrayList<Solution>();
			for (int i = 0; i < nodeCount; i++) {
				bestSolutions.add(null);
			}
		}
		
		public void clearData() {
			for (String[] row : data) {
				Arrays.fill(row, null);
			}
			Collections.fill(bestSolutions, null);
			fireTableDataChanged();
		}
		
		@Override
		public void receiveStatistics(Statistics statistics) {
			for (int i = 0; i < nodeCount; i++) {
				NodeContainer nodeContainer = workDescription.getNodeContainerList().get(i);
				if (nodeContainer.getTopologyNode().equals(statistics.getNode())) {
					for (AbstractEvent abstractEvent : statistics.getEventList()) {
						if (abstractEvent instanceof PopulationStat) {
							PopulationStat populationStat = (PopulationStat) abstractEvent;
							data[i][0] = ArrayUtils.toString(populationStat.getBestFitness(), format);
							data[i][1] = ArrayUtils.toString(populationStat.getWorstFitness(), format);
							data[i][2] = ArrayUtils.toString(populationStat.getAverageFitness(), format);
							data[i][3] = ArrayUtils.toString(populationStat.getStandardDeviation(), format);
							data[i][4] = DurationFormatUtils.formatDurationHMS(populationStat.getTime());
							data[i][5] = String.valueOf(populationStat.getIterationCount());
						} else if (abstractEvent instanceof NewBestEvent) {
							bestSolutions.set(i, statistics.getBestSolution());
						}
					}
				}
			}
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return nodeCount;
		}

		@Override
		public int getColumnCount() {
			return 10;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return workDescription.getNodeContainerList().get(rowIndex).getTopologyNode().getName();
			case 1:
				return workDescription.getNodeContainerList().get(rowIndex).getAlgorithmName();
			case 2:
				return workDescription.getNodeContainerList().get(rowIndex).getWorkerInformation();
			case 3:
				return bestSolutions.get(rowIndex);
			case 4:
				return data[rowIndex][0];
			case 5:
				return data[rowIndex][1];
			case 6:
				return data[rowIndex][2];
			case 7:
				return data[rowIndex][3];
			case 8:
				return data[rowIndex][4];
			case 9:
				return data[rowIndex][5];
			default:
				return null;
			}
		}

		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 3) 
				return bestSolutions.get(rowIndex) != null;
			else 
				return false;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Node name";
			case 1:
				return "Algorithm";
			case 2:
				return "Computer";
			case 3:
				return "Best seen";
			case 4:
				return "Best";
			case 5:
				return "Worst";
			case 6:
				return "Average";
			case 7:
				return "Deviation";
			case 8:
				return "Time";
			case 9:
				return "Iteration";
			default:
				return null;
			}
		}
		
	}

}
