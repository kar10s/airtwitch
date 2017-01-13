/**
 * AirPlayServiceDiscovery.java
 * Created: 13.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.airplay;

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
public class AirPlayServiceDiscovery
{
	public static final String AIRPLAY_SERVICE_TYPE = "_airplay._tcp.local.";

	/**
	 * Register a listener for device information.
	 *
	 * @param deviceResolutionCallback
	 *          Will be called when a device (service) resolves.
	 * @throws AirTwitchException
	 */
	public void registerListener(Consumer<DeviceInfo> deviceResolutionCallback) throws AirTwitchException
	{
		try
		{
			JmDNS jmdns = JmDNS.create();
			jmdns.addServiceListener(AIRPLAY_SERVICE_TYPE, new AirPlayServiceListener(deviceResolutionCallback));
		}
		catch (IOException exception)
		{
			throw new AirTwitchException("Cannot register service listener", exception);
		}
	}
}
