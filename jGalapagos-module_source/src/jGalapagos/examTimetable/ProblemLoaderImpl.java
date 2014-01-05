package jGalapagos.examTimetable;

import jGalapagos.core.ProblemLoader;
import jGalapagos.examTimetable.model.ConstantData;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import layout.SpringUtilities;

public class ProblemLoaderImpl implements ProblemLoader {
	
	private static final long serialVersionUID = 1L;
	
	private final JPanel panel;
	
	private final BrowseTextfield projectFile;
	private final BrowseTextfield byYearsFile;
	private final BrowseTextfield courseGroupsFile;
	private final BrowseTextfield studentGroupFile;
	private final BrowseTextfield coursePairsFile;
	
	public ProblemLoaderImpl() {
		panel = new JPanel(new BorderLayout());
		
		projectFile = new BrowseTextfield("modules/examTimetable/2010-2011-ljetni-mi/projekt.txt");
		byYearsFile = new BrowseTextfield("modules/examTimetable/2010-2011-ljetni-mi/poGodinama.txt");
		courseGroupsFile = new BrowseTextfield("modules/examTimetable/2010-2011-ljetni-mi/grupePredmeta.txt");
		studentGroupFile = new BrowseTextfield("modules/examTimetable/2010-2011-ljetni-mi/grupaStudenata.txt");
		coursePairsFile = new BrowseTextfield("modules/examTimetable/2010-2011-ljetni-mi/dijeljeniStudentiT.txt");
		
		JPanel configurationPanel = new JPanel(new SpringLayout());
		configurationPanel.add(new JLabel("Courses and terms"));
		configurationPanel.add(projectFile);
		configurationPanel.add(new JLabel("Courses by year"));
		configurationPanel.add(byYearsFile);
		configurationPanel.add(new JLabel("Course groups"));
		configurationPanel.add(courseGroupsFile);
		configurationPanel.add(new JLabel("Student groups"));
		configurationPanel.add(studentGroupFile);
		configurationPanel.add(new JLabel("Course pairs"));
		configurationPanel.add(coursePairsFile);
		SpringUtilities.makeCompactGrid(configurationPanel, 5, 2, 5, 5, 5, 5);

		panel.add(configurationPanel, BorderLayout.NORTH);
	}
	
	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public Serializable loadProblem() {
		ConstantData constantData = ConstantData.loadTextData(projectFile.getText(), byYearsFile.getText(), 
				courseGroupsFile.getText(), studentGroupFile.getText(), coursePairsFile.getText());
		return constantData;
	}

	@Override
	public void fireReset() {
		// TODO Auto-generated method stub
		
	}
	
}
