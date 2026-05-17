import org.junit.Test;
import static org.junit.Assert.*;

import openapi.*;

public class MiscParseEmitTest {

	@Test
	public void testTestsParse() {
		tests.Parse parser = new tests.Parse();
		String source = "class MyIntegrationTest { void test_myOp() {} void test_myOp() {} }";
		OpenAPI api = tests.Parse.parse(source);
		assertNotNull(api);
		assertEquals(1, api.paths.pathItems.size());
		assertTrue(api.paths.pathItems.containsKey("/myOp"));
		assertEquals("myOp", api.paths.pathItems.get("/myOp").get.operationId);

		// Exception branch
		OpenAPI apiErr = tests.Parse.parse("invalid java");
		assertNotNull(apiErr);

		// Non-matching class name
		OpenAPI api2 = tests.Parse.parse("class MyClass { void test_myOp() {} }");
		assertEquals(0, api2.paths.pathItems.size());

		// Non-matching method name
		OpenAPI api3 = tests.Parse.parse("class MyIntegrationTest { void myOp() {} }");
		assertEquals(0, api3.paths.pathItems.size());
	}

	@Test
	public void testTestsEmit() throws Exception {
		tests.Emit emitter = new tests.Emit();
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.title = "Test";
		api.paths = new Paths();
		PathItem pi = new PathItem();

		Operation opGet = new Operation();
		opGet.operationId = "getOp";
		pi.get = opGet;

		Operation opPost = new Operation();
		opPost.operationId = "postOp";
		opPost.requestBody = new RequestBody();
		pi.post = opPost;

		Operation opPut = new Operation();
		// no operation ID
		opPut.requestBody = new RequestBody();
		pi.put = opPut;

		Operation opDelete = new Operation();
		opDelete.operationId = "deleteOp";
		pi.delete = opDelete;

		api.paths.pathItems.put("/test", pi);

		// Tests Emit parameter logic
		PathItem pi2 = new PathItem();
		pi2.parameters = new java.util.ArrayList<>();
		Parameter pathParam = new Parameter();
		pathParam.name = "id";
		pathParam.in = "path";
		pi2.parameters.add(pathParam);

		Operation opGet2 = new Operation();
		opGet2.parameters = new java.util.ArrayList<>();
		Parameter opParam = new Parameter();
		opParam.name = "id";
		opParam.in = "path";
		opGet2.parameters.add(opParam);

		Parameter opParam2 = new Parameter();
		opParam2.name = "query";
		opParam2.in = "query";
		opGet2.parameters.add(opParam2);

		// null param handling
		opGet2.parameters.add(new Parameter());
		opGet2.parameters.add(new Object());
		pi2.parameters.add(new Object());

		pi2.get = opGet2;
		api.paths.pathItems.put("/test2", pi2);

		// post edge cases
		PathItem pi3 = new PathItem();
		Operation postArray = new Operation();
		postArray.requestBody = new RequestBody();
		postArray.parameters = new java.util.ArrayList<>();
		Parameter postParam = new Parameter();
		postParam.name = "id";
		postParam.in = "query";
		postArray.parameters.add(postParam);
		pi3.post = postArray;
		api.paths.pathItems.put("/createWithArray", pi3);

		PathItem pi4 = new PathItem();
		Operation postList = new Operation();
		postList.requestBody = new RequestBody();
		postList.parameters = new java.util.ArrayList<>();
		postList.parameters.add(postParam);
		pi4.post = postList;
		api.paths.pathItems.put("/createWithList", pi4);

		String res = tests.Emit.emit(api, null);
		assertNotNull(res);
		assertTrue(res.contains("testGetOp"));
		assertTrue(res.contains("testPostOp"));
		assertTrue(res.contains("testPuttest"));
		assertTrue(res.contains("testDeleteOp"));

		// empty info title
		OpenAPI api2 = new OpenAPI();
		api2.info = new Info();
		api2.info.title = "";
		tests.Emit.emit(api2, null);

		// null info title
		OpenAPI api3 = new OpenAPI();
		api3.info = new Info();
		api3.paths = new Paths();
		api3.paths.pathItems = null;
		tests.Emit.emit(api3, null);
		// empty opId, non-alphanumeric
		OpenAPI api4 = new OpenAPI();
		api4.paths = new Paths();
		PathItem pi4_1 = new PathItem();
		Operation op4 = new Operation();
		op4.operationId = "";
		pi4_1.get = op4;
		api4.paths.pathItems.put("/abc", pi4_1);
		tests.Emit.emit(api4, null);

		// parameter with empty safeName
		OpenAPI api5 = new OpenAPI();
		api5.paths = new Paths();
		PathItem pi5 = new PathItem();
		Operation op5 = new Operation();
		op5.operationId = "testSafeName";
		op5.parameters = new java.util.ArrayList<>();
		Parameter p_invalid = new Parameter();
		p_invalid.name = "$"; // non-alphanumeric will become empty
		p_invalid.in = "query";
		op5.parameters.add(p_invalid);
		pi5.get = op5;
		api5.paths.pathItems.put("/testSafeNamePath", pi5);
		tests.Emit.emit(api5, null);
		// Cover private hasMember method
		java.lang.reflect.Method method = tests.Emit.class.getDeclaredMethod("hasMember",
				com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class, String.class);
		method.setAccessible(true);
		com.github.javaparser.ast.body.ClassOrInterfaceDeclaration decl = new com.github.javaparser.ast.body.ClassOrInterfaceDeclaration();
		decl.addMethod("myMethod");
		boolean hasIt = (boolean) method.invoke(null, decl, "myMethod");
		assertTrue(hasIt);
		boolean noHave = (boolean) method.invoke(null, decl, "otherMethod");
		assertFalse(noHave);

		// Branch for non-method declaration
		decl.addField(String.class, "myField");
		boolean noHaveField = (boolean) method.invoke(null, decl, "myField");
		assertFalse(noHaveField);
	}

