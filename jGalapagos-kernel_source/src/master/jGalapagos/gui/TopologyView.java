package jGalapagos.gui;

import jGalapagos.master.Connection;
import jGalapagos.master.NodeContainer;
import jGalapagos.master.WorkDescription;
import jGalapagos.master.WorkDescription.NodeContainerListener;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

public class TopologyView extends JComponent {
	
	private static final long serialVersionUID = 1L;
	
	private final Map<NodeContainer, NodeContainerView> nodeContainerMap = new HashMap<NodeContainer, TopologyView.NodeContainerView>();
	private NodeContainerView selectedAlgorithmView;
	
	public TopologyView(final SelectedNodeContainerListener selectedNodeContainerListener, final WorkDescription workDescription) {
		NodeContainerListener nodeContainerListener = new NodeContainerListener() {
			
			@Override
			public void changed() {
				// add new
				for (NodeContainer nodeContainer : workDescription.getNodeContainerList()) {
					NodeContainerView nodeContainerView = nodeContainerMap.get(nodeContainer);
					if (nodeContainerView == null) {
						nodeContainerView = new NodeContainerView(nodeContainer);
						nodeContainerMap.put(nodeContainer, nodeContainerView);
					}
					for (Connection connection : nodeContainer.getConnectionList()) {
						NodeContainer destinationNodeContainer = connection.getNodeContainer();
						NodeContainerView destinationNodeContainerView = nodeContainerMap.get(destinationNodeContainer);
						if (destinationNodeContainerView == null) {
							destinationNodeContainerView = new NodeContainerView(destinationNodeContainer);
							nodeContainerMap.put(destinationNodeContainer, destinationNodeContainerView);
						}
					}
				}
				
				List<NodeContainer> toDelete = new ArrayList<NodeContainer>();
				for (NodeContainer nodeContainer : nodeContainerMap.keySet()) {
					if (!workDescription.getNodeContainerList().contains(nodeContainer)) {
						toDelete.add(nodeContainer);
					}
				}
				for (NodeContainer nodeContainer : toDelete) {
					nodeContainerMap.remove(nodeContainer);
					if(selectedAlgorithmView != null){
						if(selectedAlgorithmView.getNodeContainer() == nodeContainer){
							selectedAlgorithmView = null;
							selectedNodeContainerListener.selectedNodeContainer(null);
						}						
					}
				}
				
				repaint();
			}
		};
		workDescription.addNodeContainerListener(nodeContainerListener);
		
		final int[] lastPosition = new int[2];
		
		addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
            	for (NodeContainerView algorithmView : nodeContainerMap.values()) {
            		if (algorithmView.isInside(e.getX(), e.getY())) {
	                	if (selectedAlgorithmView != null) {
	                		selectedAlgorithmView.setSelected(false);
	                	}
	                	selectedAlgorithmView = algorithmView;
	                	selectedNodeContainerListener.selectedNodeContainer(selectedAlgorithmView.getNodeContainer());
	                	selectedAlgorithmView.setSelected(true);
	                	lastPosition[0] = e.getX() - selectedAlgorithmView.getX();
	                	lastPosition[1] = e.getY() - selectedAlgorithmView.getY();
	                	repaint();
	                	return;
	                }
            	}
            	
            	if (selectedAlgorithmView != null) {
            		selectedAlgorithmView.setSelected(false);
            	}
            	selectedAlgorithmView = null;
            	selectedNodeContainerListener.selectedNodeContainer(null);
            	repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter(){
            public void mouseDragged(MouseEvent e){
            	if (selectedAlgorithmView != null) {
            		final int newX = e.getX() - lastPosition[0];
            		final int newY = e.getY() - lastPosition[1];
            		selectedAlgorithmView.setPosition(newX, newY);
            	}
            	repaint();
            }
        });
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (!(g instanceof Graphics2D)) {
			throw new IllegalStateException("Graphics object is expected to be instance of Graphics2D.");
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		for (NodeContainerView from : nodeContainerMap.values()) {
			for (Connection connection : from.getNodeContainer().getConnectionList()) {
				NodeContainerView to = nodeContainerMap.get(connection.getNodeContainer());
				paintConnection(from, to, connection.getInterval(), g2d);
			}
		}
		
		for (NodeContainerView nodeContainerView : nodeContainerMap.values()) {
			if (selectedAlgorithmView != nodeContainerView) {
				nodeContainerView.paintNodeContainerView(g2d);
			}
		}
		
		if (selectedAlgorithmView != null) {
			selectedAlgorithmView.paintNodeContainerView(g2d);
		}
	}
	
	
	private static final int border = 5;
	private final Font valueFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	private final Color valueBackgroundColor = new Color(200, 200, 200, 255);
	private final Color valueFontColor = Color.BLACK;
	private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	
	private void paintConnection(NodeContainerView from, NodeContainerView to, int value, Graphics2D g) {
		String valueString = decimalFormat.format(value);
		double xFrom = from.getCenterX();
		double yFrom = from.getCenterY();
		double xTo = to.getCenterX();
		double yTo = to.getCenterY();
		int xFromInt = (int) xFrom;
		int yFromInt = (int) yFrom;

		double xMiddle = xFrom < xTo ? xFrom : xTo;
		double yMiddle = yFrom < yTo ? yFrom : yTo;
		xMiddle += Math.abs(xFrom - xTo)/2;
		yMiddle += Math.abs(yFrom - yTo)/2;
		double slope = (yTo - yFrom)/(xTo - xFrom);
		double perpSlopeAngle = Math.atan2(yTo - yFrom, xTo - xFrom) + Math.PI/2;
		double perpSlope = -(1/slope);
		final double k = 50;
		
		double xControl = xMiddle + k * Math.cos(perpSlopeAngle);
		double b = yMiddle - perpSlope*xMiddle ;
		int yControl = (int)(perpSlope*xControl + b);
		
		QuadCurve2D curve = new QuadCurve2D.Double();
		curve.setCurve(xFrom, yFrom, xControl, yControl, xTo, yTo);
		g.draw(curve);
		if (!to.isInside(xFromInt, yFromInt)) {
			int xArrow = 0, yArrow = 0;
			Point intersection = curveLengthFromEndIntersection(curve, to.getRadius()+1);
			xArrow = intersection.x;
			yArrow = intersection.y;
			int tangentPrecision = 10;
			Point outerPoint = curveLengthFromEndIntersection(curve, to.getRadius() + tangentPrecision);
			double angle = Math.atan2(outerPoint.getY() - intersection.getY(), outerPoint.getX() - intersection.getX());
			double angle1 = angle + Math.PI / 12.0;
			double angle2 = angle - Math.PI / 12.0;
			
			Polygon triangle = new Polygon();
			triangle.addPoint(xArrow, yArrow);
			triangle.addPoint(xArrow + (int) (Math.cos(angle1) * 20.0), yArrow + (int) (Math.sin(angle1) * 20.0));
			triangle.addPoint(xArrow + (int) (Math.cos(angle2) * 20.0), yArrow + (int) (Math.sin(angle2) * 20.0));
			g.fillPolygon(triangle);
			
		}
		
		Rectangle2D titleBounds = valueFont.getStringBounds(valueString, g.getFontRenderContext());
		int width = (int) titleBounds.getWidth() + 2 * border;
		int height = (int) titleBounds.getHeight() + 2 * border;
		Point curveMiddlePoint = getQuadCurvePoints(curve, 0.5);
		double xCurveMiddle = curveMiddlePoint.getX();
		double yCurveMiddle = curveMiddlePoint.getY();
		int x = (int) xCurveMiddle - width/2;
		int y = (int) yCurveMiddle - height/2;
		g.setColor(valueBackgroundColor);
		g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.setColor(valueFontColor);
        g.setFont(valueFont);
        g.drawString(valueString, x + border, y + height - border);
	}
	
