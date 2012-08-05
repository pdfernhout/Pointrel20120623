package org.pointrel.pointrel20120623.demos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jdesktop.swingworker.SwingWorker;
import org.pointrel.pointrel20120623.core.NewTransactionCallback;
import org.pointrel.pointrel20120623.core.TransactionVisitor;
import org.pointrel.pointrel20120623.core.Utility;
import org.pointrel.pointrel20120623.core.Workspace;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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
	JTextField userIDTextField = new JTextField();
	JTextArea chatLogTextArea = new JTextArea();
	JScrollPane chatLogTextAreaScrollPane = new JScrollPane(chatLogTextArea);
	JTextField sendTextField = new JTextField();
	JButton sendButton = new JButton("Send");	
	
	// TODO: Figure out what to do about UUID of chat
	String chatAppChatUUID = "default_chat";
	
	class ChatItem {
		final public static String ContentType = "text/vnd.pointrel.SimpleChatApp.ChatItem.json";
		final public static String Version = "20120623.0.1.0";
		
		final String chatUUID;
		final String timestamp;
		final String userID;
		final String chatMessage;
		
		ChatItem(String chatUUID, String timestamp, String userID, String chatMessage) {
			this.chatUUID = chatUUID;
			this.timestamp = timestamp;
			this.userID = userID;
			this.chatMessage = chatMessage;
		}
		
		public ChatItem(byte[] content) throws IOException {
			boolean typeChecked = false;
			boolean versionChecked = false;
			String chatUUID_Read = null;
			String timestamp_Read = null;
			String userID_Read = null;
			String chatMessage_Read = null;
			
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
				} else if (fieldName.equals("chatUUID")) {
					chatUUID_Read = jsonParser.getText();
				} else if (fieldName.equals("timestamp")) {
					timestamp_Read = jsonParser.getText();
				} else if (fieldName.equals("userID")) {
					userID_Read = jsonParser.getText();
				} else if (fieldName.equals("chatMessage")) {
					chatMessage_Read = jsonParser.getText();
				} else {
					throw new IOException("Unrecognized field '" + fieldName + "'");
				}
			}
			jsonParser.close();
			
			if (!typeChecked) {
				throw new RuntimeException("Expected type of " + ContentType + "  but no  field");
			}
			
			if (!versionChecked) {
				throw new RuntimeException("Expected version of " + Version + "  but no version field");
			}
			
			chatUUID = chatUUID_Read;
			timestamp = timestamp_Read;
			userID = userID_Read;
			chatMessage = chatMessage_Read;
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
				jsonGenerator.writeStringField("chatUUID", chatUUID);
				jsonGenerator.writeStringField("timestamp", timestamp);
				jsonGenerator.writeStringField("userID", userID);
				jsonGenerator.writeStringField("chatMessage", chatMessage);
				jsonGenerator.writeEndObject();
				jsonGenerator.close();
				return outputStream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public String toString() {
			return timestamp + " | " + userID + " | " + chatMessage;
		}

		public String getLogText() {
			String timestampForLog = timestamp.replace("T", " ");
			timestampForLog = timestampForLog.replace("Z", " GMT");
			timestampForLog = "<" + timestampForLog + ">";

			String message = chatMessage;
			if (!message.endsWith("\n")) message += "\n";
			
			String logText = userID + " "  + timestampForLog + ":\n" + message;
			return logText;
		}
	}
	
	public SimpleChatApp(Workspace workspace) {
		this.workspace = workspace;
	}

	public JPanel openGUI() {
		appPanel.setLayout(new BorderLayout());
		appPanel.add(chatPanel, BorderLayout.CENTER);
		
		userIDTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, userIDTextField.getPreferredSize().height));
		// uriTextField.setEditable(false);
		
		sendTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, sendTextField.getPreferredSize().height));
		
		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
		chatPanel.add(userIDTextField);
		chatPanel.add(chatLogTextAreaScrollPane);
		chatPanel.add(sendTextField);
		chatPanel.add(sendButton);
		
		chatLogTextArea.setLineWrap(true);
		chatLogTextArea.setWrapStyleWord(true);
		
		hookupActions();
		
		userIDTextField.setText("ENTER_USERID");
		
		return appPanel;
	}

	private void hookupActions() {
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});
		
		// Do something when enter is pressed
		sendTextField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {sendButtonPressed(); }});

		final NewTransactionCallback newTransactionCallback = new NewTransactionCallback(ChatItem.ContentType) {
			
			@Override
			protected void insert(String resourceUUID) {
				byte[] chatItemContent = workspace.getContentForURI(resourceUUID);
				if (chatItemContent == null) {
					System.out.println("content not found for chat item: " + resourceUUID);
				}
				final ChatItem chatItem;
				try {
					chatItem = new ChatItem(chatItemContent);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				System.out.println("================ about to add new chatItem to log");
				addChatItemToLog(chatItem);
			}
		};
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				workspace.addNewTransactionCallback(newTransactionCallback);
			}}
		);
	}
	
	protected void addChatItemToLog(final ChatItem chatItem) {
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
		// TODO: Fix user somehow in app
		workspace.setUser(userID);
		final String message = sendTextField.getText();
		sendTextField.setText("");
		sendButton.setEnabled(false);
		
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
			public String doInBackground() {
				String timestamp = Utility.currentTimestamp();
					
				ChatItem chatItem = new ChatItem(chatAppChatUUID, timestamp, userID, message);
				String uri = workspace.addContent(chatItem.toJSONBytes(), ChatItem.ContentType);
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
			
			final ChatItem chatItem = new ChatItem(chatAppChatUUID, timestamp, "TestUserID", message);
			String uri = workspace.addContent(chatItem.toJSONBytes(), ChatItem.ContentType);
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
