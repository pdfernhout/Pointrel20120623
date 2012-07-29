package org.pointrel.pointrel20120623.demos;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pointrel.pointrel20120623.core.Session;
import org.pointrel.pointrel20120623.core.Utility;

public class ResourceViewerUtilityApp {
	public static String FrameNameBase = "ResourceViewer";
	
	Session session;
	
	JPanel appPanel = new JPanel();
	DefaultListModel resourceListModel = new DefaultListModel();
	JSplitPane splitPane = new JSplitPane();
	JPanel listPanel = new JPanel();
	JList resourceList = new JList(resourceListModel);
	JScrollPane resourceListScrollPane = new JScrollPane(resourceList);
	JButton chooseResourceButton = new JButton("Choose resource ...");
	JButton chooseWorkspaceButton = new JButton("Choose workspace ...");
	JButton chooseArchiveButton = new JButton("Choose archive ...");
	JPanel contentPanel = new JPanel();
	JTextField uriTextField = new JTextField();
	JTextField contentTypeTextField = new JTextField();
	JTextArea contentTextArea = new JTextArea();
	JScrollPane contentTextAreaScrollPane = new JScrollPane(contentTextArea);
	// JPanel controlPanel = new JPanel();
	JLabel adviceLabel = new JLabel("Double-click on \"pointrel\" items\nin the editor to load them");
	JButton saveResourceButton = new JButton("Save resource");

	public ResourceViewerUtilityApp(Session session) {
		this.session = session;
	}
	
	// TODO: Need to be able to set user ID to save resources
	
	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		Session session = new Session(archive, Session.DefaultWorkspaceVariable, null);
		// Session session = new Session("http://twirlip.com/pointrel/");
		final JFrame frame = new JFrame(FrameNameBase);
		final ResourceViewerUtilityApp app = new ResourceViewerUtilityApp(session);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPanel appPanel = app.openGUI();
				frame.setSize(800, 600);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(appPanel);
				frame.setVisible(true);
			}
		});
	}

	public JPanel openGUI() {		
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.add(resourceListScrollPane);
		listPanel.add(chooseWorkspaceButton);
		listPanel.add(chooseResourceButton);
		listPanel.add(chooseArchiveButton);
		
		uriTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, uriTextField.getPreferredSize().height));
		uriTextField.setEditable(false);
		
		contentTypeTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, contentTypeTextField.getPreferredSize().height));
		
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(uriTextField);
		contentPanel.add(contentTypeTextField);
		contentPanel.add(contentTextAreaScrollPane);
		contentPanel.add(adviceLabel);
		contentPanel.add(saveResourceButton);
		//contentPanel.add(controlPanel);
		
		splitPane.setLeftComponent(listPanel);
		splitPane.setRightComponent(contentPanel);
		
		appPanel.setLayout(new BorderLayout());
		appPanel.add(splitPane, BorderLayout.CENTER);
		
		hookupActions();
		
		// addSomeResourceItemsToList();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateFrameTitle();
			} 
		});

		return appPanel;
	}
	
	Component getTop() {
		return appPanel;
	}
	
	void setTitle(String newTitle) {
		JFrame frame = (JFrame) SwingUtilities.getRoot(appPanel);
		frame.setTitle(newTitle);
	}

	private void updateFrameTitle() {
		this.setTitle(FrameNameBase + " on: " + session.getArchiveDirectory().getAbsolutePath());
	}

