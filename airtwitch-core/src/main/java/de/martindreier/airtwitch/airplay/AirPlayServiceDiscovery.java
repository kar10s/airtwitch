/**
 * AirPlayServiceDiscovery.java
 * Created: 13.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.airplay;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;
import javax.jmdns.JmDNS;
import de.martindreier.airtwitch.AirTwitchException;

/**
 * Discover AirPlay services.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AirPlayServiceDiscovery implements Closeable
{
	public static final String	AIRPLAY_SERVICE_TYPE	= "_airplay._tcp.local.";

	/**
	 * JmDNS instance.
	 */
	private JmDNS								jmdns;

	/**
	 * Initialize the JmDNS instance.
	 *
	 * @throws AirTwitchException
	 */
	protected synchronized void initialize() throws AirTwitchException
	{
		if (jmdns == null)
		{
			try
			{
				jmdns = JmDNS.create();
			}
			catch (IOException exception)
			{
				throw new AirTwitchException("Cannot register service listener", exception);
			}
		}
	}

	/**
	 * Register a listener for device information.
	 *
	 * @param deviceResolutionCallback
	 *          Will be called when a device (service) resolves.
	 * @throws AirTwitchException
	 */
	public void registerListener(Consumer<DeviceInfo> deviceResolutionCallback) throws AirTwitchException
	{
		initialize();
		jmdns.addServiceListener(AIRPLAY_SERVICE_TYPE, new AirPlayServiceListener(deviceResolutionCallback));
	}

	/**
	 * Stop listening to mDNS announcements.
	 *
	 * @throws AirTwitchException
	 */
	@Override
	public synchronized void close() throws IOException
	{
		if (jmdns != null)
		{
			jmdns.close();
		}
	}
}
