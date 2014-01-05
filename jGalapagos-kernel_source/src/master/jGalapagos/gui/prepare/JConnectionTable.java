package jGalapagos.gui.prepare;

import jGalapagos.master.WorkDescription;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;


public class JConnectionTable extends JTable {
	

	private static final long serialVersionUID = 1L;
	private final ConnectionTableModel model;
	private final WorkDescription workDescription;
	
	public JConnectionTable(ConnectionTableModel connectionTableModel, String deleteButtonLabel, WorkDescription workDescription) {
		super(connectionTableModel);
		this.workDescription = workDescription;
		model = connectionTableModel;
		this.getColumn(deleteButtonLabel).setCellRenderer(new ButtonRenderer());
		this.getColumn(deleteButtonLabel).setCellEditor(new ButtonEditor());
	}
	
	
	
	private class ButtonRenderer extends JButton implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}
	
	private class ButtonEditor extends DefaultCellEditor {
		
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private String label;
		private boolean isPushed;

		public ButtonEditor() {
			super(new JCheckBox());
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			label = (value == null) ? "" : value.toString();
			button.setText(label);
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				int selectedRow = getSelectedRow();
				int editingRow = getEditingRow();
				if(selectedRow != -1){
					model.removeRow(editingRow);
					model.fireTableDataChanged();
					workDescription.fireNodeContainerChanged();
					fireEditingStopped();					
				}
			}
			isPushed = false;
			return new String(label);
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}

}
