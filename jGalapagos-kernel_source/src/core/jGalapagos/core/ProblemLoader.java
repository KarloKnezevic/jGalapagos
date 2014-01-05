package jGalapagos.core;

import java.io.Serializable;

import javax.swing.JComponent;

/**
 * 
 * @author Mihej Komar
 *
 */
public interface ProblemLoader {
	
	public JComponent getComponent();
	
	public Serializable loadProblem() throws Exception;
	
	public void fireReset();

}
