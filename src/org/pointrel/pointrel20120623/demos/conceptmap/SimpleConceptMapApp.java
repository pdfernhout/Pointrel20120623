package org.pointrel.pointrel20120623.demos.conceptmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.pointrel.pointrel20120623.core.TransactionVisitor;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;


// TODO: Make sure done writing before close
// TODO: Background writing and reading as SwingWorker
// TODO: How to know when reading/writing and how if locks GUI or otherwise user knows what is happening

public class SimpleConceptMapApp {
	public static String FrameNameBase = "Simple Concept Map";
	
	Workspace workspace;
	
	JPanel appPanel = new JPanel();
	ConceptMapPanel mapPanel = new ConceptMapPanel(this);
	JButton newConceptButton = new JButton("New concept...");
	
	private int ReloadFrequency_ms = 3000;

	// TODO: Fix this so it is settable
	public static String DefaultConceptMapUUID = "default_concept_map";
	public String conceptMapUUIDForApp = DefaultConceptMapUUID;
	
	public SimpleConceptMapApp(Workspace workspace) {
		this.workspace = workspace;
	}

	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		// TODO: Fix user
		String user = "unknown_user@example.com";
		Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, archive, user);
		//Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, "http://twirlip.com/pointrel/", user);
		final JFrame frame = new JFrame(FrameNameBase);
		final SimpleConceptMapApp app = new SimpleConceptMapApp(workspace);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPanel appPanel = app.openGUI();
				frame.setSize(600, 600);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(appPanel);
				frame.setVisible(true);
			}
		});
	}
	
	public JPanel openGUI() {
		appPanel.setLayout(new BorderLayout());
		appPanel.add(mapPanel, BorderLayout.CENTER);
		appPanel.add(newConceptButton, BorderLayout.SOUTH);
		
		hookupActions();
		return appPanel;
	}

	private void hookupActions() {
		newConceptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {newConceptButtonPressed();}});
		
		// Update every ten seconds
		ActionListener runnable = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkForAndLoadUpdatedConceptMap();
			}
		};
		runnable.actionPerformed(null);
		Timer timer = new Timer(ReloadFrequency_ms , runnable);
		timer.start();
	}

	protected void newConceptButtonPressed() {
		String name = JOptionPane.showInputDialog(appPanel, "Concept name?");
		if (name == null || name.length() == 0) return;
		// TODO: Ensure no newlines in name
		ConceptDrawable drawable = new ConceptDrawable();
		drawable.rectangle = new Rectangle(250, 250, 100, 100);
		drawable.color = Color.blue;
		drawable.concept = name;
		mapPanel.conceptMap.drawables.add(drawable);
		mapPanel.repaint();
		saveConceptMap();
	}
	
	void saveConceptMap() {
		byte[] jsonBytes = mapPanel.conceptMap.toJSONBytes();
		System.out.println(new String(jsonBytes));
		
		String conceptMapVersionURI = workspace.addContent(jsonBytes, ConceptMapVersion.ContentType);
		System.out.println("Writing concept map ==============================================");
		workspace.addSimpleTransaction(conceptMapVersionURI, "new concept map version");
		System.out.println("Just wrote new concept map: " + new String(jsonBytes));
		//System.out.println(newMap.drawables.size());
	}
	
	private void checkForAndLoadUpdatedConceptMap() {
		System.out.println("checkForAndLoadUpdatedConceptMap test");
		if (mapPanel.selected != null) return;
		System.out.println("checkForAndLoadUpdatedConceptMap proceeding");

		ConceptMapVersion newMap = loadLatestConceptMapForUUID(conceptMapUUIDForApp);
		if (newMap != null) {
			mapPanel.conceptMap = newMap;
			mapPanel.repaint();
		}
	}
	
	// TODO: Genericize the code below which is from SimpleNoteTakerApp, so the common patters is common, also to ChatApp
	// TODO: Should have an index
	class ConceptMapVersionCollector extends TransactionVisitor {
		final String encodedContentType = Utility.encodeContentType(ConceptMapVersion.ContentType);
		ArrayList<ConceptMapVersion> conceptMaps = new ArrayList<ConceptMapVersion>();
		final int maximumCount;
		final String documentUUID;
		
		ConceptMapVersionCollector(String documentUUID, int maximumCount) {
			this.documentUUID = documentUUID;
			this.maximumCount = maximumCount;
		}
		
		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
		
		public boolean resourceInserted(String resourceUUID) {
			if (!resourceUUID.endsWith(encodedContentType)) return false;
			byte[] conceptMapContent = workspace.getContentForURI(resourceUUID);
			ConceptMapVersion conceptMap;
			try {
				conceptMap = new ConceptMapVersion(conceptMapContent);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			System.out.println("conceptMap.documentUUID: " + conceptMap.documentUUID);
			if (conceptMap.documentUUID.equals(documentUUID)) {
				conceptMaps.add(conceptMap);
				if (maximumCount > 0 && conceptMaps.size() >= maximumCount) return true;
			}
			return false;
		}
	}
	
	class ConceptMapUUIDCollector extends TransactionVisitor {
		final String encodedContentType = Utility.encodeContentType(ConceptMapVersion.ContentType);
		final HashSet<String> conceptMapUUIDs = new HashSet<String>();
		final int maximumCount;
		
		ConceptMapUUIDCollector(int maximumCount) {
			this.maximumCount = maximumCount;
		}
		
		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
		
		public boolean resourceInserted(String resourceUUID) {
			if (!resourceUUID.endsWith(encodedContentType)) return false;
			byte[] conceptMapContent = workspace.getContentForURI(resourceUUID);
			ConceptMapVersion conceptMap;
			try {
				conceptMap = new ConceptMapVersion(conceptMapContent);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			conceptMapUUIDs.add(conceptMap.documentUUID);
			if (maximumCount > 0 && conceptMapUUIDs.size() >= maximumCount) return true;
			return false;
		}
	}
	
	// Finds most recently added version of concept map
	ConceptMapVersion loadLatestConceptMapForUUID(String uuid) {
		ArrayList<ConceptMapVersion> listItems = loadConceptMapVersionsForUUID(uuid, 1);
		if (listItems == null || listItems.isEmpty()) return null;
		return listItems.get(0);
	}
	
	// Finds all added versions of a concept map up to a maximumCount (use zero for all)
	ArrayList<ConceptMapVersion> loadConceptMapVersionsForUUID(String uuid, int maximumCount) {
		// TODO: Should create, maintain, and use an index
		String transactionURI = workspace.getLatestTransaction();
		ConceptMapVersionCollector visitor = new ConceptMapVersionCollector(uuid, maximumCount);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, visitor);
		if (visitor.conceptMaps.isEmpty()) return null;
		return visitor.conceptMaps;			
	}
	
	// Finds all uuids for concept maps up to a maximumCount (use zero for all)
	Set<String> loadConceptMapUUIDs(int maximumCount) {
		// TODO: Should create, maintain, and use an index
		String transactionURI = workspace.getLatestTransaction();
		ConceptMapUUIDCollector visitor = new ConceptMapUUIDCollector(maximumCount);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, visitor);
		if (visitor.conceptMapUUIDs.isEmpty()) return new HashSet<String>();
		return visitor.conceptMapUUIDs;			
	}
}
