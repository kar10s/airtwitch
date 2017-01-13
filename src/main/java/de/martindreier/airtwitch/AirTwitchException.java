package de.martindreier.airtwitch;

/**
 * AirTwitch exception.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AirTwitchException extends Exception
{

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -5520747128555440957L;

	/**
	 * Create a new exception with a message and a cause.
	 *
	 * @param message
	 *          The message. Can be formatted as per
	 *          {@link String#format(String, Object...)} with parameter
	 *          <code>params</code>.
	 * @param cause
	 *          The exception cause.
	 * @param params
	 *          Message parameters.
	 */
	public AirTwitchException(String message, Throwable cause, Object... params)
	{
		super(String.format(message, params), cause);
	}

	/**
	 * Create a new exception with a message and a cause.
	 *
	 * @param message
	 *          The message. Can be formatted as per
	 *          {@link String#format(String, Object...)} with parameter
	 *          <code>params</code>.
	 * @param params
	 *          Message parameters.
	 */
	public AirTwitchException(String message, Object... params)
	{
		super(String.format(message, params));
	}

}
