/**
 * Streams.java
 * Created: 26.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui.internal;

import java.util.List;
import java.util.function.Consumer;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.twitch.Channel;
import de.martindreier.airtwitch.twitch.LiveStream;
import de.martindreier.airtwitch.twitch.Twitch;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Stream data model.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class Streams
{
	/**
	 * Search result list for channel search.
	 */
	private ListProperty<Channel>			channels	= new SimpleListProperty<>(FXCollections.observableArrayList());

	/**
	 * List of streams for selected channel.
	 */
	private ListProperty<LiveStream>	streams		= new SimpleListProperty<>(FXCollections.observableArrayList());

	/**
	 * Twitch client instance.
	 */
	private Twitch										twitchClient;

	/**
	 * Create a new instance of the stream data model. Initializes the Twitch
	 * client.
	 *
	 * @throws AirTwitchException
	 *           Error while initializing the Twitch client.
	 */
	public Streams() throws AirTwitchException
	{
		twitchClient = new Twitch();
	}

	/**
	 * Get the list of channels returned by the search. Call
	 * {@link #searchStreams(String, Consumer)} to update the list.
	 *
	 * @return Channels property.
	 */
	public ListProperty<Channel> getChannels()
	{
		return channels;
	}

	/**
	 * Get the list of streams for the selected channel. Call
	 * {@link #getStreamsForChannel(Channel)} to update the list.
	 *
	 * @return Streams property.
	 */
	public ListProperty<LiveStream> getStreams()
	{
		return streams;
	}

	/**
	 * Update the stream list with the streams for a channel.
	 *
	 * @param selectedChannel
	 *          The selected channel. May be <code>null</code>.
	 */
	public void getStreamsForChannel(Channel selectedChannel)
	{
		if (selectedChannel != null)
		{
			try
			{
				selectedChannel.requestChannelToken();
				streams.setAll(selectedChannel.getLiveStreams());
			}
			catch (AirTwitchException exception)
			{
				ErrorDialog.showError("Error finding streams for selected channel", exception);
			}
		}
		else
		{
			streams.clear();
		}
	}

	/**
	 * Search for streams. After this method returns the list of streams obtained
	 * from {@link #getStreams()} is updated with the search results.
	 *
	 * @param searchTerm
	 *          The search term.
	 */
	public void searchStreams(String searchTerm)
	{
		this.channels.clear();
		this.streams.clear();
		try
		{
			List<Channel> channels = twitchClient.searchChannels(searchTerm);
			this.channels.addAll(channels);
		}
		catch (AirTwitchException exception)
		{
			ErrorDialog.showError("Error searching for streams", exception);
		}
	}

}
