package milestonetwo.controller;

import milestonetwo.entity.MetricEntity;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
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
	public Instances applyBalancing(Instances training, int balancingSelectionIndex, MetricEntity metricEntity) {
				
		this.metricEntity = metricEntity;
		this.training = training;
		
		if(balancingSelectionIndex == 0) return applyNoSampling();
		
		if(balancingSelectionIndex == 1) return applyOversampling();
		
		if(balancingSelectionIndex == 2) return applyUndersampling();

		if(balancingSelectionIndex == 3) return applySmote();

		return null;
	
	}
	
	private static Instances applyNoSampling() {
		
		metricEntity.setBalancing("No Sampling");	    
		return training;
	}
		
	private static Instances applyUndersampling() {
		
		metricEntity.setBalancing("Undersampling");
				
		SpreadSubsample  underSampling = new SpreadSubsample();
		try {
			underSampling.setInputFormat(training);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] opts = new String[]{ "-M", "1.0"};
		try {
			underSampling.setOptions(opts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(underSampling);
		
		Instances trainingUndersampling = null;
		try {
			return Filter.useFilter(training, underSampling);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trainingUndersampling;		
	}

	private static Instances applyOversampling(){
		
		metricEntity.setBalancing("Oversampling");
			
		float percentageMajorityClass = metricEntity.getPercentageMajorityClass();
				
		String[] optsOverSampling = new String[]{"-B", "1.0", "-Z", String.valueOf(2 * percentageMajorityClass)};
		
		Resample  overSampling = new Resample();
		try {
			overSampling.setOptions(optsOverSampling);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			overSampling.setInputFormat(training);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(overSampling);
			
		Instances trainingOverSampling = null;
		try {
			return Filter.useFilter(training, overSampling);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trainingOverSampling;
	}

	private static Instances applySmote() {
		
		metricEntity.setBalancing("Smote");
		
		SMOTE smote = new SMOTE(); 
		try {
			smote.setInputFormat(training);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		FilteredClassifier filteredClassifier = new FilteredClassifier();
		filteredClassifier.setFilter(smote);
				
		Instances trainingSmote = null;
		try {
			
			trainingSmote = Filter.useFilter(training, smote);
			return trainingSmote;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return trainingSmote; 
	}
	

}
