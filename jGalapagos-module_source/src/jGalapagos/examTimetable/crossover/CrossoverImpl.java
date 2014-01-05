package jGalapagos.examTimetable.crossover;

import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.model.VariableTimetable;
import jGalapagos.examTimetable.util.RandomGenerator;

import org.apache.commons.configuration.Configuration;


/**
 * 
 * @author Mihej Komar
 * @author Đorđe Grbić
 * 
 * Implementation of uniform crossover method.
 * Copies first parent to a child object and copies elements from second parent with probability of 0.5
 * if element doesn't violate hard constraints
 */
public final class CrossoverImpl implements Crossover {

	private final ConstantData constantData;
	private final RandomGenerator random;
	
	public CrossoverImpl(ConstantData constantData, RandomGenerator random, Configuration configuration) {
		this.constantData = constantData;
		this.random = random;
	}

	@Override
	public void crossover(final ConstantTimetable parent1, final ConstantTimetable parent2, final VariableTimetable child) {
		final int courseCount = constantData.getCourseCount();
		child.makeMeEqualAs(parent1);
		
		for (int courseIndex = 0; courseIndex < courseCount; courseIndex++) {
			int termIndex = parent2.getTermIndex(courseIndex);
			if (random.nextBoolean() && child.isTransferPossible(courseIndex, termIndex)) {
				child.setTermIndex(courseIndex, termIndex);
			}
		}
	}

}
