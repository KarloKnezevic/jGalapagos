package jGalapagos.gui;

import jGalapagos.gui.openSolution.OpenSolutionTab;
import jGalapagos.gui.prepare.PrepareTab;
import jGalapagos.master.ModuleContainer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Mihej Komar
 *
 */
public class MainWindow extends JFrame implements TabContainer {
	
	private final Log log = LogFactory.getLog(MainWindow.class);
	
	private static final long serialVersionUID = 1L;
	private JTabbedPane tabbedPane;
	private LogComponent logComponent;
	private final ModuleContainer moduleContainer;
	private final TabContainer mainWindowInterface = this;
	
	private Map<ActionName, Action> actionMap = new EnumMap<ActionName, Action>(ActionName.class);

	public MainWindow(ModuleContainer moduleContainer) throws IOException {
		this.moduleContainer = moduleContainer;

		setTitle("Master");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width - 100, screenSize.height - 100);
		addActions();
		addMenu();
		addContent();
		
		logComponent.addMessage(new Date(), "Proba");
		addTab(new PrepareTab(moduleContainer, this));
//		addTab(new TabExperiments());

		setVisible(true);
	}
	
	@Override
	public void addTab(Tab tab) {
		try {
			tabbedPane.addTab(tab.getName(), tab.getContent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tab.isClosable()) {
			tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new ButtonTabComponent(tab.getName(), tabbedPane));
		}
	}
	
	private void addActions() {
		Action actionExit = new AbstractAction("Exit") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		actionExit.putValue(Action.SHORT_DESCRIPTION, "Exit program.");
		
		Action actionOpenSolution = new AbstractAction("Open solution") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					addTab(new OpenSolutionTab(moduleContainer, mainWindowInterface));
				} catch (IOException e1) {
					log.warn(e1, e1);
				}
			}
		};
		actionOpenSolution.putValue(Action.SHORT_DESCRIPTION, "Open solution");
		
		actionMap.put(ActionName.EXIT, actionExit);
		actionMap.put(ActionName.OPEN_SOLUTION, actionOpenSolution);
	}
	
	private void addMenu() {
		JMenu menuFile = new JMenu("File");
		menuFile.add(new JMenuItem(actionMap.get(ActionName.OPEN_SOLUTION)));
		menuFile.addSeparator();
		menuFile.add(new JMenuItem(actionMap.get(ActionName.EXIT)));
		
		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(menuFile);
		setJMenuBar(jMenuBar);
	}
	
	private void addContent() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setMinimumSize(new Dimension(10, 10));
		
		logComponent = new LogComponent();
		logComponent.setMinimumSize(new Dimension(10, 10));
		
//		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.setTopComponent(tabbedPane);
//		splitPane.setBottomComponent(logComponent);
//		splitPane.setOneTouchExpandable(true);
//		splitPane.setResizeWeight(0.7);
//		splitPane.setDividerLocation(getHeight() - 300);
//
//		getContentPane().add(splitPane);
		
		getContentPane().add(tabbedPane);
	}
	
	private enum ActionName {
		EXIT, OPEN_SOLUTION
	}
	
	public static void main(String[] args) throws Exception {
		
		File modulesDirs = new File("modules");
		List<ModuleContainer> moduleList = new ArrayList<ModuleContainer>();
		
		for (File moduleDir : modulesDirs.listFiles()) {
			if (!moduleDir.isDirectory() || moduleDir.isHidden()) continue;
			try {
				moduleList.add(new ModuleContainer(moduleDir));
			} catch (Exception e) {
				new ExceptionView("Error loading module " + moduleDir.getName(), e);
				return;
			}
		}
		
		final ModuleContainer module = (ModuleContainer) JOptionPane.showInputDialog(null, "Choose module", "jGalapagos", JOptionPane.PLAIN_MESSAGE, null, moduleList.toArray(), moduleList.get(0));
		if (module == null) return;
		
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				try {
					new MainWindow(module);
				} catch (Throwable e) {
					new ExceptionView("Error while starting application", e);
				}
			}
		});
	}

}