//	private void addSomeResourceItemsToList() {
//		add("pointrel://sha256_c7be1ed902fb8dd4d48997c6452f5d7e509fbcdbe2808b16bcf4edce4c07d14e_14.text%2Fplain");
//		resourceListModel.addElement("pointrel://sha256_a05205672fded582132281b10998abd96c6450b6d4bac5319ed8031a643af0f7_136.text%2Fpointrel-transaction");
//	}

	private void hookupActions() {
		chooseResourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {chooseResourceButtonPressed(); }});

		chooseWorkspaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {chooseWorkspaceButtonPressed(); }});

		chooseArchiveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {chooseArchiveButtonPressed(); }});
		
		saveResourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {saveResourceButtonPressed(); }});
		
		resourceList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {resourceListSelectionChanged(event);}});

		contentTextArea.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e){
		        if(e.getClickCount() == 2){
		            doubleClickInTextArea();
		        }
		    }
		});		
	}

	protected void doubleClickInTextArea() {
		System.out.println("Double click");
        // Get the URL at the caret and add it
        String text = contentTextArea.getText().substring(contentTextArea.getCaretPosition());
        String uri = contentTextArea.getSelectedText() + new StringTokenizer(text, " \t\n\r\f,\"").nextToken();
        System.out.println("uri: " + uri); 
        if (Utility.isValidPointrelURI(uri)) {
        	addOrSelectURI(uri);
        }
	}
	
	boolean isUserIDEmpty() {
		if (session.getUser() != null && session.getUser().length() != 0) return false;
		String userID = JOptionPane.showInputDialog(getTop(), "Enter user ID");
		if (userID == null || userID.length() == 0) return true;
		session.setUser(userID);
		return false;
		
	}

	protected void saveResourceButtonPressed() {
		if (isUserIDEmpty()) {
			JOptionPane.showMessageDialog(getTop(), "User ID must be specified in order to store a resource");
			return;
		}
		String contentType = contentTypeTextField.getText();
		String contentString = contentTextArea.getText();
		String uri = session.addContent(contentString, contentType);
		addOrSelectURI(uri);
	}

	private void addOrSelectURI(String uri) {
		int index = resourceListModel.indexOf(uri);
		if (index != -1) {
			resourceList.setSelectedIndex(index);
		} else {
			resourceListModel.addElement(uri);
			resourceList.setSelectedIndex(resourceListModel.getSize() - 1);
		}
	}

	protected void resourceListSelectionChanged(ListSelectionEvent event) {
		String uri = (String) resourceList.getSelectedValue();
		if (uri == null) {
			contentTypeTextField.setText("");
			contentTextArea.setText("");
			uriTextField.setText("");
			return;
		}
		String contentString = session.getContentForURIAsString(uri);
		String contentType = Utility.contentTypeForURI(uri);
		if (contentString == null) {
			contentTypeTextField.setText(contentType);
			contentTextArea.setText("NOT FOUND");
		} else {
			contentTypeTextField.setText(contentType);
			contentTextArea.setText(contentString);
		}
		uriTextField.setText(uri);
		uriTextField.setCaretPosition(0);
		contentTypeTextField.setCaretPosition(0);
		contentTextArea.setCaretPosition(0);
	}

	protected void chooseResourceButtonPressed() {
		String uri = JOptionPane.showInputDialog(getTop(), "Resource URI?");
		if (uri == null || uri.length() == 0) return;
		this.addOrSelectURI(uri);
	}
	
	protected void chooseWorkspaceButtonPressed() {
		// String variableName = JOptionPane.showInputDialog(frame, "Variable?");
		//if (variableName == null || variableName.length() == 0) {
		//	return;
		//}
		
		ArrayList<String> variableNames = session.getAllVariableNames();
		String[] variableNamesArray = (String[]) variableNames.toArray(new String[variableNames.size()]);
		String selection = (String) JOptionPane.showInputDialog(getTop(), "Please choose a workspace", "Workspaces", JOptionPane.QUESTION_MESSAGE, null, variableNamesArray, null);
		System.out.println("You selected: " + selection);
		if (selection == null || selection.length() == 0) return;
	
		String uri = session.getVariable(selection);
		if (uri == null) JOptionPane.showMessageDialog(getTop(), "Workspace not found: " + selection);
		this.addOrSelectURI(uri);
	}
	
	protected void chooseArchiveButtonPressed() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);	
		try {
			fileChooser.setCurrentDirectory(session.getArchiveDirectory().getParentFile().getCanonicalFile());
			System.out.println("Dir supplied: " + session.getArchiveDirectory().getParentFile().getCanonicalFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Dir actual: " + fileChooser.getCurrentDirectory());
		int returnedValue = fileChooser.showOpenDialog(getTop());
		if (returnedValue == JFileChooser.APPROVE_OPTION) {
			File archive = fileChooser.getSelectedFile();
			// TODO: Need some way to confirm this is an archive
			session = new Session(archive, Session.DefaultWorkspaceVariable, null);
			updateFrameTitle();
			resourceListModel.clear();
			// addSomeResourceItemsToList();
		}
	}
}
