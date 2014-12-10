package main;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.neighboursearch.LinearNNSearch;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;
import weka.filters.unsupervised.instance.SubsetByExpression;

/**
 * class to perform space exploration
 * @author Alexia Lou
 *
 */
public class SpaceExplorer {
	private Clusterer clusterer;
	private int f;	// number of sample retrieved for each false negative
	private double dist;	// normalized distance within which the samples around false negatives are chosen
	private int maxBound;

	public SpaceExplorer(Clusterer c, int falsePanelty, double distance, int bounds) {
		f = (falsePanelty<=1)? 10:falsePanelty;
		dist = (distance<=0 || distance>=0.1)? 0.01:distance;
		maxBound = (bounds<=3)? 10:bounds;
		clusterer = c;
	}


	/**
	 * 
	 * @param sampleArea: area to select samples from
	 * @return sample extracted from relevant object discovery stage
	 * @throws Exception
	 */
	public Instances getDiscovery(Instances sampleArea) throws Exception {
		Instances discov = new Instances(sampleArea.stringFreeStructure());
		LinearNNSearch neighbour = new LinearNNSearch(sampleArea);
		if(sampleArea.numInstances() <= 1)
			return discov;
		Instances centroid = clusterer.getCentroid(sampleArea);		
		for(int i = 0; i < centroid.numInstances(); i++){
			discov.add(neighbour.nearestNeighbour(centroid.instance(i)));
		}
		return discov;
	}


	/**
	 * 
	 * @param init: initial data set
	 * @param fn: false negative predictions
	 * @param discoSoFar: total number of relevant objects discovered in relevant object discovery stage
	 * @return sample extracted from mis-classified object exploration stage
	 * @throws Exception
	 */
	public Instances getMisclassified(Instances set, Instances fn, int discoSoFar) throws Exception {
		Instances misClass = new Instances(set.stringFreeStructure());
		if(fn == null || fn.numInstances() == 0)
			return misClass;
		clusterer.buildClusterer(set);;
		if (fn.numInstances() > discoSoFar) {	
			LinkedList<Integer> numCluster = new LinkedList<Integer>();
			for(int i = 0; i < fn.numInstances(); i++) {
				Integer fnClus = clusterer.getInstanceCluster(fn.instance(i));
				if (!numCluster.contains(fnClus))
					numCluster.add(fnClus);
			}
			for(int i = 0; i < numCluster.size(); i++) {
				Instances cluster = clusterer.getCluster(set, (int)numCluster.get(i));
				if(cluster.numInstances()<=f)
					misClass = appendInstances(misClass,cluster);
				else{
					Resample getF = new Resample();
					getF.setInputFormat(cluster);
					getF.setNoReplacement(true);
					getF.setSampleSizePercent((double)f*100/cluster.numInstances());
					Instances temp = Filter.useFilter(cluster, getF);
					misClass = appendInstances(misClass,temp);
				}
			}
		} else {
			for(int i = 0; i < fn.numInstances(); i++) {
				Instances inRange = getInstancesWithin(set, fn.instance(i), dist);
				
				if(inRange.numInstances()<=f)
					misClass = appendInstances(misClass,inRange);
				else{
					Resample getF = new Resample();
					getF.setInputFormat(inRange);
					getF.setNoReplacement(true);
					getF.setSampleSizePercent((double)f*100/inRange.numInstances());
					Instances temp = Filter.useFilter(inRange, getF);				
					misClass = appendInstances(misClass,temp);
				}
			}
		}
		return misClass;
	}



