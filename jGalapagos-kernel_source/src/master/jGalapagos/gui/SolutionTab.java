package jGalapagos.gui;

import jGalapagos.core.Module;
import jGalapagos.core.Solution;

import java.io.Serializable;

import javax.swing.JComponent;

/**
 * 
 * @author Mihej Komar
 *
 */
public class SolutionTab implements Tab {
	
	private final Serializable problemDescription;
	private final Solution solution;
	private final Module core;

	public SolutionTab(Serializable problemDescription, Solution solution, Module core) {
		this.problemDescription = problemDescription;
		this.solution = solution;
		this.core = core;
	}

	@Override
	public boolean isClosable() {
		return true;
	}
	
	@Override
	public String getName() {
		return "Timetable";
	}
	
	@Override
	public JComponent getContent() throws Exception {
		return core.createSolutionViewer(solution, problemDescription);
	}
	
	@Override
	public void closeAction() { }

}
