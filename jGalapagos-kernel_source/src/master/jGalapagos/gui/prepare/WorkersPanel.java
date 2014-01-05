package jGalapagos.gui.prepare;

import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkDescription.WorkerInformationListener;
import jGalapagos.util.NetworkUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.table.AbstractTableModel;

import layout.SpringUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Mihej Komar
 *
 */
public class WorkersPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final WorkDescription workDescription;
	private final WorkersTableModel workersTableModel = new WorkersTableModel();
	private final JTextField listeningSocket;
	
	public WorkersPanel(final WorkDescription workDescription) throws IOException {
		this.workDescription = workDescription;
		listeningSocket = new JTextField(20);
		listeningSocket.setEditable(false);
		
		WorkerInformationListener workerInformationListener = new WorkerInformationListener() {
			
			@Override
			public void changed() {
				workersTableModel.fireTableDataChanged();
			}
		};
		workDescription.addWorkerInformationListener(workerInformationListener);
		
		JButton buttonConnectTo = new JButton("Connect to...");
		buttonConnectTo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String hosts = JOptionPane.showInputDialog(null, "Enter all recipients of UDP broadcast. Format:\nhost1:port1 host2:port2", "localhost:10000");
				if (hosts == null) return;
				List<InetSocketAddress> workerAddressList = new ArrayList<InetSocketAddress>();
				String[] hostStrings = hosts.split(" ");
				for (String hostString : hostStrings) {
					String[] data = hostString.split(":");
					InetSocketAddress inetSocketAddress = new InetSocketAddress(data[0], Integer.parseInt(data[1]));
					workerAddressList.add(inetSocketAddress);
				}
				try {
					workDescription.getMasterComunicator().connectToWorkers(workerAddressList);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error while connecting to workers", "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		JButton buttonUpdateStatus = new JButton("Update status");
		buttonUpdateStatus.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				workDescription.getMasterComunicator().updateImplStatus();
			}
		});
		
		JButton buttonUpdateImplementations = new JButton("Update implementations");
		buttonUpdateImplementations.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				workDescription.getMasterComunicator().updateImpl();
			}
		});
		
		JButton buttonChangeListening = new JButton("Change");
		buttonChangeListening.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new ListeningForWorkersWindow();
			}
		});
		
		JPanel listeningPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		listeningPortPanel.add(new JLabel("Listening on:"));
		listeningPortPanel.add(listeningSocket);
		listeningPortPanel.add(buttonChangeListening);
		
		JTable table = new JTable(workersTableModel);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		centerPanel.add(listeningPortPanel, BorderLayout.SOUTH);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonConnectTo);
		buttonPanel.add(buttonUpdateStatus);
		buttonPanel.add(buttonUpdateImplementations);

		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		table.getParent().setBackground(Color.WHITE);
	}
	
	private class ListeningForWorkersWindow extends JFrame {
		
		private static final long serialVersionUID = 1L;
		private final Log log = LogFactory.getLog(ListeningForWorkersWindow.class);

		public ListeningForWorkersWindow() {
			setTitle("Listening for workers on socket...");
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLocation(200, 300);
			
			final JComboBox comboBox = new JComboBox(NetworkUtils.getInterfaceAddresses());
			final JTextField textfieldPort = new JTextField("10000");
			
			JPanel panel = new JPanel(new SpringLayout());
			panel.add(new JLabel("Network interface:"));
			panel.add(comboBox);
			panel.add(new JLabel("Port:"));
			panel.add(textfieldPort);
			SpringUtilities.makeCompactGrid(panel, 2, 2, 5, 5, 5, 5);
			
			JButton buttonConnect = new JButton("Listen");
			buttonConnect.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						InetAddress localInetAddress = (InetAddress) comboBox.getSelectedItem();
						int port = Integer.parseInt(textfieldPort.getText());
						workDescription.getMasterComunicator().setWaitingForConnections(port, localInetAddress);
						listeningSocket.setText(localInetAddress + ":" + port);
						dispose();
					} catch (BindException e1) {
						JOptionPane.showMessageDialog(null, "Port already in use", "Error", JOptionPane.WARNING_MESSAGE);
					} catch (Exception e1) {
						log.warn(e1, e1);
					}
				}
			});
			
			JButton buttonCancel = new JButton("Cancel");
			buttonCancel.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(buttonConnect);
			buttonPanel.add(buttonCancel);
			
			getContentPane().add(panel, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			pack();
			setVisible(true);
		}
		
	}
	
	private class WorkersTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return workDescription.getWorkerInformationList().size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return workDescription.getWorkerInformationList().get(rowIndex).getTcpConnection();
			} else if (columnIndex == 1) {
				int processors = workDescription.getWorkerInformationList().get(rowIndex).getAvailableProcessors();
				return processors == 0 ? "unknown" : processors;
			} else {
				return workDescription.getWorkerInformationList().get(rowIndex).getWorkerStatus();
			}
		}
		
		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "IP address";
			} else if (column == 1) {
				return "Processors"; 
			} else {
				return "Status";
			}
		}
		
	}

}