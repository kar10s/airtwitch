/**
 * ErrorDialog.java
 * Created: 23.04.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.commons.lang3.SystemUtils;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * History manager.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class History
{
	/**
	 * Default entries save file.
	 */
	protected static final Path							DEFAULT_SAVE_FILE	= SystemUtils.getUserHome().toPath()
					.resolve(".airtwitch_history");

	/**
	 * Maximum number of entries in entries.
	 */
	protected static final int							MAX_HISTORY_SIZE	= 10;

	/**
	 * Save file for entries.
	 */
	protected Path													saveFile;

	/**
	 * If true, the entries is automatically saved on each update.
	 */
	protected boolean												autosave;

	/**
	 * Internal entries list. First entry (index 0) is considered the newest entry
	 * into the history.
	 */
	protected ObservableList<String>				entries						= FXCollections.observableArrayList();

	/**
	 * Property for internal entries list.
	 *
	 * @see #entries
	 */
	protected ReadOnlyListProperty<String>	entriesProperty		= new SimpleListProperty<>(entries);

	/**
	 * Create a new entries.
	 *
	 * @param saveFile
	 *          The path to the entries save file. May be <code>null</code>, then
	 *          the default save file is used.
	 * @param autosave
	 *          <code>true</code> to automatically save the entries on each
	 *          update. {@link #save()} has to be called if this is
	 *          <code>false</code>.
	 */
	public History(Path saveFile, boolean autosave)
	{
		if (saveFile == null)
		{
			this.saveFile = DEFAULT_SAVE_FILE;
		}
		else
		{
			this.saveFile = saveFile;
		}
		this.autosave = autosave;
	}

	/**
	 * Load the entries.
	 *
	 * @throws IOException
	 *           Error reading entries file.
	 */
	public void load() throws IOException
	{
		entries.clear();
		if (Files.exists(saveFile))
		{
			Files.lines(saveFile).limit(MAX_HISTORY_SIZE).forEachOrdered(line -> entries.add(line));
		}
	}

	/**
	 * Save the entries.
	 *
	 * @throws IOException
	 *           Error writing entries file.
	 */
	public void save() throws IOException
	{
		Files.write(saveFile, entries, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.WRITE);
	}

	/**
	 * Add a new entry to the history. If this entry already exists in the
	 * history, it is moved to the top of the list. Otherwise it is added to the
	 * top, possibly replacing the oldest entry if the maximum size is already
	 * reached.
	 *
	 * @param newEntry
	 *          The new entry.
	 * @throws IOException
	 *           Error saving list. May only be thrown if the list is set to save
	 *           automatically.
	 */
	public void add(String newEntry) throws IOException
	{
		// Remove existing entry, if already in list
		entries.remove(newEntry);

		// Add new entry at first position
		entries.add(0, newEntry);

		// Remove oldest entries if history exceeds maximum size
		while (entries.size() > MAX_HISTORY_SIZE)
		{
			entries.remove(entries.size() - 1);
		}

		if (autosave)
		{
			save();
		}
	}

	/**
	 * Get the entries property.
	 *
	 * @return Property for history entries. Read-only.
	 */
	public ReadOnlyListProperty<String> getEntriesProperty()
	{
		return entriesProperty;
	}
}
