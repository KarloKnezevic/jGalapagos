package jGalapagos.core;

import java.io.Serializable;

/**
 * 
 * @author Mihej Komar
 *
 */
public interface Solution extends Serializable, Comparable<Solution> {
	
	public double[] getFitness();

}
