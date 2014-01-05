package jGalapagos.examTimetable;

import jGalapagos.core.Algorithm;
import jGalapagos.core.Module;
import jGalapagos.core.ProblemLoader;
import jGalapagos.core.ProblemViewer;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.model.ConstantData;
import jGalapagos.examTimetable.model.ConstantTimetable;
import jGalapagos.examTimetable.util.AlgorithmUtilities;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExamTimetableModule implements Module {
	
	private final Log log = LogFactory.getLog(ExamTimetableModule.class);
	
	@Override
	public ProblemLoader createProblemLoader() {
		return new ProblemLoaderImpl();
	}

	@Override
	public ProblemViewer createProblemViewer() {
		return new TimetableProblemViewer();
	}

	@Override
	public Algorithm createAlgorithm(Configuration configuration, Serializable problemDescription, AlgorithmStatistics statistics, AtomicBoolean stopRequested) {
		ConstantData constantData = (ConstantData) problemDescription;
		String algorithmClassName = configuration.getString("class");
		Algorithm algorithm = null;
		try {
			@SuppressWarnings("unchecked")
			Class<Algorithm> algorithmClass = (Class<Algorithm>) Class.forName(algorithmClassName);
			Constructor<Algorithm> constructor; constructor = algorithmClass.getConstructor(Configuration.class, ConstantData.class, AlgorithmStatistics.class, AtomicBoolean.class);
			algorithm = constructor.newInstance(configuration, constantData, statistics, stopRequested);
		} catch (Exception e) {
			log.error(e, e);
		}
		return algorithm;
	}
	
	@Override
	public JComponent createSolutionViewer(Solution solution, Serializable problemDescription) {
		return new SolutionView((ConstantTimetable) solution, (ConstantData) problemDescription);
	}
	
	@Override
	public Solution loadSolution(File file, Serializable problemDescription) {
		ConstantData constantData = (ConstantData) problemDescription;
		ConstantTimetable constantTimetable = AlgorithmUtilities.readTimetable(file.getAbsolutePath(), constantData).getConstantTimetable();
		return constantTimetable;
	}

}