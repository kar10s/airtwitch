/**
 * Demo.java
 * Created: 22.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.twitch;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.AirPlayServiceDiscovery;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import de.martindreier.airtwitch.airplay.StreamControl;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class Demo
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try (AirPlayServiceDiscovery discovery = new AirPlayServiceDiscovery())
		{
			CompletableFuture<DeviceInfo> info = new CompletableFuture<>();
			discovery.registerListener(device -> info.complete(device));

			Twitch twitch = new Twitch();
			Channel theChannel = null;
			for (Channel channel : twitch.searchChannels("99damage"))
			{
				System.out.println(channel.getName() + ": " + channel.getStatus());
				if (channel.getName().equals("99Damage"))
				{
					theChannel = channel;
				}
			}
			theChannel.requestChannelToken();
			LiveStream theStream = null;
			if (theChannel.isLive())
			{
				List<LiveStream> liveStreams = theChannel.getLiveStreams();
				for (LiveStream liveStream : liveStreams)
				{
					System.out.println(liveStream.getTitle() + ": " + liveStream.getStreamUri());
					if (liveStream.getTitle().equals("medium"))
					{
						theStream = liveStream;
					}
				}
			}
			else
			{
				System.out.println("NOT LIVE");
			}
			if (theStream != null)
			{
				StreamControl control = info.get(1, TimeUnit.MINUTES).createStream(theStream.getStreamUri());
				System.out.println("Starting");
				control.play();
				System.out.println("Running, press <Enter> to exit...");
				System.in.read();
				System.out.println("Stopping");
				control.stop();
			}
		}
		catch (AirTwitchException | InterruptedException | ExecutionException | TimeoutException | IOException exception)
		{
			exception.printStackTrace();
		}
	}

}
