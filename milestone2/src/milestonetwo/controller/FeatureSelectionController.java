package milestonetwo.controller;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import milestonetwo.entity.MetricEntity;

/**
 * This class applies:
 * 
 * No selection 
 * Best first
 * 
 * @author Mattia Di Battista
 *
 */
public class FeatureSelectionController {
	
	private MetricEntity metricEntity;
	private Instances training;
	private Instances testing;
	
	/**
	 * @param selection
	 * @param training
	 * @param testing
	 * @param metricEntity
	 * @return
	 * @throws Exception
	 */
	
	public Instances [] applyFeatureSelection(int selection, Instances training, Instances testing, MetricEntity metricEntity) throws Exception {
				
		this.metricEntity = metricEntity;
		this.training = training;
		this.testing = testing;

		if(selection == 0) return applyNoSelection();
		if(selection == 1) return applyBestFirst();
		
		return null;
					
	}
	
	private Instances [] applyNoSelection(){
		
		this.metricEntity.setFeatureSelection("No Selection");
		
		Instances [] newSet = {this.training, this.testing};
		return newSet;
		
	}
	
	private Instances [] applyBestFirst() throws Exception{
		
		this.metricEntity.setFeatureSelection("Best First");
		
		//create AttributeSelection object
		AttributeSelection filter = new AttributeSelection();
		
		//create evaluator and search algorithm objects
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();
			
		//set the filter to use the evaluator and search algorithm
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(this.training);
		
		Instances trainingFeatureSelection = Filter.useFilter(this.training, filter);
		Instances testingFeatureSelection = Filter.useFilter(this.testing, filter);
		
		int numAttr = trainingFeatureSelection.numAttributes();
		
		trainingFeatureSelection.setClassIndex(numAttr - 1);
		testingFeatureSelection.setClassIndex(numAttr - 1);
				
		Instances [] newSet = {trainingFeatureSelection, testingFeatureSelection};
		return newSet;
		
	}
	
}
