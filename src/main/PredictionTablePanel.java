package main;

import weka.classifiers.evaluation.NominalPrediction;
import weka.core.FastVector;
import weka.core.Instances;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * UI class to display prediction result at each iteration
 * @author Alexia Lou
 *
 */
public class PredictionTablePanel extends JPanel{
	private static final long serialVersionUID = -5482039011706390311L;

	private JTable jt;
	private JButton jb_print = new JButton("Print");

	public PredictionTablePanel() {
		super();
		jb_print.setName("jb");
		jb_print.setToolTipText("print");
		setLayout(new BorderLayout());
		add(jb_print, BorderLayout.PAGE_START);
		jb_print.setEnabled(false);
		jb_print.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					print();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(new JFrame(), e1.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}


	@SuppressWarnings("serial")
	public void display(FastVector preds, Instances test) {
		for(Component c:this.getComponents()){
			if(c.getName()!="jb")
				this.remove(c);
		}

		NominalPrediction pred;
		Object data[][];
		String[] colName;

		if (preds == null) {
			JOptionPane.showMessageDialog(null, "No data available for display!");
			return;
		}

		// fill table
		colName = new String[test.numAttributes()+3];
		colName[0] = "No.";
		int i = 1;
		while(i < test.numAttributes()+1){
			colName[i] = test.attribute(i-1).name();
			i++;
		}
		colName[i] = "Predicted";
		colName[i+1] = "Error";

		data = new Object[preds.size()][];
		for (int j = 0; j < preds.size(); j++) {
			pred = (NominalPrediction) preds.elementAt(j);
			Object[] entry = new Object[test.numAttributes()+3];
			entry[0] = j;
			int k = 1;
			while(k < test.numAttributes()+1){
				if(test.attribute(k-1).isNumeric())
					entry[k] = test.instance(j).value(k-1);
				else
					entry[k] = test.instance(j).stringValue(k-1);
				k++;
			}
			entry[k] = test.classAttribute().value((int) pred.predicted());
			entry[k+1] = (pred.predicted() != pred.actual()) ? "+" : "";
			data[j] = entry;
		}

		jt = new JTable(data, colName){
			@Override
			public boolean isCellEditable(int rowIndex, int colIndex){
				return false;
			}
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				int rendererWidth = component.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
				return component;
			}
		};
		
		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BorderLayout());
		JTableHeader header = jt.getTableHeader();
		tablePane.add(header,BorderLayout.PAGE_START);
		tablePane.add(jt, BorderLayout.CENTER);
		add(tablePane, BorderLayout.CENTER);
		jb_print.setEnabled(true);
	}

	public void print() throws Exception {
		jt.print();
	}

}