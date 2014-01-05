package jGalapagos.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ExceptionView extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public ExceptionView(String title, Throwable exception) {
		setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(600, 350);
		
		Writer result = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(result);
	    exception.printStackTrace(printWriter);
		
		JTextArea stacktraceTextarea = new JTextArea(result.toString());
		stacktraceTextarea.setEditable(false);
		stacktraceTextarea.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		JScrollPane scrollPane = new JScrollPane(stacktraceTextarea);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), scrollPane.getBorder()));
		
		JButton buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonClose);
		
		JPanel mainContent = new JPanel(new BorderLayout());
		mainContent.add(scrollPane, BorderLayout.CENTER);
		mainContent.add(buttonPanel, BorderLayout.SOUTH);
		
		ContentComponent contentComponent = new ContentComponent(mainContent, "An error has occurred", exception.toString(), "data/images/warning.png");
		getContentPane().add(contentComponent);
		
		setVisible(true);
	}

}
