package main;

import org.junit.Assert; 
import org.junit.Test;

public class ServerTest {

	@Test
	public void test() {
		Server server = new Server(8001);
		Assert.assertNotNull(server);
		
	}

}
