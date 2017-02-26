/**
 * Devices.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui.internal;

import java.io.IOException;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.AirPlayServiceDiscovery;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Device handler.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class Devices
{
	/**
	 * List of discovered devices.
	 */
	private ListProperty<DeviceInfo>	devices		= new SimpleListProperty<>(FXCollections.observableArrayList());

	/**
	 * Singleton instance.
	 */
	private static Devices						instance	= new Devices();

	/**
	 * Service discovery instance.
	 */
	private AirPlayServiceDiscovery		serviceDiscovery;

	private Devices()
	{
		// To make instance inaccessible
	}

	/**
	 * Get device handler instance.
	 *
	 * @return Singleton instance.
	 */
	public static synchronized Devices getInstance()
	{
		return instance;
	}

	/**
	 * Initialize the device handler and start listening to the service discovery.
	 */
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
				ErrorDialog.showError("Could not register device listener!", exception);
			}
		}
	}

	/**
	 * Shut down the device listener. Terminates the service discovery.
	 */
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
				ErrorDialog.showError("Could not shut down device listener!", exception);
			}
			serviceDiscovery = null;
		}
	}

	/**
	 * Get the list of discovered devices.
	 *
	 * @return Devices property.
	 */
	public ListProperty<DeviceInfo> getDevices()
	{
		return devices;
	}
}
