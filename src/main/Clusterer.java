package main;

import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;





/**
 * class for clustering the unexplored data space at each iteration 
 * @author Alexia Lou
 *
 */
public class Clusterer extends SimpleKMeans{
	private static final long serialVersionUID = 1701507268054109149L;
	
	private int k;	// cluster base
	private double l;	// level factor

	public Clusterer(int base, double factor) {
		super();
		this.k = (base<=5)? 20:base;
		this.l = (factor<=1)? 1.5:factor;	
	}
		

	/**
	 * sets parameters for the clusterer 
	 * @param set
	 * @return return the centroid of each cluster
	 * @throws Exception
	 */
	public Instances getCentroid(Instances set) throws Exception {
		Instances centroid = set;
		if(set.numInstances() > k){
			setNumClusters(k);
			setMaxIterations(10);
			setDontReplaceMissingValues(true);
			setPreserveInstancesOrder(true);
			buildClusterer(set);
			centroid = getClusterCentroids();
			k = (int) Math.ceil(k*l);
		}
		return centroid;
	}

	
	/**
	 * should only be called after buildClusterer(Instances)
	 * @param input
	 * @return cluster assignment of all Instance in input
	 * @throws Exception 
	 */
	public int[] getClusterAssignments(Instances input) throws Exception {
		int[] assignment = new int[input.numInstances()];
		if(input.numInstances() >= getNumClusters()) 
			for(int i = 0; i < assignment.length; i++) {
				assignment[i] = clusterInstance(input.instance(i));
			}
			
		else {
			for(int i = 0; i < assignment.length; i++)
				assignment[i] = 0;
		}
		return assignment;
	}


	/**
	 * should only be called after buildClusterer(Instances)
	 * @param input
	 * @return cluster assignment of input in set
	 * @throws Exception 
	 */
	public int getInstanceCluster(Instance input) throws Exception {
		return clusterInstance(input);
	}


	/**
	 * should only be called after buildClusterer(Instances)
	 * @param input
	 * @param clusNum
	 * @return all the instances in input that belongs to cluster clusNum
	 * @throws Exception
	 */
	public Instances getCluster(Instances input, int clusNum) throws Exception {
		Instances cluster = new Instances(input.stringFreeStructure());
		int[] assignments = getClusterAssignments(input);
		for(int i = 0; i < assignments.length; i++) {
			if(assignments[i] == clusNum)
				cluster.add(input.instance(i));
		}	
		return cluster;
	}

}
