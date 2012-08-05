package org.pointrel.pointrel20120623.demos.chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingworker.SwingWorker;
import org.pointrel.pointrel20120623.core.NewTransactionCallback;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;

// TODO: Need to add "pending" items
// TODO: Maybe have some facility in Workspace to do the add resource in the background?
public class SimpleChatApp {
	
	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		// TODO: fix user
		String user = "unknown_user@example.com";
		Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, archive, user);
		//Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, "http://twirlip.com/pointrel/", user);
		final JFrame frame = new JFrame(FrameNameBase);
		final SimpleChatApp app = new SimpleChatApp(workspace);
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
	
	public static String FrameNameBase = "Simple Chat";
	
	public int maxChatItemsOnRefresh = 20;
	
	Workspace workspace;
	
	JPanel appPanel = new JPanel();
	
	JPanel chatPanel = new JPanel();
	JTextField chatUUIDTextField = new JTextField();
	JTextField userIDTextField = new JTextField();
	JTextArea chatLogTextArea = new JTextArea();
	JScrollPane chatLogTextAreaScrollPane = new JScrollPane(chatLogTextArea);
	JTextField sendTextField = new JTextField();
	JButton sendButton = new JButton("Send");	
	
	// TODO: Figure out what to do about UUID of chat
	String chatAppChatUUID = "default_chat";
	
	public SimpleChatApp(Workspace workspace) {
		this.workspace = workspace;
	}

	public JPanel openGUI() {
		appPanel.setLayout(new BorderLayout());
		appPanel.add(chatPanel, BorderLayout.CENTER);
		
		chatUUIDTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, chatUUIDTextField.getPreferredSize().height));
		userIDTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, userIDTextField.getPreferredSize().height));
		// uriTextField.setEditable(false);
		
		sendTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, sendTextField.getPreferredSize().height));
		
		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
		chatPanel.add(chatUUIDTextField);
		chatPanel.add(userIDTextField);
		chatPanel.add(chatLogTextAreaScrollPane);
		chatPanel.add(sendTextField);
		chatPanel.add(sendButton);
		
		chatLogTextArea.setLineWrap(true);
		chatLogTextArea.setWrapStyleWord(true);
		
		hookupActions();
		
		chatUUIDTextField.setText(chatAppChatUUID);
		userIDTextField.setText(this.workspace.getUser());
		
		return appPanel;
	}
	
	NewTransactionCallback newTransactionCallback;

	private void hookupActions() {
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
		
		// Do something when enter is pressed
		sendTextField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
		
		chatUUIDTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {chatUUIDTextFieldEnterPressed();}});

		userIDTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {userIDTextFieldEnterPressed();}});

		newTransactionCallback = createNewTransactionCallback();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				workspace.addNewTransactionCallback(newTransactionCallback);
			}}
		);
	}

	protected NewTransactionCallback createNewTransactionCallback() {
		return new NewTransactionCallback(ChatMessage.ContentType) {
			
			@Override
			protected void insert(String resourceUUID) {
				byte[] chatItemContent = workspace.getContentForURI(resourceUUID);
				if (chatItemContent == null) {
					System.out.println("content not found for chat item: " + resourceUUID);
				}
				final ChatMessage chatItem;
				try {
					chatItem = new ChatMessage(chatItemContent);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				// TODO: Improve all this to reduce redundancy of making all objects for every chat etc.
				if (!chatAppChatUUID.equals(chatItem.chatUUID)) {
					System.out.println("==== chatItem is for another chat");
					return;
				}
				System.out.println("================ about to add new chatItem to log");
				addChatItemToLog(chatItem);
			}
		};
	}
	
	protected void userIDTextFieldEnterPressed() {
		workspace.setUser(userIDTextField.getText());
	}

	protected void chatUUIDTextFieldEnterPressed() {
		this.chatAppChatUUID = chatUUIDTextField.getText();
		System.out.println("Updated chat uuid to: " + chatAppChatUUID);
		// Refresh entire system
		this.chatLogTextArea.setText("");
		workspace.removeNewTransactionCallback(newTransactionCallback);
		newTransactionCallback = createNewTransactionCallback();
		workspace.addNewTransactionCallback(newTransactionCallback);
	}

	protected void addChatItemToLog(final ChatMessage chatItem) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("================ about to add new chatItem to log2");
				chatLogTextArea.append(chatItem.getLogText());
			}});
	}

	protected void sendButtonPressed() {
		final String userID = userIDTextField.getText();
		if (userID.length() == 0 || userID.equals("USERID")) {
			JOptionPane.showMessageDialog(appPanel, "Please set the user ID first");
			return;
		}
		if (chatAppChatUUID.length() == 0) {
			JOptionPane.showMessageDialog(appPanel, "Please set the chatUUID first");
			return;
		}
		final String message = sendTextField.getText();
		sendTextField.setText("");
		sendButton.setEnabled(false);
		
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
			public String doInBackground() {
				String timestamp = Utility.currentTimestamp();
					
				ChatMessage chatItem = new ChatMessage(chatAppChatUUID, timestamp, userID, message);
				String uri = workspace.addContent(chatItem.toJSONBytes(), ChatMessage.ContentType);
				workspace.addSimpleTransaction(uri, "New chat message");
				
				if (message.equals("test100")) {
					test100();
				}
				return chatItem.getLogText();
			}
			public void done() {
				sendButton.setEnabled(true);
			}
		};
		worker.execute();
	}

	// Could be reworked to use SwingWorker's publish/process methods
	private void test100() {
		System.out.println("starting writing 100 chat items: " + Utility.currentTimestamp());
		for (int i = 0; i < 100; i++) {
			String timestamp = Utility.currentTimestamp();
			String message = "Testing... #" + i + " " + timestamp;
			
			final ChatMessage chatItem = new ChatMessage(chatAppChatUUID, timestamp, "TestUserID", message);
			String uri = workspace.addContent(chatItem.toJSONBytes(), ChatMessage.ContentType);
			workspace.addSimpleTransaction(uri, "New chat message");
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					chatLogTextArea.append(chatItem.getLogText());
				}
			});	
		}
		System.out.println("finishing writing 100 chat items: " + Utility.currentTimestamp());
	}
}
