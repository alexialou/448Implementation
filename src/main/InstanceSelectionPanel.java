package main;

import weka.core.Instances;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.BorderFactory;


/**
 * UI class used to display extracted samples
 * @author Alexia Lou
 *
 */
public class InstanceSelectionPanel extends JPanel {
	private static final long serialVersionUID = 627131485290359194L;

	class InstanceTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -4152987434024338064L;
		private Instances m_Instances;
		private boolean [] m_Selected;

		public InstanceTableModel(Instances instances) {
			setInstances(instances);
		}

		public void setInstances(Instances instances) {
			m_Instances = instances;
			m_Selected = new boolean [m_Instances.numInstances()];
		}

		public int getRowCount() {
			return m_Selected.length;
		}

		public int getColumnCount() {
			return m_Instances.numAttributes()+2;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex >= 0 && rowIndex < m_Instances.numInstances()) {
				switch (columnIndex) {
				case 0:
					return new Integer(rowIndex);
				case 1:
					return new Boolean(m_Selected[rowIndex]);
				default:
					if(columnIndex >= 0 && columnIndex < m_Instances.numAttributes()+2)
						if(m_Instances.attribute(columnIndex-2).isNumeric())
							return m_Instances.instance(rowIndex).value(columnIndex-2);
						else
							return m_Instances.instance(rowIndex).stringValue(columnIndex-2);
					else
						return null;
				}
			}
			else
				return null;
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return new String("No.");
			case 1:
				return new String();
			default:
				if(columnIndex >= 0 && columnIndex < m_Instances.numAttributes() + 2)
					return new String(m_Instances.attribute(columnIndex-2).name());
				else
					return null;
			}
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				m_Selected[rowIndex] = ((Boolean) value).booleanValue();
				fireTableRowsUpdated(0, m_Selected.length);
			}
		}

		public Class<? extends Object> getColumnClass(int columnIndex) {
			return getValueAt(0, columnIndex).getClass();
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 1) 
				return true;
			return false;
		}

		public int [] getSelectedInstances() {
			int [] r1 = new int[getRowCount()];
			int selCount = 0;
			for (int i = 0; i < getRowCount(); i++) {
				if (m_Selected[i])
					r1[selCount++] = i;
			}
			int [] result = new int[selCount];
			System.arraycopy(r1, 0, result, 0, selCount);
			return result;
		}

		public void selectAll() {
			for (int i = 0; i < m_Selected.length; i++) 
				m_Selected[i] = true;
			fireTableRowsUpdated(0, m_Selected.length);
		}

		public void selectNone() {
			for (int i = 0; i < m_Selected.length; i++) 
				m_Selected[i] = false;
			fireTableRowsUpdated(0, m_Selected.length);
		}

		public void selectInvert() {
			for (int i = 0; i < m_Selected.length; i++) 
				m_Selected[i] = !m_Selected[i];
			fireTableRowsUpdated(0, m_Selected.length);
		}

		public void setSelectedInstances(boolean [] selected) throws Exception {
			if (selected.length != m_Selected.length)
				throw new Exception("Supplied array does not have the same number " + "of elements as there are instances!");
			for (int i = 0; i < selected.length; i++)
				m_Selected[i] = selected[i];
			fireTableRowsUpdated(0, m_Selected.length);
		}
	}

	private JButton m_SelectAll = new JButton("All");
	private JButton m_SelectNone = new JButton("None");
	private JButton m_SelectInvert = new JButton("Invert");
	@SuppressWarnings("serial")
	private JTable m_Table = new JTable(){ 
		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
			Component component = super.prepareRenderer(renderer, row, column);
			int rendererWidth = component.getPreferredSize().width;
			TableColumn tableColumn = getColumnModel().getColumn(column);
			tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
			return component;
		}
	};
	private InstanceTableModel m_Model;

	public InstanceSelectionPanel() {
		m_SelectAll.setEnabled(false);
		m_SelectAll.setToolTipText("Select All");
		m_SelectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Model.selectAll();
			}
		});

		m_SelectNone.setEnabled(false);
		m_SelectNone.setToolTipText("Select None");
		m_SelectNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Model.selectNone();
			}
		});

		m_SelectInvert.setEnabled(false);
		m_SelectInvert.setToolTipText("Select Invert");
		m_SelectInvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Model.selectInvert();
			}
		});

		m_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_Table.setColumnSelectionAllowed(false); 

		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		jp.setLayout(new GridLayout(1, 3, 5, 5));
		jp.add(m_SelectAll);
		jp.add(m_SelectNone);
		jp.add(m_SelectInvert);
		setLayout(new BorderLayout());
		add(jp, BorderLayout.PAGE_START);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BorderLayout());
		JTableHeader header = m_Table.getTableHeader();
		tablePane.add(header,BorderLayout.PAGE_START);
		tablePane.add(m_Table, BorderLayout.CENTER);

		add(tablePane, BorderLayout.CENTER);
	}

	public void setPreferredScrollableViewportSize(Dimension d) {
		m_Table.setPreferredScrollableViewportSize(d);
	}

	public void setInstances(Instances newInstances) {
		if (m_Model == null) {
			m_Model = new InstanceTableModel(newInstances);
			m_Table.setModel(m_Model);
			TableColumnModel tcm = m_Table.getColumnModel();
			tcm.getColumn(0).setMaxWidth(60);
			tcm.getColumn(1).setMaxWidth(tcm.getColumn(1).getMinWidth());
		} else {
			m_Model.setInstances(newInstances);
			m_Table.clearSelection();
		}
		m_SelectAll.setEnabled(true);
		m_SelectNone.setEnabled(true);
		m_SelectInvert.setEnabled(true);
		m_Table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		m_Table.revalidate();
		m_Table.repaint();
	}

	public int [] getSelectedInstances() {
		return (m_Model == null)? null: m_Model.getSelectedInstances();
	}

	public void setSelectedInstances(boolean[] selected) throws Exception {
		m_Model.setSelectedInstances(selected);
	}

	public TableModel getTableModel() {
		return m_Model;
	}

	public ListSelectionModel getSelectionModel() {
		return m_Table.getSelectionModel();
	}

}