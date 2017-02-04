/**
 * SearchReply.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch.replies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpRequest;
import de.martindreier.airtwitch.twitch.ResponseHelper;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class SearchReply extends AbstractReply
{

	public SearchReply(int channelCount)
	{
		super(Pattern.compile(".*/kraken/search/channels\\?(.*)=(.+)"), ResponseHelper.searchResponse(channelCount));
	}

	/**
	 * @see de.martindreier.airtwitch.twitch.replies.AbstractReply#doesMatch(java.util.regex.Matcher)
	 */
	@Override
	protected boolean doesMatch(HttpRequest request, Matcher requestMatcher)
	{
		return requestMatcher.group(1).equals("query");
	}

}
