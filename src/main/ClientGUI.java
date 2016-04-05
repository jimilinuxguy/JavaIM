package main;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {
	private JTextArea defaultBody;

	private boolean connected;

	private Client client;

	private int defaultPort;
	private String defaultHost;
	
	private JTextField chatInputMessage;
	private JTextField serverAddress;
	private JTextField serverPort;
	private JButton connectButton;
	private JButton sendButton;
	private String username;
	private JList<String> userList;
	private DefaultListModel<String> userListModel;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmQuit;
	static final long serialVersionUID = 1234567890;

	// Constructor connection receiving a socket number
	ClientGUI(String host, int port) {
		super("Sanchez Chat");

		setBackground(Color.BLACK);
		defaultPort = port;
		defaultHost = host;

		 username = getUsername();

		// The NorthPanel with:
		JPanel northPanel = new JPanel(new GridLayout(1,1));
		northPanel.setBackground(Color.DARK_GRAY);

		JPanel serverAndPort = new JPanel(new GridLayout(1,6, 1, 1));
		serverAndPort.setBackground(Color.DARK_GRAY);
		northPanel.add(serverAndPort);
		
		serverAddress = new JTextField();
		serverAddress.setBackground(Color.BLACK);
		serverAddress.setForeground(Color.CYAN);
		serverAddress.setText("localhost");
		serverAndPort.add(serverAddress);
		serverAddress.setColumns(10);
		
		serverPort = new JTextField();
		serverPort.setForeground(Color.CYAN);
		serverPort.setBackground(Color.BLACK);
		serverPort.setText("6667");
		serverAndPort.add(serverPort);
		serverPort.setColumns(10);

		client = new Client(Integer.parseInt(serverPort.getText()), username, this);
		
		connectButton = new JButton("Connect");
		connectButton.setBackground(Color.DARK_GRAY);
		connectButton.setForeground(Color.DARK_GRAY);
		connectButton.setContentAreaFilled(false);
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			   // test if we can start the Client
			   try {
				if(!client.start()) {
				    return;
				   }
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			   connected = true;
			   
			   // disable login button
			   connectButton.setEnabled(false);
			   serverAddress.setEditable(false);
			   serverPort.setEditable(false);
			}
		});

		serverAndPort.add(connectButton);
		getContentPane().add(northPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(Color.DARK_GRAY);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10) );
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		GridBagLayout gbl_centerPanel = new GridBagLayout();
		gbl_centerPanel.columnWidths = new int[]{290, 0, 0};
		gbl_centerPanel.rowHeights = new int[]{0, 500, 0};
		gbl_centerPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_centerPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		centerPanel.setLayout(gbl_centerPanel);

//		scrollBody.add(chatBody);
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		//scrollBody = new JScrollPane();
		defaultBody = new JTextArea();
		defaultBody.setBorder(BorderFactory.createCompoundBorder(border, 
		            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		defaultBody.setEditable(false);
		defaultBody.setText("Welcome to Sanchez Chat\n");
		defaultBody.append("Commands available: /who /quit");
		
		GridBagConstraints gbc_defaultBody = new GridBagConstraints();
		gbc_defaultBody.insets = new Insets(0, 0, 0, 5);
		gbc_defaultBody.fill = GridBagConstraints.BOTH;
		gbc_defaultBody.gridx = 0;
		gbc_defaultBody.gridy = 1;
		centerPanel.add(defaultBody, gbc_defaultBody);
		
		userListModel = new DefaultListModel<String>();
		addUser(username);

		userList =  new JList<>(userListModel);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 1;
		gbc_list.gridy = 1;


		centerPanel.add(userList, gbc_list);
		JPanel southPanel = new JPanel();
		southPanel.setBackground(Color.DARK_GRAY);
		southPanel.setLayout(new BorderLayout(0, 0));
		
		chatInputMessage = new JTextField();
		chatInputMessage.addActionListener(this);
		chatInputMessage.addFocusListener(new FocusListener() {
		      public void focusGained(FocusEvent e) {
		    	  if ( e.getSource() == chatInputMessage) {
		    		  if ( chatInputMessage.getText().equals("Your message here") ) {
		    			  chatInputMessage.setText("");
		    		  }
		    	  }
		      }

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		chatInputMessage.setText("Your message here");
		southPanel.add(chatInputMessage);
		chatInputMessage.setColumns(10);

		getContentPane().add(southPanel, BorderLayout.SOUTH);
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		southPanel.add(sendButton, BorderLayout.EAST);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(this);
		mnFile.add(mntmQuit);
		setVisible(true);

	}

	void append(String str) {
		addChat(str);
	}
	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	void connectionFailed() {
		connectButton.setEnabled(true);
		serverPort.setText("" + defaultPort);
		serverAddress.setText(defaultHost);
		serverAddress.setEditable(false);
		serverPort.setEditable(false);
		connected = false;
	}
		

	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGUI("localhost", 6667);
	}

	protected void addChat(String s) {
		defaultBody.append(s);
	}

	public static String getUsername()
	{
		String username= JOptionPane.showInputDialog(null, "Enter your name", "Enter name", JOptionPane.PLAIN_MESSAGE);
		if(username == null || username.isEmpty()) {
			getUsername();
		}
		
		return username;
	}

    public void focusGained(FocusEvent e) {
        Object o = e.getSource();
		System.out.println(o == chatInputMessage);
		if (o == chatInputMessage) {
			chatInputMessage.setText("");
			chatInputMessage.grabFocus();
		}
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (connected) {
			int messageType = ChatMessage.MESSAGE;
			if (chatInputMessage.getText().equals("/who")) {
				messageType = ChatMessage.ONLINE;
			} else if (chatInputMessage.getText().equals("/quit")) {
				messageType = ChatMessage.QUIT;
			} else if (chatInputMessage.getText().contains("/join ")) {
				messageType = ChatMessage.JOIN;
			}
			client.sendMessage(new ChatMessage(messageType, chatInputMessage.getText()));  
			chatInputMessage.setText("");
		}
		
		if ( o == connectButton ) {
			client = new Client(
					Integer.parseInt(serverPort.getText()),
					username,
					this
				);
		}
	}
	
	void addUser(String user) {
		userListModel.addElement(user);
	}
}

