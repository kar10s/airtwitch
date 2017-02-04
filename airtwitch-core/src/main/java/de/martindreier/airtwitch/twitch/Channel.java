/**
 * Channel.java
 * Created: 14.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import com.google.gson.annotations.SerializedName;
import com.iheartradio.m3u8.Encoding;
import com.iheartradio.m3u8.Format;
import com.iheartradio.m3u8.ParseException;
import com.iheartradio.m3u8.PlaylistException;
import com.iheartradio.m3u8.PlaylistParser;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;
import de.martindreier.airtwitch.AirTwitchException;

/**
 * Channel information and access.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class Channel
{
	/**
	 * Reference to Twitch API.
	 */
	private final Twitch				api;

	/**
	 * Channel information.
	 */
	private ChannelInfo					channelInfo;

	/**
	 * Authorization token for this channel.
	 */
	private ChannelToken				channelToken;

	/**
	 * API endpoint for the user playlist API.
	 */
	private URI									usherApi;

	/**
	 * Random source.
	 */
	private Random							random					= new Random();

	/**
	 * Stream information.
	 */
	private Stream							streamInfo;

	/**
	 * Base URI for Usher API.
	 */
	// private static final String USHER_API_BASE =
	// "https://usher.twitch.tv/api/channel/hls/";
	private static final String	USHER_API_BASE	= "https://usher.ttvnw.net/api/channel/hls/";

	/**
	 * Create a new channel object.
	 *
	 * @param api
	 *          API reference for further requests.
	 * @param channelInfo
	 *          Channel information.
	 */
	Channel(Twitch api, ChannelInfo channelInfo)
	{
		// Check input arguments
		if (api == null)
		{
			throw new IllegalArgumentException("API reference may not be null");
		}
		if (channelInfo == null)
		{
			throw new IllegalArgumentException("Channel information may not be null");
		}
		if (channelInfo.name == null || channelInfo.id == null)
		{
			throw new IllegalArgumentException(
							String.format("Channel information is incomplete (id: %s, name: %s)", channelInfo.id, channelInfo.name));
		}
		// Set references
		this.api = api;
		this.channelInfo = channelInfo;
		// Build User API endpoint URL
		try
		{
			this.usherApi = new URI(USHER_API_BASE + channelInfo.name + ".m3u8");
		}
		catch (URISyntaxException exception)
		{
			throw new IllegalArgumentException(String.format("Could not build Usher API for channel %s (ID %s)", exception,
							channelInfo.name, channelInfo.id));
		}
	}

	/**
	 * Get the channel name.
	 *
	 * @return The readable channel name.
	 */
	public String getName()
	{
		return channelInfo.displayName;
	}

	public boolean isLive()
	{
		if (streamInfo == null)
		{
			try
			{
				readStreamInfo();
			}
			catch (AirTwitchException exception)
			{
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
		}
		return streamInfo != null;
	}

	/**
	 * Get channel status message.
	 *
	 * @return The status message.
	 */
	public String getStatus()
	{
		return channelInfo.status;
	}

	/**
	 * Request the channel token to authorize to the usher.
	 *
	 * @throws AirTwitchException
	 */
	public void requestChannelToken() throws AirTwitchException
	{
		String channelTokenPath = String.format("/api/channels/%s/access_token", channelInfo.name);
		channelToken = api.get(channelTokenPath, Collections.emptyList(), ChannelToken.class,
						(api, channelToken) -> channelToken, null);
	}

	protected void readStreamInfo() throws AirTwitchException
	{
		String streamInfoPath = String.format("/kraken/streams/%s", channelInfo.id);
		streamInfo = api.get(streamInfoPath, Collections.emptyList(), StreamInfo.class, (api, streamInfo) -> streamInfo,
						StreamInfo::new).stream;
	}

	/**
	 * Get list of live streams for this channel. List may be empty if the channel
	 * is not live.
	 *
	 * @return List of streams. May be empty but never <code>null</code>.
	 * @throws AirTwitchException
	 *           Error while retrieving the streams.
	 */
	public List<LiveStream> getLiveStreams() throws AirTwitchException
	{
		try
		{
			URI streamPlaylist = new URIBuilder(usherApi).addParameters(getChannelParameters()).build();
			HttpGet getStreamPlaylist = new HttpGet(streamPlaylist);
			getStreamPlaylist.addHeader("Accept", "application/vnd.apple.mpegurl");
			AtomicReference<String> playlistSource = new AtomicReference<String>();
			boolean success = api.sendRequest(getStreamPlaylist, content -> playlistSource.set(content),
							(status, content) -> System.err.println(content));
			if (success)
			{
				try (ByteArrayInputStream in = new ByteArrayInputStream(
								playlistSource.get().getBytes(Charset.forName("UTF-8"))))
				{
					PlaylistParser parser = new PlaylistParser(in, Format.M3U, Encoding.UTF_8);
					Playlist playlist = parser.parse();
					if (playlist.hasMediaPlaylist())
					{
						List<TrackData> tracks = playlist.getMediaPlaylist().getTracks();
						return tracks.stream().map(track -> track.getUri()).map(URI::create).map(LiveStream::build)
										.collect(Collectors.toList());
					}
					else
					{
						return Collections.emptyList();
					}
				}
			}
			else
			{
				return Collections.emptyList();
			}
		}
		catch (URISyntaxException | IOException | ParseException | PlaylistException exception)
		{
			throw new AirTwitchException("Could not retrieve live streams for channel %s", exception, channelInfo.name);
		}
	}

	/**
	 * Get channel parameters for Usher API.
	 *
	 * @return Parameter list.
	 */
	protected List<NameValuePair> getChannelParameters()
	{
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("player", "twitchweb"));
		params.add(new BasicNameValuePair("token", channelToken.token));
		params.add(new BasicNameValuePair("sig", channelToken.sig));
		params.add(new BasicNameValuePair("$allow_audio_only", "true"));
		params.add(new BasicNameValuePair("allow_source", "true"));
		params.add(new BasicNameValuePair("type", "any"));
		params.add(new BasicNameValuePair("p", Integer.toString(random.nextInt(999999))));
		return params;
	}

	/**
	 * Channel authorization token representation for JSON deserialization.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	static class ChannelToken
	{
		private String	token;
		private String	sig;
		@SuppressWarnings("unused")
		private String	mobileRestricted;
	}

	/**
	 * Channel information class for JSON deserialization.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	static class ChannelInfo
	{
		@SerializedName("_id")
		String	id;

		String	broadcasterLanguage;

		Date		createdAt;

		String	displayName;

		long		followers;

		String	game;

		String	language;

		boolean	mature;

		String	name;

		String	status;

		Date		updatedAt;

		long		views;
	}

	/**
	 * Channel search result class for JSON deserializtion.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	static class ChannelSearchResult
	{
		@SerializedName("_total")
		long					total;

		ChannelInfo[]	channels;
	}

	/**
	 * Outer wrapper for stream information for JSON deserialization.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	static class StreamInfo
	{
		private Stream stream;
	}

	/**
	 * Stream information for JSON deserialization.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	@SuppressWarnings("unused")
	static class Stream
	{
		@SerializedName("_id")
		private long		id;
		private String	game;
		private long		viewers;
		private int			videoHeight;
		private int			averageFps;
		private long		delay;
		private Date		createdAt;
		private boolean	isPlaylist;
	}
}
