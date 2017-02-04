/**
 * TwitchClientTest.java
 * Created: 14.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.test.HttpTestClient;
import de.martindreier.airtwitch.twitch.replies.ChannelReply;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
@RunWith(JUnit4.class)
public class TwitchClientTest
{
	/**
	 * Twitch Client ID used for testing.
	 */
	private static final String	TEST_CLIENT_ID	= "xxxTESTxxx";

	/**
	 * ID of test channel.
	 */
	private static final String	TEST_CHANNEL_ID	= "1234";

	/**
	 * Client under test.
	 */
	private Twitch							testClient;

	/**
	 * HTTP client for testing.
	 */
	private HttpTestClient			httpClient			= new HttpTestClient();

	@Before
	public void initializeTestClient() throws AirTwitchException
	{
		testClient = new Twitch()
		{
			@Override
			protected String determineClientID()
			{
				// Provide static client ID
				return TEST_CLIENT_ID;
			}

			/**
			 * @see de.martindreier.airtwitch.twitch.Twitch#initializeHttpClient()
			 */
			@Override
			protected CloseableHttpClient initializeHttpClient()
			{
				return httpClient;
			}
		};
	}

	@After
	public void resetHttpClient() throws IOException
	{
		httpClient.close();
	}

	@Test
	public void getChannelById() throws AirTwitchException, ClientProtocolException, IOException
	{
		httpClient.registerHandler("/kraken/channels/(.*)", new ChannelReply(TEST_CHANNEL_ID));
		Channel c = testClient.getChannelById(TEST_CHANNEL_ID);
		assertNotNull("No channel returned", c);
	}

	@Test
	public void getNonexistingChannelById() throws AirTwitchException, ClientProtocolException, IOException
	{
		Channel c = testClient.getChannelById("0000");
		assertNull("Channel returned when none should be found", c);
	}

}
