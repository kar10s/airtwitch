/**
 * ChannelReply.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch.replies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.mockito.Mock;
import de.martindreier.airtwitch.twitch.ResponseHelper;

/**
 * Function constructing an HTTP response for a channel. Returns
 * <code>null</code> if a channel other than the specified channel is returned.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class ChannelReply extends AbstractReply
{

	/**
	 * Successful reply.
	 */
	@Mock
	private StatusLine	statusOk;

	/**
	 * ID of the channel for which this channel response is valid.
	 */
	private String			channelId;

	/**
	 * @param string
	 */
	public ChannelReply(String channelId)
	{
		super(Pattern.compile(".*/kraken/channels/(.*?)(\\?.*)?"));
		this.channelId = channelId;
		this.initMocks();
	}

	/**
	 * @see de.martindreier.airtwitch.twitch.replies.AbstractReply#getResponseContent()
	 */
	@Override
	protected HttpEntity getResponseContent()
	{
		return ResponseHelper.channelResponse(channelId);
	}

	/**
	 * @see de.martindreier.airtwitch.twitch.replies.AbstractReply#doesMatch(java.util.regex.Matcher)
	 */
	@Override
	protected boolean doesMatch(Matcher requestMatcher)
	{
		return requestMatcher.group(1).equalsIgnoreCase(channelId);
	}

}