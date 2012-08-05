package org.pointrel.pointrel20120623.demos.notetaker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pointrel.pointrel20120623.core.NewTransactionCallback;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;


// TODO: GUI issue too -- when to update? Maybe should show that a later version exists when looking at one
// TODO: Also, how to resolve edit conflicts?

/* 
 * Simple note taker application
 * Each "note" is a document with a constant UUID but with possibly
 * multiple versions with different contents or titles
 */
public class SimpleNoteTakerApp {

	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		// TODO: Fix user
		String user = "unknown_user@example.com";
		Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, archive, user);
		//Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, "http://twirlip.com/pointrel/", user);
		final JFrame frame = new JFrame(FrameNameBase);
		final SimpleNoteTakerApp app = new SimpleNoteTakerApp(workspace);
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
	
	final public static String FrameNameBase = "Simple Note Taker App";
	final public static String applicationIdentifier = SimpleNoteTakerApp.class.getCanonicalName();
	
	Workspace workspace;
	
	JPanel appPanel = new JPanel();
	
	DefaultListModel noteListModel = new DefaultListModel();
	JList noteList = new JList(noteListModel);
	JScrollPane noteListScrollPane = new JScrollPane(noteList);
	JTextArea textArea = new JTextArea();
	JScrollPane textAreaScrollPane = new JScrollPane(textArea);
	JSplitPane splitPane = new JSplitPane();
	JButton refreshListButton = new JButton("Refresh list");
	JButton newNoteButton = new JButton("New note");
	JButton renameNoteButton = new JButton("Rename note");
	JButton noteVersionsButton = new JButton("Note versions");
	JButton saveNoteButton = new JButton("Save note");
	JPanel listPanel = new JPanel();
	protected NewTransactionCallback newTransactionCallback;
	
	public SimpleNoteTakerApp(Workspace workspace) {
		this.workspace = workspace;
	}
	
	void saveItem(NoteVersion noteVersion) {
		String noteURI = workspace.addContent(noteVersion.toJSONBytes(), NoteVersion.ContentType);
		workspace.addSimpleTransaction(noteURI, "Updating note");
	}
	
	protected void refreshListButtonPressed() {
		HashMap<String,Integer> present = new HashMap<String,Integer>();
		for (int i = 0; i < noteListModel.getSize(); i++) {
			NoteVersion noteVersion = (NoteVersion) noteListModel.get(i);
			present.put(noteVersion.documentUUID, i);
		}
		for (Entry<String, CopyOnWriteArrayList<NoteVersion>> entry: this.notes.entrySet()) {
			CopyOnWriteArrayList<NoteVersion> versions = entry.getValue();
			if (versions.isEmpty()) continue;
			// Get the last one which is presumably latest; otherwise could sort by time
			// TODO: Multi-threading -- could this next line fail if size was decreased while looking up last note?
			NoteVersion latestNoteVersion = versions.get(versions.size() - 1);
			if (!present.containsKey(entry.getKey())) {
				System.out.println("Adding note version for: " + entry.getKey());
				noteListModel.addElement(latestNoteVersion);
			} else {
				// Update the note to the latest if it is not the currently selected one
				NoteVersion selectedNoteVersion = (NoteVersion)noteList.getSelectedValue();
				if (selectedNoteVersion == null || selectedNoteVersion.documentUUID != entry.getKey()) {
					int index = present.get(entry.getKey());
					System.out.println("updating note to latests for: " + entry.getKey());
					noteListModel.set(index, latestNoteVersion);
				}
			}
		}
	}
	
	public JPanel openGUI() {
		appPanel.setLayout(new BorderLayout());
		appPanel.add(splitPane, BorderLayout.CENTER);
		
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
		listPanel.add(noteListScrollPane);
		listPanel.add(refreshListButton);
		listPanel.add(newNoteButton);
		listPanel.add(renameNoteButton);
		listPanel.add(noteVersionsButton);
		listPanel.add(saveNoteButton);
		
		splitPane.setLeftComponent(listPanel);
		splitPane.setRightComponent(textAreaScrollPane);
		
		hookupActions();
		
		return appPanel;
	}

	void hookupActions() {
		refreshListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {refreshListButtonPressed();}});
		newNoteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {newNoteButtonPressed();}});
		renameNoteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {renameNoteButtonPressed();}});
		noteVersionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {noteVersionsButtonPressed();}});
		saveNoteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {saveNoteButtonPressed();}});
		
		noteList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {listSelectionChanged(event);}});
		
		newTransactionCallback = createNewTransactionCallback();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				workspace.addNewTransactionCallback(newTransactionCallback);
			}}
		);
	}
	
	final ConcurrentHashMap<String,CopyOnWriteArrayList<NoteVersion>> notes = new ConcurrentHashMap<String,CopyOnWriteArrayList<NoteVersion>>();
	
	protected NewTransactionCallback createNewTransactionCallback() {
		return new NewTransactionCallback(NoteVersion.ContentType) {
			
			@Override
			protected void insert(String resourceUUID) {
				byte[] noteVersionContent = workspace.getContentForURI(resourceUUID);
				if (noteVersionContent == null) {
					System.out.println("content not found for note version: " + resourceUUID);
				}
				final NoteVersion noteVersion;
				try {
					noteVersion = new NoteVersion(noteVersionContent);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				CopyOnWriteArrayList<NoteVersion> versions = notes.get(noteVersion.documentUUID);
				if (versions == null) {
					versions = new CopyOnWriteArrayList<NoteVersion>();
					System.out.println("================ about to add new note");
					notes.put(noteVersion.documentUUID, versions);
					// TODO: Should do some sort of repaint where multiple updates can get merged
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							refreshListButtonPressed();
						}});
				}
				System.out.println("================ about to add new note version");
				versions.add(noteVersion);
			}
		};
	}
	protected void listSelectionChanged(ListSelectionEvent event) {
		setCurrentlySelectedListItemInfo();
	}

	private void setCurrentlySelectedListItemInfo() {
		int index = noteList.getSelectedIndex();
		if (index == -1) {
			this.setTitle(FrameNameBase);
			return;
		}
		NoteVersion item = (NoteVersion) noteListModel.get(index);
		this.setTitle(FrameNameBase + ": " + item.title);
		textArea.setText(item.noteBody);
	}
	
	Component getTop() {
		return appPanel;
	}
	
	void setTitle(String newTitle) {
		JFrame frame = (JFrame) SwingUtilities.getRoot(appPanel);
		frame.setTitle(newTitle);
	}
	
	void newNoteButtonPressed() {
		String newTitle = JOptionPane.showInputDialog(getTop(), "Title for new note?");
		if (newTitle == null || newTitle.trim().length() == 0) return;
		if (newTitle.indexOf('\n') != -1) {
			JOptionPane.showMessageDialog(getTop(), "Title can not have a newline: " +  newTitle);
			return;
		}
		String uuid = Utility.generateUUID(applicationIdentifier);
		String timestamp = Utility.currentTimestamp();
		String userID = workspace.getUser();
		NoteVersion noteVersion = new NoteVersion(uuid, timestamp, userID, newTitle, "");
		saveItem(noteVersion);
		// String comment = "new note";
		// workspace.addToListForVariable(applicationIdentifier, noteVersion.documentUUID, comment);
		noteListModel.addElement(noteVersion);
		noteList.setSelectedIndex(noteListModel.size() - 1);
	}

	void renameNoteButtonPressed() {
		int index = noteList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(getTop(), "A list item must be selected first");
			return;
		}
		
		NoteVersion oldListItem = (NoteVersion) noteListModel.get(index);
		String oldTitle = oldListItem.title;
		String newTitle = JOptionPane.showInputDialog(getTop(), "New title for new note?", oldTitle);
		if (newTitle == null || newTitle.trim().length() == 0 || newTitle.equals(oldTitle)) return;
		if (newTitle.indexOf('\n') != -1) {
			JOptionPane.showMessageDialog(getTop(), "Title can not have a newline: " +  newTitle);
			return;
		}
		String timestamp = Utility.currentTimestamp();
		String userID = workspace.getUser();
		NoteVersion newListItem = new NoteVersion(oldListItem.documentUUID, timestamp, userID, newTitle, oldListItem.noteBody);
		noteListModel.set(index, newListItem);
		saveItem(newListItem);
		this.setTitle(FrameNameBase + ": " + newTitle);
	}
	
	void noteVersionsButtonPressed() {
		int index = noteList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(getTop(), "A list item must be selected first");
			return;
		}
		
		NoteVersion noteVersion = (NoteVersion) noteListModel.get(index);
		
		CopyOnWriteArrayList<NoteVersion> versions = notes.get(noteVersion.documentUUID);
		if (versions == null) {
			System.out.println("Problem reading versions for: " + noteVersion.documentUUID);
			return;
		}
		NoteVersion[] versionsArray = (NoteVersion[]) versions.toArray(new NoteVersion[versions.size()]);
		NoteVersion selection = (NoteVersion) JOptionPane.showInputDialog(getTop(), "Please choose a version", "List item versions", JOptionPane.QUESTION_MESSAGE, null, versionsArray , versions.get(0));
		System.out.println("You selected: " + selection);
		if (selection != null) {
			noteListModel.set(index, selection);
			setCurrentlySelectedListItemInfo();
		}
	}
	
	void saveNoteButtonPressed() {
		int index = noteList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(getTop(), "A list item must be selected first");
			return;
		}
		NoteVersion oldListItem = (NoteVersion) noteListModel.get(index);
		String text = textArea.getText();
		String timestamp = Utility.currentTimestamp();
		String userID = workspace.getUser();
		NoteVersion newListItem = new NoteVersion(oldListItem.documentUUID, timestamp, userID, oldListItem.title, text);
		saveItem(newListItem);
		// Wait for new transaction to show it in list
		// noteListModel.set(index, newListItem);
	}
}