	private Point getQuadCurvePoints(QuadCurve2D curve, double t){
		double xCurveMiddle = (1-t)*(1-t)*curve.getX1() + 2*t*(1-t)*curve.getCtrlX() + t*t*curve.getX2();
		double yCurveMiddle = (1-t)*(1-t)*curve.getY1() + 2*t*(1-t)*curve.getCtrlY() + t*t*curve.getY2();
		
		return new Point((int)xCurveMiddle,(int)yCurveMiddle);
	}
	
	private Point curveNodeintersection(QuadCurve2D curve, NodeContainerView node){
		
//		Point leftPoint = new Point((int)curve.getX1(), (int)curve.getY1());
//		double leftSlider = 0;
//		
//		Point rightPoint = new Point((int)curve.getX2(), (int)curve.getY2());
//		double rightSlider = 1;
//		
//		Point probePoint = new Point(0, 0);
//		double probeSlider = -1;
//		
//		while(leftPoint.distance(rightPoint) > 1){
//			probeSlider = leftSlider + ((rightSlider - leftSlider) / 2);
//			probePoint = getQuadCurvePoints(curve, probeSlider);
//			
//			if(node.isInside(probePoint.x, probePoint.y)){
//				rightPoint.setLocation(probePoint);
//				rightSlider = probeSlider;
//			}else{
//				leftPoint.setLocation(probePoint);
//				leftSlider = probeSlider;
//			}
//		}
//		return rightPoint;
		return curveLengthFromEndIntersection(curve, node.getRadius());
	}
	
