/**
 * MainController.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import de.martindreier.airtwitch.AirTwitchException;
import de.martindreier.airtwitch.airplay.DeviceInfo;
import de.martindreier.airtwitch.airplay.StreamControl;
import de.martindreier.airtwitch.twitch.Channel;
import de.martindreier.airtwitch.twitch.LiveStream;
import de.martindreier.airtwitch.ui.internal.Devices;
import de.martindreier.airtwitch.ui.internal.ErrorDialog;
import de.martindreier.airtwitch.ui.internal.History;
import de.martindreier.airtwitch.ui.internal.MappingCellFactory;
import de.martindreier.airtwitch.ui.internal.Streams;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

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
	private ComboBox<String>							streamName;

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
	 * Background processing thread.
	 */
	private ExecutorService								background		= Executors.newFixedThreadPool(1);

	/**
	 * History instance using default save file.
	 */
	private History												history				= new History(null, true);

	/**
	 * Initialize the service discovery.
	 *
	 * @throws AirTwitchException
	 */
	@FXML
	public void initialize() throws AirTwitchException
	{
		// Load history
		try
		{
			history.load();
		}
		catch (IOException exception)
		{
			throw new AirTwitchException("Could not load history", exception);
		}

		// Set cell factories for lists
		deviceList.setCellFactory(MappingCellFactory.create(DeviceInfo::getName));
		channelList.setCellFactory(MappingCellFactory.create(this::formatChannelName, Channel::isLive));
		streamList.setCellFactory(MappingCellFactory.create(LiveStream::getTitle));

		// Initialize stream model
		streamAccess = new Streams();
		initializeDataBinding();
	}

	/**
	 * Format the channel name with live status and stream description.
	 *
	 * @param channel
	 *          Channel.
	 * @return Enriched channel name.
	 */
	private String formatChannelName(Channel channel)
	{
		if (channel.isLive())
		{
			if (channel.getStatus().isEmpty())
			{
				return channel.getName();
			}
			else
			{
				return String.format("%s: %s", channel.getName(), channel.getStatus());
			}
		}
		else
		{
			return String.format("%s (not live)", channel.getName());
		}
	}

	/**
	 * Initialize all data bindings.
	 */
	protected void initializeDataBinding()
	{
		// Bind history to search term drop down
		streamName.itemsProperty().bind(history.getEntriesProperty());

		// Bind device list and update with devices discovered during startup
		deviceList.getItems().addAll(Devices.getInstance().getDevices());
		Devices.getInstance().getDevices().bind(deviceList.itemsProperty());
		// deviceList.setItems(new ObservableListWrapper<>(new
		// LinkedList<>(Devices.getInstance().getDevices())));

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
		background.submit(() -> {
			Channel selectedChannel = channelList.getSelectionModel().getSelectedItem();
			streamAccess.getStreamsForChannel(selectedChannel);
		});
	}

	/**
	 * Search for the stream name entered in the text field {@link #streamName}.
	 */
	public void search()
	{
		String searchTerm = streamName.getValue().trim();
		saveToHistory(searchTerm);
		background.submit(() -> {

			if (searchTerm.length() == 0)
			{
				return;
			}
			streamAccess.searchStreams(searchTerm);
		});
	}

	/**
	 * Save search term to entries. If this is a new search term, it is added to
	 * the entries, possibly replacing the oldest entry. If it is already in the
	 * entries, it is move to the top.
	 *
	 * @param searchTerm
	 *          The search term to add to the history.
	 */
	protected void saveToHistory(String searchTerm)
	{
		try
		{
			history.add(searchTerm);
			streamName.getSelectionModel().select(0);
		}
		catch (IOException exception)
		{
			ErrorDialog.showError("Could not save search history", exception);
		}
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
			background.submit(() -> {
				try
				{
					StreamControl stream = delectedDevice.createStream(selectedStream.getStreamUri());
					stream.play();
					Platform.runLater(() -> {
						streamControl.set(stream);
					});
				}
				catch (AirTwitchException exception)
				{
					ErrorDialog.showError("Could not start playback", exception);
				}
			});
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
			background.submit(() -> {
				try
				{
					stream.stop();
				}
				catch (AirTwitchException exception)
				{
					ErrorDialog.showError("Could not stop playback", exception);
				}
				Platform.runLater(() -> {
					streamControl.set(null);
				});
			});
		}
	}

	/**
	 * Stop playing stream and sgut down background processing.
	 */
	public void shutdown()
	{
		stop();
		background.shutdown();
	}
}
