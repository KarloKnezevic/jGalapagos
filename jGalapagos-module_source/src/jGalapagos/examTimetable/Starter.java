package jGalapagos.examTimetable;

import jGalapagos.core.Algorithm;
import jGalapagos.core.Solution;
import jGalapagos.core.statistics.AlgorithmStatistics;
import jGalapagos.examTimetable.model.ConstantData;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Starter {

	public static void main(String[] args) throws Exception {
		String algorithmName = "GaGeneration1";
		
		
		PropertiesConfiguration conf = new PropertiesConfiguration("data/algorithm.conf");
		Configuration algConf = conf.subset(algorithmName);
		
		ConstantData constantData = ConstantData.loadTextData(
				"data/2010-2011-ljetni-mi/projekt.txt",
				"data/2010-2011-ljetni-mi/poGodinama.txt",
				"data/2010-2011-ljetni-mi/grupePredmeta.txt",
				"data/2010-2011-ljetni-mi/grupaStudenata.txt",
				"data/2010-2011-ljetni-mi/dijeljeniStudentiT.txt"		
		);
		
		AlgorithmStatistics algorithmStatistics = new AlgorithmStatistics() {

			private static final long serialVersionUID = 1L;

			@Override
			public void startStatistic() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addPopulationStat(double[] bestFitness, double[] worstFitness,
					double[] averageFitness, double[] standardDeviation,
					long iterationCount) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void addBestSolution(Solution bestSolution) {
				System.out.println(bestSolution);
			}
		};
		
		ExamTimetableModule coreImpl = new ExamTimetableModule();
		Algorithm algorithm = coreImpl.createAlgorithm(algConf, constantData, algorithmStatistics, new AtomicBoolean());
		algorithm.runAlgorithm();
	}

}