	/**
	 * 
	 * @param set: data set to select instances from
	 * @param prev: boundary discovered at previous iteration
	 * @param curr: boundary discovered at current iteration
	 * @return sample extracted from boundary exploration stage
	 * @throws Exception
	 */
	public Instances getBoundarySamples(Instances set, HashMap<String, LinkedList<Double>> prev, HashMap<String, LinkedList<Double>> curr) throws Exception {
		Instances result = new Instances(set.stringFreeStructure());
		int boundError = 3;
		Set<String> key = curr.keySet();
		for(String k: key){
			LinkedList<Double> currbound = curr.get(k);			
			if(!currbound.isEmpty()){
				Attribute attr = set.attribute(k);
				double min = set.kthSmallestValue(attr, 1);
				double max = set.kthSmallestValue(attr, set.numInstances());
				Iterator<Double> currItr = currbound.iterator();

				if(prev != null && !prev.get(k).isEmpty()){
					LinkedList<Double> prevbound = prev.get(k);	
					Iterator<Double> prevItr = prevbound.iterator();
					while(currItr.hasNext() && prevItr.hasNext()){
						double boundary = currItr.next();

						double pct = Math.abs(boundary-prevItr.next())/(max-min);
						int numSample = (int) (pct*(maxBound-boundError)+boundError);
						double attrMin = boundary-0.01*(max-min);
						double attrMax = boundary+0.01*(max-min);
						Instances temp = getInstancesWithin(set, attr.index(),attrMin,attrMax,numSample);
						result = appendInstances(result, temp);
					}
				}
				while(currItr.hasNext()){
					int numSample = boundError;
					double boundary = currItr.next();
					double attrMin = boundary-0.01*(max-min);
					double attrMax = boundary+0.01*(max-min);
					Instances temp = getInstancesWithin(set, attr.index(),attrMin,attrMax, numSample);
					result = appendInstances(result, temp);
				}
			}
		}
		return result;
	}


	/**
	 * 
	 * @param set: data set to select instances from
	 * @param attrIndex: attribute to apply the (min, max)
	 * @param min: lower bound of the required value range
	 * @param max: upper bound of the required value range
	 * @param k: number of instances to return
	 * @return return: k instances with attribute at attrIndex falls with in (min, max)
	 * @throws Exception
	 */
	private Instances getInstancesWithin(Instances set, int attrIndex, double min, double max, int k) throws Exception{
		Instances result = new Instances(set.stringFreeStructure());
		String expr = "(ATT"+(attrIndex+1)+">="+ new BigDecimal(min)+") and (ATT"+(attrIndex+1)+"<="+new BigDecimal(max)+")";
		SubsetByExpression getRange = new SubsetByExpression();
		getRange.setInputFormat(set);
		getRange.setExpression(expr);
		result = Filter.useFilter(set, getRange);
		if(result.numInstances() > 0){
			Resample getK = new Resample();
			getK.setInputFormat(result);
			getK.setNoReplacement(true);
			getK.setSampleSizePercent((double)k*100/result.numInstances());
			result = Filter.useFilter(result, getK);
		}
		return result;
	}



	/**
	 * 
	 * @param set: data set to select instances from
	 * @param centr: center instance
	 * @param inputDist: maximum distance required from centr
	 * @return: all instances with inputDist(normalized) from centr
	 */
	private Instances getInstancesWithin(Instances set, Instance centr, double inputDist) {
		Instances inRange = new Instances(set.stringFreeStructure());

		EuclideanDistance distance = new EuclideanDistance(set);
		distance.setDontNormalize(false);
		double[] dists = new double[set.numInstances()];
		for(int i = 0; i < set.numInstances(); i++) 
			dists[i] = distance.distance(centr, set.instance(i));
		double min = Utils.kthSmallestValue(dists, 1);
		double max = Utils.kthSmallestValue(dists, dists.length);
		for(double i : dists){
			if (i < (double)(max-min)*inputDist+min)
				inRange.add(set.instance(ArrayUtils.indexOf(dists, i)));
		}
		return inRange;
	}


	/**
	 * 
	 * @param inst1
	 * @param inst2
	 * @return: inst1 with inst2 appended at the end of it
	 */
	private Instances appendInstances(Instances inst1, Instances inst2) {
		if(inst1 == null)
			inst1 = inst2;
		else {
			for (int i = 0; i < inst2.numInstances(); i++)
				inst1.add(inst2.instance(i));
		}
		return inst1;
	}
}
