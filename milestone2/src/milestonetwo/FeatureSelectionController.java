package milestonetwo;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.core.converters.ArffSaver;
import java.io.File;
import weka.core.converters.ConverterUtils.DataSource;

public class FeatureSelectionController {
	
	public String applyFeatureSelection(String fileARFF, int selection) throws Exception {
		
		//switch = 0, return unfiltered .arff file
		//else return the filtered .arff file
		
		if(selection == 0) return fileARFF;
		
		//load dataset
		DataSource source = new DataSource(fileARFF);
		Instances dataset = source.getDataSet();
		
		//create AttributeSelection object
		AttributeSelection filter = new AttributeSelection();
		
		//create evaluator and search algorithm objects
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();
			
		//set the algorithm to search backward
		//search.setSearchBackwards(true);
				
		//set the filter to use the evaluator and search algorithm
		filter.setEvaluator(eval);
		filter.setSearch(search);
		//specify the dataset
		filter.setInputFormat(dataset);
		//apply
		Instances newData = Filter.useFilter(dataset, filter);
		//save
		ArffSaver saver = new ArffSaver();
		saver.setInstances(newData);
		
		//saver.setFile(new File(fileARFF));
		saver.setFile(new File("/home/mattia/Desktop/ingegneria_software_2/Falessi/isw2_deliverable_2/milestone2/test.arff"));
		saver.writeBatch();
		
		return "";
			
	}
	
}
