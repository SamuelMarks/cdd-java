import static org.junit.Assert.*;
import openapi.OpenAPI;

/**
 * End-to-end integration test runner verifying WASI simulation and round-trip
 * parsing/emission across modules.
 */
public class TestRunner {
	/**
	 * Default constructor.
	 */
	public TestRunner() {
	}

	/**
	 * Executes integration assertions across WASI simulation, mocks, classes,
	 * routes, tests, functions, and docstrings.
	 *
	 * @param args
	 *            Command-line arguments.
	 * @throws Exception
	 *             If any integration test fails.
	 */
	public static void main(String[] args) throws Exception {
		assertNotNull(new TestRunner());
		WasiEnvironmentTest.run();

		OpenAPI mockApi = mocks.Parse.parse("server.createContext(\"/api/test\", handler);");
		assertNotNull(mockApi.paths);
		assertNotNull(mockApi.paths.pathItems);
		assertTrue(mockApi.paths.pathItems.containsKey("/api/test"));

		String fakeClass = "/**\n * A mock dto.\n */\npublic class MyDto {\n    /**\n     * A mock prop.\n     */\n    public String myStr;\n}";
		OpenAPI api = classes.Parse.parse(fakeClass);
		String emitted = classes.Emit.emit(api, null);
		assertTrue(emitted.contains("MyDto"));
		assertTrue(emitted.contains("myStr"));

		String fakeClient = "public class TestClient { public java.net.http.HttpResponse<String> getItems(String q) { HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/items\")).GET(); return null; } }";
		OpenAPI routeApi = routes.Parse.parse(fakeClient);
		assertNotNull(routeApi.paths);
		assertNotNull(routeApi.paths.pathItems);
		assertTrue(routeApi.paths.pathItems.containsKey("/items"));

		String rEmitted = routes.Emit.emit(routeApi, null);
		assertTrue(rEmitted.contains("getItems"));

		String cbClient = "public class CbClient {\n/**\n * @callback myEvt {$request.query.url} POST\n */\npublic java.net.http.HttpResponse<String> getCb() { HttpRequest.newBuilder().uri(URI.create(baseUrl + \"/cb\")).GET(); return null; } }";
		OpenAPI cbApi = routes.Parse.parse(cbClient);
		assertNotNull(cbApi.paths.pathItems.get("/cb").get.callbacks);

		String cbEmitted = routes.Emit.emit(cbApi, null);
		assertTrue(cbEmitted.contains("myEvtCallbackHandler"));

		String mEmitted = mocks.Emit.emit(routeApi, null);
		assertTrue(mEmitted.contains("/items"));

		String tEmitted = tests.Emit.emit(routeApi, null);
		assertTrue(tEmitted.contains("testGetItems"));

		String fEmitted = functions.Emit.emit(routeApi, null);
		assertTrue(fEmitted.contains("Helper Functions"));

		String dEmitted = docstrings.Emit.emitDocsJson(routeApi, true, true);
		assertTrue(dEmitted.contains("getItems"));
		assertFalse(dEmitted.contains("import my.api.ApiClient;"));
		assertFalse(dEmitted.contains("public class Example"));

		String dEmittedFull = docstrings.Emit.emitDocsJson(routeApi, false, false);
		assertTrue(dEmittedFull.contains("import my.api.ApiClient;"));
		assertTrue(dEmittedFull.contains("public class Example"));
	}
}
