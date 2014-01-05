package jGalapagos.examTimetable.selection;

import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;

public class SelectionFactory {
	
	public static Selection getInstance(RandomGenerator random, Configuration configuration) {
		String className = configuration.getString("class");
		if (className.equals(RouletteWheelSelection.class.getName())) {
			return new RouletteWheelSelection(random, configuration);
		} else if (className.equals(TournamentSelection.class.getName())) {
			return new TournamentSelection(random, configuration);
		} else {
			throw new IllegalArgumentException("Unknown Selection: " + className);
		}
	}

}
