package main;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	private ArrayList<ClientThread> clients;
	private SimpleDateFormat sdf;
	private int port;
	private ServerSocket serverSocket;
	protected Map<String, ChatTopic> topics = new HashMap<String, ChatTopic>();

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm");
		clients = new ArrayList<ClientThread>();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void start() {

		/* create socket server and wait for connection requests */
		while(true) 
		{
			// format message saying we are waiting
			display("Server waiting for Clients on port " + port + ".");
			
			Socket socket;
			try {
				socket = serverSocket.accept();
				ClientThread t = new ClientThread(socket);  // make a thread of it
				for(ClientThread user : clients) {
					if ( user.username.toLowerCase().equals(t.username.toLowerCase())  ) {
						t.writeMsg("\nUsername already in use");
						t.close();
					}
				}
				System.out.println("t="+t.username);
				clients.add(t);									// save it in the ArrayList
				t.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  	// accept connection
			// if I was asked to stop
			
		}

	}		

	
	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}

	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = "<"+time + "> " + message + "\n";

		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = clients.size(); --i >= 0;) {
			ClientThread ct = clients.get(i);
			if(!ct.writeMsg(messageLf)) {
				clients.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	synchronized void remove(int id) {
		for(int i = 0; i < clients.size(); ++i) {
			ClientThread ct = clients.get(i);
			if(ct.userId == id) {
				clients.remove(i);
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		int portNumber = 6667;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int userId;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			userId = ++uniqueId;
			this.socket = socket;
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();
				// Switch on the type of message receive
				System.out.println("Type = " + cm.getType());
				switch(cm.getType()) {
				case ChatMessage.DIRECT:
					String[] parts = message.split(" ");
					String userTarget = parts[1];
					String msg = parts[2];
					System.out.println("Target = " + userTarget);
					System.out.println("Message = " + msg);
					
					for (int i = 0; i < clients.size(); i++) {
						ClientThread ct = clients.get(i);
						System.out.println("Thread name = " + ct.username);
						if (ct.username.toLowerCase().equals(userTarget.toLowerCase())) {
							System.out.println("Sending message");
							ct.writeMsg(username + " told you directly: " +msg);
						}
					}
					break;
				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.JOIN:
					String topicName = message.toLowerCase();
					System.out.println("trying .. " + topicName);
					if (!topics.containsKey(topicName)) {
						topics.put(topicName, new ChatTopic(topicName) );
					}
					topics.get(topicName).users.add(this);
					for (ClientThread c : topics.get(topicName).users) {
						System.out.println("U  = " + c.username);
					}
					break;
				case ChatMessage.PART:
					break;
				case ChatMessage.QUIT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.ONLINE:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					for(int i = 0; i < clients.size(); ++i) {
						ClientThread ct = clients.get(i);
						System.out.println("Sending: " + ct.username);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(userId);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				System.out.println("Disconnecting the user");
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}

