package main;

import org.junit.Assert; 
import org.junit.Test;


public class ChatMessageTest {

	@Test
	public void test() {
		ChatMessage cm = new ChatMessage(0, null);
		Assert.assertEquals(0, ChatMessage.ONLINE);
		Assert.assertEquals(0, cm.getType());

		cm = new ChatMessage(1, "Hello World");
		Assert.assertEquals(1,  ChatMessage.MESSAGE);
		Assert.assertEquals(1, cm.getType());
		Assert.assertEquals("Hello World", cm.getMessage());

		cm = new ChatMessage(2, "Goodbye World");
		Assert.assertEquals(2,  ChatMessage.QUIT);
		Assert.assertEquals(2, cm.getType());
		Assert.assertEquals("Goodbye World", cm.getMessage());
		
	}

}
