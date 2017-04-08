package de.martindreier.airtwitch.ui.internal;

import java.util.function.Function;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Cell factory which maps an item to a text using a callback function.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 * @param <T>
 *          Item type of the cell.
 */
public class MappingCellFactory<T> implements Callback<ListView<T>, ListCell<T>>
{
	/**
	 * Mapper function.
	 */
	private Function<T, String>		mapper;

	/**
	 * Mapper function for enabled state of cell.
	 */
	private Function<T, Boolean>	enabledMapper;

	/**
	 * Create new cell factory.
	 *
	 * @param mapper
	 *          Mapper function.
	 * @return Cell factory mapping the provided type to text.
	 * @param <T>
	 *          Item type of the cell.
	 */
	public static <T> MappingCellFactory<T> create(Function<T, String> mapper)
	{
		return new MappingCellFactory<T>(mapper, null);
	}

	/**
	 * Create new cell factory.
	 *
	 * @param mapper
	 *          Mapper function for cell text content
	 * @param enabledMapper
	 *          Mapper function for cell eneabled state.
	 * @return Cell factory mapping the provided type to text.
	 * @param <T>
	 *          Item type of the cell.
	 */
	public static <T> MappingCellFactory<T> create(Function<T, String> mapper, Function<T, Boolean> enabledMapper)
	{
		return new MappingCellFactory<T>(mapper, enabledMapper);
	}

	/**
	 * Create new cell factory.
	 *
	 * @param mapper
	 *          Mapper function for cell text content.
	 * @param enabledMapper
	 *          Mapper function for cell eneabled state.
	 */
	public MappingCellFactory(Function<T, String> mapper, Function<T, Boolean> enabledMapper)
	{
		if (mapper == null)
		{
			throw new IllegalArgumentException("Mapper may not be null");
		}
		this.mapper = mapper;
		this.enabledMapper = enabledMapper;
	}

	@Override
	public ListCell<T> call(ListView<T> param)
	{
		return new ListCell<T>()
		{
			@Override
			protected void updateItem(T item, boolean empty)
			{
				super.updateItem(item, empty);

				// Set empty cell if no data was given, else use the mapper function to
				// get the cell content
				if (empty || item == null)
				{
					setText(null);
					setDisable(false);
				}
				else
				{
					setText(mapper.apply(item));
					// Using the enabled mapper allows the cell to be disabled
					if (enabledMapper != null)
					{
						setDisable(!enabledMapper.apply(item).booleanValue());
					}
				}

			}
		};
	}

}