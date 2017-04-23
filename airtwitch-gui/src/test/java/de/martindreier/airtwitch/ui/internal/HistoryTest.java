/**
 * HistoryTest.java
 * Created: 23.04.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.ui.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link History}.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class HistoryTest
{

	/**
	 * Object under test.
	 */
	private History			history;

	/**
	 * Counter for new entries.
	 */
	private AtomicLong	newEntryCounter;

	@Before
	public void setup() throws Exception
	{
		Path historyFile = Paths.get(HistoryTest.class.getResource("test_history").toURI());
		// Set up without autosave, file from classpath is not writable
		history = new History(historyFile, false);
		newEntryCounter = new AtomicLong(0);
	}

	@Test
	public void initialHistoryShouldBeEmpty()
	{
		assertEquals("Initial entries not empty", 0, history.entries.size());
	}

	@Test
	public void historyFileShouldBeLoaded() throws IOException
	{
		givenHistoryIsLoaded();

		thenHistoryShouldNotExceedMaximumSize();
		thenFirstEntryShouldBe("Entry1");
		thenLastEntryShouldBe("Entry10");
	}

	@Test
	public void newEntryShouldPushOutOldestEntry() throws IOException
	{
		givenHistoryIsLoaded();

		whenEntryIsAdded();
		whenEntryIsAdded();

		thenHistoryShouldNotExceedMaximumSize();
		thenFirstEntryShouldBe("New Entry 2");
		thenLastEntryShouldBe("Entry8");
	}

	// ***** GIVEN *****

	protected void givenHistoryIsLoaded() throws IOException
	{
		history.load();
		assertNotEquals("History not loaded from file", 0, history.entries.size());
	}

	// ***** WHEN *****

	protected void whenEntryIsAdded() throws IOException
	{
		history.add("New Entry " + newEntryCounter.incrementAndGet());
	}

	// ***** THEN *****

	protected void thenHistoryShouldNotExceedMaximumSize()
	{
		assertEquals("History size does not match maximum", History.MAX_HISTORY_SIZE, history.entries.size());
	}

	protected void thenFirstEntryShouldBe(String expected)
	{
		assertEquals("History in incorrect order (first entry incorrect)", expected, history.entries.get(0));
	}

	protected void thenLastEntryShouldBe(String expected)
	{
		assertEquals("History in incorrect order (last entry incorrect)", expected,
						history.entries.get(History.MAX_HISTORY_SIZE - 1));
	}

}
