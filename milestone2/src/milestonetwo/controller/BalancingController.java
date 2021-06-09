package milestonetwo.controller;

import milestonetwo.entity.MetricEntity;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

/**
 * This class applies:
 * 
 * No sampling
 * Oversampling
 * Udersampling
 * SMOTE
 * 
 * @author Mattia Di Battista
 *
 */
public class BalancingController {
	
	private static MetricEntity metricEntity;
	private static Instances training;
	
	/**
	 * @param training
	 * @param balancingSelectionIndex
	 * @param metricEntity
	 * @return
	 * @throws Exception
	 */
	
	@SuppressWarnings("static-access")
	public Instances applyBalancing(Instances training, int balancingSelectionIndex, MetricEntity metricEntity) throws Exception {
				
		this.metricEntity = metricEntity;
		this.training = training;
		
		if(balancingSelectionIndex == 0) return applyNoSampling();
		
		if(balancingSelectionIndex == 1) return applyOversampling();
		
		if(balancingSelectionIndex == 2) return applyUndersampling();

		if(balancingSelectionIndex == 3) return applySmote();

		return null;
	
	}
	
	private static Instances applyNoSampling() throws Exception {
		
		metricEntity.setBalancing("No Sampling");	    
		return training;
	}
		
	private static Instances applyUndersampling() throws Exception {
		
		metricEntity.setBalancing("Undersampling");
				
		SpreadSubsample  underSampling = new SpreadSubsample();
		underSampling.setInputFormat(training);
		
		String[] opts = new String[]{ "-M", "1.0"};
		underSampling.setOptions(opts);
		
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(underSampling);
		
		Instances trainingUndersampling = Filter.useFilter(training, underSampling);
		return trainingUndersampling;
		
	}

	private static Instances applyOversampling() throws Exception {
		
		metricEntity.setBalancing("Oversampling");
			
		float percentageMajorityClass = 1;
		String[] optsOverSampling = new String[]{"-B", "1.0", "-Z", String.valueOf(2 * percentageMajorityClass  * 100)};
		
		Resample  overSampling = new Resample();
		overSampling.setOptions(optsOverSampling);
		overSampling.setInputFormat(training);
		
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(overSampling);
			
		Instances trainingOverSampling = Filter.useFilter(training, overSampling);
		return trainingOverSampling;
	}

	private static Instances applySmote() throws Exception {
		
		metricEntity.setBalancing("Smote");
						
		SMOTE smote = new SMOTE(); 
		smote.setInputFormat(training);
		
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(smote);
		
		Instances trainingSmote = Filter.useFilter(training, smote); 
		return trainingSmote;
	}
	

}
