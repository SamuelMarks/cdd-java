package routes;

import org.junit.Test;
import static org.junit.Assert.*;
import openapi.OpenAPI;
import java.util.*;

/**
 * Comprehensive unit and round-trip tests for {@link routes.Parse} and
 * {@link routes.Emit}.
 */
public class RoutesParseEmitTest {

	/**
	 * Verifies exhaustive parsing and emission of Javadoc tags, operations,
	 * parameters, request bodies, responses, callbacks, and webhooks.
	 */
	@Test
	public void testParseEmitExhaustive() {
		String code1 = """
				    /**
				     * @openapiVersion 3.1.0
				     * @openapiSelf my-self
				     * @jsonSchemaDialect dialect1
				     * @title MyTestClient
				     * @version 1.0.0
				     * @summary Test Summary
				     * @description Test Description
				     * @termsOfService http://tos
				     * @contactName Contact Name
				     * @contactEmail test@test.com
				     * @contactUrl http://contact
				     * @licenseName MIT
				     * @licenseIdentifier MIT-id
				     * @licenseUrl http://mit
				     * @globalTag myTag summary=a_b description=c_d externalDocsUrl=http://ext parent=p kind=k
				     * @globalTag tagOnly
				     * @server http://server Server Desc
				     * @serverName MainServer
				     * @serverVariable MainServer port 8080 port_desc
				     * @serverVariable MainServer host localhost
				     * @serverVariable MissingServer host localhost
				     * @serverVariableEnum MainServer version v1,v2
				     * @serverVariableEnum MissingServer version v1,v2
				     * @securityScheme sec1 type=http scheme=bearer in=header name=Authorization bearerFormat=JWT openIdConnectUrl=http://oidc
				     * @securityScheme secNull type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @securitySchemeFlow implicit sec1 http://auth http://refresh
				     * @securitySchemeFlow implicit sec1_null - -
				     * @securitySchemeFlow password sec1 http://token http://refresh
				     * @securitySchemeFlow password sec1_null - -
				     * @securitySchemeFlow clientCredentials sec1 http://token http://refresh
				     * @securitySchemeFlow clientCredentials sec1_null - -
				     * @securitySchemeFlow authorizationCode sec1 http://auth http://token http://refresh
				     * @securitySchemeFlow authorizationCode sec1_null - - -
				     * @securitySchemeFlow deviceAuthorization sec1 http://device http://token http://refresh
				     * @securitySchemeFlow deviceAuthorization sec1_null - - -
				     * @securitySchemeFlowScope sec1 implicit scope1 desc1
				     * @securitySchemeFlowScope sec1 implicit scopeNull -
				     * @securitySchemeFlowScope sec1 password scope2 desc2
				     * @securitySchemeFlowScope sec1 clientCredentials scope3 desc3
				     * @securitySchemeFlowScope sec1 authorizationCode scope4 desc4
				     * @securitySchemeFlowScope sec1 deviceAuthorization scope5 desc5
				     * @componentResponse r1 response_desc
				     * @componentResponse r2
				     * @componentParameter p1 p_name header p_desc
				     * @componentParameter p2 - - p_desc
				     * @componentRequestBody rb1 rb_desc
				     * @componentRequestBody rb2
				     * @componentHeader h1 h_desc
				     * @componentHeader h2
				     * @componentLink l1 op_id
				     * @componentLinkOpId l1 op_id2
				     * @componentLinkOpRef l1 op_ref
				     * @componentLinkDesc l1 l_desc
				     * @componentLinkServer l1 http://link_server
				     * @componentLinkParam l1 p_name p_value
				     * @componentLinkParam l1 p_name2
				     * @componentLinkRequestBody l1 rb_value
				     * @componentCallback cb1 path1 get
				     * @componentCallback cb2 path2 post
				     * @componentCallback cb3 path3 put
				     * @componentCallback cb4 path4 delete
				     * @componentCallback cb5 path5 patch
				     * @componentCallback cb6 path6 query
				     * @componentPathItem pi1 pi_desc
				     * @componentPathItem pi2
				     * @componentMediaType mt1 application/json
				     * @componentMediaType mt2 -
				     * @pathSummary /path1 p_summary
				     * @pathDescription /path1 p_desc
				     * @pathServer /path1 http://pserver p_server_desc
				     * @pathServer /path2 http://pserver2
				     * @pathParameter /path1 p1 query p_desc
				     * @pathParameter /path2 - - p_desc
				     */
				    public class MyClientClient {
				        /**
				         * Endpoint desc
				         * Second line
				         * @callback cb_name cb_expr get
				         * @callback cb_name2 cb_expr2 post
				         * @callback cb_name3 cb_expr3 put
				         * @callback cb_name4 cb_expr4 delete
				         * @callback cb_name5 cb_expr5 patch
				         * @callback cb_name6 cb_expr6 query
				         * @tag op_tag
				         * @externalDocs http://ext_doc ext_desc
				         * @externalDocs http://ext_doc2
				         * @deprecated
				         * @operationServer http://op_server op_server_desc
				         * @operationServer http://op_server2
				         * @operationSecurity sec1 scope1,scope2
				         * @operationSecurity sec2
				         * @param p1 p1_desc
				         * @requiredParam p1
				         * @deprecatedParam p1
				         * @paramAllowEmptyValue p1
				         * @paramStyle p1 style1
				         * @paramExplode p1
				         * @paramAllowReserved p1
				         * @paramSchema p1 string
				         * @paramContent p1 application/json
				         * @paramExample p1 ex1
				         * @paramExamples p1 ex_key ex_sum|ex_desc|ex_val
				         * @paramExamples p1 ex_key_empty ||
				         * @requestBody rb_desc
				         * @requestBodyRequired true
				         * @requestBodyContent application/json
				         * @requestBodyContentSchema application/json object
				         * @requestBodyContentExample application/json ex_body
				         * @requestBodyContentItemSchema application/json string
				         * @requestBodyContentPrefixEncoding application/json text/plain
				         * @requestBodyContentItemEncoding application/json text/html
				         * @requestBodyEncoding application/json enc1 text/xml
				         * @requestBodyEncodingPrefixEncoding application/json enc1 text/csv
				         * @requestBodyEncodingItemEncoding application/json enc1 text/css
				         * @requestBodyContentExamples application/json ex_key2 ex_sum2|ex_desc2|ex_val2
				         * @requestBodyContentExamples application/json ex_key_empty2 ||
				         * @responseDefault def_desc
				         * @responseDefault
				         * @response 200 200_desc
				         * @response 201
				         * @response default default_desc
				         * @response default
				         * @responseHeader default h1 h1_desc
				         * @responseHeaderRequired default h1
				         * @responseHeaderDeprecated default h1
				         * @responseHeaderStyle default h1 style2
				         * @responseHeaderExplode default h1
				         * @responseHeaderSchema default h1 integer
				         * @responseHeaderExample default h1 ex_h1
				         * @responseHeaderContent default h1 application/xml
				         * @responseHeaderExamples default h1 ex_key3 ex_sum3|ex_desc3|ex_val3
				         * @responseHeaderExamples default h1 ex_key_empty3 ||
				         * @responseContent default application/json
				         * @responseContentItemSchema default application/json boolean
				         * @responseContentPrefixEncoding default application/json text/plain
				         * @responseContentItemEncoding default application/json text/html
				         * @responseEncoding default application/json enc2 text/xml
				         * @responseEncodingPrefixEncoding default application/json enc2 text/csv
				         * @responseEncodingItemEncoding default application/json enc2 text/css
				         * @responseContentSchema default application/json array
				         * @responseContentExample default application/json ex_res
				         * @responseContentExamples default application/json ex_key4 ex_sum4|ex_desc4|ex_val4
				         * @responseContentExamples default application/json ex_key_empty4 ||
				         * @responseContent 200 text/plain
				         * @responseContentSchema 200 text/plain string
				         * @responseContentItemSchema 200 text/plain string
				         * @responseContentPrefixEncoding 200 text/plain text/csv
				         * @responseContentItemEncoding 200 text/plain text/tsv
				         * @responseEncoding 200 text/plain enc3 text/xml
				         * @responseEncodingPrefixEncoding 200 text/plain enc3 text/csv
				         * @responseEncodingItemEncoding 200 text/plain enc3 text/css
				         * @responseContentExample 200 text/plain hello
				         * @responseContentExamples 200 text/plain ex_key5 sum5|desc5|val5
				         * @responseLink default l2
				         * @responseLinkOpId default l2 op2
				         * @responseLinkOpRef default l2 op2_ref
				         * @responseLinkDesc default l2 l2_desc
				         * @responseLinkServer default l2 http://l2_server
				         * @responseLinkParam default l2 p2 v2
				         * @responseLinkParam default l2 p3
				         * @responseLinkRequestBody default l2 rb2
				         * @responseLink 200 l3
				         * @responseLinkOpId 200 l3 op3
				         * @responseLinkOpRef 200 l3 op3_ref
				         * @responseLinkDesc 200 l3 l3_desc
				         * @responseLinkServer 200 l3 http://l3_server
				         * @responseLinkParam 200 l3 p3 v3
				         * @responseLinkRequestBody 200 l3 rb3
				         */
				        public HttpResponse<String> getMethod(String p1, String body, String h2, String auth) {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path1/" + p1 + "?q=1"));
				            b.header("h2", h2);
				            b.header("Authorization", auth);
				            b.GET();
				            return null;
				        }

				        public HttpResponse<String> postMethod(String requestBody) {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path2"));
				            b.POST(HttpRequest.BodyPublishers.ofString(requestBody));
				            return null;
				        }

				        public HttpResponse<String> putMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path3"));
				            b.PUT(HttpRequest.BodyPublishers.noBody());
				            return null;
				        }

				        public HttpResponse<String> deleteMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path4"));
				            b.DELETE();
				            return null;
				        }

				        public HttpResponse<String> patchMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path5"));
				            b.method("PATCH", HttpRequest.BodyPublishers.noBody());
				            return null;
				        }

				        public HttpResponse<String> queryMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path6"));
				            b.QUERY(HttpRequest.BodyPublishers.noBody());
				            return null;
				        }

				        public HttpResponse<String> optionsMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path7"));
				            b.OPTIONS();
				            return null;
				        }

				        public HttpResponse<String> headMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path8"));
				            b.HEAD();
				            return null;
				        }

				        public HttpResponse<String> traceMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path9"));
				            b.TRACE();
				            return null;
				        }

				        public HttpResponse<String> customMethod() {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/path10"));
				            b.method("CUSTOM", HttpRequest.BodyPublishers.noBody());
				            return null;
				        }
				    }

				    public interface MyWebhookHandler {
				        void onPost(String body, String param1);
				    }

				    public interface EmptyWebhookHandler {
				    }

				    public interface AnotherWebhookHandler {
				        void onPost();
				    }

				    public interface NoBodyWebhookHandler {
				        void onPost(String param1, String param2);
				    }
				""";

		OpenAPI api = Parse.parse(code1);
		assertNotNull(api);

		// Add callbacks programmatically to hit coverage
		openapi.Callback cbGet = new openapi.Callback();
		cbGet.pathItems = new HashMap<>();
		openapi.PathItem cbPi = new openapi.PathItem();
		cbPi.post = new openapi.Operation();
		cbPi.post.parameters = new ArrayList<>();
		openapi.Parameter cbParam = new openapi.Parameter();
		cbParam.name = "cb_param";
		cbPi.post.parameters.add(openapi.ReferenceOr.ofValue(cbParam));
		cbGet.pathItems.put("cb_expr", cbPi);

		Map<String, openapi.ReferenceOr<openapi.Callback>> cbs = new HashMap<>();
		cbs.put("cb_name", openapi.ReferenceOr.ofValue(cbGet));

		// Put callbacks on different methods
		if (api.paths.pathItems.containsKey("/path3")) {
			api.paths.pathItems.get("/path3").put.callbacks = cbs;
		}
		if (api.paths.pathItems.containsKey("/path4")) {
			api.paths.pathItems.get("/path4").delete.callbacks = cbs;
		}
		if (api.paths.pathItems.containsKey("/path5")) {
			api.paths.pathItems.get("/path5").patch.callbacks = cbs;
		}
		if (api.paths.pathItems.containsKey("/path6")) {
			api.paths.pathItems.get("/path6").query.callbacks = cbs;
		}

		// Generate new client
		String emitted = Emit.emit(api, null);
		assertNotNull(emitted);

		// Let's emit into existing source with methods to trigger existing
		// implementation check
		String existing = """
				public class MyClientClient {
				    public HttpResponse<String> getMethod(String p1, String body, String h2, String auth) {
				        return null;
				    }
				    public interface MyWebhookHandler {}
				    public interface cb_nameCallbackHandler {}
				}
				""";
		String emittedExisting = Emit.emit(api, existing);
		assertNotNull(emittedExisting);

		// Emitting with empty or whitespace existing source
		Emit.emit(api, "   ");
		Emit.emit(api, "");

		// Create an OpenAPI object manually to test additional branches in Emit
		OpenAPI manualApi = new OpenAPI();
		// Model with null info
		Emit.emit(manualApi, null);

		manualApi.info = new openapi.Info();
		manualApi.info.title = null;
		manualApi.security = new ArrayList<>();
		openapi.SecurityRequirement req = new openapi.SecurityRequirement();
		req.requirements = new HashMap<>();
		req.requirements.put("secBasic", new ArrayList<>());
		req.requirements.put("secApi", new ArrayList<>());
		req.requirements.put("secBearer", new ArrayList<>());
		manualApi.security.add(req);
		manualApi.components = new openapi.Components();
		manualApi.components.securitySchemes = new HashMap<>();
		openapi.SecurityScheme sch1 = new openapi.SecurityScheme();
		sch1.type = "http";
		sch1.scheme = "basic";
		manualApi.components.securitySchemes.put("secBasic", openapi.ReferenceOr.ofValue(sch1));
		openapi.SecurityScheme sch2 = new openapi.SecurityScheme();
		sch2.type = "apiKey";
		sch2.in = "header";
		sch2.name = "X-API-Key";
		manualApi.components.securitySchemes.put("secApi", openapi.ReferenceOr.ofValue(sch2));
		openapi.SecurityScheme sch3 = new openapi.SecurityScheme();
		sch3.type = "http";
		sch3.scheme = "bearer";
		manualApi.components.securitySchemes.put("secBearer", openapi.ReferenceOr.ofValue(sch3));

		manualApi.paths = new openapi.Paths();
		manualApi.paths.pathItems = new HashMap<>();
		openapi.PathItem pi = new openapi.PathItem();
		pi.summary = "pi_sum";
		pi.description = "pi_desc";
		pi.ref = "pi_ref";
		pi.parameters = new ArrayList<>();
		openapi.Parameter pathParam = new openapi.Parameter();
		pathParam.name = "X-API-Key"; // Duplicate param check in Emit
		pathParam.in = "header";
		pi.parameters.add(openapi.ReferenceOr.ofValue(pathParam));

		pi.get = new openapi.Operation();
		pi.get.operationId = "myOpId";
		pi.get.summary = "";
		pi.get.description = "";
		pi.get.parameters = new ArrayList<>();
		openapi.Parameter p = new openapi.Parameter();
		p.name = "X-API-Key"; // Duplicate param check in Emit
		p.in = "header";
		pi.get.parameters.add(openapi.ReferenceOr.ofValue(p));

		openapi.Parameter pEmptyName = new openapi.Parameter();
		pEmptyName.name = "!"; // safeName becomes empty
		pi.get.parameters.add(openapi.ReferenceOr.ofValue(pEmptyName));

		manualApi.paths.pathItems.put("/manual", pi);

		// Also a path item with operationId = null to test method name fallback
		openapi.PathItem pi2 = new openapi.PathItem();
		pi2.get = new openapi.Operation();
		pi2.get.operationId = null;
		manualApi.paths.pathItems.put("/manual2/{id}", pi2);

		// A path item with ending +""
		openapi.PathItem pi3 = new openapi.PathItem();
		pi3.get = new openapi.Operation();
		pi3.get.operationId = null;
		manualApi.paths.pathItems.put("/manual3/{id}+\"\"", pi3);

		String emittedManual = Emit.emit(manualApi, null);
		assertNotNull(emittedManual);
		assertTrue(emittedManual.contains("myOpId"));

		// Test Emit empty OpenAPI
		OpenAPI emptyApi = new OpenAPI();
		String emptyEmitted = Emit.emit(emptyApi, null);
		assertNotNull(emptyEmitted);

		// Also test boundary arities and malformed inputs in Parse
		String edgeCode = """
				    /**
				     * @contactName
				     * @contactEmail
				     * @contactUrl
				     * @licenseName
				     * @licenseIdentifier
				     * @licenseUrl
				     * @globalTag
				     * @globalTag a b
				     * @globalTag a summary= description= externalDocsUrl= parent= kind=
				     * @server
				     * @serverName
				     * @serverVariable
				     * @serverVariable MainServer
				     * @serverVariable MainServer port
				     * @serverVariableEnum
				     * @serverVariableEnum MainServer
				     * @serverVariableEnum MainServer port
				     * @securityScheme
				     * @securitySchemeFlow
				     * @securitySchemeFlow implicit
				     * @securitySchemeFlow implicit sec1
				     * @securitySchemeFlow implicit sec1 -
				     * @securitySchemeFlow password sec1 -
				     * @securitySchemeFlow clientCredentials sec1 -
				     * @securitySchemeFlow authorizationCode sec1 - -
				     * @securitySchemeFlow deviceAuthorization sec1 - -
				     * @securitySchemeFlowScope
				     * @securitySchemeFlowScope sec1
				     * @securitySchemeFlowScope sec1 implicit
				     * @componentResponse
				     * @componentParameter
				     * @componentRequestBody
				     * @componentHeader
				     * @componentLink
				     * @componentLinkOpId
				     * @componentLinkOpRef
				     * @componentLinkDesc
				     * @componentLinkServer
				     * @componentLinkParam
				     * @componentLinkParam l1
				     * @componentLinkRequestBody
				     * @componentCallback
				     * @componentCallback cb1
				     * @componentCallback cb1 path
				     * @componentPathItem
				     * @componentMediaType
				     * @pathSummary
				     * @pathDescription
				     * @pathServer
				     * @pathServer /path
				     * @pathParameter
				     * @pathParameter /path
				     * @pathParameter /path p1
				     */
				    public class EdgeClient {

				        /**
				         * @callback
				         * @callback cb
				         * @callback cb exp
				         * @externalDocs
				         * @operationServer
				         * @operationSecurity
				         * @param
				         * @paramStyle
				         * @paramSchema
				         * @paramContent
				         * @paramExample
				         * @paramExamples
				         * @paramExamples p1
				         * @paramExamples p1 ex
				         * @requestBodyContentSchema
				         * @requestBodyContentExample
				         * @requestBodyContentItemSchema
				         * @requestBodyContentPrefixEncoding
				         * @requestBodyContentItemEncoding
				         * @requestBodyEncoding
				         * @requestBodyEncoding application/json
				         * @requestBodyEncodingPrefixEncoding
				         * @requestBodyEncodingPrefixEncoding application/json
				         * @requestBodyEncodingItemEncoding
				         * @requestBodyEncodingItemEncoding application/json
				         * @requestBodyContentExamples
				         * @requestBodyContentExamples application/json
				         * @response
				         * @responseHeader
				         * @responseHeader 200
				         * @responseHeaderStyle
				         * @responseHeaderSchema
				         * @responseHeaderExample
				         * @responseHeaderContent
				         * @responseHeaderExamples
				         * @responseContent
				         * @responseContentItemSchema
				         * @responseContentPrefixEncoding
				         * @responseContentItemEncoding
				         * @responseEncoding
				         * @responseEncoding 200
				         * @responseEncoding 200 application/json
				         * @responseEncodingPrefixEncoding
				         * @responseEncodingPrefixEncoding 200
				         * @responseEncodingPrefixEncoding 200 application/json
				         * @responseEncodingItemEncoding
				         * @responseEncodingItemEncoding 200
				         * @responseEncodingItemEncoding 200 application/json
				         * @responseContentSchema
				         * @responseContentExample
				         * @responseContentExamples
				         * @responseContentExamples 200
				         * @responseLink
				         * @responseLinkOpId
				         * @responseLinkOpRef
				         * @responseLinkDesc
				         * @responseLinkServer
				         * @responseLinkParam
				         * @responseLinkParam 200
				         * @responseLinkParam 200 l1
				         * @responseLinkRequestBody
				         */
				        public HttpResponse<String> edge(String p1) {
				            HttpRequest.Builder b = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/edge?q=" + p1));
				            b.method("GET", HttpRequest.BodyPublishers.noBody());
				            return null;
				        }
				    }
				""";
		OpenAPI edgeApi = Parse.parse(edgeCode);
		assertNotNull(edgeApi);
		assertNotNull(edgeApi.paths);
		assertTrue(edgeApi.paths.pathItems.containsKey("/edge"));

		OpenAPI errApi = Parse.parse("this is not java code {{");

		assertNotNull(errApi);
	}

