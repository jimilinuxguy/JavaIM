package main;
import java.io.*;

public class ChatMessage implements Serializable {


	static final int ONLINE = 0;
	static final int MESSAGE = 1;
	static final int QUIT = 2;
	static final int JOIN = 3;
	static final int PART = 4;
	static final int DIRECT = 5;
	
	static final long serialVersionUID = 1233211;	
	
	private int type;
	private String message;
	

	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	int getType() {
		return type;
	}
	String getMessage() {
		return message;
	}
}

