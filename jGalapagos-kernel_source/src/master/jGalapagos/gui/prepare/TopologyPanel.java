package jGalapagos.gui.prepare;

import jGalapagos.gui.SelectedNodeContainerListener;
import jGalapagos.gui.TopologyView;
import jGalapagos.master.Connection;
import jGalapagos.master.ModuleContainer;
import jGalapagos.master.NodeContainer;
import jGalapagos.master.TopologyType;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkerInformation;
import jGalapagos.master.WorkerStatus;
import jGalapagos.master.WorkDescription.AlgorithmListener;
import jGalapagos.master.WorkDescription.NodeContainerListener;
import jGalapagos.master.WorkDescription.WorkerInformationListener;
import jGalapagos.util.NetworkUtils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import layout.SpringUtilities;

/**
 * 
 * @author Mihej Komar
 *
 */
public class TopologyPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String PANEL_ADD_NODE = "add";
	private int nodeNumber = 1;
	private final WorkDescription workDescription;
	private final TopologyView topologyView;
	private static final JPanel editNodePanel = new JPanel(new CardLayout());;
	private final AtomicReference<NodeContainer> sourceOfNewConnection = new AtomicReference<NodeContainer>();
	private static final List<NodeContainer> newAutogeneratedTopologyNodes = new CopyOnWriteArrayList<NodeContainer>();
	
	private static final List<NewNodeContainersListener> newNodeContainersListenerList = new ArrayList<NewNodeContainersListener>();

	public TopologyPanel(final WorkDescription workDescription, final ModuleContainer moduleContainer) {
		this.workDescription = workDescription;
		
		editNodePanel.add(createAddNodePanel(), PANEL_ADD_NODE);
		
		SelectedNodeContainerListener selectedNodeContainerListener = new SelectedNodeContainerListener() {
			@Override
			public void selectedNodeContainer(NodeContainer nodeContainer) {
				NodeContainer source = sourceOfNewConnection.get();
				if (source != null && nodeContainer != source) {
					if (nodeContainer != null) {
						Connection connection = new Connection(nodeContainer);
						connection.setInterval(10000);
						source.getConnectionList().add(connection);
						workDescription.fireNodeContainerChanged();
					}
					sourceOfNewConnection.set(null);
				}
				CardLayout cardLayout = (CardLayout) (editNodePanel.getLayout());
				if (nodeContainer == null) {
					cardLayout.show(editNodePanel, PANEL_ADD_NODE);
				} else {
					cardLayout.show(editNodePanel, nodeContainer.toString());
				}
			}
		};
		
		topologyView = new TopologyView(selectedNodeContainerListener, workDescription);
		topologyView.setPreferredSize(new Dimension(10000, 10000));
		
		JScrollPane topologyViewScrollPane = new JScrollPane(topologyView);
		topologyViewScrollPane.setMinimumSize(new Dimension(10, 10));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(topologyViewScrollPane);
		splitPane.setRightComponent(editNodePanel);
		splitPane.setResizeWeight(0.7);
		
		JButton buttonCreateDefaultTopology = new JButton("Default topology");
		buttonCreateDefaultTopology.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int nodeCount = Integer.parseInt(moduleContainer.getDefaultTopologyConfig().getString("nodeCount"));
				for (WorkerInformation workerInfo : workDescription.getWorkerInformationList()) {
					if (workerInfo.getWorkerStatus() == WorkerStatus.READY_NEW_IMPL) {
						nodeCount += workerInfo.getAvailableProcessors();
					}
				}
				
				TopologyType topologyType;
				try {
					topologyType = TopologyType.valueOf(moduleContainer.getDefaultTopologyConfig().getString("type"));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Wrong data in defaultTopology.type", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				int interval;
				switch (topologyType) {
				case COMPLETE:
					interval = Integer.parseInt(moduleContainer.getDefaultTopologyConfig().getString("interval"));
					createCompleteTopology(workDescription, nodeCount, interval);
					break;
				case RING:
					interval = Integer.parseInt(moduleContainer.getDefaultTopologyConfig().getString("interval"));
					createRingTopology(workDescription, nodeCount, interval);
					break;
				case TREE:
					interval = Integer.parseInt(moduleContainer.getDefaultTopologyConfig().getString("interval"));
					createTreeTopology(workDescription, nodeCount, interval);
				case UNCONNECTED:
					createUnconnectedTopology(workDescription, nodeCount);
					break;
				default:
					JOptionPane.showMessageDialog(null, "Not implemented", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		JButton buttonAutogenerate = new JButton("Custom topology");
		buttonAutogenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AutogenerateFrame(workDescription);
			}
		});
		
		NewNodeContainersListener newCustomTopologyListener = new NewNodeContainersListener() {
			
			@Override
			public void changed() {
				if(!newAutogeneratedTopologyNodes.isEmpty()){
					for (NodeContainer nodeContainer : newAutogeneratedTopologyNodes) {
						editNodePanel.add(createEditNodePanel(nodeContainer), nodeContainer.toString());
					}
					newAutogeneratedTopologyNodes.clear();
				}
				
			}
		};
		
		addNewNodeContainersListener(newCustomTopologyListener);
		
		
		
		JButton buttonAssignWorkers = new JButton("Assign workers");
		buttonAssignWorkers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<WorkerInformation> workerInformationList = new ArrayList<WorkerInformation>();
				for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
					if (workerInformation.getWorkerStatus() == WorkerStatus.READY_NEW_IMPL) {
						workerInformationList.add(workerInformation);
					}
				}
				if (workerInformationList.isEmpty()) {
					JOptionPane.showMessageDialog(null, "No workers", "Error", JOptionPane.WARNING_MESSAGE);
				} else {
					NetworkUtils.assignProcessors(workDescription.getNodeContainerList(), workerInformationList);
					workDescription.fireNodeContainerChanged();
				}
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonCreateDefaultTopology);
		buttonPanel.add(buttonAutogenerate);
		buttonPanel.add(buttonAssignWorkers);
		
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private JPanel createEditNodePanel(final NodeContainer nodeContainer) {
		final JTextField nameTextfield = new JTextField();
		final DefaultComboBoxModel algorithmComboModel = new DefaultComboBoxModel();
		final DefaultComboBoxModel computerComboboxModel = new DefaultComboBoxModel();
		final ConnectionTableModel connectionTableModel = new ConnectionTableModel(nodeContainer);
		for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
			if (workerInformation.getWorkerStatus() == WorkerStatus.READY_NEW_IMPL) {
				computerComboboxModel.addElement(workerInformation);
			}
		}
		
		for (String algorithm : workDescription.getAlgorithmConfiguration().keySet()){
			algorithmComboModel.addElement(algorithm);
		}
		
		final JComboBox algorithmCombobox = new JComboBox(algorithmComboModel);
		final JComboBox computerCombobox = new JComboBox(computerComboboxModel);
		
		AlgorithmListener algorithmListener = new AlgorithmListener() {
			
			@Override
			public void changed() {
				algorithmComboModel.removeAllElements();
				for (String algorithm : workDescription.getAlgorithmConfiguration().keySet()) {
					algorithmComboModel.addElement(algorithm);
				}
			}
		};
		workDescription.addAlgoithmListener(algorithmListener);
		
		WorkerInformationListener workerInformationListener = new WorkerInformationListener() {
			
			@Override
			public void changed() {
				computerComboboxModel.removeAllElements();
				for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
					if (workerInformation.getWorkerStatus() == WorkerStatus.READY_NEW_IMPL) {
						computerComboboxModel.addElement(workerInformation);
					}
				}
			}
		};
		workDescription.addWorkerInformationListener(workerInformationListener);
		
		NodeContainerListener nodeContainerListener = new NodeContainerListener() {
			
			@Override
			public void changed() {
				nameTextfield.setText(nodeContainer.getNodeName());
				computerComboboxModel.setSelectedItem(nodeContainer.getWorkerInformation());
				algorithmCombobox.setSelectedItem(nodeContainer.getAlgorithmName());
				connectionTableModel.fireTableDataChanged();
			}
		};
		workDescription.addNodeContainerListener(nodeContainerListener);

		final JPanel addNodeDetailsPanel = new JPanel(new SpringLayout());
		addNodeDetailsPanel.add(new JLabel("Name"));
		addNodeDetailsPanel.add(nameTextfield);
		addNodeDetailsPanel.add(new JLabel("Algorithm"));
		addNodeDetailsPanel.add(algorithmCombobox);
		addNodeDetailsPanel.add(new JLabel("Computer"));
		addNodeDetailsPanel.add(computerCombobox);
		SpringUtilities.makeCompactGrid(addNodeDetailsPanel, 3, 2, 5, 5, 5, 5);
		
		JButton buttonSave = new JButton("Save");
		buttonSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				nodeContainer.setNodeName(nameTextfield.getText());
				nameTextfield.setText("Node" + nodeNumber);
				nodeContainer.setAlgorithmName((String) algorithmCombobox.getSelectedItem());
				nodeContainer.setWorkerInformation((WorkerInformation) computerComboboxModel.getSelectedItem());
				workDescription.fireNodeContainerChanged();
			}
		});
		
		JButton buttonDeleteNode = new JButton("Delete");
		
		buttonDeleteNode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				List<NodeContainer> nodeContainerList = workDescription.getNodeContainerList();
				for (int i = 0; i < nodeContainerList.size(); i++) {
					NodeContainer nodeContainerIter = nodeContainerList.get(i);
					List<Connection> connectionList = nodeContainerIter.getConnectionList();
					for (int j = 0; j < connectionList.size(); j++) {
						Connection connection = connectionList.get(j);
						if(connection.getNodeContainer().getNodeName().contentEquals(nodeContainer.getNodeName())){
							connectionList.remove(j);
							j--;
						}
					}
					if(nodeContainerIter.getNodeName().contentEquals(nodeContainer.getNodeName())){
						nodeContainerList.remove(i);
						i--;
					}
				}
				workDescription.fireNodeContainerChanged();
				
			}
		});
		
		JButton buttonAddConnection = new JButton("Add connection");
		buttonAddConnection.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceOfNewConnection.set(nodeContainer);
			}
		});
		
		JPanel addNodeButtonsPanel = new JPanel();
		addNodeButtonsPanel.add(buttonSave);
		addNodeButtonsPanel.add(buttonAddConnection);
		addNodeButtonsPanel.add(buttonDeleteNode);
		
		JPanel addNodePanel = new JPanel(new BorderLayout());
		addNodePanel.add(addNodeDetailsPanel, BorderLayout.CENTER);
		addNodePanel.add(addNodeButtonsPanel, BorderLayout.SOUTH);
		
		final JConnectionTable table = new JConnectionTable(connectionTableModel, "Delete", workDescription);
	    
		JPanel panel = new JPanel(new BorderLayout());
		panel.setMinimumSize(new Dimension(10, 10));
		panel.add(addNodePanel, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		table.getParent().setBackground(Color.WHITE);
		return panel;
	}

	private JPanel createAddNodePanel() {
		final JTextField nameTextfield = new JTextField("Node1");
		final DefaultComboBoxModel computerComboboxModel = new DefaultComboBoxModel();
		final DefaultComboBoxModel algorithmComboModel = new DefaultComboBoxModel();
		final JComboBox algorithmCombobox = new JComboBox(algorithmComboModel);
		final JComboBox computerCombobox = new JComboBox(computerComboboxModel);
		
		AlgorithmListener algorithmListener = new AlgorithmListener() {
			
			@Override
			public void changed() {
				algorithmComboModel.removeAllElements();
				for (String algorithm : workDescription.getAlgorithmConfiguration().keySet()) {
					algorithmComboModel.addElement(algorithm);
				}
			}
		};
		workDescription.addAlgoithmListener(algorithmListener);
		
		WorkerInformationListener workerInformationListener = new WorkerInformationListener() {
			
			@Override
			public void changed() {
//				computerComboboxModel.removeAllElements();
//				for (WorkerInformation workerInformation : workDescription.getWorkerInformationList()) {
//					if (workerInformation.getWorkerStatus() == WorkerStatus.READY_NEW_IMPL) {
//						computerComboboxModel.addElement(workerInformation);
//					}
//				}
			}
		};
		workDescription.addWorkerInformationListener(workerInformationListener);

		final JPanel addNodeDetailsPanel = new JPanel(new SpringLayout());
		addNodeDetailsPanel.add(new JLabel("Name"));
		addNodeDetailsPanel.add(nameTextfield);
		addNodeDetailsPanel.add(new JLabel("Algorithm"));
		addNodeDetailsPanel.add(algorithmCombobox);
		addNodeDetailsPanel.add(new JLabel("Computer"));
		addNodeDetailsPanel.add(computerCombobox);
		SpringUtilities.makeCompactGrid(addNodeDetailsPanel, 3, 2, 5, 5, 5, 5);
		
		JButton buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(workDescription.nodeNameExists(nameTextfield.getText())){
					JButton button = new JButton("ok");
					String nodeExistsLabel = "Node named \""+ nameTextfield.getText() +"\" already exists. Try another name";
					JOptionPane.showMessageDialog(button, nodeExistsLabel);
					return;
				}
				NodeContainer nodeContainer = new NodeContainer(nameTextfield.getText());
				nodeNumber++;
				nameTextfield.setText("Node" + nodeNumber);
				nodeContainer.setAlgorithmName((String) algorithmCombobox.getSelectedItem());
				nodeContainer.setWorkerInformation((WorkerInformation) computerComboboxModel.getSelectedItem());
				workDescription.getNodeContainerList().add(nodeContainer);
				editNodePanel.add(createEditNodePanel(nodeContainer), nodeContainer.toString());
				workDescription.fireNodeContainerChanged();
			}
		});
		
		JPanel addNodeButtonsPanel = new JPanel();
		addNodeButtonsPanel.add(buttonAdd);
		
		JPanel addNodePanel = new JPanel(new BorderLayout());
		addNodePanel.add(addNodeDetailsPanel, BorderLayout.CENTER);
		addNodePanel.add(addNodeButtonsPanel, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setMinimumSize(new Dimension(10, 10));
		panel.add(addNodePanel, BorderLayout.NORTH);
		return panel;
	}
	
	private static class AutogenerateFrame extends JFrame {

		private static final long serialVersionUID = 1L;
		private final WorkDescription workDescription;

		public AutogenerateFrame(WorkDescription workDescription) {
			this.workDescription = workDescription;
			setTitle("Autogenerate topology");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			final CardLayout cardLayout = new CardLayout();
			final JPanel mainPanel = new JPanel(cardLayout);
			mainPanel.setBorder(BorderFactory.createTitledBorder("Topology parameters"));
			mainPanel.add(completePanel(), TopologyType.COMPLETE.toString());
			mainPanel.add(toroidalPanel(), TopologyType.TOROIDAL.toString());
			mainPanel.add(ringPanel(), TopologyType.RING.toString());
			mainPanel.add(islandsPanel(), TopologyType.ISLANDS.toString());
			mainPanel.add(treePanel(), TopologyType.TREE.toString());
			mainPanel.add(unconnectedPanel(), TopologyType.UNCONNECTED.toString());
			
			final JComboBox topologyTypesCombobox = new JComboBox(TopologyType.values());
			
			topologyTypesCombobox.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					cardLayout.show(mainPanel, ((TopologyType) topologyTypesCombobox.getSelectedItem()).toString());
				}
			});
			
			JPanel comboboxPanel = new JPanel(new BorderLayout());
			comboboxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			comboboxPanel.add(topologyTypesCombobox, BorderLayout.CENTER);
			comboboxPanel.add(new JLabel("Topology: "), BorderLayout.WEST);
			
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(comboboxPanel, BorderLayout.NORTH);
			getContentPane().add(mainPanel, BorderLayout.CENTER);
			pack();
			setSize(500, getSize().height);
			setVisible(true);
		}

		private JPanel completePanel() {
			final JTextField nodeCountTextfield = new JTextField("6");
			final JTextField intervalTextfield = new JTextField("1000");
			
			JButton buttonGenerate = new JButton("Generate");
			buttonGenerate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Integer nodeCount = parseInt(nodeCountTextfield);
					if (nodeCount == null || nodeCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid node count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					Integer interval = parseInt(intervalTextfield);
					if (interval == null || interval < 1) {
						JOptionPane.showMessageDialog(null, "Invalid interval", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					createCompleteTopology(workDescription, nodeCount, interval);
				}
			});
			
			JPanel contentPanel = new JPanel(new SpringLayout());
			contentPanel.add(new JLabel("Node count:"));
			contentPanel.add(nodeCountTextfield);
			contentPanel.add(new JLabel("Interval:"));
			contentPanel.add(intervalTextfield);
			SpringUtilities.makeCompactGrid(contentPanel, 2, 2, 5, 5, 5, 5);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonGenerate);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPanel, BorderLayout.NORTH);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			return panel;
		}
		
		private JPanel toroidalPanel() {
			final JTextField rowCountTextfield = new JTextField("2");
			final JTextField columnCountTextfield = new JTextField("3");
			final JTextField intervalTextfield = new JTextField("1000");
			
			JButton buttonGenerate = new JButton("Generate");
			buttonGenerate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Integer rowCount = parseInt(rowCountTextfield);
					if (rowCount == null || rowCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid row count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}	
							
					Integer columnCount = parseInt(columnCountTextfield);
					if (columnCount == null || columnCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid column count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					Integer interval = parseInt(intervalTextfield);
					if (interval == null || interval < 1) {
						JOptionPane.showMessageDialog(null, "Invalid interval", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					createToroidTopology(workDescription, rowCount, columnCount, interval);
				}
			});
			
			JPanel contentPanel = new JPanel(new SpringLayout());
			contentPanel.add(new JLabel("Row count:"));
			contentPanel.add(rowCountTextfield);
			contentPanel.add(new JLabel("Column count:"));
			contentPanel.add(columnCountTextfield);
			contentPanel.add(new JLabel("Interval:"));
			contentPanel.add(intervalTextfield);
			SpringUtilities.makeCompactGrid(contentPanel, 3, 2, 5, 5, 5, 5);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonGenerate);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPanel, BorderLayout.NORTH);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			return panel;
		}
		
		private JPanel ringPanel() {
			final JTextField nodeCountTextfield = new JTextField("6");
			final JTextField intervalTextfield = new JTextField("1000");
			
			JButton buttonGenerate = new JButton("Generate");
			buttonGenerate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Integer nodeCount = parseInt(nodeCountTextfield);
					if (nodeCount == null || nodeCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid node count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					Integer interval = parseInt(intervalTextfield);
					if (interval == null || interval < 1) {
						JOptionPane.showMessageDialog(null, "Invalid interval", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					createRingTopology(workDescription, nodeCount, interval);
				}
			});
			
			JPanel contentPanel = new JPanel(new SpringLayout());
			contentPanel.add(new JLabel("Node count:"));
			contentPanel.add(nodeCountTextfield);
			contentPanel.add(new JLabel("Interval:"));
			contentPanel.add(intervalTextfield);
			SpringUtilities.makeCompactGrid(contentPanel, 2, 2, 5, 5, 5, 5);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonGenerate);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPanel, BorderLayout.NORTH);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			return panel;
		}
		
		private JPanel islandsPanel() {
			final JTextField islandCountTextfield = new JTextField("2");
			final JTextField nodesPerIslandTextfield = new JTextField("3");
			final JTextField outerIntervalTextfield = new JTextField("5000");
			final JTextField innerIntervalTextfield = new JTextField("1000");
			
			JButton buttonGenerate = new JButton("Generate");
			buttonGenerate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Integer islandCount = parseInt(islandCountTextfield);
					if (islandCount == null || islandCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid island count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}	
							
					Integer nodesPerIsland = parseInt(nodesPerIslandTextfield);
					if (nodesPerIsland == null || nodesPerIsland < 1) {
						JOptionPane.showMessageDialog(null, "Invalid node count per island", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					Integer outerInterval = parseInt(outerIntervalTextfield);
					if (outerInterval == null || outerInterval < 1) {
						JOptionPane.showMessageDialog(null, "Invalid outer interval", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					Integer innerInterval = parseInt(innerIntervalTextfield);
					if (innerInterval == null || innerInterval < 1) {
						JOptionPane.showMessageDialog(null, "Invalid inner interval", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					createIslandTopology(workDescription, islandCount, nodesPerIsland, innerInterval, outerInterval);
				}
			});
			
			JPanel contentPanel = new JPanel(new SpringLayout());
			contentPanel.add(new JLabel("Island count:"));
			contentPanel.add(islandCountTextfield);
			contentPanel.add(new JLabel("Nodes per island:"));
			contentPanel.add(nodesPerIslandTextfield);
			contentPanel.add(new JLabel("Outer interval:"));
			contentPanel.add(outerIntervalTextfield);
			contentPanel.add(new JLabel("Inner interval:"));
			contentPanel.add(innerIntervalTextfield);
			SpringUtilities.makeCompactGrid(contentPanel, 4, 2, 5, 5, 5, 5);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonGenerate);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPanel, BorderLayout.NORTH);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			return panel;
		}
		
		private JPanel treePanel() {
			final JTextField nodeCountTextfield = new JTextField("6");
			final JTextField intervalTextfield = new JTextField("1000");
			
			JButton buttonGenerate = new JButton("Generate");
			buttonGenerate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Integer nodeCount = parseInt(nodeCountTextfield);
					if (nodeCount == null) return;
					if (nodeCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid node count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					Integer interval = parseInt(intervalTextfield);
					if (interval == null) return;
					if (interval < 1) {
						JOptionPane.showMessageDialog(null, "Invalid interval", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					createTreeTopology(workDescription, nodeCount, interval);
				}
			});
			
			JPanel contentPanel = new JPanel(new SpringLayout());
			contentPanel.add(new JLabel("Node count:"));
			contentPanel.add(nodeCountTextfield);
			contentPanel.add(new JLabel("Interval:"));
			contentPanel.add(intervalTextfield);
			SpringUtilities.makeCompactGrid(contentPanel, 2, 2, 5, 5, 5, 5);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonGenerate);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPanel, BorderLayout.NORTH);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			return panel;
		}
		
		private JPanel unconnectedPanel() {
			final JTextField nodeCountTextfield = new JTextField("6");
			
			JButton buttonGenerate = new JButton("Generate");
			buttonGenerate.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Integer nodeCount = parseInt(nodeCountTextfield);
					if (nodeCount == null) return;
					if (nodeCount < 1) {
						JOptionPane.showMessageDialog(null, "Invalid node count", "Error", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					createUnconnectedTopology(workDescription, nodeCount);
				}
			});
			
			JPanel contentPanel = new JPanel(new SpringLayout());
			contentPanel.add(new JLabel("Node count:"));
			contentPanel.add(nodeCountTextfield);
			SpringUtilities.makeCompactGrid(contentPanel, 1, 2, 5, 5, 5, 5);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonGenerate);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(contentPanel, BorderLayout.NORTH);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			return panel;
		}
		
		private Integer parseInt(JTextField textField) {
			try {
				return Integer.parseInt(textField.getText());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
	}
	
	private static String[] getAlgorithmNames(WorkDescription workDescription) {
		Set<String> algorithmNameSet = workDescription.getAlgorithmConfiguration().keySet();
		if (algorithmNameSet.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Not algorithm configurations available", "Error", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		return algorithmNameSet.toArray(new String[algorithmNameSet.size()]);
	}
	
	private static void createCompleteTopology(WorkDescription workDescription, int nodeCount, int interval) {
		String[] algorithmNames = getAlgorithmNames(workDescription);
		if (algorithmNames == null) return;
		
		List<NodeContainer> nodeContainerList = NetworkUtils.createCompleteTopology(algorithmNames, nodeCount, interval);
		workDescription.getNodeContainerList().clear();
		workDescription.getNodeContainerList().addAll(nodeContainerList);
		workDescription.fireNodeContainerChanged();
		
		newAutogeneratedTopologyNodes.addAll(nodeContainerList);
		fireNewNodeContainers();
	}
	
	private static void createToroidTopology(WorkDescription workDescription, int rowCount, int columnCount, int interval) {
		String[] algorithmNames = getAlgorithmNames(workDescription);
		if (algorithmNames == null) return;
		
		List<NodeContainer> nodeContainerList = NetworkUtils.createToroidTopology(algorithmNames, rowCount, columnCount, interval);
		workDescription.getNodeContainerList().clear();
		workDescription.getNodeContainerList().addAll(nodeContainerList);
		workDescription.fireNodeContainerChanged();

		newAutogeneratedTopologyNodes.addAll(nodeContainerList);
		fireNewNodeContainers();
	}
	
	private static void createRingTopology(WorkDescription workDescription, int nodeCount, int interval) {
		String[] algorithmNames = getAlgorithmNames(workDescription);
		if (algorithmNames == null) return;
		
		List<NodeContainer> nodeContainerList = NetworkUtils.createRingTopology(algorithmNames, nodeCount, interval);
		workDescription.getNodeContainerList().clear();
		workDescription.getNodeContainerList().addAll(nodeContainerList);
		workDescription.fireNodeContainerChanged();

		newAutogeneratedTopologyNodes.addAll(nodeContainerList);
		fireNewNodeContainers();
	}
	
	private static void createIslandTopology(WorkDescription workDescription, int islandCount, int nodesPerIsland, int innerInterval, int outerInterval) {
		String[] algorithmNames = getAlgorithmNames(workDescription);
		if (algorithmNames == null) return;
		
		List<NodeContainer> nodeContainerList = NetworkUtils.createIslandTopology(algorithmNames, islandCount, nodesPerIsland, innerInterval, outerInterval);
		workDescription.getNodeContainerList().clear();
		workDescription.getNodeContainerList().addAll(nodeContainerList);
		workDescription.fireNodeContainerChanged();

		newAutogeneratedTopologyNodes.addAll(nodeContainerList);
		fireNewNodeContainers();
	}
	
	private static void createTreeTopology(WorkDescription workDescription, int nodeCount, int interval) {
		String[] algorithmNames = getAlgorithmNames(workDescription);
		if (algorithmNames == null) return;
		
		List<NodeContainer> nodeContainerList = NetworkUtils.createTreeTopology(algorithmNames, nodeCount, interval);
		workDescription.getNodeContainerList().clear();
		workDescription.getNodeContainerList().addAll(nodeContainerList);
		workDescription.fireNodeContainerChanged();

		newAutogeneratedTopologyNodes.addAll(nodeContainerList);
		fireNewNodeContainers();
	}
	
	private static void createUnconnectedTopology(WorkDescription workDescription, int nodeCount) {
		String[] algorithmNames = getAlgorithmNames(workDescription);
		if (algorithmNames == null) return;
		
		List<NodeContainer> nodeContainerList = NetworkUtils.createUnconnectedTopology(algorithmNames, nodeCount);
		workDescription.getNodeContainerList().clear();
		workDescription.getNodeContainerList().addAll(nodeContainerList);
		workDescription.fireNodeContainerChanged();

		newAutogeneratedTopologyNodes.addAll(nodeContainerList);
		fireNewNodeContainers();
	}
	
	private static void addNewNodeContainersListener(NewNodeContainersListener listener){
		newNodeContainersListenerList.add(listener);
	}
	
	private static void fireNewNodeContainers(){
		for (NewNodeContainersListener listener : newNodeContainersListenerList) {
			listener.changed();
		}
	}
	
	public static interface NewNodeContainersListener{
		
		public void changed();
		
	}
	
	
//	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
//		SwingUtilities.invokeAndWait(new Runnable() {
//			
//			@Override
//			public void run() {
//				new AutogenerateFrame();
//			}
//		});
//	}

}
