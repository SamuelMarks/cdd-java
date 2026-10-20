package openapi;

import java.net.URI;

/**
 * Functional abstraction for fetching external OpenAPI or Arazzo documents from
 * a resolved {@link URI}.
 */
@FunctionalInterface
public interface ReferenceFetcher {
	/**
	 * Fetches the raw string content of the resource located at {@code uri}.
	 *
	 * @param uri
	 *            The resolved target {@link URI} (without fragment).
	 * @return The raw specification string content.
	 * @throws Exception
	 *             If an I/O or HTTP error occurs while fetching the resource.
	 */
	String fetch(URI uri) throws Exception;
}
