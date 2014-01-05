package jGalapagos.gui.prepare;

import jGalapagos.master.ModuleContainer;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkDescription.AlgorithmListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ConfigPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final JFileChooser fileChooser;
	private final Map<String, List<Object[]>> algorithmConfiguration = new HashMap<String, List<Object[]>>();
	private final WorkDescription workDescription;

	public ConfigPanel(ModuleContainer moduleContainer, final WorkDescription workDescription) {
		this.workDescription = workDescription;
		fileChooser = new JFileChooser(moduleContainer.getDirectory());
		
		final JButton loadButton = new JButton("Load...");
		loadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
			        File file = fileChooser.getSelectedFile();
			        loadConfiguration(file);
				 }
			}
		});

		final ConfigTableModel configTableModel = new ConfigTableModel();
		final JTable table = new JTable(configTableModel);

		final DefaultListModel listModel = new DefaultListModel();
		
		final JList algoritmList = new JList(listModel);
		algoritmList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		algoritmList.setPreferredSize(new Dimension(200, 200));
		algoritmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		algoritmList.setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void setSelectionInterval(int index0, int index1) {
				super.setSelectionInterval(index0, index1);
				final String algorithmName = (String) algoritmList.getSelectedValue();
				configTableModel.changeCurrentConfig(algorithmName);
			}
		});
		
		AlgorithmListener algorithmListener = new AlgorithmListener() {
			
			@Override
			public void changed() {
				listModel.clear();
				for (String algorithmName : workDescription.getAlgorithmConfiguration().keySet()) {
					listModel.addElement(algorithmName);
				}
				algoritmList.setPreferredSize(new Dimension(200, 200));
			}
		};
		workDescription.addAlgoithmListener(algorithmListener);
		
		JScrollPane algorithmListScrollPane = new JScrollPane(algoritmList);
		algorithmListScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), algorithmListScrollPane.getBorder()));
		
		JScrollPane textAreaScrollPane = new JScrollPane(table);
		textAreaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), textAreaScrollPane.getBorder()));
		table.getParent().setBackground(Color.WHITE);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(loadButton);
		
		setLayout(new BorderLayout());
		add(algorithmListScrollPane, BorderLayout.WEST);
		add(textAreaScrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		if (moduleContainer.getDefaultAlgorithmConfiguration() != null) {
			loadConfiguration(moduleContainer.getDefaultAlgorithmConfiguration());
		}
	}
	
	
	private void loadConfiguration(File file) {
		PropertiesConfiguration configuration;
		try {
			configuration = new PropertiesConfiguration(file);
		} catch (ConfigurationException e1) {
			JOptionPane.showMessageDialog(null, "Error while loading file", "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		String[] algorithms = configuration.getStringArray("algorithms");
		for (String algorithm : algorithms) {
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
			propertiesConfiguration.append(configuration.subset(algorithm));
			workDescription.getAlgorithmConfiguration().put(algorithm, propertiesConfiguration);
			algorithmConfiguration.put(algorithm, toObjectArray(propertiesConfiguration));
		}
		
		workDescription.fireAlgorithmChanged();
	}
	
	private List<Object[]> toObjectArray(PropertiesConfiguration propertiesConfiguration) {
		List<Object[]> objectArrayList = new ArrayList<Object[]>();
		@SuppressWarnings("unchecked")
		Iterator<Object> configIterator = propertiesConfiguration.getKeys();
		while (configIterator.hasNext()) {
			String key = (String) configIterator.next();
			Object value = propertiesConfiguration.getProperty(key);
			objectArrayList.add(new Object[] { key, value });
		}
		return objectArrayList;
	}
	
	private class ConfigTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private String currentAlgorithmName;
		private List<Object[]> currentData; 
		private PropertiesConfiguration currentConfiguration;
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return (column == 1);
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			return currentData.get(row)[column];
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			Object[] rowData = currentData.get(row);
			String key = (String) rowData[0];
			rowData[1] = aValue;
			currentConfiguration.setProperty(key, aValue);
			if (key.equals("name")) {
				algorithmConfiguration.remove(currentAlgorithmName);
				workDescription.getAlgorithmConfiguration().remove(currentAlgorithmName);
				currentAlgorithmName = (String) aValue;
				algorithmConfiguration.put(currentAlgorithmName, currentData);
				workDescription.getAlgorithmConfiguration().put(currentAlgorithmName, currentConfiguration);
			}
			workDescription.fireAlgorithmChanged();
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public int getRowCount() {
			return (currentData == null) ? 0 : currentData.size();
		}
		
		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Key";
			} else {
				return "Value";
			}
		}
		
		public void changeCurrentConfig(String algorithmName) {
			currentAlgorithmName = algorithmName;
			currentData = algorithmConfiguration.get(algorithmName);
			currentConfiguration = workDescription.getAlgorithmConfiguration().get(algorithmName);
			fireTableDataChanged();
		}
		
	};

}
