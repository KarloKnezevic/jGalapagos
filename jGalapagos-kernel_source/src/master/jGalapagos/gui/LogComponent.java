package jGalapagos.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * 
 * @author Mihej Komar
 *
 */
public class LogComponent extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final List<String[]> messageList = new ArrayList<String[]>();
	private final DateFormat format = new SimpleDateFormat("HH:mm:ss");
	
	public LogComponent() {
		super(new BorderLayout());
		JTable table = new JTable(new MyTableModel());
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(200);
		add(new JScrollPane(table));
		table.getParent().setBackground(Color.WHITE);
	}
	
	public void addMessage(Date date, String message) {
		String[] data = new String[] { format.format(date), message };
		messageList.add(data);
	}
	
	private class MyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return messageList.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return messageList.get(rowIndex)[columnIndex];
		}
		
		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Date";
			} else {
				return "Message";
			}
		}
		
	}

}