	/**
	 * Verifies parsing of class-level and method-level Javadoc tags in both forward
	 * and reverse order.
	 */
	@Test
	public void testGeneratedTagsCoverage() {
		String source = "package test;\n" + "import java.net.http.HttpResponse;\n" + "/**\n"
				+ " * @openapiVersion 3.2.0\n" + " * @openapiSelf self\n" + " * @jsonSchemaDialect dialect\n"
				+ " * @title title\n" + " * @version 1.0.0\n" + " * @summary sum\n" + " * @description desc\n"
				+ " * @termsOfService tos\n" + " * @contactName name\n" + " * @contactEmail email\n"
				+ " * @contactUrl url\n" + " * @licenseName lname\n" + " * @licenseIdentifier lident\n"
				+ " * @licenseUrl lurl\n" + " * @globalTag tag1\n" + " * @server srv url desc\n"
				+ " * @serverName srv\n" + " * @serverVariable srv port 8080 desc\n"
				+ " * @serverVariableEnum srv enum v1,v2\n" + " * @securityScheme sec1 type=http scheme=basic\n"
				+ " * @securitySchemeFlow implicit sec1 http http scope1=a\n"
				+ " * @securitySchemeFlowScope implicit sec1 scope1 desc1\n" + " * @componentResponse r1\n"
				+ " * @componentParameter p1\n" + " * @componentRequestBody rb1\n" + " * @componentHeader h1\n"
				+ " * @componentLink l1\n" + " * @componentLinkOpId l1 opId\n" + " * @componentLinkOpRef l1 opRef\n"
				+ " * @componentLinkDesc l1 desc\n" + " * @componentLinkServer l1 srv\n"
				+ " * @componentLinkParam l1 p1 v1\n" + " * @componentLinkRequestBody l1 rb\n"
				+ " * @componentCallback cb1\n" + " * @componentPathItem pi1\n" + " * @componentMediaType mt1\n"
				+ " * @pathSummary /path sum\n" + " * @pathDescription /path desc\n" + " * @pathServer /path s1\n"
				+ " * @pathParameter /path p1\n" + " * @responseHeaderExamples 200 h1 ex1 sum|desc|val\n"
				+ " * @responseHeader 200 h1 desc\n" + " * @responseHeaderRequired 200 h1 true\n"
				+ " * @responseHeaderDeprecated 200 h1 true\n" + " * @responseHeaderStyle 200 h1 simple\n"
				+ " * @responseHeaderExplode 200 h1 true\n" + " * @responseHeaderSchema 200 h1 string\n"
				+ " * @responseHeaderExample 200 h1 val\n" + " * @responseHeaderContent 200 h1 application/json\n"
				+ " * @responseContentExamples 200 application/json ex1 sum|desc|val\n"
				+ " * @responseContent 200 application/json\n"
				+ " * @responseContentItemSchema 200 application/json object\n"
				+ " * @responseContentPrefixEncoding 200 application/json enc1 object\n"
				+ " * @responseContentItemEncoding 200 application/json enc1 object\n"
				+ " * @responseEncoding 200 application/json enc1 object\n"
				+ " * @responseEncodingPrefixEncoding 200 application/json enc1 enc2 object\n"
				+ " * @responseEncodingItemEncoding 200 application/json enc1 enc2 object\n"
				+ " * @responseContentSchema 200 application/json object\n"
				+ " * @responseContentExample 200 application/json ex1\n" + " * @responseLinkParam 200 l1 p1 v1\n"
				+ " * @responseLink 200 l1\n" + " * @responseLinkOpId 200 l1 opId\n"
				+ " * @responseLinkOpRef 200 l1 opRef\n" + " * @responseLinkDesc 200 l1 desc\n"
				+ " * @responseLinkServer 200 l1 s1\n" + " * @responseLinkRequestBody 200 l1 rb\n" + " */\n"
				+ "class GeneratedClassTags {\n" + "    /**\n" + "     * @callback cb1\n" + "     * @tag tag1\n"
				+ "     * @externalDocs url desc\n" + "     * @deprecated true\n" + "     * @operationServer s1\n"
				+ "     * @operationSecurity sec1\n" + "     * @param p1 desc\n" + "     * @requiredParam p1\n"
				+ "     * @deprecatedParam p1\n" + "     * @paramAllowEmptyValue p1\n"
				+ "     * @paramStyle p1 simple\n" + "     * @paramExplode p1\n" + "     * @paramAllowReserved p1\n"
				+ "     * @paramSchema p1 string\n" + "     * @paramContent p1 application/json\n"
				+ "     * @paramExample p1 val\n" + "     * @paramExamples p1 ex1 sum|desc|val\n"
				+ "     * @requestBody desc\n" + "     * @requestBodyRequired true\n"
				+ "     * @requestBodyContent application/json\n"
				+ "     * @requestBodyContentSchema application/json object\n"
				+ "     * @requestBodyContentExample application/json ex1\n"
				+ "     * @requestBodyContentItemSchema application/json object\n"
				+ "     * @requestBodyContentPrefixEncoding application/json enc1 object\n"
				+ "     * @requestBodyContentItemEncoding application/json enc1 object\n"
				+ "     * @requestBodyEncoding application/json enc1 object\n"
				+ "     * @requestBodyEncodingPrefixEncoding application/json enc1 enc2 object\n"
				+ "     * @requestBodyEncodingItemEncoding application/json enc1 enc2 object\n"
				+ "     * @requestBodyContentExamples application/json ex1 sum|desc|val\n"
				+ "     * @responseDefault desc\n" + "     * @response 200 desc\n" + "     */\n"
				+ "    public HttpResponse<String> testMethod(String Authorization) { new StringBuilder(baseUrl + \"/test\"); HttpRequest.newBuilder().uri(java.net.URI.create(baseUrl + \"/test\")).GET(); return null; }\n"
				+ "}\n";

		openapi.OpenAPI api = routes.Parse.parse(source);
		assertNotNull(api);
		assertEquals("3.2.0", api.openapi);
		assertEquals("self", api.$self);
		assertEquals("dialect", api.jsonSchemaDialect);
		assertEquals("title", api.info.title);
		assertEquals("1.0.0", api.info.version);

		String sourceReversed = "package test;\n" + "import java.net.http.HttpResponse;\n" + "/**\n"
				+ " * @responseLinkRequestBody 200 l1 rb\n" + " * @responseLinkServer 200 l1 s1\n"
				+ " * @responseLinkDesc 200 l1 desc\n" + " * @responseLinkOpRef 200 l1 opRef\n"
				+ " * @responseLinkOpId 200 l1 opId\n" + " * @responseLink 200 l1\n"
				+ " * @responseLinkParam 200 l1 p1 v1\n" + " * @responseContentExample 200 application/json ex1\n"
				+ " * @responseContentSchema 200 application/json object\n"
				+ " * @responseEncodingItemEncoding 200 application/json enc1 enc2 object\n"
				+ " * @responseEncodingPrefixEncoding 200 application/json enc1 enc2 object\n"
				+ " * @responseEncoding 200 application/json enc1 object\n"
				+ " * @responseContentItemEncoding 200 application/json enc1 object\n"
				+ " * @responseContentPrefixEncoding 200 application/json enc1 object\n"
				+ " * @responseContentItemSchema 200 application/json object\n"
				+ " * @responseContent 200 application/json\n"
				+ " * @responseContentExamples 200 application/json ex1 sum|desc|val\n"
				+ " * @responseHeaderContent 200 h1 application/json\n" + " * @responseHeaderExample 200 h1 val\n"
				+ " * @responseHeaderSchema 200 h1 string\n" + " * @responseHeaderExplode 200 h1 true\n"
				+ " * @responseHeaderStyle 200 h1 simple\n" + " * @responseHeaderDeprecated 200 h1 true\n"
				+ " * @responseHeaderRequired 200 h1 true\n" + " * @responseHeader 200 h1 desc\n"
				+ " * @responseHeaderExamples 200 h1 ex1 sum|desc|val\n" + " * @pathParameter /path p1\n"
				+ " * @pathServer /path s1\n" + " * @pathDescription /path desc\n" + " * @pathSummary /path sum\n"
				+ " * @componentMediaType mt1\n" + " * @componentPathItem pi1\n" + " * @componentCallback cb1\n"
				+ " * @componentLinkRequestBody l1 rb\n" + " * @componentLinkParam l1 p1 v1\n"
				+ " * @componentLinkServer l1 srv\n" + " * @componentLinkDesc l1 desc\n"
				+ " * @componentLinkOpRef l1 opRef\n" + " * @componentLinkOpId l1 opId\n" + " * @componentLink l1\n"
				+ " * @componentHeader h1\n" + " * @componentRequestBody rb1\n" + " * @componentParameter p1\n"
				+ " * @componentResponse r1\n" + " * @securitySchemeFlowScope implicit sec1 scope1 desc1\n"
				+ " * @securitySchemeFlow implicit sec1 http http scope1=a\n"
				+ " * @securityScheme sec1 type=http scheme=basic\n" + " * @serverVariableEnum srv enum v1,v2\n"
				+ " * @serverVariable srv port 8080 desc\n" + " * @serverName srv\n" + " * @server srv url desc\n"
				+ " * @globalTag tag1\n" + " * @licenseUrl lurl\n" + " * @licenseIdentifier lident\n"
				+ " * @licenseName lname\n" + " * @contactUrl url\n" + " * @contactEmail email\n"
				+ " * @contactName name\n" + " * @termsOfService tos\n" + " * @description desc\n" + " * @summary sum\n"
				+ " * @version 1.0.0\n" + " * @title title\n" + " * @jsonSchemaDialect dialect\n"
				+ " * @openapiSelf self\n" + " * @openapiVersion 3.2.0\n" + " */\n" + "class GeneratedClassTags {}\n";
		openapi.OpenAPI apiRev = routes.Parse.parse(sourceReversed);
		assertNotNull(apiRev);
		assertEquals("3.2.0", apiRev.openapi);
		assertEquals("title", apiRev.info.title);
	}

