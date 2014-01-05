package jGalapagos.gui;

import javax.swing.JComponent;

/**
 * 
 * @author Mihej Komar
 *
 */
public interface Tab {
	
	public JComponent getContent() throws Exception;

	public String getName();
	
	public boolean isClosable();
	
	public void closeAction();

}
