/**
 * DeviceList.java
 * Created: 25.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.cli.devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.AirPlayServiceDiscovery;
import de.martindreier.airtwitch.airplay.DeviceInfo;

/**
 * List of mDNS devices.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class DeviceList
{
	/**
	 * Singleton instance.
	 */
	private static DeviceList listInstance;

	/**
	 * Get the device list.
	 *
	 * @return Device list instance.
	 */
	public static synchronized DeviceList getInstance()
	{
		if (listInstance == null)
		{
			listInstance = new DeviceList();
			listInstance.initialize();
		}
		return listInstance;
	}

	/**
	 * Internal list of known devices.
	 */
	private final List<DeviceInfo>				devices						= new ArrayList<>(5);

	/**
	 * Service discovery instance.
	 */
	private final AirPlayServiceDiscovery	serviceDiscovery	= new AirPlayServiceDiscovery();

	/**
	 * Add a new device to the internal list.
	 *
	 * @param device
	 *          Device object.
	 */
	protected void addDevice(DeviceInfo device)
	{
		if (device == null)
		{
			throw new IllegalArgumentException("Device may not be null");
		}
		devices.add(device);
	}

	/**
	 * Initialize the device list.
	 */
	public void initialize()
	{
		try
		{
			serviceDiscovery.registerListener(this::addDevice);
		}
		catch (AirTwitchException exception)
		{
			System.err.println("Could not register device listener");
			exception.printStackTrace();
		}
	}

	/**
	 * Print listed devices to standard out.
	 */
	public void printDeviceList()
	{
		System.out.println("Discovered devices");
		int index = 0;
		for (DeviceInfo device : devices)
		{
			System.out.print(index);
			System.out.print(": ");
			System.out.println(device.getName());
		}
	}

	/**
	 * Get registered device at selected index.
	 * 
	 * @return Selected device, or <code>null</code> if index is invalid.
	 */
	public DeviceInfo getDevice(int index)
	{
		if (index < 0 || index >= devices.size())
		{
			return null;
		}
		else
		{
			return devices.get(index);
		}
	}

	/**
	 * Shut down the device list.
	 */
	public void shutdown()
	{
		if (serviceDiscovery != null)
		{
			try
			{
				serviceDiscovery.close();
			}
			catch (IOException exception)
			{
				System.err.println("Could not shut down device listener");
				exception.printStackTrace();
			}
		}
	}
}