	/**
	 * Verifies {@link routes.Emit#emit} across callbacks, webhooks, examples, and
	 * existing class bodies.
	 */
	@Test
	public void testRoutesEmitCoverage() {
		openapi.OpenAPI api = new openapi.OpenAPI();
		api.info = new openapi.Info();
		api.info.title = "ServerRoutes";

		api.paths = new openapi.Paths();
		api.paths.pathItems = new java.util.HashMap<>();
		openapi.PathItem pi = new openapi.PathItem();
		api.paths.pathItems.put("/coverage", pi);

		pi.post = new openapi.Operation();
		pi.post.callbacks = new java.util.HashMap<>();
		openapi.Callback cb = new openapi.Callback();
		openapi.PathItem cbPi = new openapi.PathItem();
		cbPi.post = new openapi.Operation();
		cbPi.post.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
		cbPi.parameters = new java.util.ArrayList<>();
		openapi.Parameter cbP = new openapi.Parameter();
		cbP.name = "cb_param";
		cbPi.parameters.add(openapi.ReferenceOr.ofValue(cbP));
		cb.pathItems = new java.util.HashMap<>();
		cb.pathItems.put("cb1", cbPi);
		pi.post.callbacks.put("cb1", openapi.ReferenceOr.ofValue(cb));

		pi.put = new openapi.Operation();
		pi.put.callbacks = new java.util.HashMap<>();
		pi.put.callbacks.put("cb2", openapi.ReferenceOr.ofValue(cb));

		pi.delete = new openapi.Operation();
		pi.delete.callbacks = new java.util.HashMap<>();
		pi.delete.callbacks.put("cb3", openapi.ReferenceOr.ofValue(cb));

		pi.patch = new openapi.Operation();
		pi.patch.callbacks = new java.util.HashMap<>();
		pi.patch.callbacks.put("cb4", openapi.ReferenceOr.ofValue(cb));

		pi.query = new openapi.Operation();
		pi.query.callbacks = new java.util.HashMap<>();
		pi.query.callbacks.put("cb5", openapi.ReferenceOr.ofValue(cb));

		api.webhooks = new java.util.HashMap<>();
		openapi.PathItem wh = new openapi.PathItem();
		wh.post = new openapi.Operation();
		wh.post.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
		wh.parameters = new java.util.ArrayList<>();
		openapi.Parameter whP = new openapi.Parameter();
		whP.name = "wh_param";
		wh.parameters.add(openapi.ReferenceOr.ofValue(whP));
		api.webhooks.put("wh1", wh);

		pi.get = new openapi.Operation();
		pi.get.operationId = "existingOp";
		pi.get.parameters = new java.util.ArrayList<>();
		openapi.Parameter getP1 = new openapi.Parameter();
		getP1.in = "query";
		getP1.name = "q1";
		openapi.Example ex1 = new openapi.Example();
		ex1.value = common.PayloadValue.ofString("val");
		ex1.summary = "sum";
		ex1.description = "desc";
		getP1.examples = new java.util.HashMap<>();
		getP1.examples.put("ex1", openapi.ReferenceOr.ofValue(ex1));

		openapi.Example ex2 = new openapi.Example();
		ex2.value = null;
		ex2.summary = null;
		ex2.description = null;
		getP1.examples.put("ex2", openapi.ReferenceOr.ofValue(ex2));
		pi.get.parameters.add(openapi.ReferenceOr.ofValue(getP1));

		pi.parameters = new java.util.ArrayList<>();
		openapi.Parameter pathParam = new openapi.Parameter();
		pathParam.name = "q1";
		pathParam.in = "query";
		pi.parameters.add(openapi.ReferenceOr.ofValue(pathParam));

		pi.patch.responses = new openapi.Responses();
		pi.patch.responses.statusCodes = new java.util.HashMap<>();
		openapi.Response getRes = new openapi.Response();
		getRes.content = new java.util.HashMap<>();
		openapi.MediaType mt = new openapi.MediaType();
		mt.examples = new java.util.HashMap<>();
		mt.examples.put("mtEx1", openapi.ReferenceOr.ofValue(ex1));
		mt.examples.put("mtEx2", openapi.ReferenceOr.ofValue(ex2));
		getRes.content.put("application/json", mt);
		pi.patch.responses.statusCodes.put("200", openapi.ReferenceOr.ofValue(getRes));

		pi.patch.parameters = new java.util.ArrayList<>();
		pi.patch.parameters.add(openapi.ReferenceOr.ofValue(getP1));
		pi.patch.security = new java.util.ArrayList<>();
		openapi.SecurityRequirement secReq = new openapi.SecurityRequirement();
		secReq.addRequirement("basicAuth", new java.util.ArrayList<>());
		pi.patch.security.add(secReq);

		api.components = new openapi.Components();
		api.components.securitySchemes = new java.util.HashMap<>();
		openapi.SecurityScheme ss = new openapi.SecurityScheme();
		ss.type = "http";
		ss.scheme = "basic";
		api.components.securitySchemes.put("basicAuth", openapi.ReferenceOr.ofValue(ss));

		openapi.Parameter authParam = new openapi.Parameter();
		authParam.name = "Authorization";
		authParam.in = "header";
		pi.patch.parameters.add(openapi.ReferenceOr.ofValue(authParam));

		String existing = "class ServerRoutesClient {\n" + "    public void existingOp(String q1) {\n"
				+ "        // Preserve this\n" + "    }\n" + "}\n";
		String emitted = routes.Emit.emit(api, existing);
		assertNotNull(emitted);
		assertTrue(emitted.contains("// Preserve this"));
		assertTrue(emitted.contains("wh1WebhookHandler"));
	}

