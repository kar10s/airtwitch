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
