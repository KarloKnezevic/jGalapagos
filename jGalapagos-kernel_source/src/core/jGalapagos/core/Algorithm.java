package jGalapagos.core;

import java.util.List;

/**
 * 
 * @author Mihej Komar
 *
 */
public interface Algorithm {
	
	public void runAlgorithm();
	
	public List<Solution> getSolutionsToSend(); 
	
	public void receiveForeignSolutions(List<Solution> solutionList);

}
