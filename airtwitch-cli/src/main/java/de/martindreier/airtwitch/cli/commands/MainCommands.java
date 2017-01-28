/**
 * MainCommands.java
 * Created: 25.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.cli.commands;

import java.io.IOException;
import com.budhash.cliche.Command;
import com.budhash.cliche.Shell;
import com.budhash.cliche.ShellDependent;
import com.budhash.cliche.ShellFactory;

/**
 * Handler for CLI main menu.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class MainCommands implements ShellDependent
{
	private Shell									shell;

	private final DeviceCommands	device	= new DeviceCommands();

	private final StreamCommands	streams	= new StreamCommands();

	@Override
	public void cliSetShell(Shell theShell)
	{
		this.shell = theShell;
	}

	/**
	 * Get device command reference.
	 *
	 * @return Device command reference.
	 */
	public DeviceCommands getDevice()
	{
		return device;
	}

	/**
	 * Get stream command reference.
	 *
	 * @return Stream command reference.
	 */
	public StreamCommands getStream()
	{
		return streams;
	}

	/**
	 * Go to devices submenu.
	 */
	@Command(description = "Go to device menu")
	public void devices()
	{
		try
		{
			getDevice().printSelectedDevice();
			getDevice().listDevices();
			ShellFactory.createSubshell("devices", shell, shell.getAppName(), getDevice()).commandLoop();
			getDevice().printSelectedDevice();
		}
		catch (IOException exception)
		{
			System.out.println("Could not switch to device menu");
			exception.printStackTrace();
		}
	}

	@Command(description = "Go to stream menu")
	public void stream()
	{
		try
		{
			getStream().printSelectedStream();
			ShellFactory.createSubshell("streams", shell, shell.getAppName(), getStream()).commandLoop();
			getStream().printSelectedStream();
		}
		catch (IOException exception)
		{
			System.out.println("Could not switch to stream menu");
			exception.printStackTrace();
		}
	}
}
