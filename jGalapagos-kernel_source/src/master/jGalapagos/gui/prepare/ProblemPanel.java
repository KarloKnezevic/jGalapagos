package jGalapagos.gui.prepare;

import jGalapagos.core.Module;
import jGalapagos.core.ProblemLoader;
import jGalapagos.core.ProblemViewer;
import jGalapagos.gui.ExceptionView;
import jGalapagos.master.WorkDescription;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ProblemPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final ProblemViewer problemViewer;
	private final ProblemLoader problemLoader;

	public ProblemPanel(Module module, final WorkDescription workDescription) {
		problemViewer = module.createProblemViewer();
		problemLoader = module.createProblemLoader();

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(createTopComponent(module, workDescription));
		splitPane.setBottomComponent(createBottomComponent());
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(getHeight() / 2);
		
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}
	
	private JComponent createTopComponent(Module module, final WorkDescription workDescription) {
		JButton buttonLoad = new JButton("Load");
		buttonLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Serializable problemDescription = null;
				try {
					problemDescription = problemLoader.loadProblem();
				} catch (Exception e1) {
					new ExceptionView("Error loading problem data", e1);
					return;
				}
				problemViewer.updateProblem(problemDescription);
				workDescription.setProblemDescription(problemDescription);
			}
		});
		
//		JButton buttonReset = new JButton("Reset");
//		buttonReset.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				problemLoader.fireReset();
//			}
//		});
		
		JPanel topComponentButtonPanel = new JPanel();
		topComponentButtonPanel.add(buttonLoad);
//		topComponentButtonPanel.add(buttonReset);
		
		JPanel topComponent = new JPanel(new BorderLayout());
		topComponent.setBorder(BorderFactory.createTitledBorder("Load problem"));
		topComponent.add(problemLoader.getComponent(), BorderLayout.CENTER);
		topComponent.add(topComponentButtonPanel, BorderLayout.SOUTH);
		return topComponent;
	}
	
	private JComponent createBottomComponent() {
		JPanel bottomComponent = new JPanel(new BorderLayout());
		bottomComponent.setBorder(BorderFactory.createTitledBorder("View problem"));
		bottomComponent.add(problemViewer.getComponent(), BorderLayout.CENTER);
		return bottomComponent;
	}

}