	/**
	 * Verifies individual class and method Javadoc tags when surrounding context is
	 * sparse.
	 */
	@Test
	public void testIndividualTagsForNullBranches() {
		String[] classTags = {"openapiVersion", "openapiSelf", "jsonSchemaDialect", "title", "version", "summary",
				"description", "termsOfService", "contactName", "contactEmail", "contactUrl", "licenseName",
				"licenseIdentifier", "licenseUrl", "globalTag", "server", "serverName", "serverVariable",
				"serverVariableEnum", "securityScheme", "securitySchemeFlow", "securitySchemeFlowScope",
				"componentResponse", "componentParameter", "componentRequestBody", "componentHeader", "componentLink",
				"componentLinkOpId", "componentLinkOpRef", "componentLinkDesc", "componentLinkServer",
				"componentLinkParam", "componentLinkRequestBody", "componentCallback", "componentPathItem",
				"componentMediaType", "pathSummary", "pathDescription", "pathServer", "pathParameter",
				"responseHeaderExamples", "responseHeader", "responseHeaderRequired", "responseHeaderDeprecated",
				"responseHeaderStyle", "responseHeaderExplode", "responseHeaderSchema", "responseHeaderExample",
				"responseHeaderContent", "responseContentExamples", "responseContent", "responseContentItemSchema",
				"responseContentPrefixEncoding", "responseContentItemEncoding", "responseEncoding",
				"responseEncodingPrefixEncoding", "responseEncodingItemEncoding", "responseContentSchema",
				"responseContentExample", "responseLinkParam", "responseLink", "responseLinkOpId", "responseLinkOpRef",
				"responseLinkDesc", "responseLinkServer", "responseLinkRequestBody"};

		String[] payloads = {"val1", "val1 val2", "val1 val2 val3", "val1 val2 val3 val4 val5 val6"};
		for (String tag : classTags) {
			for (String baseContent : payloads) {
				String content = baseContent;
				if (tag.equals("serverVariable") && baseContent.length() > 15)
					content = "srv port 8080 desc";
				if (tag.equals("serverVariableEnum") && baseContent.length() > 15)
					content = "srv enum v1,v2";
				if (tag.equals("securityScheme") && baseContent.length() > 15)
					content = "sec1 type=http scheme=basic";
				if (tag.equals("securitySchemeFlow") && baseContent.length() > 15)
					content = "implicit sec1 http http scope1=a";

				String source = "package test;\n/**\n * @" + tag + " " + content + "\n */\nclass Test {}";
				assertNotNull(routes.Parse.parse(source));
			}
		}

		String[] methodTags = {"callback", "tag", "externalDocs", "deprecated", "operationServer", "operationSecurity",
				"param", "requiredParam", "deprecatedParam", "paramAllowEmptyValue", "paramStyle", "paramExplode",
				"paramAllowReserved", "paramSchema", "paramContent", "paramExample", "paramExamples", "requestBody",
				"requestBodyRequired", "requestBodyContent", "requestBodyContentSchema", "requestBodyContentExample",
				"requestBodyContentItemSchema", "requestBodyContentPrefixEncoding", "requestBodyContentItemEncoding",
				"requestBodyEncoding", "requestBodyEncodingPrefixEncoding", "requestBodyEncodingItemEncoding",
				"requestBodyContentExamples", "requestBodyContentUnknown", "responseDefault", "response",
				"responseHeaderExamples", "responseHeader", "responseHeaderRequired", "responseHeaderDeprecated",
				"responseHeaderStyle", "responseHeaderExplode", "responseHeaderSchema", "responseHeaderExample",
				"responseHeaderContent", "responseHeaderUnknown", "responseContentExamples", "responseContent",
				"responseContentItemSchema", "responseContentPrefixEncoding", "responseContentItemEncoding",
				"responseEncoding", "responseEncodingPrefixEncoding", "responseEncodingItemEncoding",
				"responseContentSchema", "responseContentExample", "responseLinkParam", "responseLink",
				"responseLinkOpId", "responseLinkOpRef", "responseLinkDesc", "responseLinkServer",
				"responseLinkRequestBody", "responseLinkUnknown"};

		for (String tag : methodTags) {
			for (String content : payloads) {
				String source = "package test;\nimport java.net.http.HttpResponse;\nclass Test {\n" + "    /**\n * @"
						+ tag + " " + content + "\n */\n"
						+ "    public HttpResponse<String> testMethod(String Authorization) { new StringBuilder(baseUrl + \"/test\"); HttpRequest.newBuilder().uri(java.net.URI.create(baseUrl + \"/test\")).GET(); return null; }\n"
						+ "}";
				OpenAPI parsedMethod = routes.Parse.parse(source);
				assertNotNull(parsedMethod);
				assertTrue(parsedMethod.paths.pathItems.containsKey("/test"));
			}
		}
	}

