/**
 * TestHttpResponse.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.test;

import java.io.IOException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHttpResponse;

/**
 * HTTP response object which can be constructed directly and implements
 * {@link CloseableHttpResponse}.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class TestHttpResponse extends BasicHttpResponse implements CloseableHttpResponse
{
	/**
	 * @param ver
	 * @param code
	 * @param reason
	 */
	public TestHttpResponse(ProtocolVersion ver, int code, String reason)
	{
		super(ver, code, reason);
	}

	/**
	 * @param statusline
	 */
	public TestHttpResponse(StatusLine statusline)
	{
		super(statusline);
	}

	/**
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException
	{
		// No-Op
	}

}