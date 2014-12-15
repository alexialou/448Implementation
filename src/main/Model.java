package main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.trees.SimpleCart;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SerializationHelper;


/**
 * Model trained using feedback from user
 * @author Alexia Lou
 *
 */
public class Model extends SimpleCart{
	private static final long serialVersionUID = 6456412713498497484L;

	private Instances train;
	private Instances test;
	private FastVector prediction;


	public Model() {
		super();
		setUsePrune(true);
	}

	/**
	 * 
	 * @param trainData
	 * @return a String description of the decision tree trained by trainData
	 * @throws Exception 
	 */
	public void train(Instances input) throws Exception {
		this.train = input;
		buildClassifier(train);
	}


	/**
	 * save a .model file in the same directory where the program runs
	 * @throws Exception
	 */
	public void exportModel() throws Exception{
		SerializationHelper.write("./output.model", this);
	}


	/**
	 * evaluate the model using testData
	 * @param testData
	 * @return evaluation stats
	 * @throws Exception
	 */
	public String evaluate(Instances testData) throws Exception {
		this.test = testData;
		Evaluation eval;
		eval = new Evaluation(this.train);
		eval.evaluateModel(this, test);
		prediction = eval.predictions();
		return eval.toSummaryString(false);
	}


	public FastVector getPred() {
		return prediction;
	}


	/**
	 * should only be called after calling evaluation (Instances testData)
	 * class attribute is of the form <class0, class1>, where class0 is considered as negative
	 * @param pred is one of "positive" and "negative"
	 * @return falsely predicted instances specified by pred
	 */
	public Instances getFalse(String pred) {
		Instances result = new Instances(test.stringFreeStructure());
		for(int i = 0; i < prediction.size(); i++){
			NominalPrediction normPred = (NominalPrediction) prediction.elementAt(i);
			if(pred.equalsIgnoreCase("positive") && normPred.actual() != normPred.predicted() && normPred.actual() == 0.0)
				result.add(test.instance(i));
			else if(pred.equalsIgnoreCase("negative") && normPred.actual() != normPred.predicted() && normPred.actual() == 1.0)
				result.add(test.instance(i));
			else {}

		}
		int classIndex = result.classIndex();
		if (classIndex != -1) {
			result.setClassIndex(-1);
			result.deleteAttributeAt(classIndex);
		}
		return result;
	}


	

	/**
	 * preconditions: 
	 * 	this is not null
	 * 	all attributes (except class attribute) are numeric
	 */
	public HashMap<String, LinkedList<Double>> getAllBoundaries(){	
		HashMap<String, LinkedList<Double>> hm = new HashMap<String, LinkedList<Double>>();
		// initialize a hash map of boundaries 
		// using attribute name as the key with a list of values representing the boundary values of each attributes
		for(int i = 0; i < train.numAttributes()-1; i++)
			hm.put(train.attribute(i).name(), new LinkedList<Double>());
		
		String[] nodes = parseNodes();
		for(String s: nodes){
			String[] parts = s.split("\\s+");
			if(!hm.get(parts[0]).contains(Double.parseDouble(parts[2])))
				hm.get(parts[0]).add(Double.parseDouble(parts[2]));
		} 
		return hm;
	}


	/**
	 * helper class to parse the decision tree string
	 * @return parsed nodes of the decision tree
	 */
	private String[] parseNodes() {
		String treeString = this.toString().replaceAll("\\(\\d+\\.\\d+/\\d+\\.\\d+\\)", "");
		treeString = treeString.replaceAll("\\:", "");
		String[] split = treeString.split("\\n+");
		split = (String[]) ArrayUtils.removeElement(split, split[0]);
		split = (String[]) ArrayUtils.removeElement(split, split[split.length-2]);
		split = (String[]) ArrayUtils.removeElement(split, split[split.length-1]);
		for(int i = 0; i < split.length; i++){
			Pattern pattern = Pattern.compile("\\|\\s*");
			Matcher matcher = pattern.matcher(split[i]);
			if(matcher.find())
				split[i] = split[i].replaceAll("(\\|\\s*)+","");
		}
		for(String s: split){
			if(s.trim().equals("irrelevant") || s.trim().equals("relevant") )
				split = (String[]) ArrayUtils.removeElement(split, s);
		}	
		return split;
	}

}
