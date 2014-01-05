package jGalapagos.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * 
 * @author Mihej Komar
 *
 */
public class ImageComponent extends JComponent {
	
	private static final long serialVersionUID = 1L;
	private final BufferedImage image;
	
	public ImageComponent(String path) throws IOException {
		image = ImageIO.read(new File(path));
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(image, getInsets().left, getInsets().top, null);
	}
	
	@Override
	public Dimension getPreferredSize() {
		final int width = image.getWidth() + getInsets().left + getInsets().right;
		final int height = image.getHeight() + getInsets().top + getInsets().bottom;
		return new Dimension(width, height);
	}

}