package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * UI class used to return user's sample selection feedback
 * @author Alexia Lou
 *
 */
public class InstanceSelectorDialog extends JDialog {

	private static final long serialVersionUID = 7013565741449160720L;
	private InstanceSelectionPanel m_Panel;
	private JButton m_Confirm = new JButton("Confirm");
	private JButton m_Exit = new JButton("Exit");
	private int m_Result;
	public static final int EXIT_OPTION = 1;
	public static final int CONFIRM_OPTION = 0;

	public InstanceSelectorDialog(Frame parentFrame, InstanceSelectionPanel panel) {
		super(parentFrame, "InstanceSelectorDialog", ModalityType.DOCUMENT_MODAL);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		m_Panel = panel;
		m_Confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = CONFIRM_OPTION;
				setVisible(false);
			}
		});
		m_Exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Result = EXIT_OPTION;
				setVisible(false);
			}
		});

		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		jp.setLayout(new GridLayout(1, 2, 100, 5));
		jp.add(m_Confirm);
		jp.add(m_Exit);
		setLayout(new BorderLayout());		
		add(jp, BorderLayout.PAGE_END);
		m_Panel.setPreferredScrollableViewportSize(new Dimension(500,300));
		add(new JScrollPane(m_Panel), BorderLayout.CENTER);
		setSize(750, 450);
		revalidate();
		repaint();
	}

	public void setPanel(InstanceSelectionPanel panel) {
		m_Panel = panel;
	}

	public int showDialog() {
		m_Result = EXIT_OPTION;
		setVisible(true);
		return m_Result;
	}

}
