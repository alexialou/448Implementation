package main;


import java.io.File;
import java.util.LinkedList;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.AddCluster;
import weka.filters.unsupervised.instance.SubsetByExpression;


/**
 * class representing the data set
 *  
 * @author Alexia Lou
 *
 */
public class Dataset{

	private Instances sample;
	private Instances train;
	private Instances unlabeled;
	private Instances unexplored;
	private static InstanceComparator compara;
	private int discovSoFar;
	private LinkedList<Integer> exclude;


	public Dataset(Instances inst) throws Exception{
		unlabeled = new Instances(inst);
		unexplored = new Instances(inst);
		sample = new Instances(inst.stringFreeStructure());
		train = new Instances(inst.stringFreeStructure());

		FastVector attrVal = new FastVector();
		attrVal.addElement("irrelevant");
		attrVal.addElement("relevant");
		Attribute classAttr = new Attribute("class", attrVal);
		train.insertAttributeAt(classAttr, inst.numAttributes());

		discovSoFar = 0;
		exclude = null;
		compara = new InstanceComparator();
	}


	/**
	 * 
	 * @param type
	 * @return Instances specified by "type"
	 */
	public Instances get(String type) {
		if(type.equalsIgnoreCase("unlabeled")) 
			return unlabeled;
		else if(type.equalsIgnoreCase("unexplored")) 
			return unexplored;
		else if(type.equalsIgnoreCase("sample")) 
			return sample;
		else if(type.equalsIgnoreCase("train")) 
			return train;
		else 
			throw new IllegalArgumentException();
	}


	/**
	 * add more to Sample instances
	 * @param more
	 */
	public void addToSample(Instances more){
		if(more == null){}
		else if(!more.equalHeaders(sample))
			throw new IllegalArgumentException();
		else
			sample = copy(more, sample);
	}


	/**
	 * 
	 * @param feedback: user feedback
	 * @param the last discov samples are samples extracted in discover phrase
	 * @return a list of clusters numbers to be excluded from sampling in next iteration
	 * @throws Exception 
	 */
	public void label(int[] feedback, int discov, Clusterer clust) throws Exception{
		exclude = new LinkedList<Integer>();
		int discovIndex = sample.numInstances() - discov;

		Add objClass = new Add();
		String[] option = {"-T", "NOM", "-N", "class", "-L", "irrelevant, relevant"};
		objClass.setOptions(option);
		objClass.setInputFormat(this.sample);

		Instances temp = Filter.useFilter(this.sample, objClass);
		temp.setClassIndex(temp.attribute("class").index());
		for (int i : feedback) {				
			temp.instance(i).setClassValue("relevant");
			if(i >= discovIndex && i < sample.numInstances()) {
				exclude.add(i-discovIndex);
				discovSoFar++;
			}
		}
		for (int i = 0; i < temp.numInstances(); i++) {
			if (temp.instance(i).classIsMissing())
				temp.instance(i).setClassValue("irrelevant");
		}
		train = copy(train, temp);
		setUnexplored(clust);
	}


	/**
	 * 
	 * @return the total number of relevant object discovered at
	 * relevant object discovery stage, used to determine which 
	 * method to use at mis-classified object exploration stage.
	 */
	public int totalRelevant(){
		return discovSoFar;
	}


	/**
	 * reset sample for next iteration
	 * also remove already labeled instances from unlabeled and unexplored
	 * @throws Exception 
	 */
	public void resetSample() throws Exception{
		unlabeled = remove(unlabeled, sample);
		unexplored = remove(unexplored, sample);
		sample.delete();
	}


	/**
	 * exports the training set to the same directory where the program runs
	 * @throws Exception
	 */
	public void exportTrain() throws Exception {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(train);
		saver.setFile(new File("./train.csv"));
		saver.writeBatch();
	}


	/**
	 * cluster number starts with 0
	 * @param prev
	 * @param exclude
	 * @return next sampling area
	 * @throws Exception 
	 */
	private void setUnexplored(Clusterer clus) throws Exception {
		if (exclude != null && !exclude.isEmpty()){
			AddCluster clusFilter = new AddCluster();
			clusFilter.setClusterer(clus);
			clusFilter.setInputFormat(unexplored);
			unexplored = Filter.useFilter(unexplored, clusFilter);
			int clusterIndex = unexplored.attribute("cluster").index()+1;
			String expr = "not (ATT"+clusterIndex+" is "+"\'cluster"+(exclude.pop()+1)+"\')";
			while(!exclude.isEmpty())
				expr = expr.concat(" and not (ATT"+clusterIndex+" is "+"\'cluster"+(exclude.pop()+1)+"\')");
			SubsetByExpression sbe = new SubsetByExpression();
			sbe.setExpression(expr);
			sbe.setInputFormat(unexplored);
			unexplored = Filter.useFilter(unexplored, sbe);
			unexplored.deleteAttributeAt(clusterIndex-1);
		}
	}


	/**
	 * pre-condition: src and dest are of the same header format
	 * @param src
	 * @param dest
	 * @return the combination of src and dest
	 */
	private static Instances copy(Instances src, Instances dest){
		if (dest == null)
			return src;
		else if(src == null){}
		else{
			for(int i = 0; i < src.numInstances(); i++){
				if(!contains(dest, src.instance(i)))
					dest.add(src.instance(i));
			}
		}
		return dest;
	}


	/**
	 * pre-condition: set and oneInst are of the same header format
	 * @param set
	 * @param oneInst
	 * @return whether set contains oneInst
	 */
	private static boolean contains(Instances set, Instance oneInst) {
		for(int i = 0; i < set.numInstances(); i++){
			if(compara.compare(set.instance(i),oneInst) == 0)
				return true;
		}
		return false;
	}


	/**
	 * remove a subset of an instances
	 * @param src
	 * @param toRemove
	 * @return src with all occurrences of instances in toRemoved deleted
	 */
	private static Instances remove(Instances src, Instances toRemove){
		Instances result = new Instances(src.stringFreeStructure());
		for(int i = 0; i < src.numInstances(); i++){
			int count = 0;
			secondloop:
				for (int j = 0; j < toRemove.numInstances(); j++){
					if(compara.compare(src.instance(i), toRemove.instance(j)) == 0){
						count++;
						break secondloop;
					}
				}
			if(count == 0)
				result.add(src.instance(i));
		}
		return result;
	}


}