	@Test
	public void testDocstringsParse() {
		docstrings.Parse p = new docstrings.Parse();
		assertNotNull(docstrings.Parse.parse("any"));
	}

	@Test
	public void testDocstringsEmit() throws Exception {
		docstrings.Emit e = new docstrings.Emit();
		OpenAPI api = new OpenAPI();
		api.paths = new Paths();
		PathItem pi = new PathItem();
		pi.get = new Operation();
		pi.post = new Operation();
		pi.put = new Operation();
		pi.delete = new Operation();
		pi.patch = new Operation();
		pi.options = new Operation();
		pi.head = new Operation();
		pi.trace = new Operation();
		api.paths.pathItems.put("/path", pi);

		String json1 = docstrings.Emit.emitDocsJson(api, false, false);
		assertTrue(json1.contains("import my.api.ApiClient"));
		assertTrue(json1.contains("public class Example"));

		String json2 = docstrings.Emit.emitDocsJson(api, true, true);
		assertFalse(json2.contains("import my.api.ApiClient"));
		assertFalse(json2.contains("public class Example"));

		Operation op = new Operation();
		op.operationId = "myOp";
		pi.get = op;
		api.paths.pathItems.put("/path2", pi);
		docstrings.Emit.emitDocsJson(api, false, false);

		// Test nulls
		docstrings.Emit.emitDocsJson(null, false, false);
		docstrings.Emit.emitDocsJson(new OpenAPI(), false, false);
		OpenAPI apiNullPaths = new OpenAPI();
		apiNullPaths.paths = new Paths();
		apiNullPaths.paths.pathItems = null;
		docstrings.Emit.emitDocsJson(apiNullPaths, false, false);
		// Test missing get and empty path map
		OpenAPI emptyApi = new OpenAPI();
		emptyApi.paths = new Paths();
		emptyApi.paths.pathItems.put("/empty", new PathItem());
		PathItem noGet = new PathItem();
		noGet.post = new Operation();
		emptyApi.paths.pathItems.put("/noget", noGet);
		docstrings.Emit.emitDocsJson(emptyApi, false, false);

		// Empty operationId
		Operation emptyOpId = new Operation();
		emptyOpId.operationId = "";
		noGet.post = emptyOpId;
		docstrings.Emit.emitDocsJson(emptyApi, false, false);	}

	@Test
	public void testMocksParse() {
		mocks.Parse p = new mocks.Parse();
		String src = "server.createContext(\"/test\", handler);";
		OpenAPI api = mocks.Parse.parse(src);
		assertNotNull(api.paths.pathItems.get("/test"));

		// with class
		String src2 = "class Mock { void test() { server.createContext(\"/test2\"); } }";
		OpenAPI api2 = mocks.Parse.parse(src2);
		assertNotNull(api2.paths.pathItems.get("/test2"));

		// error
		mocks.Parse.parse("invalid");

		// empty args
		mocks.Parse.parse("class Mock { void test() { server.createContext(); } }");

		// multiple for same path
		mocks.Parse.parse("class Mock { void test() { server.createContext(\"/test3\"); server.createContext(\"/test3\"); } }");

		// Different method scope or no scope
		mocks.Parse.parse("class Mock { void test() { createContext(\"/test\"); something.createContext(\"/test\"); } }");
		}

	@Test
	public void testMocksEmit() {
		mocks.Emit e = new mocks.Emit();
		OpenAPI api = new OpenAPI();
		api.info = new Info();
		api.info.title = "TestMock";
		api.paths = new Paths();
		api.paths.pathItems.put("/test/{id}", new PathItem());
		api.paths.pathItems.put("/test2/", new PathItem());

		String newSrc = mocks.Emit.emit(api, null);
		assertTrue(newSrc.contains("MockServer"));

		// Re-emit to same src
		String resrc = mocks.Emit.emit(api, newSrc);
		assertTrue(resrc.contains("MockServer"));

		// Re-emit to existing source with different class name to trigger fallback
		String otherSrc = "class OtherClass {}";
		String otherResrc = mocks.Emit.emit(api, otherSrc);
		assertTrue(otherResrc.contains("start"));

		// existing source is empty
		String emptyResrc = mocks.Emit.emit(api, "   ");
		assertTrue(emptyResrc.contains("MockServer"));

		// Try with missing model paths
		mocks.Emit.emit(new OpenAPI(), null);
		OpenAPI mockNullItems = new OpenAPI();
		mockNullItems.paths = new Paths();
		mockNullItems.paths.pathItems = null;
		mocks.Emit.emit(mockNullItems, null);
		// Empty title
		OpenAPI api2 = new OpenAPI();
		api2.info = new Info();
		api2.info.title = "";
		mocks.Emit.emit(api2, null);

		// null info
		OpenAPI api3 = new OpenAPI();
		api3.paths = new Paths();
		api3.paths.pathItems.put("/", new PathItem()); // test handlerPath.length() <= 1
		mocks.Emit.emit(api3, null);	}

	@Test
	public void testFunctionsParse() {
		functions.Parse p = new functions.Parse();
		assertNotNull(functions.Parse.parse("test"));
	}

	@Test
	public void testFunctionsEmit() {
		functions.Emit e = new functions.Emit();
		String src = "class Test {}";
		String out1 = functions.Emit.emit(null, src);
		assertTrue(out1.contains("class Test {}"));

		String out2 = functions.Emit.emit(null, null);
		assertTrue(out2.contains("Helper Functions Generated"));

		String out3 = functions.Emit.emit(null, "   ");
		assertTrue(out3.contains("Helper Functions Generated"));
	}
}
