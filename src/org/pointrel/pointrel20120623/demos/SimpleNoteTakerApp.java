package org.pointrel.pointrel20120623.demos;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

import org.pointrel.pointrel20120623.core.TransactionVisitor;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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
	
	public SimpleNoteTakerApp(Workspace workspace) {
		this.workspace = workspace;
	}
	
	class NoteVersion {
		final public static String ContentType = "text/vnd.pointrel.SimpleNoteTaker.ListItem.json";
		final public static String Version = "20120623.0.1.0";
		
		final String documentUUID;
		final String timestamp;
		final String userID;
		final String title;
		final String noteBody;
		
		NoteVersion(String documentUUID, String timestamp, String userID, String title, String noteBody) {
			this.documentUUID = documentUUID;
			this.timestamp = timestamp;
			this.userID = userID;
			this.title = title;
			this.noteBody = noteBody;
		}
		
		public NoteVersion(byte[] content) throws IOException {
			boolean typeChecked = false;
			boolean versionChecked = false;
			String documentUUID_Read = null;
			String timestamp_Read = null;
			String userID_Read = null;
			String title_Read = null;
			String noteBody_Read = null;
			
			JsonFactory jsonFactory = new JsonFactory();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
			JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);

			if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected data to start with an Object");
			}
			
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = jsonParser.getCurrentName();
				jsonParser.nextToken();

				if (fieldName.equals("type")) {
					String type = jsonParser.getText();
					if (!ContentType.equals(type)) {
						throw new RuntimeException("Expected type of " + ContentType + "  but got : " + type);
					}
					typeChecked = true;
				} else if (fieldName.equals("version")) {
					String version = jsonParser.getText();
					if (!Version.equals(version)) {
						throw new RuntimeException("Expected version of " + Version + "  but got : " + version);
					}
					versionChecked = true;
				} else if (fieldName.equals("documentUUID")) {
					documentUUID_Read = jsonParser.getText();
				} else if (fieldName.equals("timestamp")) {
					timestamp_Read = jsonParser.getText();
				} else if (fieldName.equals("userID")) {
					userID_Read = jsonParser.getText();
				} else if (fieldName.equals("title")) {
					title_Read = jsonParser.getText();
				} else if (fieldName.equals("noteBody")) {
					noteBody_Read = jsonParser.getText();
				} else {
					throw new IOException("Unrecognized field '" + fieldName + "'");
				}
			}
			jsonParser.close();
			
			if (!typeChecked) {
				throw new RuntimeException("Expected type of " + ContentType + "  but no type field");
			}
			
			if (!versionChecked) {
				throw new RuntimeException("Expected version of " + Version + "  but no version field");
			}
			
			documentUUID = documentUUID_Read;
			timestamp = timestamp_Read;
			userID = userID_Read;
			title = title_Read;
			noteBody = noteBody_Read;
		}
		
		public byte[] toJSONBytes() {
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				JsonFactory jsonFactory = new JsonFactory();
				JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);

				jsonGenerator.useDefaultPrettyPrinter();
				  
				jsonGenerator.writeStartObject();
				jsonGenerator.writeStringField("type", ContentType);
				jsonGenerator.writeStringField("version", Version);
				jsonGenerator.writeStringField("documentUUID", documentUUID);
				jsonGenerator.writeStringField("timestamp", timestamp);
				jsonGenerator.writeStringField("userID", userID);
				jsonGenerator.writeStringField("title", title);
				jsonGenerator.writeStringField("noteBody", noteBody);
				jsonGenerator.writeEndObject();
				jsonGenerator.close();
				return outputStream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public String toString() {
			return title + " | " + timestamp + " | " + userID;
		}
	}
	
	void saveItem(NoteVersion listItem) {
		String noteURI = workspace.addContent(listItem.toJSONBytes(), NoteVersion.ContentType);
		workspace.addSimpleTransactionToWorkspace(noteURI, "Updating note");
	}
	
	class NoteVersionCollector extends TransactionVisitor {
		final String encodedContentType = Utility.encodeContentType(NoteVersion.ContentType);
		ArrayList<NoteVersion> listItems = new ArrayList<NoteVersion>();
		final int maximumCount;
		final String documentUUID;
		
		NoteVersionCollector(String documentUUID, int maximumCount) {
			this.documentUUID = documentUUID;
			this.maximumCount = maximumCount;
		}
		
		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
		
		public boolean resourceInserted(String resourceUUID) {
			if (!resourceUUID.endsWith(encodedContentType)) return false;
			byte[] noteVersionContent = workspace.getContentForURI(resourceUUID);
			NoteVersion noteVersion;
			try {
				noteVersion = new NoteVersion(noteVersionContent);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			if (noteVersion.documentUUID.equals(documentUUID)) {
				listItems.add(noteVersion);
				if (maximumCount > 0 && listItems.size() >= maximumCount) return true;
			}
			return false;
		}
	}
	
	class NoteUUIDCollector extends TransactionVisitor {
		final String encodedContentType = Utility.encodeContentType(NoteVersion.ContentType);
		final HashSet<String> noteUUIDs = new HashSet<String>();
		final int maximumCount;
		
		NoteUUIDCollector(int maximumCount) {
			this.maximumCount = maximumCount;
		}
		
		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
		
		public boolean resourceInserted(String resourceUUID) {
			if (!resourceUUID.endsWith(encodedContentType)) return false;
			byte[] noteVersionContent = workspace.getContentForURI(resourceUUID);
			NoteVersion noteVersion;
			try {
				noteVersion = new NoteVersion(noteVersionContent);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			noteUUIDs.add(noteVersion.documentUUID);
			if (maximumCount > 0 && noteUUIDs.size() >= maximumCount) return true;
			return false;
		}
	}
	
	// Finds most recently added version of note
	NoteVersion loadListItemForUUID(String uuid) {
		ArrayList<NoteVersion> listItems = loadNoteVersionsForUUID(uuid, 1);
		if (listItems == null || listItems.isEmpty()) return null;
		return listItems.get(0);
	}
	
	// Finds all added versions of a note up to a maximumCount (use zero for all)
	ArrayList<NoteVersion> loadNoteVersionsForUUID(String uuid, int maximumCount) {
		// TODO: Should create, maintain, and use an index
		String transactionURI = workspace.getLatestTransactionForWorkspace();
		NoteVersionCollector visitor = new NoteVersionCollector(uuid, maximumCount);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, visitor);
		if (visitor.listItems.isEmpty()) return null;
		return visitor.listItems;			
	}
	
	// Finds all uuids for notes up to a maximumCount (use zero for all)
	Set<String> loadNoteUUIDs(int maximumCount) {
		// TODO: Should create, maintain, and use an index
		String transactionURI = workspace.getLatestTransactionForWorkspace();
		NoteUUIDCollector visitor = new NoteUUIDCollector(maximumCount);
		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(workspace, transactionURI, visitor);
		if (visitor.noteUUIDs.isEmpty()) return new HashSet<String>();
		return visitor.noteUUIDs;			
	}
	
	protected void refreshListButtonPressed() {
		Set<String> uuids = this.loadNoteUUIDs(0);
		noteListModel.clear();
		for (String uuid : uuids) {
			NoteVersion listItem = this.loadListItemForUUID(uuid);
			noteListModel.addElement(listItem);
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
		NoteVersion listItem = new NoteVersion(uuid, timestamp, userID, newTitle, "");
		saveItem(listItem);
		// String comment = "new note";
		// workspace.addToListForVariable(applicationIdentifier, listItem.documentUUID, comment);
		noteListModel.addElement(listItem);
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
		
		NoteVersion listItem = (NoteVersion) noteListModel.get(index);
		ArrayList<NoteVersion> versions = loadNoteVersionsForUUID(listItem.documentUUID, 0);
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
		noteListModel.set(index, newListItem);
		saveItem(newListItem);
	}
}
