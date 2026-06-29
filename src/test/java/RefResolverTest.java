import openapi.RefResolver;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;

public class RefResolverTest {

	private static HttpServer server;
	private static int port;

	@BeforeClass
	public static void setUp() throws Exception {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/test.yaml", new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				String response = "openapi: 3.0.0";
				exchange.sendResponseHeaders(200, response.length());
				OutputStream os = exchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			}
		});
		server.createContext("/error", new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				exchange.sendResponseHeaders(404, 0);
				exchange.close();
			}
		});
		server.setExecutor(null);
		server.start();
		port = server.getAddress().getPort();
	}

	@AfterClass
	public static void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	public void testFetchLocalFile() throws Exception {
		File temp = File.createTempFile("test", ".yaml");
		Files.writeString(temp.toPath(), "test content");

		RefResolver resolver = new RefResolver();
		String content = resolver.fetch(temp.getName(), temp.getParentFile().toURI().toString());
		assertEquals("test content", content);

		// Test cache
		Files.writeString(temp.toPath(), "new content");
		String cached = resolver.fetch(temp.getName(), temp.getParentFile().toURI().toString());
		assertEquals("test content", cached);
	}

	@Test
	public void testFetchHttp() throws Exception {
		RefResolver resolver = new RefResolver();
		String content = resolver.fetch("http://127.0.0.1:" + port + "/test.yaml", "http://127.0.0.1:" + port + "/");
		assertEquals("openapi: 3.0.0", content);
	}

	@Test
	public void testFetchHttpError() {
		RefResolver resolver = new RefResolver();
		try {
			resolver.fetch("http://127.0.0.1:" + port + "/error", "http://127.0.0.1:" + port + "/");
			fail("Expected exception");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("Failed to resolve reference"));
		}
	}
}
