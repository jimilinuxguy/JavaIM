package main;

import java.net.*;
import java.io.*;
import java.util.*;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

	// if I use a GUI or not
	private ClientGUI cg;
	
	private String username;
	private int port;

	/**
	 * 
	 * @param server A string containing the value to use  for the server name or IP address to connect to
	 * @param port An integer defining the port that we are going to connect to
	 * @param username A string value for the chat client's name/handle
	 * @param cg Instance of Client GUI class
	 */
	Client(int port, String username, ClientGUI cg) {
		this.port = port;
		this.username = username;

		this.cg = cg;
		System.out.println(port + " " +username);
		
	}
	
	/**
	 * Method to connect to the server, returns true on success, false on failure or exception
	 * @return boolean
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public boolean start() throws UnknownHostException, IOException {
		/*
		 * Attempt to connect to the socket using the defined server and port
		 * Per doctor Ding, we should catch more than just the generic Exception class, 
		 * so here we catch the IOException/SocketException and also the
		 * UnknownHostException
		 */
		try {
			socket = new Socket("127.0.0.1",port);
		} 
		catch(SocketException ec) { 
			display("\nError connecting to server:" + ec);
			return false;
		}
		catch (UnknownHostException ec) {
			display("\nError connecting to server:" + ec);
			return false;
		}
		
		String msg = "\nConnection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		/* Creating both Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		if(cg == null) {
			System.out.println(msg);      // println in console mode
		} else {
			cg.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
		}
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
		// inform the GUI
		if(cg != null)
			cg.connectionFailed();
			
	}
	/*
	 * > java Client username portNumber
	 * If the portNumber is not specified 6667 is used
	 * If the username is not specified "Anonymous" is used
	 */
	public static void main(String[] args) throws Exception {
		// default values
		int portNumber = 6667;
		String userName = "ChatUser";
		if (args.length == 1) {
			userName = args[0];	
		}
		if (args.length == 2) {
			portNumber = Integer.parseInt(args[1]);
			userName = args[0];			
		}


		Client client = new Client(portNumber, userName, null);

		try {
			if(!client.start()) {
				return;
			}
		} catch (Exception ex) {
			
		}
		
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true) {
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();
			System.out.println("message="+msg);
			// logout if message is LOGOUT
			if(msg.equals("/quit")) {
				client.sendMessage(new ChatMessage(ChatMessage.QUIT, ""));
				// break to do the disconnect
				break;
			} else if (msg.equals("/who") ) {
				System.out.println("checking users online");
				client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));
				break;
			} else if (msg.contains("/join ") ) {
				String[] data = msg.split("/join ");
				String topicName = data[1];
				client.sendMessage(new ChatMessage(ChatMessage.JOIN, topicName));
			} else if (msg.contains("/part")) {
				String[] data = msg.split("/part ");
				String topicName = data[1];
				client.sendMessage(new ChatMessage(ChatMessage.PART, topicName));				
			}
			else {
				System.out.println("Sending chat");
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// done disconnect
		client.disconnect();	
		scan.close();
	}

	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					// if console mode print the message and add back the prompt
					if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						
						if (msg.contains("CONN ")) {
							String[] userData = msg.split("CONN ");
							cg.addUser(userData[1]);
							cg.append("*** " + userData[1] + " connnected");
						}
						else {
							cg.append(msg);
						}
					}
				}
				catch(IOException e) {
					display("\nServer has closed the connection\n");
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
