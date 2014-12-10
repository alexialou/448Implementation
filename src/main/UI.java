package main;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import weka.core.Instances;


/**
 * entry class for the program
 * @author Alexia Lou
 *
 */
public class UI {

	public UI() {}

	public static void main(String[] args) {
		DataLoader dl = new DataLoader();
		ParameterDialog pd = new ParameterDialog();
		InstanceSelectionPanel isp = new InstanceSelectionPanel();
		InstanceSelectorDialog isd = new InstanceSelectorDialog(new JFrame(), isp);
		isd.setLocationByPlatform(true);

		JTextPane statText = new JTextPane();
		JTextPane treeText = new JTextPane();
		PredictionTablePanel ptp = new PredictionTablePanel();

		JSplitPane stats = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(statText), new JScrollPane(treeText));
		stats.setDividerLocation(180+stats.getInsets().top);
		
		JSplitPane all = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(ptp), stats);
		all.setDividerLocation(500 + all.getInsets().left);
		JFrame jf = new JFrame();
		jf.setContentPane(all);	
		jf.pack();
		jf.setSize(750, 450);
		isd.setLocationRelativeTo(jf);

		Instances fn;

		try {
			Instances data = dl.load();
			if(data != null){
				pd.showDialog();
				double[] para = pd.getResults();

				Dataset dataset = new Dataset(data);
				Clusterer c = new Clusterer((int) para[0], para[1]);
				SpaceExplorer expr = new SpaceExplorer(c,(int) para[2],para[3],(int)para[4]);
				Model tree = new Model();
				Instances discov = expr.getDiscovery(dataset.get("unexplored"));
				dataset.addToSample(discov);

				HashMap<String, LinkedList<Double>> prevBounds = null;
				HashMap<String, LinkedList<Double>> currBounds = null;

				isp.setInstances(dataset.get("sample"));
				int result = isd.showDialog();

				while(result == InstanceSelectorDialog.CONFIRM_OPTION) {
					int[] labelPos = isp.getSelectedInstances();
					int discovIndex = discov.numInstances();
					dataset.label(labelPos, discovIndex, c);
					dataset.resetSample();
					Instances trainData = dataset.get("train");
					
					//TODO
					System.out.println("train start:\t" + Calendar.getInstance().getTimeInMillis());
					tree.train(trainData);
					System.out.println("train end:\t" + Calendar.getInstance().getTimeInMillis());
					
					treeText.setText(tree.toString());
					statText.setText(tree.evaluate(trainData));
					ptp.display(tree.getPred(), trainData);
					jf.setVisible(true);	
					
					//TODO
					System.out.println("FN start:\t" + Calendar.getInstance().getTimeInMillis());
					fn = tree.getFalse("negative");
					Instances misClassified = expr.getMisclassified(dataset.get("unlabeled"), fn, dataset.totalRelevant());
					dataset.addToSample(misClassified);
					System.out.println("FN end:\t" + Calendar.getInstance().getTimeInMillis());

					//TODO
					System.out.println("boundary start:\t" + Calendar.getInstance().getTimeInMillis());
					currBounds = tree.getAllBoundaries();
					Instances boundaries = expr.getBoundarySamples(dataset.get("unlabeled"), prevBounds, currBounds);
					prevBounds = currBounds;
					dataset.addToSample(boundaries);
					System.out.println("boundary end:\t" + Calendar.getInstance().getTimeInMillis());

					//TODO
					System.out.println("disco start:\t" + Calendar.getInstance().getTimeInMillis());
					discov = expr.getDiscovery(dataset.get("unexplored"));
					dataset.addToSample(discov);
					System.out.println("disco end:\t" + Calendar.getInstance().getTimeInMillis());
					
					//TODO
					System.out.println("\tmiss:\t" + misClassified.numInstances());
					System.out.println("\tbound:\t" + boundaries.numInstances());
					System.out.println("\tdiscov: " + discov.numInstances());
					
					if(dataset.get("sample").numInstances() == 0) {
						JOptionPane.showMessageDialog(null, "No more data to label!");
						tree.exportModel();
						dataset.exportTrain();
						System.exit(0);;
					} else{
						isp.setInstances(dataset.get("sample"));
						isd.setPanel(isp);
						
						//TODO
						System.out.println("end: " + Calendar.getInstance().getTimeInMillis() + "\n");
						
						result = isd.showDialog();
					}
				}
				if(result == InstanceSelectorDialog.EXIT_OPTION) {
					tree.exportModel();
					dataset.exportTrain();
					System.exit(0);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			String msg = (e.getMessage() == null)? "null": e.getMessage();
			JOptionPane.showMessageDialog(new JFrame(), msg, "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

}
