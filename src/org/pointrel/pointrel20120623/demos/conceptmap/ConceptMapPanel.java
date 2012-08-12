package org.pointrel.pointrel20120623.demos.conceptmap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import org.pointrel.pointrel20120623.core.Utility;

@SuppressWarnings("serial") 
class ConceptMapPanel extends JPanel implements MouseListener, MouseMotionListener {

	private final SimpleConceptMapApp simpleConceptMapApp;
	// TODO: Think more about this concept -- is this really mutable or not? When are new versions made? Is something changeable with list? Etc.
	ConceptMapVersion conceptMap;
	ConceptDrawable selected = null;
	Point down = null;
	Point offset = null;
	
	ConceptMapPanel(SimpleConceptMapApp simpleConceptMapApp) {
		this.simpleConceptMapApp = simpleConceptMapApp;
		conceptMap = new ConceptMapVersion(SimpleConceptMapApp.DefaultConceptMapUUID, Utility.currentTimestamp(), simpleConceptMapApp.workspace.getUser(), "", "", null);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		ConceptDrawable drawable1 = new ConceptDrawable();
		drawable1.rectangle = new Rectangle(20, 20, 100, 100);
		drawable1.concept = "test one";
		conceptMap.drawables.add(drawable1);
		ConceptDrawable drawable2 = new ConceptDrawable();
		drawable2.rectangle = new Rectangle(50, 50, 100, 100);
		drawable2.color = Color.yellow;
		drawable2.concept = "test two";
		conceptMap.drawables.add(drawable2);
	}
	
    @Override 
    public void paintComponent(Graphics g) {
         super.paintComponent(g); 
         Graphics2D graphics = (Graphics2D) g;
         graphics.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         
         for (ConceptDrawable drawable: conceptMap.drawables) {
        	 	drawable.draw(graphics);
         }
    }


	public void mouseClicked(MouseEvent arg0) {
		// Do nothing
	}

	public void mousePressed(MouseEvent e) {
		down = e.getPoint();
		for (int i = conceptMap.drawables.size() - 1; i >= 0; i--) {
			ConceptDrawable drawable = conceptMap.drawables.get(i);
			if (drawable.inBound(down)) {
				selected = drawable;
				offset = new Point((int)drawable.rectangle.getX() - down.x, (int)drawable.rectangle.getY() - down.y);
				// TODO: Mutating the concept map; maybe should be immutable?
				conceptMap.drawables.remove(i);
				conceptMap.drawables.add(selected);
				this.repaint();
				return;
			}
		}
		selected = null;
		down = null;
		offset = null;
	}

	public void mouseReleased(MouseEvent arg0) {
		if (selected != null) {
			// TODO: Getting away with mutable concept map here because changed version is quickly discarded
			ConceptMapVersion newConceptMapVersion = new ConceptMapVersion(conceptMap.documentUUID, Utility.currentTimestamp(), simpleConceptMapApp.workspace.getUser(), conceptMap.title, conceptMap.noteBody, conceptMap.uri);
			newConceptMapVersion.drawables.addAll(conceptMap.drawables);
			conceptMap = newConceptMapVersion;
			this.simpleConceptMapApp.saveConceptMap();
		}
		selected = null;
		down = null;
		offset = null;
	}

	public void mouseEntered(MouseEvent arg0) {
		// Do nothing
	}

	public void mouseExited(MouseEvent arg0) {
		// Do nothing
	}

	public void mouseDragged(MouseEvent e) {
		if (selected == null) return;
		// TODO: Constrain on screen
		selected.rectangle.setLocation(e.getX() + offset.x, e.getY() + offset.y);
		this.repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
		// Do nothing			
	}
}