package org.pointrel.pointrel20120623.demos.wiki;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pointrel.pointrel20120623.core.NewTransactionCallback;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;

import com.petebevin.markdown.MarkdownProcessor;

// TODO: GUI issue too -- when to update? Maybe should show that a later version exists when looking at one with a content-out-of-date indicator?
// TODO: Also, how to resolve edit conflicts?
// TODO: Updated combo box with versions for currently selected wikiPage when get new transaction

/* 
 * Simple wikiPage taker application
 * Each "wikiPage" is a document with a constant UUID but with possibly
 * multiple versions with different contents or titles
 */
public class SimpleWikiApp {

	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		// TODO: Fix user
		String user = "unknown_user@example.com";
		Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, archive, user);
		//Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, "http://twirlip.com/pointrel/", user);
		final JFrame frame = new JFrame(FrameNameBase);
		final SimpleWikiApp app = new SimpleWikiApp(workspace);
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
	
	final public static String FrameNameBase = "Simple Wiki App";
	final public static String applicationIdentifier = SimpleWikiApp.class.getCanonicalName();
	
	Workspace workspace;
	
	JPanel appPanel = new JPanel();
	JSplitPane splitPane = new JSplitPane();
	
	JPanel listPanel = new JPanel();
	DefaultListModel wikiPageListModel = new DefaultListModel();
	JList wikiPageList = new JList(wikiPageListModel);
	JScrollPane wikiPageListScrollPane = new JScrollPane(wikiPageList);
	JButton newWikiPageButton = new JButton("New wiki page");
	JButton renameWikiPageButton = new JButton("Rename wiki page");
	JButton wikiPageVersionsButton = new JButton("Wiki page versions");
	
	JTabbedPane wikiTabbedPane = new JTabbedPane();
	
	JPanel wikiPageViewingPanel = new JPanel();
	JEditorPane wikiPageViewer = new JEditorPane();
	JScrollPane wikiPageViewerScrollPane = new JScrollPane(wikiPageViewer);
	
	JPanel wikiPageEditingPanel = new JPanel();
	DefaultComboBoxModel wikiPageVersionsComboBoxModel = new DefaultComboBoxModel();
	JComboBox wikiPageVersionsComboBox = new JComboBox(wikiPageVersionsComboBoxModel);
	JTextArea wikiPageEditorTextArea = new JTextArea();
	JScrollPane textAreaScrollPane = new JScrollPane(wikiPageEditorTextArea);
	DefaultComboBoxModel wikiPageContentTypeComboBoxModel = new DefaultComboBoxModel();
	JComboBox wikiPageContentTypeComboBox = new JComboBox(wikiPageContentTypeComboBoxModel);
	JButton saveWikiPageButton = new JButton("Save wiki page");

	protected NewTransactionCallback newTransactionCallback;
	
	public SimpleWikiApp(Workspace workspace) {
		this.workspace = workspace;
	}
	
	void saveWikiPageVersion(WikiPageVersion wikiPageVersion) {
		String wikiPageURI = workspace.addContent(wikiPageVersion.toJSONBytes(), WikiPageVersion.ContentType);
		workspace.addSimpleTransaction(wikiPageURI, "Updating wiki page");
	}
	
	volatile boolean listNeedsRefreshing = true;
	
	protected void refreshWikiPageList() {
		if (!listNeedsRefreshing) return;
		listNeedsRefreshing = false;
		HashMap<String,Integer> present = new HashMap<String,Integer>();
		for (int i = 0; i < wikiPageListModel.getSize(); i++) {
			WikiPageVersion wikiPageVersion = (WikiPageVersion) wikiPageListModel.get(i);
			present.put(wikiPageVersion.documentUUID, i);
		}
		// WikiPageVersion selectedWikiPageVersion = (WikiPageVersion)wikiPageList.getSelectedValue();
		//if (selectedWikiPageVersion != null) {
		//	System.out.println("Currently selected uuid = " + selectedWikiPageVersion.documentUUID);
		//}
		for (Entry<String, CopyOnWriteArrayList<WikiPageVersion>> entry: this.wikiPages.entrySet()) {
			CopyOnWriteArrayList<WikiPageVersion> versions = entry.getValue();
			if (versions.isEmpty()) continue;
			// Get the last one which is presumably latest; otherwise could sort by time
			// TODO: Multi-threading -- could this next line fail if size was decreased while looking up last wikiPage?
			WikiPageVersion latestWikiPageVersion = versions.get(versions.size() - 1);
			if (!present.containsKey(entry.getKey())) {
				System.out.println("Adding wikiPage version for: " + entry.getKey());
				wikiPageListModel.addElement(latestWikiPageVersion);
				present.put(entry.getKey(), wikiPageListModel.size() - 1);
			} else {
				// Update the wikiPage to the latest, but does not reload the content if selected
				// if (selectedWikiPageVersion == null || !selectedWikiPageVersion.documentUUID.equals(entry.getKey())) {
				int index = present.get(entry.getKey());
				System.out.println("updating wikiPage to latest for: " + entry.getKey());
				wikiPageListModel.set(index, latestWikiPageVersion);
				// }
			}
		}
	}
	
	public JPanel openGUI() {
		appPanel.setLayout(new BorderLayout());
		appPanel.add(splitPane, BorderLayout.CENTER);
		
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
		listPanel.add(wikiPageListScrollPane);
		listPanel.add(newWikiPageButton);
		listPanel.add(renameWikiPageButton);
		listPanel.add(wikiPageVersionsButton);
		
		wikiPageVersionsComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, wikiPageVersionsComboBox.getPreferredSize().height));
		wikiPageContentTypeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, wikiPageContentTypeComboBox.getPreferredSize().height));
		
		wikiPageEditingPanel.setLayout(new BoxLayout(wikiPageEditingPanel, BoxLayout.PAGE_AXIS));
		wikiPageEditingPanel.add(wikiPageVersionsComboBox);
		wikiPageEditingPanel.add(textAreaScrollPane);
		wikiPageEditingPanel.add(wikiPageContentTypeComboBox);
		wikiPageEditingPanel.add(saveWikiPageButton);
		
		wikiPageContentTypeComboBoxModel.addElement("text/plain");
		wikiPageContentTypeComboBoxModel.addElement("text/html");
		wikiPageContentTypeComboBoxModel.addElement("text/markdown");
		wikiPageContentTypeComboBox.setSelectedIndex(0);
		
		wikiPageViewingPanel.setLayout(new BorderLayout());
		wikiPageViewingPanel.add(wikiPageViewerScrollPane, BorderLayout.CENTER);
		
		wikiPageViewer.setEditable(false);
		
		wikiTabbedPane.addTab("View", wikiPageViewingPanel);
		wikiTabbedPane.addTab("Edit", wikiPageEditingPanel);
				
		splitPane.setLeftComponent(listPanel);
		splitPane.setRightComponent(wikiTabbedPane);
		
		hookupActions();
		
		return appPanel;
	}

	void hookupActions() {
		newWikiPageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {newWikiPageButtonPressed();}});
		renameWikiPageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {renameWikiPageButtonPressed();}});
		wikiPageVersionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {wikiPageVersionsButtonPressed();}});
		saveWikiPageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {saveWikiPageButtonPressed();}});
		
		wikiPageList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {listSelectionChanged(event);}});
		
		wikiPageVersionsComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {wikiPageVersionsComboBoxSelectionChanged();}});
		
		wikiPageViewer.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {hyperlinkUpdateChanged(event);}});
		
		//wikiPageTypeComboBox.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {wikiPageTypeComboBoxSelectionChanged();}});
		
		newTransactionCallback = createNewTransactionCallback();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				workspace.addNewTransactionCallback(newTransactionCallback);
			}}
		);
	}
	
	//protected void wikiPageTypeComboBoxSelectionChanged() {
	//	// TODO Auto-generated method stub	
	//}

	protected void hyperlinkUpdateChanged(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				wikiPageViewer.setPage(event.getURL());
				// TODO: What about the content type?
			} catch (IOException ioe) {
				// Some warning to user
			}
		}
	}

	protected void wikiPageVersionsComboBoxSelectionChanged() {
		System.out.println("Combo box selection changed");
		WikiPageVersion wikiPageVersion = (WikiPageVersion) wikiPageVersionsComboBox.getSelectedItem();
		if (wikiPageVersion == null) {
			wikiPageEditorTextArea.setText("");
			wikiPageViewer.setText("");
			wikiPageContentTypeComboBox.setSelectedIndex(-1);
			return;
		} else {
			String text = wikiPageVersion.content;
			//if ("markdown".equals(this.wikiPageTypeComboBox.getSelectedItem())) {
			//	MarkDown markDown = new MarkDown();
			//	text = markDown.transform(text);
			//}
			wikiPageEditorTextArea.setText(text);
		}
		//WikiPageVersion wikiPageVersion = (WikiPageVersion) wikiPageListModel.get(index);
		this.setTitle(FrameNameBase + ": " + wikiPageVersion.title);
		String wikiPageContent = wikiPageVersion.content;
		wikiPageEditorTextArea.setText(wikiPageContent);
		String wikiPageContentType = wikiPageVersion.contentType;
		System.out.println("====== wiki page content type: " + wikiPageContentType);
		wikiPageContentTypeComboBox.setSelectedItem(wikiPageContentType);
		if ("text/markdown".equals(wikiPageContentType)) {
			wikiPageContentType = "text/html";
			String transformedContent = transformContentWithMarkdown(wikiPageContent);
			wikiPageContent = "<html><body>" + transformedContent + "</body></html>";
		}
		// TODO: for testing
		// wikiPageContentType = "text/plain";
		wikiPageViewer.setContentType(wikiPageContentType);
		wikiPageViewer.setText(wikiPageContent);
	}

	protected String transformContentWithMarkdown(String wikiPageContent) {
		System.out.println("Transforming content");
		
		MarkdownProcessor markdownProcessor = new MarkdownProcessor();
		String transformedContent =  markdownProcessor.markdown(wikiPageContent);

		return transformedContent;
	}

	final ConcurrentHashMap<String,CopyOnWriteArrayList<WikiPageVersion>> wikiPages = new ConcurrentHashMap<String,CopyOnWriteArrayList<WikiPageVersion>>();
	
	protected NewTransactionCallback createNewTransactionCallback() {
		return new NewTransactionCallback(WikiPageVersion.ContentType) {
			
			@Override
			protected void insert(String resourceUUID) {
				byte[] wikiPageVersionContent = workspace.getContentForURI(resourceUUID);
				if (wikiPageVersionContent == null) {
					System.out.println("content not found for wikiPage version: " + resourceUUID);
				}
				final WikiPageVersion wikiPageVersion;
				try {
					wikiPageVersion = new WikiPageVersion(wikiPageVersionContent);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				CopyOnWriteArrayList<WikiPageVersion> versions = wikiPages.get(wikiPageVersion.documentUUID);
				if (versions == null) {
					versions = new CopyOnWriteArrayList<WikiPageVersion>();
					System.out.println("================ about to add new wiki page");
					wikiPages.put(wikiPageVersion.documentUUID, versions);
				}
				System.out.println("================ about to add new wikiPage version");
				versions.add(wikiPageVersion);
				// Uses flag to minimize refreshes if multiple changes
				listNeedsRefreshing = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						refreshWikiPageList();
					}});
			}
		};
	}
	protected void listSelectionChanged(ListSelectionEvent event) {
		setCurrentlySelectedListItemInfo();
	}

	private void setCurrentlySelectedListItemInfo() {
		int index = wikiPageList.getSelectedIndex();
		if (index == -1) {
			this.setTitle(FrameNameBase);
			return;
		}
		WikiPageVersion wikiPageVersion = (WikiPageVersion) wikiPageListModel.get(index);
		
		wikiPageVersionsComboBoxModel.removeAllElements();
		CopyOnWriteArrayList<WikiPageVersion> versions = wikiPages.get(wikiPageVersion.documentUUID);
		if (versions == null) {
			System.out.println("Problem reading versions for: " + wikiPageVersion.documentUUID);
			return;
		}
		for (WikiPageVersion version: versions) {
			wikiPageVersionsComboBoxModel.addElement(version);
		}
		wikiPageVersionsComboBox.setSelectedItem(wikiPageVersion);
		// The above will triger the combo box changed
	}
	
	Component getTop() {
		return appPanel;
	}
	
	void setTitle(String newTitle) {
		JFrame frame = (JFrame) SwingUtilities.getRoot(appPanel);
		frame.setTitle(newTitle);
	}
	
	void newWikiPageButtonPressed() {
		String newTitle = JOptionPane.showInputDialog(getTop(), "Title for new wiki page?");
		if (newTitle == null || newTitle.trim().length() == 0) return;
		if (newTitle.indexOf('\n') != -1) {
			JOptionPane.showMessageDialog(getTop(), "Title can not have a newline: " +  newTitle);
			return;
		}
		String uuid = Utility.generateUUID(applicationIdentifier);
		String timestamp = Utility.currentTimestamp();
		String userID = workspace.getUser();
		WikiPageVersion wikiPageVersion = new WikiPageVersion(uuid, timestamp, userID, newTitle, "", "text/markdown");
		saveWikiPageVersion(wikiPageVersion);
		// String comment = "new wiki page";
		// workspace.addToListForVariable(applicationIdentifier, wikiPageVersion.documentUUID, comment);
		wikiPageListModel.addElement(wikiPageVersion);
		wikiPageList.setSelectedIndex(wikiPageListModel.size() - 1);
	}
	
	String getWikiPageContentType() {
		String result = (String) wikiPageContentTypeComboBox.getSelectedItem();
		if (result == null) result = "text/markdown";
		return result;
	}

	void renameWikiPageButtonPressed() {
		int index = wikiPageList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(getTop(), "A wiki page must be selected first");
			return;
		}
		
		WikiPageVersion oldWikiPageVersion = (WikiPageVersion) wikiPageListModel.get(index);
		String oldTitle = oldWikiPageVersion.title;
		String newTitle = JOptionPane.showInputDialog(getTop(), "New title for new wiki page?", oldTitle);
		if (newTitle == null || newTitle.trim().length() == 0 || newTitle.equals(oldTitle)) return;
		if (newTitle.indexOf('\n') != -1) {
			JOptionPane.showMessageDialog(getTop(), "Title can not have a newline: " +  newTitle);
			return;
		}
		String timestamp = Utility.currentTimestamp();
		String userID = workspace.getUser();
		WikiPageVersion newListItem = new WikiPageVersion(oldWikiPageVersion.documentUUID, timestamp, userID, newTitle, oldWikiPageVersion.content, getWikiPageContentType());
		wikiPageListModel.set(index, newListItem);
		saveWikiPageVersion(newListItem);
		this.setTitle(FrameNameBase + ": " + newTitle);
	}
	
	void wikiPageVersionsButtonPressed() {
		int index = wikiPageList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(getTop(), "A list item must be selected first");
			return;
		}
		
		WikiPageVersion wikiPageVersion = (WikiPageVersion) wikiPageListModel.get(index);
		
		CopyOnWriteArrayList<WikiPageVersion> versions = wikiPages.get(wikiPageVersion.documentUUID);
		if (versions == null) {
			System.out.println("Problem reading versions for: " + wikiPageVersion.documentUUID);
			return;
		}
		WikiPageVersion[] versionsArray = (WikiPageVersion[]) versions.toArray(new WikiPageVersion[versions.size()]);
		WikiPageVersion selection = (WikiPageVersion) JOptionPane.showInputDialog(getTop(), "Please choose a version", "Wiki page versions", JOptionPane.QUESTION_MESSAGE, null, versionsArray , versions.get(0));
		System.out.println("You selected: " + selection);
		if (selection != null) {
			wikiPageListModel.set(index, selection);
			setCurrentlySelectedListItemInfo();
		}
	}
	
	void saveWikiPageButtonPressed() {
		int index = wikiPageList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(getTop(), "A list item must be selected first");
			return;
		}
		WikiPageVersion oldListItem = (WikiPageVersion) wikiPageListModel.get(index);
		String text = wikiPageEditorTextArea.getText();
		String timestamp = Utility.currentTimestamp();
		String userID = workspace.getUser();
		WikiPageVersion newWikiPageVersion = new WikiPageVersion(oldListItem.documentUUID, timestamp, userID, oldListItem.title, text, getWikiPageContentType());
		saveWikiPageVersion(newWikiPageVersion);
		wikiPageVersionsComboBoxModel.addElement(newWikiPageVersion);
		wikiPageVersionsComboBox.setSelectedItem(newWikiPageVersion);
		// Wait for new transaction to show it in list
		// wikiPageListModel.set(index, newListItem);
	}
}
