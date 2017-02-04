/**
 * Devices.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui;

import java.io.IOException;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.AirPlayServiceDiscovery;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

/**
 * Device handler.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class Devices
{
	private ListProperty<DeviceInfo>	devices		= new SimpleListProperty<>();

	private static Devices						instance	= new Devices();

	private AirPlayServiceDiscovery		serviceDiscovery;

	private Devices()
	{
		// To make instance inaccessible
	}

	public static synchronized Devices getInstance()
	{
		return instance;
	}

	public synchronized void initialize()
	{
		if (serviceDiscovery == null)
		{
			serviceDiscovery = new AirPlayServiceDiscovery();
			try
			{
				serviceDiscovery.registerListener(device -> devices.add(device));
			}
			catch (AirTwitchException exception)
			{
				AirTwitch.showError("Could not register device listener!", exception);
			}
		}
	}

	public synchronized void shutdown()
	{
		if (serviceDiscovery != null)
		{
			try
			{
				serviceDiscovery.close();
			}
			catch (IOException exception)
			{
				AirTwitch.showError("Could not shut down device listener!", exception);
			}
			serviceDiscovery = null;
		}
	}
}
