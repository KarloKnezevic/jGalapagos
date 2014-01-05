package jGalapagos.gui.experiments;

import jGalapagos.WorkDescriptionForWorker;
import jGalapagos.gui.ContentComponent;
import jGalapagos.gui.Tab;
import jGalapagos.master.MasterComunicator;
import jGalapagos.master.ModuleContainer;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkerInformation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ExperimentsTab implements Tab {
	
	private final List<Experiment> experimentList = new ArrayList<Experiment>();
	private final JFileChooser fileChooser = new JFileChooser("data/experiments/");
	private final MyTableModel myTableModel = new MyTableModel();
	private final JTable table = new JTable(myTableModel);
	private final ModuleContainer moduleContainer;
	
	public ExperimentsTab(ModuleContainer moduleContainer) {
		this.moduleContainer = moduleContainer;
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
	}

	@Override
	public JComponent getContent() throws Exception {
		JButton buttonAdd = new JButton("Add...");
		JButton buttonRemove = new JButton("Remove");
		JButton buttonMoveUp = new JButton("Move up");
		JButton buttonMoveDown = new JButton("Move down");
		JButton buttonStartStop = new JButton("Start");
		
		buttonAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
					boolean changed = false;
					for (File file : fileChooser.getSelectedFiles()) {
						try {
							Experiment experiment = new Experiment(moduleContainer, file);
							if (!experimentList.contains(experiment)) {
								experimentList.add(experiment);
								changed = true;
							}
						} catch (Exception e2) {
							// ignore
						}
					}
					if (changed) {
						myTableModel.fireTableDataChanged();
					}
				}
				
			}
		});
		
		buttonRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length == 0) return;
				Arrays.sort(selectedRows);
				for (int i = selectedRows.length; --i >= 0;) {
					experimentList.remove(selectedRows[i]);
				}
				myTableModel.fireTableDataChanged();
			}
		});
		
		buttonMoveUp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length == 0) return;
				Arrays.sort(selectedRows);
				for (int i = 0; i < selectedRows.length; i++) {
					int index = selectedRows[i];
					if (index == 0) return;
					Experiment experiment = experimentList.get(index - 1);
					experimentList.set(index - 1, experimentList.get(index));
					experimentList.set(index, experiment);
					selectedRows[i]--;
				}
				myTableModel.fireTableDataChanged();
				table.getSelectionModel().clearSelection();
				for (int i = 0; i < selectedRows.length; i++) {
					table.getSelectionModel().addSelectionInterval(selectedRows[i], selectedRows[i]);
				}
			}
		});
		
		buttonMoveDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length == 0) return;
				Arrays.sort(selectedRows);
				for (int i = selectedRows.length; --i >= 0;) {
					int index = selectedRows[i];
					if (index >= table.getRowCount() - 1) return;
					Experiment experiment = experimentList.get(index + 1);
					experimentList.set(index + 1, experimentList.get(index));
					experimentList.set(index, experiment);
					selectedRows[i]++;
				}
				myTableModel.fireTableDataChanged();
				table.getSelectionModel().clearSelection();
				for (int i = 0; i < selectedRows.length; i++) {
					table.getSelectionModel().addSelectionInterval(selectedRows[i], selectedRows[i]);
				}
			}
		});
		
		buttonStartStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ExperimentStarter experimentStarter = new ExperimentStarter();
				experimentStarter.start();
			}
		});
		
		JPanel rightButtonsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
		rightButtonsPanel.add(buttonAdd);
		rightButtonsPanel.add(buttonRemove);
		rightButtonsPanel.add(buttonMoveUp);
		rightButtonsPanel.add(buttonMoveDown);
		
		JPanel rightPanel = new JPanel();
		rightPanel.add(rightButtonsPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonStartStop);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.EAST);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		table.getParent().setBackground(Color.WHITE);
		
		return new ContentComponent(mainPanel, "Experiments", "Define list of experiments to execute", null);
	}

	@Override
	public String getName() {
		return "Experiments";
	}

	@Override
	public boolean isClosable() {
		return false;
	}

	@Override
	public void closeAction() {
		// TODO Auto-generated method stub
	}
	
	private class MyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return experimentList.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Experiment experiment = experimentList.get(rowIndex);
			if (columnIndex == 0) {
				return experiment;
			} else {
				return "TODO";
			}
		}
		
		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Path";
			} else {
				return "Status";
			}
		}
		
	}
	
	private class ExperimentStarter extends Thread {
		
		public ExperimentStarter() {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void run() {
			for (Experiment experiment : experimentList) {
				System.out.println("Starting experiment " + experiment);
				try {
					WorkDescription workDescription = experiment.createWorkDescription();
					Map<WorkerInformation, WorkDescriptionForWorker> workDescriptionsByWorkers = experiment.createWorkDescriptionsByWorkers(workDescription);
					workDescription.getMasterComunicator().startImpl(workDescriptionsByWorkers);
					
					// wait for exit
					final AtomicBoolean experimentOver = new AtomicBoolean(); 
					workDescription.getMasterComunicator().addMaxRoundCompletedListener(new MasterComunicator.MaxRoundCompletedListener() {
						
						@Override
						public void maxRoundCompleted() {
							experimentOver.set(true);
							synchronized (experimentOver) {
								experimentOver.notifyAll();
							}
						}
					});
					while (experimentOver.get() == false) {
						synchronized (experimentOver) {
							experimentOver.wait();
						}
					}
					
					workDescription.getMasterComunicator().disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			

		}
		
	}

}
