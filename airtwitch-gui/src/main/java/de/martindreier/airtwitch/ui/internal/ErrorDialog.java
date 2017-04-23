/**
 * ErrorDialog.java
 * Created: 26.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Helper for error dialogs.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class ErrorDialog
{

	/**
	 * Show error dialog.
	 *
	 * @param message
	 *          Error message.
	 */
	public static void showError(String message)
	{
		showError(message, null);
	}

	/**
	 * Show error dialog for an exception.
	 *
	 * @param exception
	 *          Exception.
	 */
	public static void showError(Throwable exception)
	{
		showError(null, exception);
	}

	/**
	 * Show error dialog. If both <code>message</code> and <code>exception</code>
	 * are <code>null</code> no dialog is shown.
	 *
	 * @param message
	 *          Error message. May be <code>null</code>.
	 * @param exception
	 *          Exception. May be <code>null</code>.
	 */
	public static void showError(String message, Throwable exception)
	{
		if (message == null && exception == null)
		{
			return;
		}
		// Use esception message as dialog message if no specific message was given
		if (message == null)
		{
			message = exception.getLocalizedMessage();
		}

		// Construct error dialog
		Alert dialog = new Alert(AlertType.ERROR);
		dialog.setTitle("AirTwitch Error");
		if (exception == null)
		{
			// Only text: Simple dialog
			dialog.setContentText(message);
			dialog.setHeaderText(null);
		}
		else
		{
			// Text and exception: Dialog with header, exception stack trace in
			// content area
			dialog.setHeaderText(message);
			StringWriter stacktrace = new StringWriter();
			stacktrace.write(exception.getMessage());
			stacktrace.write("\n");
			exception.printStackTrace(new PrintWriter(stacktrace));
			dialog.setContentText(stacktrace.toString());
		}
		// Open the dialog
		Platform.runLater(() -> {
			dialog.show();
		});
	}

}
