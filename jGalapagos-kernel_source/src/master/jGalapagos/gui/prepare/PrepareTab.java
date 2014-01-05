package jGalapagos.gui.prepare;

import jGalapagos.gui.ContentComponent;
import jGalapagos.gui.TabContainer;
import jGalapagos.gui.Tab;
import jGalapagos.master.ModuleContainer;
import jGalapagos.master.WorkDescription;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * 
 * @author Mihej Komar
 *
 */
public class PrepareTab implements Tab {
	
	private static final int BUTTON_WIDTH = 150;
	private final JPanel panelMainContent = new JPanel();
	private final WorkDescription workDescription;
	private final ModuleContainer moduleContainer;
	private final TabContainer tabContainer;

	public PrepareTab(ModuleContainer moduleContainer, TabContainer tabContainer) throws IOException {
		workDescription = new WorkDescription(moduleContainer);
		this.moduleContainer = moduleContainer;
		this.tabContainer = tabContainer;
	}
	
	@Override
	public JComponent getContent() throws IOException {
		JPanel leftPanel = createLeftPanel();
		JPanel mainContent = createMainContent();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(leftPanel), BorderLayout.WEST);
		panel.add(mainContent, BorderLayout.CENTER);
		return panel;
	}
	
	@Override
	public String getName() {
		return "Prepare";
	}

	@Override
	public boolean isClosable() {
		return true;
	}
	
	private JPanel createLeftPanel() {
		JButton[] buttons = new JButton[5];
		
		buttons[0] = new JButton("Problem");
		buttons[0].setIcon(new ImageIcon("data/images/problem.png"));
		buttons[0].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.PROBLEM.toString());
			}
		});
		
		buttons[1] = new JButton("Config");
		buttons[1].setIcon(new ImageIcon("data/images/algorithms.png"));
		buttons[1].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.ALGORITHMS.toString());
			}
		});
		
		buttons[2] = new JButton("Workers");
		buttons[2].setIcon(new ImageIcon("data/images/workers.png"));
		buttons[2].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.WORKERS.toString());
			}
		});
		
		buttons[3] = new JButton("Topology");
		buttons[3].setIcon(new ImageIcon("data/images/topology.png"));
		buttons[3].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.TOPOLOGY.toString());
			}
		});
		
		buttons[4] = new JButton("Start");
		buttons[4].setIcon(new ImageIcon("data/images/start.png"));
		buttons[4].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cardLayout = (CardLayout) (panelMainContent.getLayout());
				cardLayout.show(panelMainContent, ActivePanel.START.toString());
			}
		});
		
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(Color.GRAY);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		for (int i = 0; i < buttons.length; i++) {
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
	
	private JPanel createMainContent() throws IOException {
		ContentComponent componentProblem = new ContentComponent(new ProblemPanel(moduleContainer.getModule(), workDescription), "Problem", "Load problem data", "data/images/problem.png");
		ContentComponent componentAlgorithms = new ContentComponent(new ConfigPanel(moduleContainer, workDescription), "Algorithms", "Place to define all configurations of algorithm", "data/images/algorithms.png");
		ContentComponent componentWorkers = new ContentComponent(new WorkersPanel(workDescription), "Workers", "Place to define all configurations of algorithm", "data/images/topology.png");
		ContentComponent componentTopology = new ContentComponent(new TopologyPanel(workDescription, moduleContainer), "Topology", "Define topology", "data/images/topology.png");
		ContentComponent componentStart = new ContentComponent(new StartPanel(workDescription, tabContainer, moduleContainer), "Start", "Starts distributed system", "data/images/start.png");
		
		panelMainContent.setLayout(new CardLayout());
		panelMainContent.add(componentProblem, ActivePanel.PROBLEM.toString());
		panelMainContent.add(componentAlgorithms, ActivePanel.ALGORITHMS.toString());
		panelMainContent.add(componentWorkers, ActivePanel.WORKERS.toString());
		panelMainContent.add(componentTopology, ActivePanel.TOPOLOGY.toString());
		panelMainContent.add(componentStart, ActivePanel.START.toString());
		return panelMainContent;
	}
	
	@Override
	public void closeAction() {
		// TODO Auto-generated method stub
	}
	
	private enum ActivePanel {
		PROBLEM, ALGORITHMS, WORKERS, TOPOLOGY, START
	}

}
