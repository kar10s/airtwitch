/**
 * Twitch.java
 * Created: 13.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.martindreier.airtwitch.AirTwitchException;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class Twitch
{
	/**
	 * Environment variable: twitch client ID.
	 */
	private static final String				ENV_TWITCH_CLIENT_ID		= "TWITCH_CLIENT_ID";

	/**
	 * File name for twitch client ID provide.
	 */
	private static final String				TWITCH_CLIENT_ID_FILE		= "/twitch_client_id";

	/**
	 * Java system property: twitch client ID.
	 */
	private static final String				PROP_TWITCH_CLIENT_ID		= "twitchClientId";

	/**
	 * HTTP header field: Client ID.
	 */
	private static final String				HEADER_TWITCH_CLIENT_ID	= "Client-ID";

	/**
	 * Logging instance.
	 */
	private static final Logger				log											= Logger.getLogger(Twitch.class.getName());

	/**
	 * Indicator for Twitch API v5.
	 */
	protected static final String			TWITCH_API_V5						= "application/vnd.twitchtv.v5+json";

	/**
	 * Hostname for Twitch API.
	 */
	private static final String				TWITCH_API_HOST					= "api.twitch.tv";

	/**
	 * Twitch API client secret.
	 */
	private final String							clientID;

	/**
	 * Http client to connect to the Twitch API.
	 */
	private final CloseableHttpClient	httpClient;

	/**
	 * GSon instance.
	 */
	private final Gson								gson;

	/**
	 * Create a new Twitch API client.
	 *
	 * @throws AirTwitchException
	 */
	public Twitch() throws AirTwitchException
	{
		gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		clientID = determineClientID();
		httpClient = initializeHttpClient();
	}

	/**
	 * Initialize the HTTP client to connect to the Twitch API.
	 *
	 * @return The HTTP client.
	 */
	protected CloseableHttpClient initializeHttpClient()
	{
		List<Header> defaultHeaders = new ArrayList<>(2);
		defaultHeaders.add(new BasicHeader(HEADER_TWITCH_CLIENT_ID, clientID));
		return HttpClients.custom().setDefaultHeaders(defaultHeaders).build();
	}

	/**
	 * Get a channel by its ID or name.
	 *
	 * @param channelId
	 *          The channel ID.
	 * @return Channel object.
	 * @throws AirTwitchException
	 *           Error during the request.
	 */
	public Channel getChannelById(String channelId) throws AirTwitchException
	{
		String getChannelPath = String.format("/kraken/channels/%s", channelId);
		return get(getChannelPath, Collections.emptyList(), Channel.ChannelInfo.class,
						(twitch, channelInfo) -> new Channel(twitch, channelInfo), null);
	}

	/**
	 * Search channels.
	 *
	 * @param searchText
	 *          The search text.
	 * @return List fo channels matching the search text.
	 * @throws AirTwitchException
	 *           Error during the request,
	 */
	public List<Channel> searchChannels(String searchText) throws AirTwitchException
	{
		String searchChannelPath = "/kraken/search/channels";
		List<NameValuePair> parameters = new ArrayList<>(1);
		parameters.add(new BasicNameValuePair("query", searchText));
		BiFunction<Twitch, Channel.ChannelSearchResult, List<Channel>> resultHandler = (Twitch twitch,
						Channel.ChannelSearchResult searchResult) -> Arrays.stream(searchResult.channels)
										.map(channelInfo -> new Channel(twitch, channelInfo)).collect(Collectors.toList());
		return get(searchChannelPath, parameters, Channel.ChannelSearchResult.class, resultHandler,
						() -> Collections.emptyList());
	}

	/**
	 * Send a GET request to the twitch API.
	 *
	 * @param path
	 *          The request path.
	 * @param resultType
	 *          The result type of the request for JSON deserialization.
	 * @param resultHandler
	 *          Handler for the result to create the return object.
	 * @param errorHandler
	 *          Error handler to provide a result in case of error. May be
	 *          <code>null</code>.
	 * @param queryParameters
	 *          Query parameters. May be empty but not null.
	 * @return The object constructed by the <code>resultHandler</code>. In the
	 *         case of an error, the <code>errorHandler</code> is called to
	 *         provied the return value. If the <code>errorHandler</code> is
	 *         <code>null</code>, this function return <code>null</code>.
	 * @throws AirTwitchException
	 *           Error during the request.
	 */
	public <ReturnType, ResultType> ReturnType get(String path, List<NameValuePair> queryParameters,
					Class<ResultType> resultType, BiFunction<Twitch, ResultType, ReturnType> resultHandler,
					Supplier<ReturnType> errorHandler) throws AirTwitchException
	{
		try
		{
			URI getChannel = new URIBuilder().setScheme("https").addParameters(queryParameters).setHost(TWITCH_API_HOST)
							.setPath(path).build();
			HttpGet get = new HttpGet(getChannel);
			get.addHeader(new BasicHeader(HttpHeaders.ACCEPT, TWITCH_API_V5));
			AtomicReference<ResultType> resultInfo = new AtomicReference<>();
			boolean success = sendRequest(get, resultType, result -> resultInfo.set(result), null);
			if (success)
			{
				return resultHandler.apply(this, resultInfo.get());
			}
			else
			{
				if (errorHandler == null)
				{
					return null;
				}
				else
				{
					return errorHandler.get();
				}
			}
		}
		catch (URISyntaxException | IOException exception)
		{
			throw new AirTwitchException("Error sending request to %s for %s", exception, path, resultType.getName());
		}
	}

	/**
	 * Send a request to the Twitch API.
	 *
	 * @param request
	 *          The request to send.
	 * @param successHandler
	 *          Handler called for successful call. Must accept the unparsed
	 *          response content. May not be <code>null</code>.
	 * @param errorHandler
	 *          Handler for errors during the request. May be <code>null</code>.
	 * @return <code>true</code> if the request was successful, <code>false</code>
	 *         if it failed.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	boolean sendRequest(HttpUriRequest request, Consumer<String> successHandler,
					BiConsumer<StatusLine, String> errorHandler) throws ClientProtocolException, IOException
	{
		log.fine(() -> String.format("Sending request to %s", request.getURI().toString()));
		try (CloseableHttpResponse response = httpClient.execute(request))
		{
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() >= 200 && status.getStatusCode() < 300)
			{
				// Response OK
				log.fine(() -> String.format("Response OK (%d)", status.getStatusCode()));
				String responseContent = EntityUtils.toString(response.getEntity());
				successHandler.accept(responseContent);
				return true;
			}
			else
			{
				// Response not OK
				log.warning(() -> String.format("Request failed (%d): %s", status.getStatusCode(), status.getReasonPhrase()));
				if (errorHandler != null)
				{
					String responseContent = null;
					if (response.getEntity() != null)
					{
						responseContent = EntityUtils.toString(response.getEntity());
					}
					errorHandler.accept(status, responseContent);
				}
				return false;
			}
		}
	}

	/**
	 * Send a request to the Twitch API.
	 *
	 * @param request
	 *          The request to send.
	 * @param resultType
	 *          Type of the content result. may be <code>null</code> for requests
	 *          which do not expect a response.
	 * @param successHandler
	 *          Handler called for successful call. Must accept the parsed
	 *          response content. May be <code>null</code> if
	 *          <code>resultType</code> is <code>null</code>.
	 * @param errorHandler
	 *          Handler for errors during the request. May be <code>null</code>.
	 * @return <code>true</code> if the request was successful, <code>false</code>
	 *         if it failed.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	<ResultType> boolean sendRequest(HttpUriRequest request, Class<ResultType> resultType,
					Consumer<ResultType> successHandler, BiConsumer<StatusLine, String> errorHandler)
					throws ClientProtocolException, IOException
	{
		BiConsumer<StatusLine, String> loggingErrorHandler = (status, content) -> {
			log.severe(() -> String.format("Request failed, Error message %s\n%s", status.getReasonPhrase(), content));
			if (errorHandler != null)
			{
				errorHandler.accept(status, content);
			}
		};
		Consumer<String> converter;
		if (resultType == null)
		{
			converter = content -> {};
		}
		else
		{
			converter = responseContent -> {
				ResultType content = gson.fromJson(responseContent, resultType);
				successHandler.accept(content);
			};
		}
		return sendRequest(request, converter, loggingErrorHandler);
	}

	/**
	 * Determine the client ID. The following sources are used:
	 * <ol>
	 * <li>A Java system property with the name <code>twitchClientId</code>.</li>
	 * <li>A file named <code>twitch_client_id</code> on the classpath which
	 * contains only the ID.</li>
	 * <li>An environment variable named <code>TWITCH_CLIENT_ID</code>.</li>
	 * </ol>
	 *
	 * @return The client ID.
	 * @throws AirTwitchException
	 *           Thrown when no client ID is found.
	 */
	protected String determineClientID() throws AirTwitchException
	{
		log.entering(Twitch.class.getName(), "determineClientID");
		// 1: parameter
		if (System.getProperty(PROP_TWITCH_CLIENT_ID) != null)
		{
			log.info(() -> String.format("Determined twitch client ID %s from system property %s",
							System.getProperty(PROP_TWITCH_CLIENT_ID), PROP_TWITCH_CLIENT_ID));
			log.exiting(Twitch.class.getName(), "determineClientID", System.getProperty(PROP_TWITCH_CLIENT_ID));
			return System.getProperty(PROP_TWITCH_CLIENT_ID).trim();
		}
		// 2: Configuration file
		if (getClass().getResource(TWITCH_CLIENT_ID_FILE) != null)
		{
			try (InputStream in = getClass().getResourceAsStream(TWITCH_CLIENT_ID_FILE))
			{
				if (in != null)
				{
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String clientId = reader.readLine().trim();
					reader.close();
					log.info(() -> String.format("Loaded client ID %s from file %s", clientId, TWITCH_CLIENT_ID_FILE));
					log.exiting(Twitch.class.getName(), "determineClientID", clientId);
					return clientId;
				}
				else
				{
					log.info(() -> "Client ID file not present");
				}
			}
			catch (IOException exception)
			{
				AirTwitchException e = new AirTwitchException("Could not load twitch client ID file", exception);
				log.throwing(Twitch.class.getName(), "determineClientID", e);
				throw e;
			}
		}
		// 3: Environment variable
		if (System.getenv(ENV_TWITCH_CLIENT_ID) != null)
		{
			log.info(() -> String.format("Determined twitch client ID %s from environment variable %s",
							System.getenv(ENV_TWITCH_CLIENT_ID), ENV_TWITCH_CLIENT_ID));
			log.exiting(Twitch.class.getName(), "determineClientID", System.getenv(ENV_TWITCH_CLIENT_ID));
			return System.getenv(ENV_TWITCH_CLIENT_ID).trim();
		}
		// Fail if no client ID is found
		AirTwitchException e = new AirTwitchException("Cannot determine client ID");
		log.throwing(Twitch.class.getName(), "determineClientID", e);
		throw e;
	}
}
