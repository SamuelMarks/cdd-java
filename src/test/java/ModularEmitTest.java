import org.junit.Test;
import static org.junit.Assert.*;

import openapi.OpenAPI;
import openapi.Schema;
import openapi.Components;
import openapi.Paths;
import openapi.PathItem;
import openapi.Operation;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for modular server, DAO, ORM, seeder, route, and test code
 * generation.
 */
public class ModularEmitTest {
	/**
	 * Verifies modular code emission across {@link classes.Emit}, {@link orm.Emit},
	 * {@link dao.Emit}, {@link serverroutes.Emit}, {@link mocks.Emit},
	 * {@link seeder.Emit}, {@link servermain.Emit}, and {@link servertests.Emit}.
	 */
	@Test
	public void testEmitModular() {
		assertNotNull(new dao.Emit());
		assertNotNull(new seeder.Emit());
		assertNotNull(new servermain.Emit());
		assertNotNull(new serverroutes.Emit());
		assertNotNull(new servertests.Emit());

		OpenAPI api = new OpenAPI();
		api.components = new Components();
		api.components.schemas = new HashMap<>();

		Schema schema = new Schema();
		schema.type = "object";
		schema.properties = new HashMap<>();
		Schema prop = new Schema();
		prop.type = "string";
		schema.properties.put("name", prop);
		api.components.schemas.put("User", schema);

		Schema postSchema = new Schema();
		postSchema.type = "object";
		postSchema.properties = new HashMap<>();
		postSchema.properties.put("id", prop);
		api.components.schemas.put("Post", postSchema);
		api.components.schemas.put("NoProps", new Schema());

		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		pi.get = new Operation();
		pi.post = new Operation();
		pi.put = new Operation();
		pi.delete = new Operation();
		api.paths.pathItems.put("/users/{id}", pi);
		api.paths.pathItems.put("/users/list", pi);
		api.paths.pathItems.put("/store/inventory", new PathItem());
		PathItem patchOnly = new PathItem();
		patchOnly.patch = new Operation();
		api.paths.pathItems.put("/items", patchOnly);
		api.paths.pathItems.put("/", pi);
		api.paths.pathItems.put("/{id}", pi);

		Map<String, String> models = classes.Emit.emitModular(api);
		assertTrue(models.containsKey("models/User.java"));
		assertTrue(models.containsKey("models/Post.java"));

		Map<String, String> ormModels = orm.Emit.emitModular(api);
		assertTrue(ormModels.containsKey("models/User.java"));

		Map<String, String> daos = dao.Emit.emitModular(api);
		assertTrue(daos.containsKey("mocks/Dao.java"));
		assertTrue(daos.containsKey("mocks/UserDaos.java"));
		assertTrue(daos.containsKey("mocks/DbConfig.java"));
		assertTrue(daos.containsKey("mocks/DaoFactory.java"));
		assertTrue(daos.containsKey("exceptions/ServerException.java"));
		assertTrue(daos.get("mocks/UserDaos.java").contains("rollback()"));

		Map<String, String> routes = serverroutes.Emit.emitModular(api);
		assertTrue(routes.containsKey("routes/UsersRoutes.java"));

		Map<String, String> generatedMocks = mocks.Emit.emitModular(api);
		assertTrue(generatedMocks.containsKey("mocks/UsersMockServer.java"));
		assertTrue(generatedMocks.containsKey("mocks/RootMockServer.java"));

		Map<String, String> seeders = seeder.Emit.emitModular(api);
		assertTrue(seeders.containsKey("seeder/Seeder.java"));
		Map<String, String> sMain = servermain.Emit.emitModular(api);
		assertTrue(sMain.containsKey("servermain/Main.java"));
		Map<String, String> sTests = servertests.Emit.emitModular(api);
		assertTrue(sTests.containsKey("models/UserTest.java"));
		assertTrue(sTests.containsKey("routes/UsersRoutesTest.java"));

		// Edge cases
		OpenAPI emptyApi = new OpenAPI();
		assertEquals(0, classes.Emit.emitModular(emptyApi).size());
		assertEquals(0, orm.Emit.emitModular(emptyApi).size());
		assertEquals(1, dao.Emit.emitModular(emptyApi).size());
		assertEquals(0, serverroutes.Emit.emitModular(emptyApi).size());
		assertEquals(0, mocks.Emit.emitModular(emptyApi).size());
		assertEquals(0, seeder.Emit.emitModular(emptyApi).size());
		assertEquals(1, servermain.Emit.emitModular(emptyApi).size());
		assertEquals(0, servertests.Emit.emitModular(emptyApi).size());

		emptyApi.paths = new Paths();
		assertEquals(0, serverroutes.Emit.emitModular(emptyApi).size());
		assertEquals(0, mocks.Emit.emitModular(emptyApi).size());
		emptyApi.paths.pathItems = new HashMap<>();
		assertEquals(0, serverroutes.Emit.emitModular(emptyApi).size());
		assertEquals(0, mocks.Emit.emitModular(emptyApi).size());

		emptyApi.components = new Components();
		assertEquals(0, classes.Emit.emitModular(emptyApi).size());
		assertEquals(0, orm.Emit.emitModular(emptyApi).size());
		assertEquals(1, dao.Emit.emitModular(emptyApi).size());
		assertEquals(0, seeder.Emit.emitModular(emptyApi).size());
		assertEquals(0, servertests.Emit.emitModular(emptyApi).size());
		emptyApi.components.schemas = new HashMap<>();
		Schema enumSchema = new Schema();
		enumSchema.enumValues = java.util.Arrays.asList("a", "b");
		emptyApi.components.schemas.put("AnEnum", enumSchema);

		Map<String, String> classesModels = classes.Emit.emitModular(emptyApi);
		assertTrue(classesModels.containsKey("models/AnEnum.java"));

		assertEquals(0, orm.Emit.emitModular(emptyApi).size());
		assertEquals(4, dao.Emit.emitModular(emptyApi).size()); // ServerException, DbConfig, DaoFactory, Dao always gen
		assertEquals(1, seeder.Emit.emitModular(emptyApi).size());
		assertEquals(0, servertests.Emit.emitModular(emptyApi).size());

		// Test dao.Emit.emit and seeder.Emit.emit
		OpenAPI nullCompApi = new OpenAPI();
		assertEquals("", dao.Emit.emit(nullCompApi, null));
		assertEquals("existing", dao.Emit.emit(nullCompApi, "existing"));
		assertEquals("", seeder.Emit.emit(nullCompApi, null));
		assertEquals("existing", seeder.Emit.emit(nullCompApi, "existing"));
		nullCompApi.components = new Components();
		assertEquals("", dao.Emit.emit(nullCompApi, null));
		assertEquals("", seeder.Emit.emit(nullCompApi, null));

		String daoEmitted = dao.Emit.emit(emptyApi, null);
		assertNotNull(daoEmitted);
		String seederEmitted = seeder.Emit.emit(emptyApi, null);
		assertNotNull(seederEmitted);

		Schema allTypes = new Schema();
		allTypes.properties = new HashMap<>();

		Schema strProp = new Schema();
		strProp.type = "string";
		allTypes.properties.put("email", strProp);
		allTypes.properties.put("name", strProp);
		allTypes.properties.put("phone", strProp);
		allTypes.properties.put("other", strProp);
		allTypes.properties.put("id", strProp);

		Schema intProp = new Schema();
		intProp.type = "integer";
		allTypes.properties.put("my_int", intProp);

		Schema longProp = new Schema();
		longProp.type = "integer";
		longProp.format = "int64";
		allTypes.properties.put("my_long", longProp);

		Schema boolProp = new Schema();
		boolProp.type = "boolean";
		allTypes.properties.put("my_bool", boolProp);

		Schema doubleProp = new Schema();
		doubleProp.type = "number";
		allTypes.properties.put("my_double", doubleProp);

		Schema floatProp = new Schema();
		floatProp.type = "number";
		floatProp.format = "float";
		allTypes.properties.put("my_float", floatProp);

		Schema dateProp = new Schema();
		dateProp.type = "string";
		dateProp.format = "date";
		allTypes.properties.put("my_date", dateProp);

		Schema datetimeProp = new Schema();
		datetimeProp.type = "string";
		datetimeProp.format = "date-time";
		allTypes.properties.put("my_datetime", datetimeProp);

		Schema uuidProp = new Schema();
		uuidProp.type = "string";
		uuidProp.format = "uuid";
		allTypes.properties.put("my_uuid", uuidProp);

		Schema binProp = new Schema();
		binProp.type = "string";
		binProp.format = "binary";
		allTypes.properties.put("my_bin", binProp);

		Schema objProp = new Schema();
		objProp.type = "object";
		allTypes.properties.put("my_obj", objProp);

		Schema arrProp = new Schema();
		arrProp.type = "array";
		arrProp.items = strProp;
		allTypes.properties.put("my_arr", arrProp);

		Schema anyProp = new Schema();
		anyProp.type = "something_else";
		allTypes.properties.put("my_any", anyProp);

		Schema fkProp = new Schema();
		fkProp.$ref = "#/components/schemas/User";
		allTypes.properties.put("user_id", fkProp);

		emptyApi.components.schemas.put("AllTypes", allTypes);
		Map<String, String> seederCode = seeder.Emit.emitModular(emptyApi);
		assertTrue(seederCode.containsKey("seeder/Seeder.java"));
		orm.Emit.emitModular(emptyApi);

		Schema badName = new Schema();
		badName.properties = new HashMap<>();
		Schema nProp = new Schema();
		nProp.type = "string";
		badName.properties.put("enum", nProp);
		badName.properties.put("default", nProp);
		badName.properties.put("class", nProp);
		badName.properties.put("const", nProp);
		badName.properties.put("1bad", nProp);
		emptyApi.components.schemas.put("BadName", badName);
		emptyApi.components.schemas.put("Emit", badName);
		emptyApi.components.schemas.put("Parse", badName);

		seederCode = seeder.Emit.emitModular(emptyApi);
		assertTrue(seederCode.containsKey("seeder/Seeder.java"));
		orm.Emit.emitModular(emptyApi);
		dao.Emit.emitModular(emptyApi);
		servertests.Emit.emitModular(emptyApi);

		String fullDao = dao.Emit.emit(emptyApi, null);
		assertTrue(fullDao.contains("class BadNameDaos"));
		assertEquals(fullDao, dao.Emit.emit(emptyApi, "   "));
		String updatedDao = dao.Emit.emit(emptyApi, fullDao);
		assertTrue(updatedDao.contains("class BadNameDaos"));
		String partialDao = dao.Emit.emit(emptyApi,
				"public class BadNameDaos { public int x; public class OtherInner {} }");
		assertTrue(partialDao.contains("StubBadNameDao"));
		String emptySrcDao = dao.Emit.emit(emptyApi, "");
		assertTrue(emptySrcDao.contains("class BadNameDaos"));

		OpenAPI nullPathsApi = new OpenAPI();
		nullPathsApi.paths = new Paths();
		nullPathsApi.paths.pathItems = null;
		assertEquals(1, servermain.Emit.emitModular(nullPathsApi).size());
		assertEquals(0, serverroutes.Emit.emitModular(nullPathsApi).size());
		assertEquals(0, servertests.Emit.emitModular(nullPathsApi).size());

		PathItem deleteOnly = new PathItem();
		deleteOnly.delete = new Operation();
		api.paths.pathItems.put("/del", deleteOnly);
		serverroutes.Emit.emitModular(api);

		String fullSeeder = seeder.Emit.emit(emptyApi, null);
		assertTrue(fullSeeder.contains("class Seeder"));
		assertEquals(fullSeeder, seeder.Emit.emit(emptyApi, "   "));
		String updatedSeeder = seeder.Emit.emit(emptyApi, fullSeeder);
		assertTrue(updatedSeeder.contains("class Seeder"));
		String emptyClassSeeder = seeder.Emit.emit(emptyApi, "public class Other {}");
		assertTrue(emptyClassSeeder.contains("class Seeder"));
	}
}
