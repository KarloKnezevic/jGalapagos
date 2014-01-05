package jGalapagos.examTimetable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BrowseTextfield extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JTextField textField;
	private final JFileChooser fileChooser;
	
	public BrowseTextfield(String defaultFilePath) {
		super(new BorderLayout());
		File defaultFile = new File(defaultFilePath);
		textField = new JTextField(defaultFile.getAbsolutePath());
		fileChooser = new JFileChooser(defaultFile.getParent());
		
		JButton buttonBrowse = new JButton("Browse...");
		buttonBrowse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					textField.setText(file.getAbsolutePath());
				}
			}
		});
		
		add(buttonBrowse, BorderLayout.EAST);
		add(textField, BorderLayout.CENTER);
	}
	
	public String getText() {
		return textField.getText();
	}

}
