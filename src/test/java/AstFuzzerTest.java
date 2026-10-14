import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.*;
import openapi.*;

/**
 * Property-based fuzzer test verifying AST emitters and parsers handle sparse
 * and null-populated OpenAPI models cleanly.
 */
public class AstFuzzerTest {
	/**
	 * Fuzzes fields on a populated OpenAPI model and verifies route and class
	 * emission never fails.
	 *
	 * @throws Exception
	 *             If reflection or emission fails.
	 */
	@Test
	public void fuzzNulls() throws Exception {
		OpenAPI api = new OpenAPI();
		api.openapi = "3.0.0";
		api.info = new Info();
		api.info.title = "test";
		api.info.version = "1.0";

		api.paths = new Paths();
		api.paths.pathItems = new HashMap<>();
		PathItem pi = new PathItem();
		api.paths.pathItems.put("/test", pi);

		pi.get = new Operation();
		pi.get.operationId = "testOp";
		pi.get.parameters = new ArrayList<>();
		Parameter p = new Parameter();
		p.name = "qp";
		p.in = "query";
		p.schema = new Schema();
		p.schema.type = "string";
		pi.get.parameters.add(p);

		pi.get.requestBody = new RequestBody();
		((RequestBody) pi.get.requestBody).content = new HashMap<>();
		MediaType mt = new MediaType();
		mt.itemSchema = new HashMap<String, Object>();
		((Map<String, Object>) mt.itemSchema).put("type", "string");
		((RequestBody) pi.get.requestBody).content.put("application/json", mt);

		pi.get.responses = new Responses();
		pi.get.responses.statusCodes = new HashMap<>();
		Response resp = new Response();
		resp.description = "ok";
		pi.get.responses.statusCodes.put("200", resp);

		pi.get.callbacks = new HashMap<>();
		pi.get.callbacks.put("cb", new Callback());

		pi.get.tags = Arrays.asList("tag1");

		api.components = new Components();
		api.components.schemas = new HashMap<>();
		Schema sch = new Schema();
		sch.type = "object";
		sch.properties = new HashMap<>();
		sch.properties.put("prop", new Schema());
		api.components.schemas.put("TestSchema", sch);

		fuzzObject(api, () -> {
			String res = routes.Emit.emit(api, null);
			assertNotNull(res);
		});

		fuzzObject(api, () -> {
			String res = classes.Emit.emit(api, null);
			assertNotNull(res);
		});

		String sampleSource = "public class T {}";
		assertNotNull(routes.Parse.parse(sampleSource));
		assertNotNull(classes.Parse.parse(sampleSource));
	}

	/**
	 * Recursively mutates fields of the target object to null, empty, or inverted
	 * values while invoking the test assertion callback.
	 *
	 * @param obj
	 *            The object instance to fuzz.
	 * @param test
	 *            The assertion callback to run after each mutation.
	 * @throws Exception
	 *             If reflective field access fails.
	 */
	private void fuzzObject(Object obj, Runnable test) throws Exception {
		if (obj == null)
			return;
		Class<?> clazz = obj.getClass();
		if (clazz.getName().startsWith("java."))
			return;

		for (Field f : clazz.getFields()) {
			Object orig = f.get(obj);
			if (orig != null) {
				f.set(obj, null);
				test.run();
				f.set(obj, orig);

				if (orig instanceof Map) {
					Map<?, ?> map = (Map<?, ?>) orig;
					if (!map.isEmpty()) {
						Map<?, ?> copy = new HashMap<>(map);
						map.clear();
						test.run();
						((Map) map).putAll(copy);
					}
					for (Object val : map.values()) {
						fuzzObject(val, test);
					}
				} else if (orig instanceof List) {
					List<?> list = (List<?>) orig;
					if (!list.isEmpty()) {
						try {
							List<?> copy = new ArrayList<>(list);
							list.clear();
							test.run();
							((List) list).addAll(copy);
						} catch (UnsupportedOperationException e) {
						}
					}
					for (Object val : list) {
						fuzzObject(val, test);
					}
				} else {
					fuzzObject(orig, test);
				}

				if (orig instanceof String) {
					f.set(obj, "");
					test.run();
					f.set(obj, orig);
				} else if (orig instanceof Boolean) {
					f.set(obj, !(Boolean) orig);
					test.run();
					f.set(obj, orig);
				}
			}
		}
	}
}
