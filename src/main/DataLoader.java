package main;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.gui.ConverterFileChooser;
import weka.gui.sql.SqlViewerDialog;



/**
 * UI class for loading the dataset
 * @author Alexia Lou
 *
 */
public class DataLoader extends JDialog {

	private static final long serialVersionUID = 7920154939962762282L;
	private JButton m_Local = new JButton("Open Local File");
	private JButton m_Remote = new JButton("Connect to Database");
	private int m_Result;

	public static final int LOCAL_OPTION = 0;
	public static final int REMOTE_OPTION = 1;

	public DataLoader(){
		super(new JFrame(), "DataLoader", ModalityType.DOCUMENT_MODAL);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		m_Local.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = LOCAL_OPTION;
				setVisible(false);
			}
		});

		m_Remote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = REMOTE_OPTION;
				setVisible(false);
			}
		});

		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		jp.setLayout(new GridLayout(1, 2, 10, 5));
		jp.add(m_Local);
		jp.add(m_Remote);

		setLayout(new BorderLayout());		
		add(jp, BorderLayout.CENTER);	
		setSize(350, 70);
		revalidate();
		repaint();
	}


	public Instances load(){
		Instances data = null;
		try{
			if(showDialog() == LOCAL_OPTION){
				ConverterFileChooser cfc = new ConverterFileChooser();
				int retFile = cfc.showOpenDialog(new JFrame());
				if (retFile == ConverterFileChooser.CANCEL_OPTION)
					System.exit(0);
				if (retFile == ConverterFileChooser.APPROVE_OPTION) 
					data = cfc.getLoader().getDataSet();
			}
			else {
				SqlViewerDialog sqd = new SqlViewerDialog(new JFrame());
				sqd.setVisible(true);
				InstanceQuery iq = new InstanceQuery();
				int result = sqd.getReturnValue();
				if(result == JOptionPane.OK_OPTION){
					iq.setDatabaseURL(sqd.getURL());
					iq.setUsername(sqd.getUser());
					iq.setPassword(sqd.getPassword());
					iq.connectToDatabase();
					if(iq.isConnected()){
						data = iq.retrieveInstances(sqd.getQuery());
					}
				}
				else
					System.exit(0);
			}
		} catch(Exception e){
			JOptionPane.showMessageDialog(new JFrame(), "Something went wrong", "Warning", JOptionPane.WARNING_MESSAGE);
			load();
		}
		return data;
	}


	private int showDialog() {
		m_Result = LOCAL_OPTION;
		setVisible(true);
		return m_Result;
	}

}
