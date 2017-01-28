/**
 * DeviceCommands.java
 * Created: 25.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.cli.commands;

import com.budhash.cliche.Command;
import com.budhash.cliche.Param;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import de.martindreier.airtwitch.cli.devices.DeviceList;

/**
 * Handler for device-related commands.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class DeviceCommands
{

	/**
	 * Index of the currently selected device.
	 */
	private int selectedDeviceIndex = -1;

	/**
	 * List all devices.
	 */
	@Command(name = "list", description = "List all registered devices")
	public void listDevices()
	{
		DeviceList.getInstance().printDeviceList();
	}

	/**
	 * Select device with given index.
	 *
	 * @param index
	 *          Selected index.
	 */
	@Command(name = "select", description = "Select target device")
	public void selectDevice(
					@Param(name = "index", description = "Index of the target device you want to stream to") int index)
	{
		DeviceInfo device = DeviceList.getInstance().getDevice(index);
		if (device == null)
		{
			System.out.println(String.format("No device with index %s available", index));
		}
		else
		{
			selectedDeviceIndex = index;
			printSelectedDevice();
		}
	}

	public void printSelectedDevice()
	{
		if (selectedDeviceIndex < 0)
		{
			System.out.println("No device selected");
		}
		else
		{
			DeviceInfo device = DeviceList.getInstance().getDevice(selectedDeviceIndex);
			if (device == null)
			{
				System.out.println("No device selected");
			}
			else
			{
				System.out.print("Selected device: ");
				System.out.println(device.getName());
			}
		}
	}

}
