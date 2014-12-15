448Implementation
=================

Implementation of "Explore-by-Example: An Automatic Query Steering Framework for Interactive Data Exploration"[1]

Independent study project by Alexia Lou, under supervision of Professor Rachel Pottinger at University of British Columbia


Acceptable Input Format:  
1. local file format: Arff, C4.5, CSV, libsvm, svm light, Binary serialized instances, XRFF  
2. remote database server: MySQL  


Parameter Input (please also refer to highlighted area in the original paper for detail description):  
* cluster base: number of clusters to divide the dataset into at the first iteration  
* cluster growth factor:  factor by which the number of cluster grow at each iteration  
* FN penalty: number of extra samples retrieved around each false negative data object. (used when total number of relevant objects discovered during discovery phrase <= number of false negative predictions made at current iteration)  
* FN distance: minimum distance from false negative cluster center to retrieve samples from. (used when total number of relevant objects discovered during discovery phrase > number of false negative predictions made at current iteration)  
* max boundaries: maximum number of samples retrieved around each boundary  


Output:  
* at each iteration:  
  - all samples labeled so far  
  - prediction stats of current classifier  
  - decision tree built based on current training data  
* when exit the program (stored in the same directory as the program files):  
  - trained classifier (output.model)  
  - training set labeled by user (train.csv)  


Other:  
* The implementation is based on the optimized version of each space exploration phrase  
* "Error, not in CLASSPATH?":    
	please refer to the following web page for detail information  
	http://weka.wikispaces.com/Trying+to+add+JDBC+driver...+-+Error,+not+in+CLASSPATH%3F  
* Known cause(s) of exception:    
  1. if the original dataset has attribute named "class" or "cluster"  
  2. may throw exception for dataset containing non-numeric data  
	
	
Reference:  
1. Dimitriadou, Kyriaki, Olga Papaemmanouil, and Yanlei Diao. "Explore-by-Example: An Automatic Query Steering Framework for Interactive Data Exploration." (2014).
