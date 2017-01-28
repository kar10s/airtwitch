/**
 * HelpCommands.java
 * Created: 28.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.cli.commands;

import com.budhash.cliche.CLIException;
import com.budhash.cliche.Command;
import com.budhash.cliche.Shell;
import com.budhash.cliche.ShellDependent;

/**
 * Help commands.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class HelpCommands implements ShellDependent
{

	private Shell shell;

	/**
	 * @see com.budhash.cliche.ShellDependent#cliSetShell(com.budhash.cliche.Shell)
	 */
	@Override
	public void cliSetShell(Shell theShell)
	{
		this.shell = theShell;
	}

	/**
	 * Print help to console.
	 */
	@Command(description = "Print help")
	public void help()
	{
		System.out.println(shell.getAppName());
		System.out.println("Play live streams from Twitch directly to an AirPlay device");
		System.out.println();

		System.out.print("Select your stream from the stream submenu and your target device from the device menu. ");
		System.out.println("Then start playback with the stream command.");
		System.out.println("Type ?help for available commands or help to show this message.");
		System.out.println();

		System.out.println("Available commands:");
		try
		{
			shell.processLine("?list");
		}
		catch (CLIException exception)
		{
			System.out.println("Could not print command list");
			exception.printStackTrace();
		}
	}
}
