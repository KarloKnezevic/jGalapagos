package jGalapagos.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ContentComponent extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public ContentComponent(JComponent mainContent, String name, String description, String iconPath) {
		super(new BorderLayout());
		
		JLabel labelHeading = new JLabel(name);
		Font defaultFont = labelHeading.getFont();
		labelHeading.setFont(new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize()));
		
		JLabel labelDescription = new JLabel(description);
		labelDescription.setFont(new Font(defaultFont.getName(), Font.PLAIN, defaultFont.getSize()));
		
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textPanel.setBackground(Color.WHITE);
		textPanel.add(labelHeading, BorderLayout.NORTH);
		textPanel.add(labelDescription, BorderLayout.CENTER);
		
		JPanel headlinePanel = new JPanel(new BorderLayout());
		headlinePanel.setBackground(Color.WHITE);
		headlinePanel.add(textPanel, BorderLayout.CENTER);
		headlinePanel.add(new JSeparator(), BorderLayout.SOUTH);
		
		if (iconPath != null) {
			ImageComponent imageComponent = null;
			try {
				imageComponent = new ImageComponent(iconPath);
				imageComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			} catch (IOException e1) { }
			headlinePanel.add(imageComponent, BorderLayout.WEST);
		}
		
		add(headlinePanel, BorderLayout.NORTH);
		add(mainContent, BorderLayout.CENTER);
	}

}
