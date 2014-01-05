package jGalapagos.examTimetable.parentSelection;

import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;
/**
 * Factory that returns instance of a selection class taht implements <code>ParentSelection</code>
 * interface defined in algorithm configuration.
 */
public class ParentSelectionFactory {
	
	public static ParentSelection getInstance(RandomGenerator random, Configuration configuration) {
		String className = configuration.getString("class");
		if (className.equals(RouletteWheelParentSelection.class.getName())) {
			return new RouletteWheelParentSelection(random, configuration);
		} else if (className.equals(TournamentParentSelection.class.getName())) {
			return new TournamentParentSelection(random, configuration);
		} else {
			throw new IllegalArgumentException("Unknown ParentSelection: " + className);
		}
	}

}
