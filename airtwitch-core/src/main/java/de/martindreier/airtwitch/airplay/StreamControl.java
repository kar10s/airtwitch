/**
 * StreamControl.java
 * Created: 22.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.airplay;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import de.martindreier.airtwitch.AirTwitchException;

/**
 * Control instance to manipulate a running stream.
 *
 * @see DeviceInfo#createStream(URI)
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class StreamControl
{

	/**
	 * Availabe commands to send to the device.
	 *
	 * @author Martin Dreier <martin@martindreier.de>
	 *
	 */
	private static enum Command
	{
		PLAY("/play"), STOP("/stop");

		/**
		 * Command URI.
		 */
		private URI uri;

		private Command(String uri)
		{
			this.uri = URI.create(uri);
		}
	}

	/**
	 * Client instance.
	 */
	private CloseableHttpClient	client;
	/**
	 * Stream content URI.
	 */
	private URI									contentURI;
	/**
	 * Device information.
	 */
	private DeviceInfo					deviceInfo;

	/**
	 * Logging instance.
	 */
	private static final Logger	log	= Logger.getLogger(Command.class.getName());

	/**
	 * Create a new stream control.
	 *
	 * @param deviceInfo
	 *          Target device.
	 * @param client
	 *          Client instance.
	 * @param contentURI
	 *          Stream content URI.
	 * @see DeviceInfo#createStream(URI)
	 */
	StreamControl(DeviceInfo deviceInfo, CloseableHttpClient client, URI contentURI)
	{
		log.entering(StreamControl.class.getName(), "<init>", new Object[] { deviceInfo, client, contentURI });
		this.deviceInfo = deviceInfo;
		this.client = client;
		this.contentURI = contentURI;
		log.exiting(StreamControl.class.getName(), "<init>");
	}

	/**
	 * Stop the stream.
	 *
	 * @throws AirTwitchException
	 */
	public void stop() throws AirTwitchException
	{
		log.entering(StreamControl.class.getName(), "stop");
		sendRequest(Command.STOP, null);
		log.exiting(StreamControl.class.getName(), "stop");
	}

	/**
	 * Start the stream.
	 *
	 * @throws AirTwitchException
	 *
	 */
	public void play() throws AirTwitchException
	{
		log.entering(StreamControl.class.getName(), "play");
		List<NameValuePair> content = new ArrayList<>(2);
		content.add(new BasicNameValuePair("Content-Location", contentURI.toString()));
		content.add(new BasicNameValuePair("Start-Position", "0.0"));
		sendRequest(Command.PLAY, content);
		log.exiting(StreamControl.class.getName(), "play");
	}

	/**
	 * Send a command request to the device.
	 *
	 * @param command
	 *          The command to send.
	 * @param content
	 *          The content to send to the device. May be <code>null</code>.
	 * @throws AirTwitchException
	 */
	protected void sendRequest(Command command, List<NameValuePair> content) throws AirTwitchException
	{
		log.entering(StreamControl.class.getName(), "sendRequest", new Object[] { command, content });
		try
		{
			HttpPost request = new HttpPost(deviceInfo.getUri().resolve(command.uri));
			log.fine(() -> String.format("Sending request %s", request));

			if (content != null && content.size() > 0)
			{
				StringBuilder contentBuilder = new StringBuilder();
				for (NameValuePair parameter : content)
				{
					contentBuilder.append(parameter.getName());
					contentBuilder.append(": ");
					contentBuilder.append(parameter.getValue());
					contentBuilder.append("\n");
				}
				request.setEntity(EntityBuilder.create().setText(contentBuilder.toString()).build());
				log.fine(() -> String.format("Sending request with content:\n%s", contentBuilder.toString()));
			}
			try (CloseableHttpResponse response = client.execute(request))
			{
				if (log.isLoggable(Level.INFO))
				{
					String responseContent = EntityUtils.toString(response.getEntity());
					log.info(String.format("Response from device %s: %s", deviceInfo.getName(), responseContent));
				}
			}
			log.exiting(StreamControl.class.getName(), "sendRequest");
		}
		catch (URISyntaxException | IOException exception)
		{
			log.log(Level.SEVERE, "Request failed", exception);
			AirTwitchException e = new AirTwitchException("Could not send command %s to device %s", exception, command.name(),
							deviceInfo.getName());
			log.throwing(StreamControl.class.getName(), "sendRequest", e);
			throw e;
		}
	}
}
