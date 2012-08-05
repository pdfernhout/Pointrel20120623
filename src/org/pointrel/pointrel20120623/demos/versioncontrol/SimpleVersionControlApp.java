package org.pointrel.pointrel20120623.demos.versioncontrol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.pointrel.pointrel20120623.core.Workspace;

public class SimpleVersionControlApp {
	public static String FrameNameBase = "Simple Version Control App";
	
	Workspace workspace;

	JPanel appPanel = new JPanel();
	final JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	final JSplitPane splitPane2 = new JSplitPane();
	final JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	final DefaultListModel projectListModel = new DefaultListModel();
	final JList projectList = new JList(projectListModel);
	
	final DefaultListModel versionListModel = new DefaultListModel();
	final JList versionList = new JList(versionListModel);
	
	final DefaultListModel fileListModel = new DefaultListModel();
	final JList fileList = new JList(fileListModel);
	
	final JPanel fileListPanel = new JPanel();
	final JTextField timestampTextField = new JTextField();
	final JTextField committerTextField = new JTextField();
	final JTextArea commentTextArea = new JTextArea();
	final JScrollPane commentScrollPane = new JScrollPane(commentTextArea);
	
	final JPanel buttonPanel = new JPanel();
	final JButton loadCurrentFilesButton = new JButton("Load current files");
	final JButton commitButton = new JButton("Commit");
	
	public SimpleVersionControlApp(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		// TODO: Fix user
		String user = "unknown_user@example.com";
		Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, archive, user);
		//Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, "http://twirlip.com/pointrel/", user);
		final JFrame frame = new JFrame(FrameNameBase);
		final SimpleVersionControlApp app = new SimpleVersionControlApp(workspace);
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

	protected JPanel openGUI() {		
		buttonPanel.add(loadCurrentFilesButton);
		buttonPanel.add(commitButton);
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));		
		
		fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
		timestampTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, timestampTextField.getPreferredSize().height));
		fileListPanel.add(timestampTextField);
		committerTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, committerTextField.getPreferredSize().height));
		fileListPanel.add(committerTextField);
		fileListPanel.add(commentScrollPane);
		fileListPanel.add(buttonPanel);
		
		splitPane3.setTopComponent(fileList);
		splitPane3.setBottomComponent(fileListPanel);
		splitPane3.setDividerLocation(300);

		splitPane2.setLeftComponent(projectList);
		splitPane2.setRightComponent(versionList);
		splitPane2.setDividerLocation(300);
		
		splitPane1.setTopComponent(splitPane2);
		splitPane1.setBottomComponent(splitPane3);
		splitPane1.setDividerLocation(100);
		
		appPanel.setLayout(new BorderLayout());
		appPanel.add(splitPane1, BorderLayout.CENTER);
		
		hookupActions();
		
		committerTextField.setText("USERID");
		
		return appPanel;
	}

	private void hookupActions() {
//		sendButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
//		
//		// Do something when enter is pressed
//		sendTextField.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
//
//		refreshButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {refreshButtonPressed(); }});
//		
//		// Update every ten seconds
//		ActionListener runnable = new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				if (refreshButton.isEnabled()) refreshButtonPressed();
//			}
//		};
//		runnable.actionPerformed(null);
//		Timer timer = new Timer(10000, runnable);
//		timer.start();
	}

}
