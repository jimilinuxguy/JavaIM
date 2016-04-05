package main;

import java.util.ArrayList;

import main.Server.ClientThread;

public class ChatTopic {

	private String topicName;
	public ArrayList<ClientThread> users;

	public ChatTopic(String topic) {
		topicName = topic;
		users = new ArrayList<ClientThread>();
	}
	
	public String getTopicName()
	{
		return topicName;
	}
}