	private Point curveLengthFromEndIntersection(QuadCurve2D curve, double length){
		
		Point leftPoint = new Point((int)curve.getX1(), (int)curve.getY1());
		double leftSlider = 0;
		
		Point rightPoint = new Point((int)curve.getX2(), (int)curve.getY2());
		double rightSlider = 1;
		
		Point probePoint = new Point(0, 0);
		double probeSlider = -1;
		
		while(leftPoint.distance(rightPoint) > 1){
			probeSlider = leftSlider + ((rightSlider - leftSlider) / 2);
			probePoint = getQuadCurvePoints(curve, probeSlider);
			
			if(probePoint.distance(curve.getX2(), curve.getY2()) < length){
				rightPoint.setLocation(probePoint);
				rightSlider = probeSlider;
			}else{
				leftPoint.setLocation(probePoint);
				leftSlider = probeSlider;
			}
		}
		return rightPoint;
	}
	
	private class NodeContainerView {
		
		private final NodeContainer nodeContainer;
		private final List<String> stringList = new ArrayList<String>();
		private int x = 50;
		private int y = 50;
		private int diametar = 200;
		private int height = 20;
		private boolean selected;
		
		private final Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
		private final Font valuesFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		
		private final Color titleBackgroundColor = new Color(20, 20, 150, 255);
		private final Color selectedTitleBackgroundColor = new Color(150, 10, 10, 255);
		private final Color valuesBackgroundColor = new Color(200, 200, 200, 255);
		private final Color selectedvaluesBackgroundColor = new Color(200, 200, 200, 255);
		private final Color titleFontColor = Color.WHITE;
		private final Color selectedTitleFontColor = Color.WHITE;
		private final Color valuesFontColor = Color.BLACK;
		
		private static final int border = 5;

		public NodeContainerView(NodeContainer nodeContainer) {
			this.nodeContainer = nodeContainer;
		}

		public NodeContainer getNodeContainer() {
			return nodeContainer;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
		
		public int getCenterX(){
			return x + Math.round(diametar/2);
		}
		
		public int getCenterY(){
			return y + Math.round(diametar/2);
		}
		
		public int getRadius(){
			return Math.round(diametar/2);
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@SuppressWarnings("unused")
		public int getWidth() {
			return diametar;
		}

		@SuppressWarnings("unused")
		public int getHeight() {
			return diametar;
		}
		
		public boolean isInside(int x, int y) {
			return Math.sqrt((getCenterX() - x)*(getCenterX() - x) + (getCenterY() - y)*(getCenterY() - y)) < diametar/2;
		}
		
		public void paintNodeContainerView(Graphics2D g) {
			Rectangle2D titleBounds = titleFont.getStringBounds(nodeContainer.toString(), g.getFontRenderContext());
			stringList.clear();
			stringList.add("Algorithm: " + nodeContainer.getAlgorithmName() == null ? "N/A" : nodeContainer.getAlgorithmName());
			stringList.add("Computer: " + (nodeContainer.getWorkerInformation() == null ? "N/A" : nodeContainer.getWorkerInformation().toString()));
			diametar = (int) titleBounds.getWidth() + 10;
			int titleHeight = (int) titleBounds.getHeight() + 2 * border;
			
			int valuesHeight = 2 * border;	
			height = titleHeight + valuesHeight;
			
	    	if (isSelected()) {
	    		g.setColor(selectedTitleBackgroundColor);
	    	} else {
	    		g.setColor(titleBackgroundColor);
	    	}
	    	
	        g.fillOval(x, y, diametar, diametar);
	        g.setColor(Color.BLACK);
	        g.drawOval(x, y, diametar, diametar);
	    	if (isSelected()) {
	    		g.setColor(selectedvaluesBackgroundColor);
	    	} else {
	    		g.setColor(valuesBackgroundColor);
	    	}

	        g.setFont(titleFont);
	        
	        int xTitlePosition = x + border;
	        int yTitlePosition = y + border + diametar/2;
	        g.drawString(nodeContainer.toString(), xTitlePosition, yTitlePosition);
	        g.setColor(valuesFontColor);
	        g.setFont(valuesFont);
		}
		
	}

}

