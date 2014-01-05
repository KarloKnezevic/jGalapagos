package jGalapagos.gui.running;


import jGalapagos.core.Module;
import jGalapagos.gui.ContentComponent;
import jGalapagos.gui.Tab;
import jGalapagos.gui.TabContainer;
import jGalapagos.master.StatisticsCollector;
import jGalapagos.master.WorkDescription;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class RunningTab implements Tab {
	
	private static final int BUTTON_WIDTH = 150;
	private final JPanel panelMainContent = new JPanel();
	private final TabContainer mainWindowInterface;
	private final WorkDescription workDescription;
	private final StatisticsCollector statisticsCollector;
	private final Module coreInterface;
	
	public RunningTab(TabContainer mainWindowInterface, WorkDescription workDescription, Module coreInterface) {
		this.mainWindowInterface = mainWindowInterface;
		this.workDescription = workDescription;
		this.coreInterface = coreInterface;
		statisticsCollector = new StatisticsCollector("event_log.txt");
		workDescription.getMasterComunicator().addStatisticsListener(statisticsCollector);
	}

	@Override
	public JComponent getContent() {
		JPanel leftPanel = createLeftPanel();
		JPanel mainContent = createMainContent();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(leftPanel), BorderLayout.WEST);
		panel.add(mainContent, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createLeftPanel() {
		JButton[] buttons = new JButton[2];
		
		buttons[0] = new JButton("Table");
		buttons[0].setIcon(new ImageIcon("data/images/table.png"));
		buttons[0].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.TABLE.toString());
			}
		});
		
		buttons[1] = new JButton("Graph");
		buttons[1].setIcon(new ImageIcon("data/images/graph.png"));
		buttons[1].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.GRAPH.toString());
			}
		});
		
//		buttons[2] = new JButton("Topology");
//		buttons[2].setIcon(new ImageIcon("data/images/topology.png"));
//		buttons[2].addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
//				cardLayout.show(panelMainContent, ActivePanel.TOPOLOGY.toString());
//			}
//		});
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(Color.GRAY);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		for (int i = 0; i < 2; i++) {
			int x = (BUTTON_WIDTH - buttons[i].getPreferredSize().width) / 2;
			buttons[i].setMargin(new Insets(5, x, 5, x));
			buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
			buttons[i].setVerticalTextPosition(SwingConstants.BOTTOM);
			buttons[i].setHorizontalTextPosition(SwingConstants.CENTER);
			leftPanel.add(buttons[i]);
			leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		}
		return leftPanel;
	}
	
	private JPanel createMainContent() {
		ContentComponent componentTable = new ContentComponent(new TablePanel(mainWindowInterface, workDescription, coreInterface), "Table", "Show current results in tables", "data/images/table.png");
		ContentComponent componentGraph = new ContentComponent(new GraphPanel(mainWindowInterface, statisticsCollector, workDescription), "Graph", "Show results in plots", "data/images/graph.png");
//		ContentComponent componentTopology = new ContentComponent(new JLabel("componentTopology"), "Topology", "Display current results in topology", "data/images/topology.png");
		
		panelMainContent.setLayout(new CardLayout());
		panelMainContent.add(componentTable, ActivePanel.TABLE.toString());
		panelMainContent.add(componentGraph, ActivePanel.GRAPH.toString());
//		panelMainContent.add(componentTopology, ActivePanel.TOPOLOGY.toString());
		return panelMainContent;
	}
	
	@Override
	public String getName() {
		return "Running";
	}

	@Override
	public boolean isClosable() {
		return false;
	}
	
	@Override
	public void closeAction() {
		// TODO Auto-generated method stub
	}
	
	private enum ActivePanel {
		TABLE, GRAPH, TOPOLOGY
	}

}
