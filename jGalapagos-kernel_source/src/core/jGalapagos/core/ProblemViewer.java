package jGalapagos.core;

import java.io.Serializable;

import javax.swing.JComponent;

/**
 * 
 * @author Mihej Komar
 *
 */
public interface ProblemViewer {
	
	public JComponent getComponent();
	
	public void updateProblem(Serializable problemDescription);

}
