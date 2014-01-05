package jGalapagos.core;


import jGalapagos.core.statistics.AlgorithmStatistics;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;

import org.apache.commons.configuration.Configuration;

/**
 * Sučelje koje je potrebno implementirati kako bi sustav za distribuiranje
 * mogao raditi s implementacijom. Razred koji implementira ovo sučelje se mora
 * zvati <code>CoreImpl</code> i nalaziti se u razredu
 * <code>hr.fer.zemris.distributedAlgorithms</code>.
 * 
 * @author Mihej Komar
 * 
 */
public interface Module {

	/**
	 * Kreira objekt unutar kojeg se definira grafička komponenta za učitavanje
	 * opisa problema.
	 * 
	 * @return GUI za učitavanje problema.
	 */
	public ProblemLoader createProblemLoader();
	
	public ProblemViewer createProblemViewer();

	/**
	 * Kreira konkretan primjerak razreda {@link Algorithm}. Svaki kreirani
	 * algoritam će biti pokrenut u zasebnoj dretvi pozivom metode
	 * <code>runAlgorithm</code>.
	 * 
	 * @param configuration
	 *            Konfiguracija predviđena za konretni primjerak razreda
	 * @param problemDescription
	 *            Opis problema.
	 * @param statistics
	 *            Statistika koju je potrebno obnavljati za vrijeme rada
	 *            algoritma. Prije nego algoritam započne koristiti statistiku
	 *            (prije prve iteracije algoritma), potrebno je pozvati metodu
	 *            <code>startStatistic</code>.
	 * @param stopRequested
	 *            Postavljeno na <code>true</code> kada je potrebno prekinuti
	 *            izvršavanje algoritma.
	 * @return Konkretan primjerak razreda.
	 */
	public Algorithm createAlgorithm(Configuration configuration, Serializable problemDescription, AlgorithmStatistics statistics, AtomicBoolean stopRequested);

	/**
	 * Grafička komponenta za prikaz rješenja.
	 * 
	 * @param solution
	 *            Rješenje koje se prikazuje.
	 * @param problemDescription
	 *            Opis problema.
	 */
	public JComponent createSolutionViewer(Solution solution, Serializable problemDescription);

	/**
	 * Učitavanje rješenja iz datoteke.
	 * 
	 * @param file
	 *            Datoteka iz koje se učitava rješenje.
	 * @param problemDescription
	 *            Opis problema.
	 * @return Učitano rješenje.
	 */
	public Solution loadSolution(File file, Serializable problemDescription);

}
