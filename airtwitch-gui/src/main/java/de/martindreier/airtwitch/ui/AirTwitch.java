/**
 * AirPlayServiceDiscovery.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui;

import de.martindreier.airtwitch.ui.internal.Devices;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

		Scene scene = new Scene(root);

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
}
