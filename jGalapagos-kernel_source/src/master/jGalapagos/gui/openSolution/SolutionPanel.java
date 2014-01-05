package jGalapagos.gui.openSolution;

import jGalapagos.core.Module;
import jGalapagos.core.Solution;
import jGalapagos.gui.TabContainer;
import jGalapagos.gui.Tab;
import jGalapagos.gui.SolutionTab;
import jGalapagos.master.WorkDescription;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * @author Mihej Komar
 *
 */
public class SolutionPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public SolutionPanel(final Module module, final WorkDescription workDescription, final TabContainer tabContainer) {
		
		final JTextField solutionPath = new JTextField();
		JPanel solutionPathPanel = new JPanel(new BorderLayout());
		solutionPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Set solution path"), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		solutionPathPanel.add(new JLabel("Solution path "), BorderLayout.WEST);
		solutionPathPanel.add(solutionPath, BorderLayout.CENTER);
		
		JButton buttonLoad = new JButton("Load");
		buttonLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Serializable problemDescription = workDescription.getProblemDescription();
				if (problemDescription == null) {
					JOptionPane.showMessageDialog(null, "Problem description is not loaded");
					return;
				}
				File file = new File(solutionPath.getText());
				if (!file.exists()) {
					JOptionPane.showMessageDialog(null, "Solution file not found");;
					return;
				}
				Solution solution = module.loadSolution(file.getAbsoluteFile(), problemDescription);
				
				Tab tabSolution = new SolutionTab(problemDescription, solution, module);
				tabContainer.addTab(tabSolution);
			}
		});
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonLoad);
		
		
		setLayout(new BorderLayout());
		add(solutionPathPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
	}

}
