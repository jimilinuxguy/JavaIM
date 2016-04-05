package main;

import org.junit.Assert; 
import org.junit.Test;

public class ClientTest {

	@Test
	public void test() {
		Client client = new Client(6667, "jimiSanchez", null);
		Assert.assertNotNull(client);
	}

}
