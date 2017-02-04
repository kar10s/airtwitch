/**
 * ResponseHelper.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.martindreier.airtwitch.twitch.Channel.ChannelInfo;
import de.martindreier.airtwitch.twitch.Channel.ChannelSearchResult;

/**
 * Helper class creating JSON responses for Twitch objects. Required due to
 * package access of internal classes.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class ResponseHelper
{
	/**
	 * GSON instance.
	 */
	protected static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
					.create();

	/**
	 * Create response content for a channel.
	 *
	 * @param channelId
	 *          The channel ID.
	 * @return Default content for a channel using the given channel ID.
	 */
	public static HttpEntity channelResponse(String channelId)
	{
		return new StringEntity(gson.toJson(createChannelInfo(channelId)), ContentType.APPLICATION_JSON);
	}

	/**
	 * Create response content for a search.
	 *
	 * @param channelCount
	 *          Number of channels in the search result.
	 * @return Default content for a number of channels.
	 */
	public static HttpEntity searchResponse(int channelCount)
	{
		return new StringEntity(gson.toJson(createSearchResult(channelCount)), ContentType.APPLICATION_JSON);
	}

	/**
	 * Create search result object for channel search.
	 * 
	 * @param channelCount
	 *          Number of channels in the object.
	 * @return Default content for search result.
	 */
	protected static ChannelSearchResult createSearchResult(int channelCount)
	{
		ChannelSearchResult result = new ChannelSearchResult();
		result.total = channelCount;
		result.channels = new ChannelInfo[channelCount];
		for (int index = 0; index < channelCount; index++)
		{
			result.channels[index] = createChannelInfo(String.format("%4d", index));
		}
		return result;
	}

	/**
	 * Create channel info object for response content.
	 *
	 * @param channelId
	 *          ID of the channel.
	 * @return Channel info with default values and given channel ID.
	 */
	protected static ChannelInfo createChannelInfo(String channelId)
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
}
