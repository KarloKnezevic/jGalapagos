package jGalapagos.gui.prepare;

import jGalapagos.master.NodeContainer;

import javax.swing.table.DefaultTableModel;

public class ConnectionTableModel extends DefaultTableModel {
	
	private static final long serialVersionUID = 1L;
	private final NodeContainer nodeContainer;

	public ConnectionTableModel(NodeContainer nodeContainer) {
		this.nodeContainer = nodeContainer;
	}

	@Override
	public int getRowCount() {
		if(nodeContainer != null){
			return nodeContainer.getConnectionList().size();
		}else{
			return 0;
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}
	
	@Override
	public void setValueAt(Object val, int row, int col) {
		if(col == 1){
			
			if(val instanceof Integer){
				nodeContainer.getConnectionList().get(row).setInterval((Integer) val);
			}else if(val instanceof String){
				Integer newValue = Integer.parseInt((String)val);
				nodeContainer.getConnectionList().get(row).setInterval(newValue);
			}
			
		}
		// Indicate the change has happened:
		fireTableDataChanged();
	}
	

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return nodeContainer.getConnectionList().get(rowIndex).getNodeContainer();
		} else if(columnIndex == 1){ 
			return nodeContainer.getConnectionList().get(rowIndex).getInterval();
		} else{
			return "Delete";
		}
	}
	
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Destination node";
		} else if(column == 1) {
			return "Interval (in ms)";
		} else if(column == 2){
			return "Delete";
		} else {
			return "NO LABEL";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 1 || columnIndex == 2) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void removeRow(int row) {
		nodeContainer.getConnectionList().remove(row);
	}
}