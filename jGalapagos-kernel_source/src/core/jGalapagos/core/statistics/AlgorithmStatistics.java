package jGalapagos.core.statistics;


import jGalapagos.core.Solution;

import java.io.Serializable;

public interface AlgorithmStatistics extends Serializable, Cloneable {
	
	/**
	 * Pokreće računanje statistike.
	 */
	public void startStatistic();

	/**
	 * Postavlja novo najbolje pronađeno rješenje
	 * 
	 * @param bestSolution Novo najbolje pronađeno rješenje
	 */
	public void addBestSolution(Solution bestSolution);
	
	/**
	 * Postavlja najnovije podatke o trenutačnoj populaciji
	 */
	public void addPopulationStat(double[] bestFitness, double[] worstFitness, double[] averageFitness, double[] standardDeviation, long iterationCount);
	
}
