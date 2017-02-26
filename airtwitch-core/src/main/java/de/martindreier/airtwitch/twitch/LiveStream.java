/**
 * LiveStream.java
 * Created: 22.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import java.net.URI;
import org.apache.http.util.Args;
import com.iheartradio.m3u8.data.PlaylistData;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class LiveStream
{
	/**
	 * Stream title.
	 */
	private String	title;
	/**
	 * Stream URI.
	 */
	private URI			streamUri;

	/**
	 * Build a new live stream. The title in inferred from the URI.
	 *
	 * @param twitchStreamUri
	 *          URI for twitch stream.
	 * @return Live stream reference.
	 */
	public static LiveStream build(URI twitchStreamUri)
	{
		String title = "native";
		// Title should be the last non-filename segment of the path
		String[] pathSegments = twitchStreamUri.getPath().split("/");
		if (pathSegments.length > 2)
		{
			title = pathSegments[pathSegments.length - 2];
		}
		return new LiveStream(title, twitchStreamUri);
	}

	/**
	 * Build a new live stream. The title in inferred from the URI.
	 *
	 * @param streamPlaylist
	 *          Playlist information for twitch stream.
	 * @return Live stream reference.
	 */
	public static LiveStream build(PlaylistData streamPlaylist)
	{
		URI streamURI = URI.create(streamPlaylist.getUri());
		return new LiveStream(streamPlaylist.getStreamInfo().getVideo(), streamURI);
	}

	/**
	 * Create a new live stream reference.
	 *
	 * @param title
	 *          Stream title.
	 * @param streamUri
	 *          Stream URI.
	 */
	public LiveStream(String title, URI streamUri)
	{
		Args.notNull(title, "stream title");
		Args.notNull(streamUri, "stream URI");
		this.title = title;
		this.streamUri = streamUri;
	}

	/**
	 * Get the stream title.
	 *
	 * @return Stream title.
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Get the stream URI.
	 *
	 * @return The stream URI.
	 */
	public URI getStreamUri()
	{
		return streamUri;
	}

}
