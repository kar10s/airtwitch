/**
 * ChannelReply.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch.replies;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Function constructing an HTTP response for a channel. Returns
 * <code>null</code> if a channel other than the specified channel is returned.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public abstract class AbstractReply implements Function<HttpRequest, CloseableHttpResponse>
{

	/**
	 * Pattern to match the request URI.
	 */
	private final Pattern						requestPattern;

	/**
	 * Response object.
	 */
	@Mock
	protected CloseableHttpResponse	responseOk;

	/**
	 * Successful reply.
	 */
	@Mock
	protected StatusLine						statusOk;

	/**
	 * @param string
	 */
	public AbstractReply(Pattern requestPattern, HttpEntity responseContent)
	{
		this.requestPattern = requestPattern;
		this.initMocks(responseContent);
	}

	/**
	 * Initialize response mock.
	 * 
	 * @param responseContent
	 *          Content of successful response.
	 *
	 * @param channelInfo
	 *          Response content.
	 */
	protected void initMocks(HttpEntity responseContent)
	{
		MockitoAnnotations.initMocks(this);

		// Response OK
		when(statusOk.getStatusCode()).thenReturn(200);
		when(statusOk.getReasonPhrase()).thenReturn("OK");
		when(statusOk.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

		// Success response
		when(responseOk.getStatusLine()).thenReturn(statusOk);
		when(responseOk.getEntity()).thenReturn(responseContent);
	}

	/**
	 * Handle the request.
	 *
	 * @return A successful response if the requested channel ID matches the ID
	 *         for which this reply was created. <code>null</code> if a different
	 *         channel is requested.
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
			if (doesMatch(request, requestMatcher))
			{
				return responseOk;
			}
		}
		return null;
	}

	protected abstract boolean doesMatch(HttpRequest request, Matcher requestMatcher);

}