/**
 * AirTwitch.java
 * Created: 25.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.cli;

import java.io.IOException;
import com.budhash.cliche.Shell;
import com.budhash.cliche.ShellFactory;
import de.martindreier.airtwitch.cli.devices.DeviceList;

/**
 * Air Twitch CLI.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AirTwitch
{

	public static void main(String[] args)
	{
		// Initialize device listener
		DeviceList.getInstance();

		// Build shell and execute command loop
		Shell shell = ShellFactory.createConsoleShell("airtwitch", "AirTwitch", new MainCommands());
		try
		{
			shell.commandLoop();
		}
		catch (IOException exception)
		{
			System.out.println("Error while running command loop, terminating");
			exception.printStackTrace();
		}

		// Terminate device listener
		DeviceList.getInstance().shutdown();
	}

}
