/**
 * MainController.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui;

import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.AirPlayServiceDiscovery;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import de.martindreier.airtwitch.twitch.Channel;
import de.martindreier.airtwitch.twitch.LiveStream;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

/**
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class MainController
{
	@FXML
	private ListView<DeviceInfo>			deviceList;

	@FXML
	private ListView<Channel>					channelList;

	@FXML
	private ListView<LiveStream>			streamList;

	private ListProperty<DeviceInfo>	devices		= new SimpleListProperty<>();

	private ListProperty<Channel>			channels	= new SimpleListProperty<>();

	private ListProperty<LiveStream>	streams		= new SimpleListProperty<>();

	/**
	 * Initialize the service discovery.
	 *
	 * @throws AirTwitchException
	 */
	@SuppressWarnings("resource")
	@FXML
	public void initialize() throws AirTwitchException
	{
		initializeDataBinding();
		new AirPlayServiceDiscovery().registerListener(device -> devices.add(device));
	}

	protected void initializeDataBinding()
	{
		deviceList.itemsProperty().bind(devices);
		channelList.itemsProperty().bind(channels);
		streamList.itemsProperty().bind(streams);
	}

	/**
	 * Update the list of streams based on the selected channel.
	 */
	public void updateStreamList()
	{
		Channel selectedChannel = channelList.getSelectionModel().getSelectedItem();
		if (selectedChannel != null)
		{
			try
			{
				selectedChannel.requestChannelToken();
				streams.setAll(selectedChannel.getLiveStreams());
			}
			catch (AirTwitchException exception)
			{
				AirTwitch.showError("Error finding streams for selected channel", exception);
			}
		}
		else
		{
			streams.clear();
		}
	}
}
