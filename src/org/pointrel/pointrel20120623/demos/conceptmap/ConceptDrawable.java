package org.pointrel.pointrel20120623.demos.conceptmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.pointrel.pointrel20120623.core.Utility;

public class ConceptDrawable {
	public String uuid;
	public String concept;
	public Color color = Color.black;
	public Rectangle rectangle;
	
	public ConceptDrawable() {
		uuid = Utility.generateUUID("org.pointrel.SimpleConceptMap.ConceptDrawable");
	}
	
	public ConceptDrawable(String uuid, String concept, Color color, Rectangle rectangle) {
		this.uuid = uuid;
		this.concept = concept;
		this.color = color;
		this.rectangle = rectangle;
	}
	
	boolean inBound(Point point) {
		return rectangle.contains(point);
	}
	
	void draw(Graphics2D graphics) {
		graphics.setColor(color);
		graphics.fillOval((int)rectangle.getX(), (int)rectangle.getY(), (int)rectangle.getWidth(), (int)rectangle.getHeight());

		int xx = (int) rectangle.getWidth();
		int yy = (int) rectangle.getHeight();
		int w2 = graphics.getFontMetrics().stringWidth(concept) / 2;
		int h2 = graphics.getFontMetrics().getDescent();
		//g2d.fillRect(0, 0, xx, yy);
		//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
		graphics.setPaint(Color.red);
		graphics.drawString(concept, rectangle.x + xx / 2 - w2, rectangle.y + yy / 2 + h2);
	}
	
	void setLocation(Point point) {
		rectangle.setLocation(point);
	}
}