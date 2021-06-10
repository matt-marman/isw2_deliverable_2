package milestonetwo.controller;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<Instances> applyFeatureSelection(int selection, Instances training, Instances testing, MetricEntity metricEntity) {
				
		this.metricEntity = metricEntity;
		this.training = training;
		this.testing = testing;

		if(selection == 0) return applyNoSelection();
		if(selection == 1) return applyBestFirst();
		
		return new ArrayList<>();
					
	}
	
	private ArrayList<Instances> applyNoSelection(){
		
		this.metricEntity.setFeatureSelection("No Selection");
		
		ArrayList<Instances> newSet = new ArrayList<>();
		newSet.add(0, this.training);
		newSet.add(1, this.testing);
		
		return newSet;
		
	}
	
	private ArrayList<Instances> applyBestFirst(){
		
		this.metricEntity.setFeatureSelection("Best First");
		
		//create AttributeSelection object
		AttributeSelection filter = new AttributeSelection();
		
		//create evaluator and search algorithm objects
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();
			
		//set the filter to use the evaluator and search algorithm
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(this.training);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Instances trainingFeatureSelection = null;
		try {
			trainingFeatureSelection = Filter.useFilter(this.training, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Instances testingFeatureSelection = null;
		try {
			testingFeatureSelection = Filter.useFilter(this.testing, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int numAttr = trainingFeatureSelection.numAttributes();
		
		trainingFeatureSelection.setClassIndex(numAttr - 1);
		testingFeatureSelection.setClassIndex(numAttr - 1);
				
		ArrayList<Instances> newSet = new ArrayList<>();
		newSet.add(0, trainingFeatureSelection);
		newSet.add(1, testingFeatureSelection);
		
		return newSet;
		
	}
	
}
