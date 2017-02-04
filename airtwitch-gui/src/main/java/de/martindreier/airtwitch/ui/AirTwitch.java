/**
 * AirPlayServiceDiscovery.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * Main class for AirTwitch UI application.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AirTwitch extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		// Start service discovery
		Devices.getInstance().initialize();

		// Build main window
		Parent root = FXMLLoader.load(getClass().getResource("AirTwitch.fxml"));

		Scene scene = new Scene(root, 300, 275);

		primaryStage.setTitle("AirTwitch");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception
	{
		// End service discovery
		Devices.getInstance().shutdown();
	}

	/**
	 * Start JavaFX application.
	 *
	 * @param args
	 *          Command line arguments.
	 */
	public static void main(String[] args)
	{
		launch(args);
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
		if (message == null)
		{
			message = exception.getLocalizedMessage();
		}

		Alert dialog = new Alert(AlertType.ERROR);
		dialog.setTitle("AirTwitch Error");
		if (exception == null)
		{
			dialog.setContentText(message);
			dialog.setHeaderText(null);
		}
		else
		{
			dialog.setHeaderText(message);
			StringWriter stacktrace = new StringWriter();
			stacktrace.write(exception.getMessage());
			stacktrace.write("\n");
			exception.printStackTrace(new PrintWriter(stacktrace));
			dialog.setContentText(stacktrace.toString());
		}
		dialog.show();
	}
}
