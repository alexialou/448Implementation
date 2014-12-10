package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

/**
 * UI class for passing user's parameter setting input
 * @author Macbook
 *
 */
public class ParameterDialog extends JDialog {

	private static final long serialVersionUID = 8296073925992627358L;

	private JLabel m_BaseLabel = new JLabel("cluster base:");
	private JLabel m_GrowthLabel = new JLabel("cluster growth factor:");
	private JLabel m_PaneltyLabel = new JLabel("FN panelty:");
	private JLabel m_DistLabel = new JLabel("FN distance:");
	private JLabel m_BoundLabel = new JLabel("max boundaries:");
	
	private JTextField m_BaseText = new JTextField(1);
	private JTextField m_GrowthText = new JTextField(1);
	private JTextField m_PaneltyText = new JTextField(1);
	private JTextField m_DistText = new JTextField(1);
	private JTextField m_BoundText = new JTextField(1);

	private JButton m_Confirm = new JButton("Confirm");

	private JPanel m_Panel = new JPanel();
	private GroupLayout m_Layout = new GroupLayout(m_Panel);
	private GroupLayout.SequentialGroup hGroup = m_Layout.createSequentialGroup();
	private GroupLayout.SequentialGroup vGroup = m_Layout.createSequentialGroup();

	private int m_Result;
	private String m_BaseResult;
	private String m_GrowthResult;
	private String m_PaneltyResult;
	private String m_DistResult;
	private String m_BoundResult;


	public static final int CONFIRM_OPTION = 0;

	public ParameterDialog() {
		super(new JFrame(), "ParameterDialog", ModalityType.DOCUMENT_MODAL);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		m_Confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = CONFIRM_OPTION;
				m_BaseResult = m_BaseText.getText().trim();
				m_GrowthResult = m_GrowthText.getText().trim();
				m_PaneltyResult = m_PaneltyText.getText().trim();
				m_DistResult = m_DistText.getText().trim();
				m_BoundResult = m_BoundText.getText().trim();
				setVisible(false);
			}
		});

		m_BaseLabel.setLabelFor(m_BaseText);
		m_BaseText.setToolTipText("cluster base (>5, default: 20)");
		
		m_GrowthLabel.setLabelFor(m_GrowthText);
		m_GrowthText.setToolTipText("cluster growth factor (>1, default: 1.1)");
		
		m_PaneltyLabel.setLabelFor(m_PaneltyText);
		m_PaneltyText.setToolTipText("number of sample collected about each false nagetive (>1, default: 10)");
		
		m_DistLabel.setLabelFor(m_DistText);
		m_DistText.setToolTipText("normalized distance around false nagetives (0+~0.1, default: 0.01)");
		
		m_BoundLabel.setLabelFor(m_BoundText);
		m_BoundText.setToolTipText("maximum number of samples collected around each boundary (>3, default: 10)");
		
		
		m_Layout.setAutoCreateContainerGaps(true);
		m_Layout.setAutoCreateGaps(true);

		hGroup.addGroup(m_Layout.createParallelGroup(Alignment.TRAILING).addComponent(m_BaseLabel).addComponent(m_GrowthLabel).addComponent(m_PaneltyLabel).addComponent(m_DistLabel).addComponent(m_BoundLabel));
		hGroup.addGroup(m_Layout.createParallelGroup().addComponent(m_BaseText).addComponent(m_GrowthText).addComponent(m_PaneltyText).addComponent(m_DistText).addComponent(m_BoundText));
		m_Layout.setHorizontalGroup(hGroup);

		vGroup.addGroup(m_Layout.createParallelGroup(Alignment.BASELINE).addComponent(m_BaseLabel).addComponent(m_BaseText));
		vGroup.addGroup(m_Layout.createParallelGroup(Alignment.BASELINE).addComponent(m_GrowthLabel).addComponent(m_GrowthText));
		vGroup.addGroup(m_Layout.createParallelGroup(Alignment.BASELINE).addComponent(m_PaneltyLabel).addComponent(m_PaneltyText));
		vGroup.addGroup(m_Layout.createParallelGroup(Alignment.BASELINE).addComponent(m_DistLabel).addComponent(m_DistText));
		vGroup.addGroup(m_Layout.createParallelGroup(Alignment.BASELINE).addComponent(m_BoundLabel).addComponent(m_BoundText));

		m_Layout.setVerticalGroup(vGroup);

		m_Panel.setLayout(m_Layout);

		setLayout(new BorderLayout());
		add(m_Confirm, BorderLayout.PAGE_END);
		add(m_Panel, BorderLayout.CENTER);
		setSize(300,230);
		setResizable(false);
		revalidate();
		repaint();
	}

	public double[] getResults() {
		double m_BaseDouble = (m_BaseResult!=null && !m_BaseResult.isEmpty() && m_BaseResult.matches("\\d*(\\.\\d+)*"))? Double.parseDouble(m_BaseResult):0;
		double m_GrowthDouble = (m_GrowthResult!=null && !m_GrowthResult.isEmpty() && m_GrowthResult.matches("\\d*(\\.\\d+)*"))? Double.parseDouble(m_GrowthResult):0;
		double m_PaneltyDouble = (m_PaneltyResult!=null && !m_PaneltyResult.isEmpty() && m_PaneltyResult.matches("\\d*(\\.\\d+)*"))? Double.parseDouble(m_PaneltyResult):0;
		double m_DistDouble = (m_DistResult!=null && !m_DistResult.isEmpty() && m_DistResult.matches("\\d*(\\.\\d+)*"))? Double.parseDouble(m_DistResult):0;
		double m_BoundDouble = (m_BoundResult!=null && !m_BoundResult.isEmpty() && m_BoundResult.matches("\\d*(\\.\\d+)*"))? Double.parseDouble(m_BoundResult):0;
		double[] result = {m_BaseDouble, m_GrowthDouble, m_PaneltyDouble, m_DistDouble, m_BoundDouble};
		return result;
	}

	public int showDialog() {
		m_Result = CONFIRM_OPTION;
		setVisible(true);
		return m_Result;
	}

}




