/**
 * HttpTestClient.java
 * Created: 04.02.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * HTTP client for testing. Register a URI pattern and a handler producing a
 * response using the method {@link #registerHandler(String, Function)}. Only
 * the URI path is matched to this pattern, while method, host and query
 * parameters are disregarded. The handler is then passed the full
 * {@link HttpRequest} for processing. The handler may return <code>null</code>
 * to indicate that it refuses to process the request, so the next handler is
 * called. Handler order is not defined.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
@SuppressWarnings("deprecation")
public class HttpTestClient extends CloseableHttpClient
{
	protected Map<Pattern, Function<HttpRequest, CloseableHttpResponse>> registeredPatterns = new HashMap<>();

	/**
	 * @see org.apache.http.client.HttpClient#getParams()
	 */
	@Override
	public HttpParams getParams()
	{
		return new BasicHttpParams();
	}

	/**
	 * @see org.apache.http.client.HttpClient#getConnectionManager()
	 */
	@Override
	public ClientConnectionManager getConnectionManager()
	{
		return new BasicClientConnectionManager();
	}

	/**
	 * Reset the registered pattern list.
	 *
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException
	{
		registeredPatterns.clear();
	}

	/**
	 * @see org.apache.http.impl.client.CloseableHttpClient#doExecute(org.apache.http.HttpHost,
	 *      org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)
	 */
	@Override
	protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
					throws IOException, ClientProtocolException
	{
		URI uri;
		try
		{
			uri = new URI(request.getRequestLine().getUri());
		}
		catch (URISyntaxException exception)
		{
			throw new RuntimeException(exception);
		}
		for (Pattern pattern : registeredPatterns.keySet())
		{
			if (pattern.matcher(uri.getPath()).matches())
			{
				Function<HttpRequest, CloseableHttpResponse> handler = registeredPatterns.get(pattern);
				CloseableHttpResponse response = handler.apply(request);
				if (response != null)
				{
					return response;
				}
			}
		}
		return new TestHttpResponse(HttpVersion.HTTP_1_1, 404, "No matching response registered");
	}

	public void registerHandler(String pattern, Function<HttpRequest, CloseableHttpResponse> handler)
	{
		Pattern compiledPattern = Pattern.compile(pattern);
		registeredPatterns.put(compiledPattern, handler);
	}
}
