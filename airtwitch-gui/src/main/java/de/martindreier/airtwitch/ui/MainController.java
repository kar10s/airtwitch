/**
 * MainController.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui;

import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import de.martindreier.airtwitch.airplay.StreamControl;
import de.martindreier.airtwitch.twitch.Channel;
import de.martindreier.airtwitch.twitch.LiveStream;
import de.martindreier.airtwitch.ui.internal.Devices;
import de.martindreier.airtwitch.ui.internal.ErrorDialog;
import de.martindreier.airtwitch.ui.internal.MappingCellFactory;
import de.martindreier.airtwitch.ui.internal.Streams;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller for the AirTwitch UI.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class MainController
{
	/**
	 * List of discovered devices.
	 */
	@FXML
	private ListView<DeviceInfo>					deviceList;

	/**
	 * Search result for channel search.
	 */
	@FXML
	private ListView<Channel>							channelList;

	/**
	 * List of streams of selected channel.
	 */
	@FXML
	private ListView<LiveStream>					streamList;

	/**
	 * Stream control property. Set only when a stream is playing.
	 */
	private ObjectProperty<StreamControl>	streamControl	= new SimpleObjectProperty<>();

	/**
	 * Search field for streams.
	 */
	@FXML
	private TextField											streamName;

	/**
	 * Data model for streams.
	 */
	private Streams												streamAccess;

	/**
	 * Play button.
	 */
	@FXML
	private Button												play;

	/**
	 * Stop button.
	 */
	@FXML
	private Button												stop;

	/**
	 * Initialize the service discovery.
	 *
	 * @throws AirTwitchException
	 */
	@FXML
	public void initialize() throws AirTwitchException
	{
		// Set cell factories for lists
		deviceList.setCellFactory(MappingCellFactory.create(DeviceInfo::getName));
		channelList.setCellFactory(MappingCellFactory.create(Channel::getName, Channel::isLive));
		streamList.setCellFactory(MappingCellFactory.create(LiveStream::getTitle));

		// Initialize stream model
		streamAccess = new Streams();
		initializeDataBinding();
	}

	/**
	 * Initialize all data bindings.
	 */
	protected void initializeDataBinding()
	{
		// Bind device list and update with devices discovered during startup
		Devices.getInstance().getDevices().bind(deviceList.itemsProperty());
		deviceList.getItems().addAll(Devices.getInstance().getDevices());

		// Channel and stream lists
		streamAccess.getChannels().bind(channelList.itemsProperty());
		streamAccess.getStreams().bind(streamList.itemsProperty());

		// Enable play button only when device and stream are selected
		play.disableProperty().bind(Bindings.or(deviceList.getSelectionModel().selectedItemProperty().isNull(),
						streamList.getSelectionModel().selectedItemProperty().isNull()));
		// Enable stop button only when stream is playing
		stop.disableProperty().bind(streamControl.isNull());
	}

	/**
	 * Update the list of streams based on the selected channel.
	 */
	public void updateStreamList()
	{
		Channel selectedChannel = channelList.getSelectionModel().getSelectedItem();
		streamAccess.getStreamsForChannel(selectedChannel);
	}

	/**
	 * Search for the stream name entered in the text field {@link #streamName}.
	 */
	public void search()
	{
		String searchTerm = streamName.getText().trim();
		if (searchTerm.length() == 0)
		{
			return;
		}
		streamAccess.searchStreams(searchTerm);
	}

	/**
	 * Start playback of selected stream.
	 */
	public void play()
	{
		LiveStream selectedStream = streamList.getSelectionModel().getSelectedItem();
		DeviceInfo delectedDevice = deviceList.getSelectionModel().getSelectedItem();
		if (selectedStream != null && delectedDevice != null)
		{
			try
			{
				StreamControl stream = delectedDevice.createStream(selectedStream.getStreamUri());
				stream.play();
				streamControl.set(stream);
			}
			catch (AirTwitchException exception)
			{
				ErrorDialog.showError("Could not start playback", exception);
			}
		}
	}

	/**
	 * Stop playback of currently playing stream.
	 */
	public void stop()
	{
		StreamControl stream = streamControl.get();
		if (stream != null)
		{
			try
			{
				stream.stop();
			}
			catch (AirTwitchException exception)
			{
				ErrorDialog.showError("Could not stop playback", exception);
			}
			streamControl.set(null);
		}
	}
}