	/**
	 * Verifies round-trip emission and parsing across all 9 HTTP verbs (GET, POST,
	 * PUT, DELETE, PATCH, OPTIONS, HEAD, TRACE, QUERY).
	 */
	@Test
	public void testAllHttpVerbsRoundTrip() {
		openapi.OpenAPI api = new openapi.OpenAPI();
		api.openapi = "3.2.0";
		api.info = new openapi.Info();
		api.info.title = "VerbTest";
		api.info.version = "1.0.0";
		api.paths = new openapi.Paths();
		api.paths.pathItems = new java.util.HashMap<>();

		openapi.PathItem pi = new openapi.PathItem();
		pi.get = new openapi.Operation();
		pi.get.operationId = "doGet";
		pi.post = new openapi.Operation();
		pi.post.operationId = "doPost";
		pi.post.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
		pi.put = new openapi.Operation();
		pi.put.operationId = "doPut";
		pi.put.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
		pi.delete = new openapi.Operation();
		pi.delete.operationId = "doDelete";
		pi.patch = new openapi.Operation();
		pi.patch.operationId = "doPatch";
		pi.patch.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
		pi.options = new openapi.Operation();
		pi.options.operationId = "doOptions";
		pi.head = new openapi.Operation();
		pi.head.operationId = "doHead";
		pi.trace = new openapi.Operation();
		pi.trace.operationId = "doTrace";
		pi.query = new openapi.Operation();
		pi.query.operationId = "doQuery";
		pi.query.requestBody = openapi.ReferenceOr.ofValue(new openapi.RequestBody());
		api.paths.pathItems.put("/verbs", pi);

		String emitted = routes.Emit.emit(api, null);
		assertTrue(emitted.contains("doOptions"));
		assertTrue(emitted.contains("doHead"));
		assertTrue(emitted.contains("doTrace"));
		assertTrue(emitted.contains("doQuery"));

		openapi.OpenAPI parsed = routes.Parse.parse(emitted);
		openapi.PathItem parsedPi = parsed.paths.pathItems.get("/verbs");
		assertNotNull(parsedPi);
		assertNotNull(new routes.Emit());
		assertNotNull(new routes.Parse());
		routes.Parse.parseCallbackInterface(new com.github.javaparser.ast.body.ClassOrInterfaceDeclaration(), null);
		String cbInterfaceSource = "/**\n" + " * @server https://s1.example.com S1\n"
				+ " * @server https://s2.example.com S2\n" + " * @securitySchemeFlow unknown s1 u1 u2\n"
				+ " * @pathServer /p https://ps1.example.com PS1\n" + " * @pathServer /p https://ps2.example.com PS2\n"
				+ " * @pathParameter /p p1 query P1\n" + " * @pathParameter /p p2 query P2\n" + " */\n"
				+ "public class T {\n" + "  public interface MyCbCallbackHandler {\n" + "    void onGet();\n"
				+ "    void onPost();\n" + "    void onPut();\n" + "    void onDelete();\n" + "    void onPatch();\n"
				+ "    void onQuery();\n" + "    void onUnknown();\n" + "    void helper();\n" + "  }\n"
				+ "  public interface MyHookWebhookHandler {\n"
				+ "    void onPost(String body, String requestBody, String extra);\n" + "    void onGet();\n" + "  }\n"
				+ "  /**\n" + "   * Summary line\n" + "   * Description line\n" + "   * @tag t1\n" + "   * @tag t2\n"
				+ "   * @param q1 q1\n" + "   * @param q2 Detailed description\n" + "   * @param\n"
				+ "   * @requestBodyContentSchema application/json object\n"
				+ "   * @requestBodyContent application/json\n"
				+ "   * @requestBodyContentPrefixEncoding application/json text/plain\n"
				+ "   * @requestBodyContentPrefixEncoding application/json application/octet-stream\n"
				+ "   * @requestBodyEncodingPrefixEncoding application/json field1 text/plain\n"
				+ "   * @requestBodyEncodingPrefixEncoding application/json field1 application/octet-stream\n"
				+ "   */\n" + "  public HttpResponse<String> m1(String q1, String q2, String authorization) {\n"
				+ "    Object unused = new Object();\n"
				+ "    StringBuilder sb = new StringBuilder(this.baseUrl + \"/p?foo=bar\");\n"
				+ "    StringBuilder sbPlain = new StringBuilder(\"plain\");\n"
				+ "    HttpRequest.newBuilder().GET();\n" + "    return null;\n" + "  }\n"
				+ "  /** Single line summary. */\n" + "  public HttpResponse<String> m2(String authorization) {\n"
				+ "    HttpRequest.newBuilder().uri(URI.create(this.baseUrl + \"/p2\")).GET();\n" + "    return null;\n"
				+ "  }\n" + "  public HttpResponse<String> mNoPath() {\n" + "    HttpRequest.newBuilder().GET();\n"
				+ "    return null;\n" + "  }\n" + "  public HttpResponse<String> mNoVerb() {\n" + "    return null;\n"
				+ "  }\n" + "}\n";
		assertNotNull(routes.Parse.parse(cbInterfaceSource));

		assertNotNull(parsedPi.get);
		assertNotNull(parsedPi.post);
		assertNotNull(parsedPi.put);
		assertNotNull(parsedPi.delete);
		assertNotNull(parsedPi.patch);
		assertNotNull(parsedPi.options);
		assertNotNull(parsedPi.head);
		assertNotNull(parsedPi.trace);
		assertNotNull(parsedPi.query);
	}
}
