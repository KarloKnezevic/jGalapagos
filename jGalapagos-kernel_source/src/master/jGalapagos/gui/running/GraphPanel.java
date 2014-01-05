package jGalapagos.gui.running;

import jGalapagos.communication.TopologyNode;
import jGalapagos.core.statistics.StatDataType;
import jGalapagos.gui.TabContainer;
import jGalapagos.master.StatisticsCollector;
import jGalapagos.master.WorkDescription;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import layout.SpringUtilities;

public class GraphPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private final TabContainer mainWindowInterface;
	private final StatisticsCollector statisticsCollector;
	private final WorkDescription workDescription;
	
	public GraphPanel(TabContainer mainWindowInterface, StatisticsCollector statisticsCollector, WorkDescription workDescription) {
		this.mainWindowInterface = mainWindowInterface;
		this.statisticsCollector = statisticsCollector;
		this.workDescription = workDescription;
		
		setLayout(new GridLayout(1, 2));
		add(createPanelOneAlgorithm());
		add(createPanelOneData());
	}
	
	private JPanel createPanelOneAlgorithm() {
		TopologyNode[] topologyNodes = new TopologyNode[workDescription.getNodeContainerList().size()];
		for (int i = 0; i < workDescription.getNodeContainerList().size(); i++) {
			topologyNodes[i] = workDescription.getNodeContainerList().get(i).getTopologyNode();
		}
		
		StatDataType[] statDataTypes = StatDataType.values();
		
		final JComboBox topologyNodeCombobox = new JComboBox(topologyNodes);
		
		JPanel statDataPanel = new JPanel();
		statDataPanel.setLayout(new BoxLayout(statDataPanel, BoxLayout.PAGE_AXIS));
		final JCheckBox[] statDataTypeCheckBoxes = new JCheckBox[statDataTypes.length];
		for (int i = 0; i < statDataTypes.length; i++) {
			JCheckBox checkBox = new JCheckBox(statDataTypes[i].toString());
			statDataTypeCheckBoxes[i] = checkBox;
			statDataPanel.add(checkBox);
		}
		
		int fitnessDimensionality = statisticsCollector.getFitnessDimensionality();
		Integer[] dimensions = new Integer[fitnessDimensionality];
		for (int i = 0; i < fitnessDimensionality; i++) {
			dimensions[i] = i;
		}
		final JComboBox dimensionsCombobox = new JComboBox(dimensions);		
		JPanel panelOneAlgorithmSelection = new JPanel(new SpringLayout());
		panelOneAlgorithmSelection.add(new JLabel("Topology node:"));
		panelOneAlgorithmSelection.add(topologyNodeCombobox);
		panelOneAlgorithmSelection.add(new JLabel("Fitness Dimension"));
		panelOneAlgorithmSelection.add(dimensionsCombobox);
		panelOneAlgorithmSelection.add(new JLabel("Statistic data type:"));
		panelOneAlgorithmSelection.add(statDataPanel);
		SpringUtilities.makeCompactGrid(panelOneAlgorithmSelection, 3, 2, 5, 5, 5, 5);
		
		JButton buttonCreateGraph = new JButton("Create");
		buttonCreateGraph.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TopologyNode selectedTopologyNode = (TopologyNode) topologyNodeCombobox.getSelectedItem();
				Integer interestDimension = (Integer) dimensionsCombobox.getSelectedItem();
				List<StatDataType> selectedStatDataTypes = new ArrayList<StatDataType>();
				for (JCheckBox checkBox : statDataTypeCheckBoxes) {
					if (checkBox.isSelected()) {
						selectedStatDataTypes.add(StatDataType.valueOf(checkBox.getText()));
					}
				}
				GraphTab tabGraph = new GraphTab(statisticsCollector, workDescription, selectedTopologyNode, selectedStatDataTypes, interestDimension.intValue());
				mainWindowInterface.addTab(tabGraph);
			}
		});
		
		JPanel panelOneAlgorithmsButtons = new JPanel();
		panelOneAlgorithmsButtons.add(buttonCreateGraph);
		
		
		JPanel panelOneAlgorithmTmp = new JPanel(new BorderLayout());
		panelOneAlgorithmTmp.setBorder(BorderFactory.createTitledBorder("Single node, multiple data"));
		panelOneAlgorithmTmp.add(panelOneAlgorithmSelection, BorderLayout.CENTER);
		panelOneAlgorithmTmp.add(panelOneAlgorithmsButtons, BorderLayout.SOUTH);
		
		JPanel panelOneAlgorithm = new JPanel(new BorderLayout());
		panelOneAlgorithm.add(panelOneAlgorithmTmp, BorderLayout.NORTH);
		return panelOneAlgorithm;
	}
	
	private JPanel createPanelOneData() {
		final TopologyNode[] topologyNodes = new TopologyNode[workDescription.getNodeContainerList().size()];
		for(int i = 0; i < topologyNodes.length; i++){
			topologyNodes[i] = workDescription.getNodeContainerList().get(i).getTopologyNode();
		}
		StatDataType[] statDataTypes = StatDataType.values();
		
		final JCheckBox[] checkBoxes = new JCheckBox[topologyNodes.length];
		JPanel topologyNodesPanel = new JPanel();
		topologyNodesPanel.setLayout(new BoxLayout(topologyNodesPanel, BoxLayout.PAGE_AXIS));
		for(int i = 0; i < topologyNodes.length; i++){
			checkBoxes[i] = new JCheckBox(topologyNodes[i].getName(), false);
			topologyNodesPanel.add(checkBoxes[i]);
		}
		
		final JComboBox statDataCombobox = new JComboBox(statDataTypes);
		
		int fitnessDimensionality = statisticsCollector.getFitnessDimensionality();
		Integer[] dimensions = new Integer[fitnessDimensionality];
		for (int i = 0; i < fitnessDimensionality; i++) {
			dimensions[i] = i;
		}
		final JComboBox dimensionsCombobox = new JComboBox(dimensions);
		
		JPanel panelOneAlgorithmSelection = new JPanel(new SpringLayout());
		panelOneAlgorithmSelection.add(new JLabel("Statistic data type:"));
		panelOneAlgorithmSelection.add(statDataCombobox);
		panelOneAlgorithmSelection.add(new JLabel("Fitness Dimension"));
		panelOneAlgorithmSelection.add(dimensionsCombobox);
		panelOneAlgorithmSelection.add(new JLabel("Topology node:"));
		panelOneAlgorithmSelection.add(topologyNodesPanel);
		SpringUtilities.makeCompactGrid(panelOneAlgorithmSelection, 3, 2, 5, 5, 5, 5);
		JButton buttonCreateGraph = new JButton("Create");
		buttonCreateGraph.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StatDataType intrestData = (StatDataType) statDataCombobox.getSelectedItem();
				Integer interestDimension = (Integer) dimensionsCombobox.getSelectedItem(); 
				List<TopologyNode> selectedTopologyNodes = new ArrayList<TopologyNode>();
				for(int i = 0; i < checkBoxes.length; i++){
					JCheckBox checkbox = checkBoxes[i];
					if(checkbox.isSelected()){
						selectedTopologyNodes.add(topologyNodes[i]);
					}
				}
				GraphTab tabGraph = new GraphTab(statisticsCollector, workDescription, selectedTopologyNodes, intrestData, interestDimension.intValue());
				mainWindowInterface.addTab(tabGraph);
				
			}
		});
		
		
		
		
		JPanel panelOneAlgorithmsButtons = new JPanel();
		panelOneAlgorithmsButtons.add(buttonCreateGraph);
		
		
		JPanel panelOneDataTmp = new JPanel(new BorderLayout());
		panelOneDataTmp.setBorder(BorderFactory.createTitledBorder("Single data, multiple nodes"));
		panelOneDataTmp.add(panelOneAlgorithmSelection, BorderLayout.CENTER);
		panelOneDataTmp.add(panelOneAlgorithmsButtons, BorderLayout.SOUTH);
		
		JPanel panelOneData = new JPanel(new BorderLayout());
		panelOneData.add(panelOneDataTmp, BorderLayout.NORTH);
		return panelOneData;
	}

}
