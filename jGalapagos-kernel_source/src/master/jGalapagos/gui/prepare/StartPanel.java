package jGalapagos.gui.prepare;


import jGalapagos.WorkDescriptionForWorker;
import jGalapagos.communication.TopologyNode;
import jGalapagos.gui.TabContainer;
import jGalapagos.gui.running.RunningTab;
import jGalapagos.master.Connection;
import jGalapagos.master.ModuleContainer;
import jGalapagos.master.NodeContainer;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkerInformation;
import jGalapagos.master.WorkerStatus;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import layout.SpringUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(StartPanel.class);

	public StartPanel(final WorkDescription workDescription, final TabContainer tabContainer, final ModuleContainer moduleContainer) {
		final JTextField maxInactivityTextfield = new JTextField("10");
		final JTextField maxDurationTextfield = new JTextField("30");
		final JTextField maxRoundsTextfield = new JTextField("30");
		
		JPanel configurationPanel = new JPanel(new SpringLayout());
		configurationPanel.setBorder(BorderFactory.createTitledBorder("Running options"));
		configurationPanel.add(new JLabel("Max. inactivity (in minutes)"));
		configurationPanel.add(maxInactivityTextfield);
		configurationPanel.add(new JLabel("Max. duration (in minutes)"));
		configurationPanel.add(maxDurationTextfield);
		configurationPanel.add(new JLabel("Max. rounds"));
		configurationPanel.add(maxRoundsTextfield);
		SpringUtilities.makeCompactGrid(configurationPanel, 3, 2, 5, 5, 5, 5);
		
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int maxInactivity;
				try {
					maxInactivity = Integer.parseInt(maxInactivityTextfield.getText());
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(null, "Max. inactivity is not a number", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				workDescription.setMaxInactivityMinutes(maxInactivity);
				
				int maxDuration;
				try {
					maxDuration = Integer.parseInt(maxDurationTextfield.getText());
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(null, "Max. duration is not a number", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				workDescription.setMaxDuration(maxDuration);
				
				int maxRounds;
				try {
					maxRounds = Integer.parseInt(maxRoundsTextfield.getText());
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(null, "Max. rounds is not a number", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (maxRounds <= 0) {
					JOptionPane.showMessageDialog(null, "Max. rounds must be greater than 0", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				workDescription.setMaxDuration(maxRounds);
				
				final Serializable problemDescription = workDescription.getProblemDescription();
				if (problemDescription == null) {
					JOptionPane.showMessageDialog(null, "Problem not loaded", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
					if (nodeContainer.getWorkerInformation() == null) {
						JOptionPane.showMessageDialog(null, "Workers aren't assigned", "Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}
					if (nodeContainer.getWorkerInformation().getWorkerStatus() != WorkerStatus.READY_NEW_IMPL) {
						JOptionPane.showMessageDialog(null, "Workers aren't ready", "Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
				
				// prepare all work descriotions by workers
				Map<WorkerInformation, WorkDescriptionForWorker> workDescriptionsByWorkers = new HashMap<WorkerInformation, WorkDescriptionForWorker>();
				for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
					WorkDescriptionForWorker workDescriptionForWorker = new WorkDescriptionForWorker(problemDescription, moduleContainer.getConfig().getString("moduleClass"));
					for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
						if (nodeContainer.getWorkerInformation().equals(workerInformation)) {
							TopologyNode topologyNode = nodeContainer.getTopologyNode();
							String algorithmName = nodeContainer.getAlgorithmName(); 
							PropertiesConfiguration configuration = workDescription.getAlgorithmConfiguration().get(algorithmName);
							ByteArrayOutputStream configurationByteStream = new ByteArrayOutputStream();
							try {
								configuration.save(configurationByteStream);
							} catch (ConfigurationException e1) {
								log.error(e1, e1);
							}
							byte[] configurationData = configurationByteStream.toByteArray();
							workDescriptionForWorker.getAlgorithmConfigurationsByNode().put(topologyNode, configurationData);
							Map<TopologyNode, Integer> connectionMap = new HashMap<TopologyNode, Integer>();
							for (Connection connection : nodeContainer.getConnectionList()) {
								TopologyNode toNode = connection.getNodeContainer().getTopologyNode();
								Integer interval = connection.getInterval();
								connectionMap.put(toNode, interval);
							}
							workDescriptionForWorker.getConnectionsMap().put(topologyNode, connectionMap);
						}
					}
					workDescriptionsByWorkers.put(workerInformation, workDescriptionForWorker);
				}
				
				workDescription.getMasterComunicator().stopWaitingForConnections();
				workDescription.getMasterComunicator().startImpl(workDescriptionsByWorkers);
				tabContainer.addTab(new RunningTab(tabContainer, workDescription, moduleContainer.getModule()));
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(startButton);
		
		setLayout(new BorderLayout());
		add(configurationPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
	}

}
