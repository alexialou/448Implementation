package main;

import java.io.File;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AddClassification;


public class test {

	public static void main(String[] args) {
		try{
			DataSource ds = new DataSource("/Volumes/Disk/MacBook/mid3.csv");
			Instances test = ds.getDataSet();

			DataSource original = new DataSource("/Volumes/Disk/MacBook/PhotoObjAllTop3000.csv");
			Instances bigTest = original.getDataSet();

			InstanceComparator comp = new InstanceComparator();
			comp.setIncludeClass(false);

			FastVector attrVal = new FastVector();
			attrVal.addElement("irrelevant");
			attrVal.addElement("relevant");
			Attribute classAttr = new Attribute("class", attrVal);
			test.insertAttributeAt(classAttr, test.numAttributes());
			test.setClass(test.attribute("class"));
			bigTest.insertAttributeAt(classAttr, bigTest.numAttributes());
			bigTest.setClass(bigTest.attribute("class"));

			int count = 0;
			for(int j = 0; j < bigTest.numInstances(); j++){
				bigTest.instance(j).setClassValue("irrelevant");
				if(bigTest.instance(j).value(bigTest.attribute("obj")) <= 60){
					bigTest.instance(j).setClassValue("relevant");
					count++;
				}
			}

			

//			for(int i = 0; i < test.numInstances(); i++){
//				for(int j = 0; j < bigTest.numInstances(); j++){
//					if(comp.compare(test.instance(i), bigTest.instance(j))==0){
//						bigTest.instance(j).setClassValue("relevant");
//					}
//				}
//			}

			AddClassification ac = new AddClassification();
			ac.setSerializedClassifierFile(new File("/Volumes/Disk/MacBook/Desktop/output.Model"));
			ac.setOutputClassification(true);
			ac.setOutputErrorFlag(true);
			ac.setInputFormat(bigTest);
			bigTest = Filter.useFilter(bigTest, ac);
			while (bigTest.numAttributes()>3){
				bigTest.deleteAttributeAt(0);
			}
			CSVSaver saver = new CSVSaver();
			saver.setInstances(bigTest);
			saver.setFile(new File("/Volumes/Disk/MacBook/Desktop/testResult.csv"));
			saver.writeBatch();
			System.out.println(count);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
