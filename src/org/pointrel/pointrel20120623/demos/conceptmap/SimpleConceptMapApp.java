package org.pointrel.pointrel20120623.demos.conceptmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.pointrel.pointrel20120623.core.NewTransactionCallback;
import org.pointrel.pointrel20120623.core.Workspace;


// Older TODO items:
// TODO: Make sure done writing before close?
// TODO: Background writing as SwingWorker?
// TODO: How to know when reading/writing and how if locks GUI or otherwise user knows what is happening?

// Current TODO items since using Workspace background loading:
// TODO: Has a bug where sometimes as start dragging will cancel it due to loading a new version of the map

public class SimpleConceptMapApp {
	public static String FrameNameBase = "Simple Concept Map";
	
	Workspace workspace;
	
	JPanel appPanel = new JPanel();
	ConceptMapPanel mapPanel = new ConceptMapPanel(this);
	JButton newConceptButton = new JButton("New concept...");
	
	// TODO: Fix this so it is settable
	public static String DefaultConceptMapUUID = "default_concept_map";
	public String conceptMapUUIDForApp = DefaultConceptMapUUID;

	private NewTransactionCallback newTransactionCallback;
	
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
		
		newTransactionCallback = createNewTransactionCallback();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				workspace.addNewTransactionCallback(newTransactionCallback);
			}}
		);
	}
	
	// TODO: Could track list of other concept maps that could be loaded
	
	protected NewTransactionCallback createNewTransactionCallback() {
		return new NewTransactionCallback(ConceptMapVersion.ContentType) {
			
			@Override
			protected void insert(String resourceUUID) {
				byte[] conceptMapVersionContent = workspace.getContentForURI(resourceUUID);
				if (conceptMapVersionContent == null) {
					System.out.println("content not found for concept map version: " + resourceUUID);
				}
				final ConceptMapVersion conceptMapVersion;
				try {
					conceptMapVersion = new ConceptMapVersion(conceptMapVersionContent);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				// TODO: Improve all this to reduce redundancy of making all objects for every concept map etc.
				if (!conceptMapUUIDForApp.equals(conceptMapVersion.documentUUID)) {
					System.out.println("==== conceptMapVersion is for another conceptMap");
					return;
				}
				System.out.println("================ about to use new conceptMapVersion");
				useNewConceptMapVersion(conceptMapVersion);
			}
		};
	}

	protected void useNewConceptMapVersion(ConceptMapVersion conceptMapVersion) {
		mapPanel.conceptMap = conceptMapVersion;
		mapPanel.repaint();
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
}
