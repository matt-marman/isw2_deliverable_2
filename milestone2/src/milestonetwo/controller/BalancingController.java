package milestonetwo.controller;

import java.io.File;
import java.io.IOException;

import milestonetwo.entity.MetricEntity;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

public class BalancingController {
	
	public String[] applyBalancing(String arffFile, int balancingSelectionIndex, String newArffFile, MetricEntity metricEntity) throws Exception {
				
		if(balancingSelectionIndex == 0) {
			
			metricEntity.setBalancing("No Sampling");
			String[] params = {arffFile, newArffFile + ".csv"};
		    
			return params;
			
		}
		
		if(balancingSelectionIndex == 1) return applyOversampling(newArffFile, arffFile, metricEntity);
		
		if(balancingSelectionIndex == 2) return applyUndersampling(newArffFile, arffFile, metricEntity);

		if(balancingSelectionIndex == 3) return applySmote(newArffFile, arffFile, metricEntity);

		return null;
	
	}
	
	private static String[] applyUndersampling(String newArffFile, String arffFile, MetricEntity metricEntity) throws Exception {
		
		metricEntity.setBalancing("Undersampling");
		
		DataSource source = new DataSource(arffFile);
		Instances data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		
		// Apply under sampling
		FilteredClassifier fc = new FilteredClassifier();
		SpreadSubsample  underSampling = new SpreadSubsample();
		
		underSampling.setInputFormat(data);
		String[] opts = new String[]{ "-M", "1.0"};
		underSampling.setOptions(opts);
		
		Instances dataUndersampling = Filter.useFilter(data, underSampling);
		
		return createFile(data, dataUndersampling, newArffFile, "Undersampling.arff", "Undersampling.csv");
		
	}

	private static String[] applyOversampling(String newArffFile, String arffFile, MetricEntity metricEntity) throws Exception {
		
		metricEntity.setBalancing("Oversampling");
		
		DataSource source = new DataSource(arffFile);
		Instances data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		
		// Apply under sampling
		FilteredClassifier fc = new FilteredClassifier();
		
		Resample  overSampling = new Resample();
		overSampling.setInputFormat(data);
		
		//float percentageMajorityClass = metricEntity.getPercentageMajorityClass();
		float percentageMajorityClass = 1;
		System.out.print("percentageMajorityClass " + percentageMajorityClass);
		
		String[] optsOverSampling = new String[]{"-B", "1.0", "-Z", String.valueOf(2 * percentageMajorityClass  * 100)};
		overSampling.setOptions(optsOverSampling);
		
		Instances dataOverSampling = Filter.useFilter(data, overSampling);
		
		return createFile(data, dataOverSampling, newArffFile, "Oversampling.arff", "Oversampling.csv");
	}

	public static String[] applySmote(String newArffFile, String arffFile, MetricEntity metricEntity) throws Exception {
		
		metricEntity.setBalancing("Smote");

		DataSource source = new DataSource(arffFile);
		Instances data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
				
		//create object of SMOTE
		SMOTE smote = new SMOTE(); 
		smote.setInputFormat(data);
		
		//Apply SMOTE on Dataset
		Instances dataSmote = Filter.useFilter(data, smote); 
		
		return createFile(data, dataSmote, newArffFile, "Smote.arff", "Smote.csv");		
	}

	public static String[] createFile(Instances data, Instances dataFiltered, String baseFile, String extensionArff, String extensionCSV) throws IOException {
		
		//create .arrf file
		ArffSaver s = new ArffSaver();
		s.setInstances(dataFiltered);
		s.setFile(new File(baseFile + extensionArff));
		s.writeBatch();
		
		// load ARFF
	    ArffLoader loader = new ArffLoader();
	    loader.setSource(new File(baseFile + extensionArff));
	    data = loader.getDataSet();

	    // save CSV
	    CSVSaver saver = new CSVSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(baseFile + extensionCSV));
	    saver.writeBatch();
	    	    
	    // sort the .csv file
	    //because balancing adds new istances at the end of file .csv
	    new SortCSV(baseFile + extensionCSV);
	    	    
	    String[] params = {baseFile + extensionArff, baseFile + extensionCSV};
	    
		return params;
	}
	
}
