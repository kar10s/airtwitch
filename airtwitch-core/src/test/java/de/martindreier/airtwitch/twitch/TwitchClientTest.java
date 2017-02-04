/**
 * TwitchClientTest.java
 * Created: 14.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.test.HttpTestClient;
import de.martindreier.airtwitch.twitch.Channel.ChannelInfo;

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
	 * GSON instance.
	 */
	protected Gson							gson						= new GsonBuilder()
					.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

	/**
	 * Client under test.
	 */
	private Twitch							testClient;

	/**
	 * HTTP client for testing.
	 */
	private HttpTestClient			httpClient			= new HttpTestClient();

	/**
	 * Successful reply.
	 */
	@Mock
	private StatusLine					statusOk;

	@Before
	public void initializeMocks()
	{
		MockitoAnnotations.initMocks(this);

		// Response OK
		when(statusOk.getStatusCode()).thenReturn(200);
		when(statusOk.getReasonPhrase()).thenReturn("OK");
		when(statusOk.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
	}

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

	/**
	 * Function constructing an HTTP response for a channel. Returns
	 * <code>null</code> if a channel other than the specified channel is
	 * returned.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	protected class ChannelReply implements Function<HttpRequest, CloseableHttpResponse>
	{

		/**
		 * Pattern to match the request URI.
		 */
		private final Pattern					requestPattern;

		/**
		 * Response object.
		 */
		@Mock
		private CloseableHttpResponse	responseOk;

		/**
		 * ID of the channel for which this channel response is valid.
		 */
		private String								channelId;

		/**
		 * @param string
		 */
		public ChannelReply(String channelId)
		{
			this.channelId = channelId;
			this.requestPattern = Pattern.compile(".*/kraken/channels/(.*?)(\\?.*)?");
			this.initMocks(createChannelInfo(channelId));
		}

		/**
		 * Create channel info object for response content.
		 *
		 * @param channelId
		 *          ID of the channel.
		 * @return Channel info with default values and given channel ID.
		 */
		protected ChannelInfo createChannelInfo(String channelId)
		{
			ChannelInfo channelInfo = new ChannelInfo();
			channelInfo.id = channelId;
			channelInfo.name = "Test";
			channelInfo.createdAt = new Date();
			channelInfo.displayName = "Test";
			channelInfo.broadcasterLanguage = "german";
			channelInfo.followers = 1;
			channelInfo.game = "Test Game";
			channelInfo.language = "german";
			channelInfo.mature = false;
			channelInfo.status = "Streaming for test";
			channelInfo.updatedAt = new Date();
			channelInfo.views = 1;
			return channelInfo;
		}

		/**
		 * Initialize response mock.
		 *
		 * @param channelInfo
		 *          Response content.
		 */
		protected void initMocks(ChannelInfo channelInfo)
		{
			MockitoAnnotations.initMocks(this);

			// Success response
			when(responseOk.getStatusLine()).thenReturn(statusOk);
			HttpEntity value = new StringEntity(gson.toJson(channelInfo), ContentType.APPLICATION_JSON);
			when(responseOk.getEntity()).thenReturn(value);
		}

		/**
		 * Handle the request.
		 *
		 * @return A successful response if the requested channel ID matches the ID
		 *         for which this reply was created. <code>null</code> if a
		 *         different channel is requested.
		 *
		 * @see java.util.function.Consumer#accept(java.lang.Object)
		 */
		@Override
		public CloseableHttpResponse apply(HttpRequest request)
		{
			assertNotNull("Twitch API called without request", request);
			Matcher requestMatcher = requestPattern.matcher(request.getRequestLine().getUri());
			if (requestMatcher.matches())
			{
				if (requestMatcher.group(1).equalsIgnoreCase(channelId))
				{
					return responseOk;
				}
			}
			return null;
		}

	}
}
