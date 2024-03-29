package org.pointrel.pointrel20120623.demos.community;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.View;

import org.pointrel.pointrel20120623.core.Workspace;
import org.pointrel.pointrel20120623.demos.chat.SimpleChatApp;
import org.pointrel.pointrel20120623.demos.conceptmap.SimpleConceptMapApp;
import org.pointrel.pointrel20120623.demos.notebook.SimpleNotebookApp;
import org.pointrel.pointrel20120623.demos.wiki.SimpleWikiApp;

// Developed kind of with Rakontu in mind...
// But maybe generalizes to a semantic desktop with multiple communities?
// Conflicted about whether to put in public key stuff and encryption for privacy given added complexity (especially to deal with key revocation), leaving out for now
public class SimpleCommunityApp {
	final Workspace workspace;
	final Community community;
	
	public SimpleCommunityApp(String communityUUID, Workspace workspace) {
		this.community = new Community(communityUUID);
		this.workspace = workspace;
	}

	public static void main(String[] args) {
		File archive = new File("./PointrelArchive");
		// TODO: Fix user
		String user = "unknown_user@example.com";
		Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable, archive, user);
		//String testIncrement = ".002";
		//Workspace workspace = new Workspace(Workspace.DefaultWorkspaceVariable + testIncrement, "http://twirlip.com/pointrel/", user);
		String communityUUID = "uuid:test001";
		final SimpleCommunityApp app = new SimpleCommunityApp(communityUUID, workspace);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.openGUI();
			}
		});
	}

	// From http://stackoverflow.com/questions/7306295/swing-jlist-with-multiline-text-and-dynamic-height
	public class MyComponentAdapter extends ComponentAdapter {
		private JList list;
		
		public MyComponentAdapter(JList list) {
			this.list = list;
		}
		
        @Override
        public void componentResized(ComponentEvent e) {
            // next line possible if list is of type JXList
            // list.invalidateCellSizeCache();
            // for core: force cache invalidation by temporarily setting fixed height
            list.setFixedCellHeight(10);
            list.setFixedCellHeight(-1);
            //list.invalidate();
        }
    };

    
	// From http://stackoverflow.com/questions/7306295/swing-jlist-with-multiline-text-and-dynamic-height
    @SuppressWarnings("serial")
	public class MyCellRenderer extends DefaultListCellRenderer {
        //final JPanel p = new JPanel(new BorderLayout());
        // final JPanel IconPanel = new JPanel(new BorderLayout());
        //final JLabel l = new JLabel("icon"); //<-- this will be an icon instead of a text
        final JLabel lt = new JLabel();
		private JScrollPane scrollPane;
		// private JTextArea ta;
        //String pre = "<html><body style='width: 200px;'>";
        //private JTextPane ta;

        MyCellRenderer(JScrollPane scrollPane) {
        		this.scrollPane = scrollPane;
            //icon
            //IconPanel.add(l, BorderLayout.NORTH);
            //p.add(IconPanel, BorderLayout.WEST);

            //p.add(lt, BorderLayout.CENTER);
            //text
        	
        		lt.setOpaque(true);

        	
            // ta = new JTextArea();
        	   //ta = new JTextPane();
            //ta.setLineWrap(true);
            //ta.setWrapStyleWord(true);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
        {
        		int width = scrollPane.getWidth();
        		//lt.setPreferredSize(new Dimension(width, Integer.MAX_VALUE));
        		//lt.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
            final String text = (String) value;
            //lt.setText(pre + text);
            lt.setText(text);
            if (isSelected) {
            		lt.setBackground(Color.LIGHT_GRAY);
            } else {
            		lt.setBackground(Color.WHITE);
            }
            lt.setFont(list.getFont());
            //lt.repaint();
            
             //return lt;
            //.setSize(width, Integer.MAX_VALUE);
            //p.setPreferredSize(new Dimension(width, 10));
    		   //p.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
            //lt.setSize(1000, width);
            Dimension preferredSize = calculatePreferredSize(lt, true, width - scrollPane.getVerticalScrollBar().getWidth());
            //lt.setMinimumSize(preferredSize);
            lt.setPreferredSize(preferredSize);
            //lt.setMaximumSize(preferredSize);
            //lt.setSize(preferredSize);
            return lt;
        	
//            ta.setText((String) value);
//            int width = list.getWidth();
//            // this is just to lure the ta's internal sizing mechanism into action
//            if (width > 0)
//                ta.setSize(width, Short.MAX_VALUE);
//            return ta;

        }
    }
    
    // From: http://stackoverflow.com/questions/1048224/get-height-of-multi-line-text-with-fixed-width-to-make-dialog-resize-properly
    /**
     * Returns the preferred size to set a component at in order to render an html string. You can
     * specify the size of one dimension.
     */
    public static java.awt.Dimension calculatePreferredSize(JLabel resizer, boolean width, int prefSize) {
		View view = (View) resizer.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);

		view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

		float w = view.getPreferredSpan(View.X_AXIS);
		float h = view.getPreferredSpan(View.Y_AXIS);

		return new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));
	}

	final public static String FrameNameBase = "Rakontu Community Application";
	
	JFrame frame = new JFrame(FrameNameBase);
	JSplitPane splitPane = new JSplitPane();
	JPanel sidebarPanel = new JPanel();
	JPanel mainPanel = new JPanel();

	JPanel communitySidebarPanel = new JPanel();
	DefaultComboBoxModel communityComboBoxModel = new DefaultComboBoxModel();
	JComboBox communityComboBox = new JComboBox(communityComboBoxModel);	
	JLabel pictureLabel = new JLabel();
	JTextArea communityDescriptionTextArea = new JTextArea();
	JScrollPane communityDescriptionScrollPane = new JScrollPane(communityDescriptionTextArea);
	
	JPanel bulletinBoardSidebarPanel = new JPanel();
	JLabel bulletinBoardLabel = new JLabel("Bulle tin board");
	DefaultListModel bulletinBoardListModel = new DefaultListModel();
	JList bulletinBoardList = new JList(bulletinBoardListModel);
	JScrollPane bulletinBoardListScrollPane = new JScrollPane(bulletinBoardList);
	
	JPanel messagesSidebarPanel = new JPanel();
	JLabel messagesLabel = new JLabel("Messages");
	DefaultListModel messagesListModel = new DefaultListModel();
	JList messagesList = new JList(messagesListModel);
	JScrollPane messagesListScrollPane = new JScrollPane(messagesList);
	
	JPanel membersSidebarPanel = new JPanel();	
	JLabel membersLabel = new JLabel("Members");
	DefaultListModel membersListModel = new DefaultListModel();
	JList membersList = new JList(membersListModel);
	JScrollPane membersListScrollPane = new JScrollPane(membersList);
	
	JSplitPane messagesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bulletinBoardSidebarPanel, messagesSidebarPanel);
	JSplitPane messagesMembersSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, messagesSplitPane, membersSidebarPanel);
	JSplitPane communityRestSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, communitySidebarPanel, messagesMembersSplitPane);

	SimpleConceptMapApp simpleConceptMapApp;
	SimpleChatApp simpleChatApp;
	SimpleNotebookApp simpleNotebookApp;
		
	JPanel simpleConceptMapAppPanel;
	JPanel simpleChatAppPanel;
	JPanel simpleNotebookAppPanel;
	
	JDesktopPane desktopPane = new JDesktopPane();

	//JTextField recipient = new JTextField();
	//JTextField type = new JTextField();
	//JTextField title = new JTextField();
	//JScrollPane bodyScrollPane = new JScrollPane();
	//JTextArea body = new JTextArea();
	//JButton sendButton = new JButton("Send");
	
	protected void openGUI() {
		frame.setSize(800, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// TODO: Use MultiSplitPane
		// http://today.java.net/pub/a/today/2006/03/23/multi-split-pane.html
		communitySidebarPanel.setLayout(new BorderLayout());
		communitySidebarPanel.add(communityComboBox, BorderLayout.NORTH);
		communitySidebarPanel.add(pictureLabel, BorderLayout.WEST);
		communitySidebarPanel.add(communityDescriptionScrollPane, BorderLayout.CENTER);
		//communitySidebarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
		
		communityDescriptionTextArea.setLineWrap(true);
		communityDescriptionTextArea.setWrapStyleWord(true);
		communityDescriptionTextArea.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		
		bulletinBoardSidebarPanel.setLayout(new BorderLayout());
		bulletinBoardSidebarPanel.add(bulletinBoardLabel, BorderLayout.NORTH);
		bulletinBoardSidebarPanel.add(bulletinBoardListScrollPane, BorderLayout.CENTER);

		bulletinBoardList.setCellRenderer(new MyCellRenderer(bulletinBoardListScrollPane));
		bulletinBoardList.addComponentListener(new MyComponentAdapter(bulletinBoardList));
		//bulletinBoardListScrollPane.setPreferredSize(new Dimension(1, Integer.MAX_VALUE));
		// bulletinBoardList.setPreferredSize(new Dimension(1, bulletinBoardList.getMaximumSize().height));
		bulletinBoardList.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

		messagesSidebarPanel.setLayout(new BorderLayout());
		messagesSidebarPanel.add(messagesLabel, BorderLayout.NORTH);
		messagesSidebarPanel.add(messagesListScrollPane, BorderLayout.CENTER);
		
		messagesList.setCellRenderer(new MyCellRenderer(messagesListScrollPane));
		messagesList.addComponentListener(new MyComponentAdapter(messagesList));
		//messagesListScrollPane.setPreferredSize(new Dimension(1, Integer.MAX_VALUE));
		// messagesList.setPreferredSize(new Dimension(1, messagesList.getMaximumSize().height));
		messagesList.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

		membersSidebarPanel.setLayout(new BorderLayout());
		membersSidebarPanel.add(membersLabel, BorderLayout.NORTH);
		membersSidebarPanel.add(membersListScrollPane, BorderLayout.CENTER);
		
		// membersList.setPreferredSize(new Dimension(1, 1));
		membersList.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		
		//sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.PAGE_AXIS));
		//sidebarPanel.add(communitySidebarPanel);
		// sidebarPanel.add(bulletinBoardSidebarPanel);
		// sidebarPanel.add.add(messagesSidebarPanel);
		// sidebarPanel.add(membersSidebarPanel);
		sidebarPanel.setLayout(new BorderLayout());
		sidebarPanel.add(communityRestSplitPane, BorderLayout.CENTER);
		
		splitPane.setLeftComponent(sidebarPanel);
		splitPane.setRightComponent(mainPanel);
		splitPane.setDividerLocation(300);
		
		frame.add(splitPane, BorderLayout.CENTER);
		
		hookupActions();
		
		addTestData();
		updateCommunityImageIcon();
		
		simpleConceptMapApp = new SimpleConceptMapApp(workspace);
		simpleConceptMapAppPanel = simpleConceptMapApp.openGUI();
		JInternalFrame conceptMapFrame = new JInternalFrame("Concept Map", true, true, true, true);
		conceptMapFrame.add(simpleConceptMapAppPanel, BorderLayout.CENTER);
		
		simpleNotebookApp = new SimpleNotebookApp(workspace);
		simpleNotebookAppPanel = simpleNotebookApp.openGUI();
		JInternalFrame notebookFrame = new JInternalFrame("Notebook", true, true, true, true);
		notebookFrame.add(simpleNotebookAppPanel, BorderLayout.CENTER);
		
		simpleChatApp = new SimpleChatApp(workspace);
		simpleChatAppPanel = simpleChatApp.openGUI();
		JInternalFrame chatFrame = new JInternalFrame("Chat", true, true, true, true);
		chatFrame.add(simpleChatAppPanel, BorderLayout.CENTER);
		
		// Make a second one for testing
		SimpleChatApp simpleChatApp2 = new SimpleChatApp(workspace);
		JPanel simpleChatAppPanel2 = simpleChatApp2.openGUI();
		JInternalFrame chatFrame2 = new JInternalFrame("Chat2", true, true, true, true);
		chatFrame2.add(simpleChatAppPanel2, BorderLayout.CENTER);
		
		SimpleWikiApp simpleWikiApp = new SimpleWikiApp(workspace);
		JPanel simpleWikiAppPanel = simpleWikiApp.openGUI();
		JInternalFrame simpleWikiAppFrame = new JInternalFrame("Wiki", true, true, true, true);
		simpleWikiAppFrame.add(simpleWikiAppPanel, BorderLayout.CENTER);
		
		desktopPane.add(conceptMapFrame);
		desktopPane.add(notebookFrame);
		desktopPane.add(chatFrame);
		desktopPane.add(chatFrame2);
		desktopPane.add(simpleWikiAppFrame);
		
		notebookFrame.pack();
		notebookFrame.setVisible(true);
		notebookFrame.setSize(new Dimension(400, 400));
		notebookFrame.setLocation(40, 40);
	
		conceptMapFrame.pack();
		conceptMapFrame.setSize(new Dimension(400, 400));
		conceptMapFrame.setVisible(true);
		conceptMapFrame.setLocation(70, 70);
		
		chatFrame.setVisible(true);
		chatFrame.pack();
		chatFrame.setSize(new Dimension(300, 500));
		chatFrame.setLocation(100, 100);
		
		chatFrame2.setVisible(true);
		chatFrame2.pack();
		chatFrame2.setSize(new Dimension(300, 500));
		chatFrame2.setLocation(130, 130);
		
		simpleWikiAppFrame.setVisible(true);
		simpleWikiAppFrame.pack();
		simpleWikiAppFrame.setSize(new Dimension(300, 500));
		simpleWikiAppFrame.setLocation(130, 130);
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(desktopPane, BorderLayout.CENTER);
				
		frame.setVisible(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				splitPane.setDividerLocation(0.3);
				communityRestSplitPane.setDividerLocation(0.15);
				messagesMembersSplitPane.setDividerLocation(0.75);
				messagesSplitPane.setDividerLocation(0.5);
			}
		});
	}

	private void addTestData() {
		this.community.name = "Harris Road Community";
		this.community.about = "A spot for everyone on our road. Issues, problems, hopes, history.";
//		membersListModel.addElement("? Joe S.");
//		membersListModel.addElement("? Melanie Smith");
//		membersListModel.addElement("? Helen (Blue house)");		 
//		membersListModel.addElement("?? Cynthia Kurtz");
//		membersListModel.addElement("? Ron M.");
		membersListModel.addElement("\u2388 Joe S.");
		membersListModel.addElement("\u263A Melanie Smith");
		membersListModel.addElement("\u260F Helen (Blue house)");		 
		membersListModel.addElement("\u260D\u2710 Cynthia Kurtz");
		membersListModel.addElement("\u231B Ron M.");
		
		bulletinBoardListModel.addElement("<html><font color='blue'>Melanie</font>, <i>Today 4:12 pm</i><br>Story workshop next Monday at 4 pm.<br>Theme: paving the road.</html>");
		bulletinBoardListModel.addElement("<html><font color='blue'>Joe S.</font>, <i>Yesterday 5:08 am</i><br>What does everyone think of the new ques tion about the dam proposal? Does it work?</html>");
		
		messagesListModel.addElement("<html><font color='blue'>Ron</font>,<i>4/5/2012</i><br>Cynthia, have you figured out how to manage your dra fts yet?<br>Happy to help.</html>");
	}

	private void updateCommunityImageIcon() {
		BufferedImage bufferedImage;
		try {
			// TODO: Fix this
			File imageFile = new File("images/rakontuTwoPeopleSmaller.png"); 
			bufferedImage = ImageIO.read(imageFile);
			pictureLabel.setIcon(new ImageIcon(bufferedImage));
			communityDescriptionTextArea.setText(community.about);
			communityComboBoxModel.removeAllElements();
			communityComboBoxModel.addElement(community.name);
			communityComboBox.setSelectedIndex(0);
		} catch (IOException e) {
			e.printStackTrace();
			pictureLabel.setIcon(null);
		}
	}

	private void hookupActions() {
		// TODO
	}
}
