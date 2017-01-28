/**
 * StreamCommands.java
 * Created: 28.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.cli.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import com.budhash.cliche.Command;
import com.budhash.cliche.Param;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.twitch.Channel;
import de.martindreier.airtwitch.twitch.LiveStream;
import de.martindreier.airtwitch.twitch.Twitch;

/**
 * Commands to list and handle streams.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class StreamCommands
{
	/**
	 * Twitch API instance.
	 */
	private Twitch			twitch;
	private Channel			selectedChannel;
	private LiveStream	selectedStream;

	/**
	 * Initialize Stream commands.
	 *
	 * @throws RuntimeException
	 *           Thrown when the Twitch API cannot be instantiated.
	 */
	public StreamCommands()
	{
		try
		{
			twitch = new Twitch();
		}
		catch (AirTwitchException exception)
		{
			System.out.println("Could not instantiate Twitch API: " + exception.getLocalizedMessage());
			exception.printStackTrace();
			// TODO: Implement better error handling
			throw new RuntimeException("Twitch API not instantiated", exception);
		}
	}

	/**
	 * Get the currently selected stream.
	 * 
	 * @return The selected stream, or <code>null</code> if no stream is selected.
	 */
	public LiveStream getSelectedStream()
	{
		return selectedStream;
	}

	/**
	 * Print the currently selected stream to the console.
	 */
	@Command(name = "print", description = "Print the name of the selected stream")
	public void printSelectedStream()
	{
		if (selectedChannel == null)
		{
			System.out.println("No channel selected");
		}
		else
		{
			System.out.print("Selected stream ");
			System.out.print(selectedStream.getTitle());
			System.out.print(" from channel ");
			System.out.println(selectedChannel.getName());
		}
	}

	/**
	 * Search for a channel. If at least one channel is found, a selection menu is
	 * printed to select a channel.
	 *
	 * @param searchTerm
	 *          The search term. Must not be empty.
	 */
	@Command(description = "Search for a channel")
	public void search(@Param(name = "searchTerm", description = "Search query") String searchTerm)
	{
		if (searchTerm == null || searchTerm.trim().isEmpty())
		{
			System.out.println("Enter a query for the channel search");
			return;
		}
		try
		{
			List<Channel> channels = twitch.searchChannels(searchTerm);
			if (channels.isEmpty())
			{
				System.out.print("No search results for ");
				System.out.println(searchTerm);
				return;
			}
			selectChannel(channels).ifPresent(this::selectStream);
		}
		catch (AirTwitchException | IOException exception)
		{
			System.out.println("Search failed");
			exception.printStackTrace();
		}
	}

	/**
	 * Select a channel from the list.
	 *
	 * @param channels
	 *          List of channels to select from.
	 * @return Optional containing selected channel. Empty if none is selected.
	 * @throws IOException
	 */
	protected Optional<Channel> selectChannel(List<Channel> channels) throws IOException
	{
		System.out.println("Select channel (press enter to keep the current channel):");
		printList(channels, 0, channel -> channel.getName() + ": " + channel.getStatus());
		return selectItem(channels);
	}

	/**
	 * Select a stream from the given channel.
	 *
	 * @param channel
	 *          Channel to select from.
	 */
	protected void selectStream(Channel channel)
	{
		try
		{
			channel.requestChannelToken();
			if (!channel.isLive())
			{
				System.out.print("Channel ");
				System.out.print(channel.getName());
				System.out.println(" is not live");
				return;
			}
			List<LiveStream> streams = channel.getLiveStreams();
			if (streams.isEmpty())
			{
				System.out.print("Channel ");
				System.out.print(channel.getName());
				System.out.println(" has no live streams");
				return;
			}
			System.out.println("Select live stream (press enter to keep the current stream):");
			printList(streams, 0, LiveStream::getTitle);
			selectItem(streams).ifPresent(selectedStream -> {
				this.selectedChannel = channel;
				this.selectedStream = selectedStream;
				printSelectedStream();
			});
		}
		catch (AirTwitchException | IOException exception)
		{
			System.out.println("Could not select a channel");
			exception.printStackTrace();
		}
	}

	/**
	 * Print a list of items to the console.
	 *
	 * @param list
	 *          List of items
	 * @param startIndex
	 *          Starting number of the output.
	 * @param printFunction
	 *          Function to convert items to text.
	 */
	protected <T> void printList(List<T> list, int startIndex, Function<T, String> printFunction)
	{
		int index = startIndex;
		for (T item : list)
		{
			System.out.print(index);
			System.out.print(") ");
			System.out.println(printFunction.apply(item));
			index++;
		}
	}

	protected <T> Optional<T> selectItem(List<T> list) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		do
		{
			String selection = in.readLine();
			if (selection == null || selection.trim().isEmpty())
			{
				return Optional.empty();
			}
			try
			{
				int selectedIndex = Integer.parseInt(selection);
				if (selectedIndex < 0 || selectedIndex >= list.size())
				{
					System.out.print(selection);
					System.out.println(" is not a valid selection");
				}
				else
				{
					return Optional.of(list.get(selectedIndex));
				}
			}
			catch (NumberFormatException e)
			{
				System.out.print(selection);
				System.out.println(" is not a valid selection");
			}
		} while (true);
	}
}
