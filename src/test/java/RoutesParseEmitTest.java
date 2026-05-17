package routes;

import org.junit.Test;
import static org.junit.Assert.*;
import openapi.OpenAPI;
import java.util.*;

public class RoutesParseEmitTest {

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
		cbPi.post.parameters.add(cbParam);
		cbGet.pathItems.put("cb_expr", cbPi);

		Map<String, Object> cbs = new HashMap<>();
		cbs.put("cb_name", cbGet);

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
		manualApi.components.securitySchemes.put("secBasic", sch1);
		openapi.SecurityScheme sch2 = new openapi.SecurityScheme();
		sch2.type = "apiKey";
		sch2.in = "header";
		sch2.name = "X-API-Key";
		manualApi.components.securitySchemes.put("secApi", sch2);
		openapi.SecurityScheme sch3 = new openapi.SecurityScheme();
		sch3.type = "http";
		sch3.scheme = "bearer";
		manualApi.components.securitySchemes.put("secBearer", sch3);

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
		pi.parameters.add(pathParam);

		pi.get = new openapi.Operation();
		pi.get.operationId = "myOpId";
		pi.get.summary = "";
		pi.get.description = "";
		pi.get.parameters = new ArrayList<>();
		openapi.Parameter p = new openapi.Parameter();
		p.name = "X-API-Key"; // Duplicate param check in Emit
		p.in = "header";
		pi.get.parameters.add(p);

		openapi.Parameter pEmptyName = new openapi.Parameter();
		pEmptyName.name = "!"; // safeName becomes empty
		pi.get.parameters.add(pEmptyName);

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

		// Test Emit empty OpenAPI
		OpenAPI emptyApi = new OpenAPI();
		Emit.emit(emptyApi, null);

		// Also test the Exception catch in Parse

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

				     * @componentLinkOpId
				     * @componentLinkOpId - - - - - - -
				     * @componentLinkOpId arg1
				     * @componentLinkOpId arg1 arg2
				     * @componentLinkOpId arg1 arg2 arg3
				     * @componentLinkOpId arg1 arg2 arg3 arg4
				     * @componentLinkOpId arg1 arg2 arg3 arg4 arg5
				     * @componentLinkOpId arg1 arg2|arg3|arg4
				     * @componentLinkOpId default application/json
				     * @componentLinkOpId 200 application/json
				     * @componentLinkOpId type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLinkOpId type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLinkOpId implicit sec1 - -
				     * @componentLinkOpId password sec1 - -
				     * @componentLinkOpId clientCredentials sec1 - -
				     * @componentLinkOpId authorizationCode sec1 - - -
				     * @componentLinkOpId deviceAuthorization sec1 - - -
				     * @licenseName
				     * @licenseName - - - - - - -
				     * @licenseName arg1
				     * @licenseName arg1 arg2
				     * @licenseName arg1 arg2 arg3
				     * @licenseName arg1 arg2 arg3 arg4
				     * @licenseName arg1 arg2 arg3 arg4 arg5
				     * @licenseName arg1 arg2|arg3|arg4
				     * @licenseName default application/json
				     * @licenseName 200 application/json
				     * @licenseName type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @licenseName type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @licenseName implicit sec1 - -
				     * @licenseName password sec1 - -
				     * @licenseName clientCredentials sec1 - -
				     * @licenseName authorizationCode sec1 - - -
				     * @licenseName deviceAuthorization sec1 - - -
				     * @responseHeaderExplode
				     * @responseHeaderExplode - - - - - - -
				     * @responseHeaderExplode arg1
				     * @responseHeaderExplode arg1 arg2
				     * @responseHeaderExplode arg1 arg2 arg3
				     * @responseHeaderExplode arg1 arg2 arg3 arg4
				     * @responseHeaderExplode arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderExplode arg1 arg2|arg3|arg4
				     * @responseHeaderExplode default application/json
				     * @responseHeaderExplode 200 application/json
				     * @responseHeaderExplode type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderExplode type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderExplode implicit sec1 - -
				     * @responseHeaderExplode password sec1 - -
				     * @responseHeaderExplode clientCredentials sec1 - -
				     * @responseHeaderExplode authorizationCode sec1 - - -
				     * @responseHeaderExplode deviceAuthorization sec1 - - -
				     * @responseContentPrefixEncoding
				     * @responseContentPrefixEncoding - - - - - - -
				     * @responseContentPrefixEncoding arg1
				     * @responseContentPrefixEncoding arg1 arg2
				     * @responseContentPrefixEncoding arg1 arg2 arg3
				     * @responseContentPrefixEncoding arg1 arg2 arg3 arg4
				     * @responseContentPrefixEncoding arg1 arg2 arg3 arg4 arg5
				     * @responseContentPrefixEncoding arg1 arg2|arg3|arg4
				     * @responseContentPrefixEncoding default application/json
				     * @responseContentPrefixEncoding 200 application/json
				     * @responseContentPrefixEncoding type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContentPrefixEncoding type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContentPrefixEncoding implicit sec1 - -
				     * @responseContentPrefixEncoding password sec1 - -
				     * @responseContentPrefixEncoding clientCredentials sec1 - -
				     * @responseContentPrefixEncoding authorizationCode sec1 - - -
				     * @responseContentPrefixEncoding deviceAuthorization sec1 - - -
				     * @responseContentItemEncoding
				     * @responseContentItemEncoding - - - - - - -
				     * @responseContentItemEncoding arg1
				     * @responseContentItemEncoding arg1 arg2
				     * @responseContentItemEncoding arg1 arg2 arg3
				     * @responseContentItemEncoding arg1 arg2 arg3 arg4
				     * @responseContentItemEncoding arg1 arg2 arg3 arg4 arg5
				     * @responseContentItemEncoding arg1 arg2|arg3|arg4
				     * @responseContentItemEncoding default application/json
				     * @responseContentItemEncoding 200 application/json
				     * @responseContentItemEncoding type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContentItemEncoding type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContentItemEncoding implicit sec1 - -
				     * @responseContentItemEncoding password sec1 - -
				     * @responseContentItemEncoding clientCredentials sec1 - -
				     * @responseContentItemEncoding authorizationCode sec1 - - -
				     * @responseContentItemEncoding deviceAuthorization sec1 - - -
				     * @responseHeaderExamples
				     * @responseHeaderExamples - - - - - - -
				     * @responseHeaderExamples arg1
				     * @responseHeaderExamples arg1 arg2
				     * @responseHeaderExamples arg1 arg2 arg3
				     * @responseHeaderExamples arg1 arg2 arg3 arg4
				     * @responseHeaderExamples arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderExamples arg1 arg2|arg3|arg4
				     * @responseHeaderExamples default application/json
				     * @responseHeaderExamples 200 application/json
				     * @responseHeaderExamples type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderExamples type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderExamples implicit sec1 - -
				     * @responseHeaderExamples password sec1 - -
				     * @responseHeaderExamples clientCredentials sec1 - -
				     * @responseHeaderExamples authorizationCode sec1 - - -
				     * @responseHeaderExamples deviceAuthorization sec1 - - -
				     * @componentLinkParam
				     * @componentLinkParam - - - - - - -
				     * @componentLinkParam arg1
				     * @componentLinkParam arg1 arg2
				     * @componentLinkParam arg1 arg2 arg3
				     * @componentLinkParam arg1 arg2 arg3 arg4
				     * @componentLinkParam arg1 arg2 arg3 arg4 arg5
				     * @componentLinkParam arg1 arg2|arg3|arg4
				     * @componentLinkParam default application/json
				     * @componentLinkParam 200 application/json
				     * @componentLinkParam type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLinkParam type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLinkParam implicit sec1 - -
				     * @componentLinkParam password sec1 - -
				     * @componentLinkParam clientCredentials sec1 - -
				     * @componentLinkParam authorizationCode sec1 - - -
				     * @componentLinkParam deviceAuthorization sec1 - - -
				     * @licenseIdentifier
				     * @licenseIdentifier - - - - - - -
				     * @licenseIdentifier arg1
				     * @licenseIdentifier arg1 arg2
				     * @licenseIdentifier arg1 arg2 arg3
				     * @licenseIdentifier arg1 arg2 arg3 arg4
				     * @licenseIdentifier arg1 arg2 arg3 arg4 arg5
				     * @licenseIdentifier arg1 arg2|arg3|arg4
				     * @licenseIdentifier default application/json
				     * @licenseIdentifier 200 application/json
				     * @licenseIdentifier type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @licenseIdentifier type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @licenseIdentifier implicit sec1 - -
				     * @licenseIdentifier password sec1 - -
				     * @licenseIdentifier clientCredentials sec1 - -
				     * @licenseIdentifier authorizationCode sec1 - - -
				     * @licenseIdentifier deviceAuthorization sec1 - - -
				     * @version
				     * @version - - - - - - -
				     * @version arg1
				     * @version arg1 arg2
				     * @version arg1 arg2 arg3
				     * @version arg1 arg2 arg3 arg4
				     * @version arg1 arg2 arg3 arg4 arg5
				     * @version arg1 arg2|arg3|arg4
				     * @version default application/json
				     * @version 200 application/json
				     * @version type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @version type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @version implicit sec1 - -
				     * @version password sec1 - -
				     * @version clientCredentials sec1 - -
				     * @version authorizationCode sec1 - - -
				     * @version deviceAuthorization sec1 - - -
				     * @responseContent
				     * @responseContent - - - - - - -
				     * @responseContent arg1
				     * @responseContent arg1 arg2
				     * @responseContent arg1 arg2 arg3
				     * @responseContent arg1 arg2 arg3 arg4
				     * @responseContent arg1 arg2 arg3 arg4 arg5
				     * @responseContent arg1 arg2|arg3|arg4
				     * @responseContent default application/json
				     * @responseContent 200 application/json
				     * @responseContent type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContent type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContent implicit sec1 - -
				     * @responseContent password sec1 - -
				     * @responseContent clientCredentials sec1 - -
				     * @responseContent authorizationCode sec1 - - -
				     * @responseContent deviceAuthorization sec1 - - -
				     * @responseHeaderSchema
				     * @responseHeaderSchema - - - - - - -
				     * @responseHeaderSchema arg1
				     * @responseHeaderSchema arg1 arg2
				     * @responseHeaderSchema arg1 arg2 arg3
				     * @responseHeaderSchema arg1 arg2 arg3 arg4
				     * @responseHeaderSchema arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderSchema arg1 arg2|arg3|arg4
				     * @responseHeaderSchema default application/json
				     * @responseHeaderSchema 200 application/json
				     * @responseHeaderSchema type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderSchema type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderSchema implicit sec1 - -
				     * @responseHeaderSchema password sec1 - -
				     * @responseHeaderSchema clientCredentials sec1 - -
				     * @responseHeaderSchema authorizationCode sec1 - - -
				     * @responseHeaderSchema deviceAuthorization sec1 - - -
				     * @responseEncodingPrefixEncoding
				     * @responseEncodingPrefixEncoding - - - - - - -
				     * @responseEncodingPrefixEncoding arg1
				     * @responseEncodingPrefixEncoding arg1 arg2
				     * @responseEncodingPrefixEncoding arg1 arg2 arg3
				     * @responseEncodingPrefixEncoding arg1 arg2 arg3 arg4
				     * @responseEncodingPrefixEncoding arg1 arg2 arg3 arg4 arg5
				     * @responseEncodingPrefixEncoding arg1 arg2|arg3|arg4
				     * @responseEncodingPrefixEncoding default application/json
				     * @responseEncodingPrefixEncoding 200 application/json
				     * @responseEncodingPrefixEncoding type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseEncodingPrefixEncoding type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseEncodingPrefixEncoding implicit sec1 - -
				     * @responseEncodingPrefixEncoding password sec1 - -
				     * @responseEncodingPrefixEncoding clientCredentials sec1 - -
				     * @responseEncodingPrefixEncoding authorizationCode sec1 - - -
				     * @responseEncodingPrefixEncoding deviceAuthorization sec1 - - -
				     * @componentLink
				     * @componentLink - - - - - - -
				     * @componentLink arg1
				     * @componentLink arg1 arg2
				     * @componentLink arg1 arg2 arg3
				     * @componentLink arg1 arg2 arg3 arg4
				     * @componentLink arg1 arg2 arg3 arg4 arg5
				     * @componentLink arg1 arg2|arg3|arg4
				     * @componentLink default application/json
				     * @componentLink 200 application/json
				     * @componentLink type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLink type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLink implicit sec1 - -
				     * @componentLink password sec1 - -
				     * @componentLink clientCredentials sec1 - -
				     * @componentLink authorizationCode sec1 - - -
				     * @componentLink deviceAuthorization sec1 - - -
				     * @responseLinkParam
				     * @responseLinkParam - - - - - - -
				     * @responseLinkParam arg1
				     * @responseLinkParam arg1 arg2
				     * @responseLinkParam arg1 arg2 arg3
				     * @responseLinkParam arg1 arg2 arg3 arg4
				     * @responseLinkParam arg1 arg2 arg3 arg4 arg5
				     * @responseLinkParam arg1 arg2|arg3|arg4
				     * @responseLinkParam default application/json
				     * @responseLinkParam 200 application/json
				     * @responseLinkParam type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLinkParam type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLinkParam implicit sec1 - -
				     * @responseLinkParam password sec1 - -
				     * @responseLinkParam clientCredentials sec1 - -
				     * @responseLinkParam authorizationCode sec1 - - -
				     * @responseLinkParam deviceAuthorization sec1 - - -
				     * @responseContentItemSchema
				     * @responseContentItemSchema - - - - - - -
				     * @responseContentItemSchema arg1
				     * @responseContentItemSchema arg1 arg2
				     * @responseContentItemSchema arg1 arg2 arg3
				     * @responseContentItemSchema arg1 arg2 arg3 arg4
				     * @responseContentItemSchema arg1 arg2 arg3 arg4 arg5
				     * @responseContentItemSchema arg1 arg2|arg3|arg4
				     * @responseContentItemSchema default application/json
				     * @responseContentItemSchema 200 application/json
				     * @responseContentItemSchema type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContentItemSchema type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContentItemSchema implicit sec1 - -
				     * @responseContentItemSchema password sec1 - -
				     * @responseContentItemSchema clientCredentials sec1 - -
				     * @responseContentItemSchema authorizationCode sec1 - - -
				     * @responseContentItemSchema deviceAuthorization sec1 - - -
				     * @responseContentExamples
				     * @responseContentExamples - - - - - - -
				     * @responseContentExamples arg1
				     * @responseContentExamples arg1 arg2
				     * @responseContentExamples arg1 arg2 arg3
				     * @responseContentExamples arg1 arg2 arg3 arg4
				     * @responseContentExamples arg1 arg2 arg3 arg4 arg5
				     * @responseContentExamples arg1 arg2|arg3|arg4
				     * @responseContentExamples default application/json
				     * @responseContentExamples 200 application/json
				     * @responseContentExamples type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContentExamples type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContentExamples implicit sec1 - -
				     * @responseContentExamples password sec1 - -
				     * @responseContentExamples clientCredentials sec1 - -
				     * @responseContentExamples authorizationCode sec1 - - -
				     * @responseContentExamples deviceAuthorization sec1 - - -
				     * @jsonSchemaDialect
				     * @jsonSchemaDialect - - - - - - -
				     * @jsonSchemaDialect arg1
				     * @jsonSchemaDialect arg1 arg2
				     * @jsonSchemaDialect arg1 arg2 arg3
				     * @jsonSchemaDialect arg1 arg2 arg3 arg4
				     * @jsonSchemaDialect arg1 arg2 arg3 arg4 arg5
				     * @jsonSchemaDialect arg1 arg2|arg3|arg4
				     * @jsonSchemaDialect default application/json
				     * @jsonSchemaDialect 200 application/json
				     * @jsonSchemaDialect type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @jsonSchemaDialect type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @jsonSchemaDialect implicit sec1 - -
				     * @jsonSchemaDialect password sec1 - -
				     * @jsonSchemaDialect clientCredentials sec1 - -
				     * @jsonSchemaDialect authorizationCode sec1 - - -
				     * @jsonSchemaDialect deviceAuthorization sec1 - - -
				     * @responseContentExample
				     * @responseContentExample - - - - - - -
				     * @responseContentExample arg1
				     * @responseContentExample arg1 arg2
				     * @responseContentExample arg1 arg2 arg3
				     * @responseContentExample arg1 arg2 arg3 arg4
				     * @responseContentExample arg1 arg2 arg3 arg4 arg5
				     * @responseContentExample arg1 arg2|arg3|arg4
				     * @responseContentExample default application/json
				     * @responseContentExample 200 application/json
				     * @responseContentExample type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContentExample type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContentExample implicit sec1 - -
				     * @responseContentExample password sec1 - -
				     * @responseContentExample clientCredentials sec1 - -
				     * @responseContentExample authorizationCode sec1 - - -
				     * @responseContentExample deviceAuthorization sec1 - - -
				     * @responseLink
				     * @responseLink - - - - - - -
				     * @responseLink arg1
				     * @responseLink arg1 arg2
				     * @responseLink arg1 arg2 arg3
				     * @responseLink arg1 arg2 arg3 arg4
				     * @responseLink arg1 arg2 arg3 arg4 arg5
				     * @responseLink arg1 arg2|arg3|arg4
				     * @responseLink default application/json
				     * @responseLink 200 application/json
				     * @responseLink type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLink type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLink implicit sec1 - -
				     * @responseLink password sec1 - -
				     * @responseLink clientCredentials sec1 - -
				     * @responseLink authorizationCode sec1 - - -
				     * @responseLink deviceAuthorization sec1 - - -
				     * @openapiVersion
				     * @openapiVersion - - - - - - -
				     * @openapiVersion arg1
				     * @openapiVersion arg1 arg2
				     * @openapiVersion arg1 arg2 arg3
				     * @openapiVersion arg1 arg2 arg3 arg4
				     * @openapiVersion arg1 arg2 arg3 arg4 arg5
				     * @openapiVersion arg1 arg2|arg3|arg4
				     * @openapiVersion default application/json
				     * @openapiVersion 200 application/json
				     * @openapiVersion type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @openapiVersion type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @openapiVersion implicit sec1 - -
				     * @openapiVersion password sec1 - -
				     * @openapiVersion clientCredentials sec1 - -
				     * @openapiVersion authorizationCode sec1 - - -
				     * @openapiVersion deviceAuthorization sec1 - - -
				     * @responseLinkDesc
				     * @responseLinkDesc - - - - - - -
				     * @responseLinkDesc arg1
				     * @responseLinkDesc arg1 arg2
				     * @responseLinkDesc arg1 arg2 arg3
				     * @responseLinkDesc arg1 arg2 arg3 arg4
				     * @responseLinkDesc arg1 arg2 arg3 arg4 arg5
				     * @responseLinkDesc arg1 arg2|arg3|arg4
				     * @responseLinkDesc default application/json
				     * @responseLinkDesc 200 application/json
				     * @responseLinkDesc type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLinkDesc type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLinkDesc implicit sec1 - -
				     * @responseLinkDesc password sec1 - -
				     * @responseLinkDesc clientCredentials sec1 - -
				     * @responseLinkDesc authorizationCode sec1 - - -
				     * @responseLinkDesc deviceAuthorization sec1 - - -
				     * @responseLinkRequestBody
				     * @responseLinkRequestBody - - - - - - -
				     * @responseLinkRequestBody arg1
				     * @responseLinkRequestBody arg1 arg2
				     * @responseLinkRequestBody arg1 arg2 arg3
				     * @responseLinkRequestBody arg1 arg2 arg3 arg4
				     * @responseLinkRequestBody arg1 arg2 arg3 arg4 arg5
				     * @responseLinkRequestBody arg1 arg2|arg3|arg4
				     * @responseLinkRequestBody default application/json
				     * @responseLinkRequestBody 200 application/json
				     * @responseLinkRequestBody type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLinkRequestBody type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLinkRequestBody implicit sec1 - -
				     * @responseLinkRequestBody password sec1 - -
				     * @responseLinkRequestBody clientCredentials sec1 - -
				     * @responseLinkRequestBody authorizationCode sec1 - - -
				     * @responseLinkRequestBody deviceAuthorization sec1 - - -
				     * @globalTag
				     * @globalTag - - - - - - -
				     * @globalTag arg1
				     * @globalTag arg1 arg2
				     * @globalTag arg1 arg2 arg3
				     * @globalTag arg1 arg2 arg3 arg4
				     * @globalTag arg1 arg2 arg3 arg4 arg5
				     * @globalTag arg1 arg2|arg3|arg4
				     * @globalTag default application/json
				     * @globalTag 200 application/json
				     * @globalTag type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @globalTag type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @globalTag implicit sec1 - -
				     * @globalTag password sec1 - -
				     * @globalTag clientCredentials sec1 - -
				     * @globalTag authorizationCode sec1 - - -
				     * @globalTag deviceAuthorization sec1 - - -
				     * @responseLinkServer
				     * @responseLinkServer - - - - - - -
				     * @responseLinkServer arg1
				     * @responseLinkServer arg1 arg2
				     * @responseLinkServer arg1 arg2 arg3
				     * @responseLinkServer arg1 arg2 arg3 arg4
				     * @responseLinkServer arg1 arg2 arg3 arg4 arg5
				     * @responseLinkServer arg1 arg2|arg3|arg4
				     * @responseLinkServer default application/json
				     * @responseLinkServer 200 application/json
				     * @responseLinkServer type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLinkServer type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLinkServer implicit sec1 - -
				     * @responseLinkServer password sec1 - -
				     * @responseLinkServer clientCredentials sec1 - -
				     * @responseLinkServer authorizationCode sec1 - - -
				     * @responseLinkServer deviceAuthorization sec1 - - -
				     * @contactUrl
				     * @contactUrl - - - - - - -
				     * @contactUrl arg1
				     * @contactUrl arg1 arg2
				     * @contactUrl arg1 arg2 arg3
				     * @contactUrl arg1 arg2 arg3 arg4
				     * @contactUrl arg1 arg2 arg3 arg4 arg5
				     * @contactUrl arg1 arg2|arg3|arg4
				     * @contactUrl default application/json
				     * @contactUrl 200 application/json
				     * @contactUrl type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @contactUrl type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @contactUrl implicit sec1 - -
				     * @contactUrl password sec1 - -
				     * @contactUrl clientCredentials sec1 - -
				     * @contactUrl authorizationCode sec1 - - -
				     * @contactUrl deviceAuthorization sec1 - - -
				     * @securitySchemeFlow
				     * @securitySchemeFlow - - - - - - -
				     * @securitySchemeFlow arg1
				     * @securitySchemeFlow arg1 arg2
				     * @securitySchemeFlow arg1 arg2 arg3
				     * @securitySchemeFlow arg1 arg2 arg3 arg4
				     * @securitySchemeFlow arg1 arg2 arg3 arg4 arg5
				     * @securitySchemeFlow arg1 arg2|arg3|arg4
				     * @securitySchemeFlow default application/json
				     * @securitySchemeFlow 200 application/json
				     * @securitySchemeFlow type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @securitySchemeFlow type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @securitySchemeFlow implicit sec1 - -
				     * @securitySchemeFlow password sec1 - -
				     * @securitySchemeFlow clientCredentials sec1 - -
				     * @securitySchemeFlow authorizationCode sec1 - - -
				     * @securitySchemeFlow deviceAuthorization sec1 - - -
				     * @responseContentSchema
				     * @responseContentSchema - - - - - - -
				     * @responseContentSchema arg1
				     * @responseContentSchema arg1 arg2
				     * @responseContentSchema arg1 arg2 arg3
				     * @responseContentSchema arg1 arg2 arg3 arg4
				     * @responseContentSchema arg1 arg2 arg3 arg4 arg5
				     * @responseContentSchema arg1 arg2|arg3|arg4
				     * @responseContentSchema default application/json
				     * @responseContentSchema 200 application/json
				     * @responseContentSchema type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseContentSchema type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseContentSchema implicit sec1 - -
				     * @responseContentSchema password sec1 - -
				     * @responseContentSchema clientCredentials sec1 - -
				     * @responseContentSchema authorizationCode sec1 - - -
				     * @responseContentSchema deviceAuthorization sec1 - - -
				     * @serverName
				     * @serverName - - - - - - -
				     * @serverName arg1
				     * @serverName arg1 arg2
				     * @serverName arg1 arg2 arg3
				     * @serverName arg1 arg2 arg3 arg4
				     * @serverName arg1 arg2 arg3 arg4 arg5
				     * @serverName arg1 arg2|arg3|arg4
				     * @serverName default application/json
				     * @serverName 200 application/json
				     * @serverName type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @serverName type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @serverName implicit sec1 - -
				     * @serverName password sec1 - -
				     * @serverName clientCredentials sec1 - -
				     * @serverName authorizationCode sec1 - - -
				     * @serverName deviceAuthorization sec1 - - -
				     * @pathServer
				     * @pathServer - - - - - - -
				     * @pathServer arg1
				     * @pathServer arg1 arg2
				     * @pathServer arg1 arg2 arg3
				     * @pathServer arg1 arg2 arg3 arg4
				     * @pathServer arg1 arg2 arg3 arg4 arg5
				     * @pathServer arg1 arg2|arg3|arg4
				     * @pathServer default application/json
				     * @pathServer 200 application/json
				     * @pathServer type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @pathServer type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @pathServer implicit sec1 - -
				     * @pathServer password sec1 - -
				     * @pathServer clientCredentials sec1 - -
				     * @pathServer authorizationCode sec1 - - -
				     * @pathServer deviceAuthorization sec1 - - -
				     * @summary
				     * @summary - - - - - - -
				     * @summary arg1
				     * @summary arg1 arg2
				     * @summary arg1 arg2 arg3
				     * @summary arg1 arg2 arg3 arg4
				     * @summary arg1 arg2 arg3 arg4 arg5
				     * @summary arg1 arg2|arg3|arg4
				     * @summary default application/json
				     * @summary 200 application/json
				     * @summary type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @summary type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @summary implicit sec1 - -
				     * @summary password sec1 - -
				     * @summary clientCredentials sec1 - -
				     * @summary authorizationCode sec1 - - -
				     * @summary deviceAuthorization sec1 - - -
				     * @termsOfService
				     * @termsOfService - - - - - - -
				     * @termsOfService arg1
				     * @termsOfService arg1 arg2
				     * @termsOfService arg1 arg2 arg3
				     * @termsOfService arg1 arg2 arg3 arg4
				     * @termsOfService arg1 arg2 arg3 arg4 arg5
				     * @termsOfService arg1 arg2|arg3|arg4
				     * @termsOfService default application/json
				     * @termsOfService 200 application/json
				     * @termsOfService type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @termsOfService type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @termsOfService implicit sec1 - -
				     * @termsOfService password sec1 - -
				     * @termsOfService clientCredentials sec1 - -
				     * @termsOfService authorizationCode sec1 - - -
				     * @termsOfService deviceAuthorization sec1 - - -
				     * @server
				     * @server - - - - - - -
				     * @server arg1
				     * @server arg1 arg2
				     * @server arg1 arg2 arg3
				     * @server arg1 arg2 arg3 arg4
				     * @server arg1 arg2 arg3 arg4 arg5
				     * @server arg1 arg2|arg3|arg4
				     * @server default application/json
				     * @server 200 application/json
				     * @server type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @server type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @server implicit sec1 - -
				     * @server password sec1 - -
				     * @server clientCredentials sec1 - -
				     * @server authorizationCode sec1 - - -
				     * @server deviceAuthorization sec1 - - -
				     * @componentRequestBody
				     * @componentRequestBody - - - - - - -
				     * @componentRequestBody arg1
				     * @componentRequestBody arg1 arg2
				     * @componentRequestBody arg1 arg2 arg3
				     * @componentRequestBody arg1 arg2 arg3 arg4
				     * @componentRequestBody arg1 arg2 arg3 arg4 arg5
				     * @componentRequestBody arg1 arg2|arg3|arg4
				     * @componentRequestBody default application/json
				     * @componentRequestBody 200 application/json
				     * @componentRequestBody type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentRequestBody type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentRequestBody implicit sec1 - -
				     * @componentRequestBody password sec1 - -
				     * @componentRequestBody clientCredentials sec1 - -
				     * @componentRequestBody authorizationCode sec1 - - -
				     * @componentRequestBody deviceAuthorization sec1 - - -
				     * @componentMediaType
				     * @componentMediaType - - - - - - -
				     * @componentMediaType arg1
				     * @componentMediaType arg1 arg2
				     * @componentMediaType arg1 arg2 arg3
				     * @componentMediaType arg1 arg2 arg3 arg4
				     * @componentMediaType arg1 arg2 arg3 arg4 arg5
				     * @componentMediaType arg1 arg2|arg3|arg4
				     * @componentMediaType default application/json
				     * @componentMediaType 200 application/json
				     * @componentMediaType type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentMediaType type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentMediaType implicit sec1 - -
				     * @componentMediaType password sec1 - -
				     * @componentMediaType clientCredentials sec1 - -
				     * @componentMediaType authorizationCode sec1 - - -
				     * @componentMediaType deviceAuthorization sec1 - - -
				     * @componentParameter
				     * @componentParameter - - - - - - -
				     * @componentParameter arg1
				     * @componentParameter arg1 arg2
				     * @componentParameter arg1 arg2 arg3
				     * @componentParameter arg1 arg2 arg3 arg4
				     * @componentParameter arg1 arg2 arg3 arg4 arg5
				     * @componentParameter arg1 arg2|arg3|arg4
				     * @componentParameter default application/json
				     * @componentParameter 200 application/json
				     * @componentParameter type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentParameter type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentParameter implicit sec1 - -
				     * @componentParameter password sec1 - -
				     * @componentParameter clientCredentials sec1 - -
				     * @componentParameter authorizationCode sec1 - - -
				     * @componentParameter deviceAuthorization sec1 - - -
				     * @componentCallback
				     * @componentCallback - - - - - - -
				     * @componentCallback arg1
				     * @componentCallback arg1 arg2
				     * @componentCallback arg1 arg2 arg3
				     * @componentCallback arg1 arg2 arg3 arg4
				     * @componentCallback arg1 arg2 arg3 arg4 arg5
				     * @componentCallback arg1 arg2|arg3|arg4
				     * @componentCallback default application/json
				     * @componentCallback 200 application/json
				     * @componentCallback type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentCallback type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentCallback implicit sec1 - -
				     * @componentCallback password sec1 - -
				     * @componentCallback clientCredentials sec1 - -
				     * @componentCallback authorizationCode sec1 - - -
				     * @componentCallback deviceAuthorization sec1 - - -
				     * @componentResponse
				     * @componentResponse - - - - - - -
				     * @componentResponse arg1
				     * @componentResponse arg1 arg2
				     * @componentResponse arg1 arg2 arg3
				     * @componentResponse arg1 arg2 arg3 arg4
				     * @componentResponse arg1 arg2 arg3 arg4 arg5
				     * @componentResponse arg1 arg2|arg3|arg4
				     * @componentResponse default application/json
				     * @componentResponse 200 application/json
				     * @componentResponse type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentResponse type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentResponse implicit sec1 - -
				     * @componentResponse password sec1 - -
				     * @componentResponse clientCredentials sec1 - -
				     * @componentResponse authorizationCode sec1 - - -
				     * @componentResponse deviceAuthorization sec1 - - -
				     * @pathSummary
				     * @pathSummary - - - - - - -
				     * @pathSummary arg1
				     * @pathSummary arg1 arg2
				     * @pathSummary arg1 arg2 arg3
				     * @pathSummary arg1 arg2 arg3 arg4
				     * @pathSummary arg1 arg2 arg3 arg4 arg5
				     * @pathSummary arg1 arg2|arg3|arg4
				     * @pathSummary default application/json
				     * @pathSummary 200 application/json
				     * @pathSummary type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @pathSummary type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @pathSummary implicit sec1 - -
				     * @pathSummary password sec1 - -
				     * @pathSummary clientCredentials sec1 - -
				     * @pathSummary authorizationCode sec1 - - -
				     * @pathSummary deviceAuthorization sec1 - - -
				     * @responseHeader
				     * @responseHeader - - - - - - -
				     * @responseHeader arg1
				     * @responseHeader arg1 arg2
				     * @responseHeader arg1 arg2 arg3
				     * @responseHeader arg1 arg2 arg3 arg4
				     * @responseHeader arg1 arg2 arg3 arg4 arg5
				     * @responseHeader arg1 arg2|arg3|arg4
				     * @responseHeader default application/json
				     * @responseHeader 200 application/json
				     * @responseHeader type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeader type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeader implicit sec1 - -
				     * @responseHeader password sec1 - -
				     * @responseHeader clientCredentials sec1 - -
				     * @responseHeader authorizationCode sec1 - - -
				     * @responseHeader deviceAuthorization sec1 - - -
				     * @serverVariableEnum
				     * @serverVariableEnum - - - - - - -
				     * @serverVariableEnum arg1
				     * @serverVariableEnum arg1 arg2
				     * @serverVariableEnum arg1 arg2 arg3
				     * @serverVariableEnum arg1 arg2 arg3 arg4
				     * @serverVariableEnum arg1 arg2 arg3 arg4 arg5
				     * @serverVariableEnum arg1 arg2|arg3|arg4
				     * @serverVariableEnum default application/json
				     * @serverVariableEnum 200 application/json
				     * @serverVariableEnum type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @serverVariableEnum type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @serverVariableEnum implicit sec1 - -
				     * @serverVariableEnum password sec1 - -
				     * @serverVariableEnum clientCredentials sec1 - -
				     * @serverVariableEnum authorizationCode sec1 - - -
				     * @serverVariableEnum deviceAuthorization sec1 - - -
				     * @responseHeaderDeprecated
				     * @responseHeaderDeprecated - - - - - - -
				     * @responseHeaderDeprecated arg1
				     * @responseHeaderDeprecated arg1 arg2
				     * @responseHeaderDeprecated arg1 arg2 arg3
				     * @responseHeaderDeprecated arg1 arg2 arg3 arg4
				     * @responseHeaderDeprecated arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderDeprecated arg1 arg2|arg3|arg4
				     * @responseHeaderDeprecated default application/json
				     * @responseHeaderDeprecated 200 application/json
				     * @responseHeaderDeprecated type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderDeprecated type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderDeprecated implicit sec1 - -
				     * @responseHeaderDeprecated password sec1 - -
				     * @responseHeaderDeprecated clientCredentials sec1 - -
				     * @responseHeaderDeprecated authorizationCode sec1 - - -
				     * @responseHeaderDeprecated deviceAuthorization sec1 - - -
				     * @securityScheme
				     * @securityScheme - - - - - - -
				     * @securityScheme arg1
				     * @securityScheme arg1 arg2
				     * @securityScheme arg1 arg2 arg3
				     * @securityScheme arg1 arg2 arg3 arg4
				     * @securityScheme arg1 arg2 arg3 arg4 arg5
				     * @securityScheme arg1 arg2|arg3|arg4
				     * @securityScheme default application/json
				     * @securityScheme 200 application/json
				     * @securityScheme type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @securityScheme type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @securityScheme implicit sec1 - -
				     * @securityScheme password sec1 - -
				     * @securityScheme clientCredentials sec1 - -
				     * @securityScheme authorizationCode sec1 - - -
				     * @securityScheme deviceAuthorization sec1 - - -
				     * @openapiSelf
				     * @openapiSelf - - - - - - -
				     * @openapiSelf arg1
				     * @openapiSelf arg1 arg2
				     * @openapiSelf arg1 arg2 arg3
				     * @openapiSelf arg1 arg2 arg3 arg4
				     * @openapiSelf arg1 arg2 arg3 arg4 arg5
				     * @openapiSelf arg1 arg2|arg3|arg4
				     * @openapiSelf default application/json
				     * @openapiSelf 200 application/json
				     * @openapiSelf type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @openapiSelf type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @openapiSelf implicit sec1 - -
				     * @openapiSelf password sec1 - -
				     * @openapiSelf clientCredentials sec1 - -
				     * @openapiSelf authorizationCode sec1 - - -
				     * @openapiSelf deviceAuthorization sec1 - - -
				     * @componentHeader
				     * @componentHeader - - - - - - -
				     * @componentHeader arg1
				     * @componentHeader arg1 arg2
				     * @componentHeader arg1 arg2 arg3
				     * @componentHeader arg1 arg2 arg3 arg4
				     * @componentHeader arg1 arg2 arg3 arg4 arg5
				     * @componentHeader arg1 arg2|arg3|arg4
				     * @componentHeader default application/json
				     * @componentHeader 200 application/json
				     * @componentHeader type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentHeader type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentHeader implicit sec1 - -
				     * @componentHeader password sec1 - -
				     * @componentHeader clientCredentials sec1 - -
				     * @componentHeader authorizationCode sec1 - - -
				     * @componentHeader deviceAuthorization sec1 - - -
				     * @securitySchemeFlowScope
				     * @securitySchemeFlowScope - - - - - - -
				     * @securitySchemeFlowScope arg1
				     * @securitySchemeFlowScope arg1 arg2
				     * @securitySchemeFlowScope arg1 arg2 arg3
				     * @securitySchemeFlowScope arg1 arg2 arg3 arg4
				     * @securitySchemeFlowScope arg1 arg2 arg3 arg4 arg5
				     * @securitySchemeFlowScope arg1 arg2|arg3|arg4
				     * @securitySchemeFlowScope default application/json
				     * @securitySchemeFlowScope 200 application/json
				     * @securitySchemeFlowScope type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @securitySchemeFlowScope type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @securitySchemeFlowScope implicit sec1 - -
				     * @securitySchemeFlowScope password sec1 - -
				     * @securitySchemeFlowScope clientCredentials sec1 - -
				     * @securitySchemeFlowScope authorizationCode sec1 - - -
				     * @securitySchemeFlowScope deviceAuthorization sec1 - - -
				     * @componentLinkRequestBody
				     * @componentLinkRequestBody - - - - - - -
				     * @componentLinkRequestBody arg1
				     * @componentLinkRequestBody arg1 arg2
				     * @componentLinkRequestBody arg1 arg2 arg3
				     * @componentLinkRequestBody arg1 arg2 arg3 arg4
				     * @componentLinkRequestBody arg1 arg2 arg3 arg4 arg5
				     * @componentLinkRequestBody arg1 arg2|arg3|arg4
				     * @componentLinkRequestBody default application/json
				     * @componentLinkRequestBody 200 application/json
				     * @componentLinkRequestBody type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLinkRequestBody type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLinkRequestBody implicit sec1 - -
				     * @componentLinkRequestBody password sec1 - -
				     * @componentLinkRequestBody clientCredentials sec1 - -
				     * @componentLinkRequestBody authorizationCode sec1 - - -
				     * @componentLinkRequestBody deviceAuthorization sec1 - - -
				     * @responseHeaderStyle
				     * @responseHeaderStyle - - - - - - -
				     * @responseHeaderStyle arg1
				     * @responseHeaderStyle arg1 arg2
				     * @responseHeaderStyle arg1 arg2 arg3
				     * @responseHeaderStyle arg1 arg2 arg3 arg4
				     * @responseHeaderStyle arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderStyle arg1 arg2|arg3|arg4
				     * @responseHeaderStyle default application/json
				     * @responseHeaderStyle 200 application/json
				     * @responseHeaderStyle type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderStyle type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderStyle implicit sec1 - -
				     * @responseHeaderStyle password sec1 - -
				     * @responseHeaderStyle clientCredentials sec1 - -
				     * @responseHeaderStyle authorizationCode sec1 - - -
				     * @responseHeaderStyle deviceAuthorization sec1 - - -
				     * @responseLinkOpRef
				     * @responseLinkOpRef - - - - - - -
				     * @responseLinkOpRef arg1
				     * @responseLinkOpRef arg1 arg2
				     * @responseLinkOpRef arg1 arg2 arg3
				     * @responseLinkOpRef arg1 arg2 arg3 arg4
				     * @responseLinkOpRef arg1 arg2 arg3 arg4 arg5
				     * @responseLinkOpRef arg1 arg2|arg3|arg4
				     * @responseLinkOpRef default application/json
				     * @responseLinkOpRef 200 application/json
				     * @responseLinkOpRef type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLinkOpRef type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLinkOpRef implicit sec1 - -
				     * @responseLinkOpRef password sec1 - -
				     * @responseLinkOpRef clientCredentials sec1 - -
				     * @responseLinkOpRef authorizationCode sec1 - - -
				     * @responseLinkOpRef deviceAuthorization sec1 - - -
				     * @licenseUrl
				     * @licenseUrl - - - - - - -
				     * @licenseUrl arg1
				     * @licenseUrl arg1 arg2
				     * @licenseUrl arg1 arg2 arg3
				     * @licenseUrl arg1 arg2 arg3 arg4
				     * @licenseUrl arg1 arg2 arg3 arg4 arg5
				     * @licenseUrl arg1 arg2|arg3|arg4
				     * @licenseUrl default application/json
				     * @licenseUrl 200 application/json
				     * @licenseUrl type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @licenseUrl type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @licenseUrl implicit sec1 - -
				     * @licenseUrl password sec1 - -
				     * @licenseUrl clientCredentials sec1 - -
				     * @licenseUrl authorizationCode sec1 - - -
				     * @licenseUrl deviceAuthorization sec1 - - -
				     * @contactEmail
				     * @contactEmail - - - - - - -
				     * @contactEmail arg1
				     * @contactEmail arg1 arg2
				     * @contactEmail arg1 arg2 arg3
				     * @contactEmail arg1 arg2 arg3 arg4
				     * @contactEmail arg1 arg2 arg3 arg4 arg5
				     * @contactEmail arg1 arg2|arg3|arg4
				     * @contactEmail default application/json
				     * @contactEmail 200 application/json
				     * @contactEmail type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @contactEmail type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @contactEmail implicit sec1 - -
				     * @contactEmail password sec1 - -
				     * @contactEmail clientCredentials sec1 - -
				     * @contactEmail authorizationCode sec1 - - -
				     * @contactEmail deviceAuthorization sec1 - - -
				     * @responseHeaderContent
				     * @responseHeaderContent - - - - - - -
				     * @responseHeaderContent arg1
				     * @responseHeaderContent arg1 arg2
				     * @responseHeaderContent arg1 arg2 arg3
				     * @responseHeaderContent arg1 arg2 arg3 arg4
				     * @responseHeaderContent arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderContent arg1 arg2|arg3|arg4
				     * @responseHeaderContent default application/json
				     * @responseHeaderContent 200 application/json
				     * @responseHeaderContent type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderContent type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderContent implicit sec1 - -
				     * @responseHeaderContent password sec1 - -
				     * @responseHeaderContent clientCredentials sec1 - -
				     * @responseHeaderContent authorizationCode sec1 - - -
				     * @responseHeaderContent deviceAuthorization sec1 - - -
				     * @componentLinkServer
				     * @componentLinkServer - - - - - - -
				     * @componentLinkServer arg1
				     * @componentLinkServer arg1 arg2
				     * @componentLinkServer arg1 arg2 arg3
				     * @componentLinkServer arg1 arg2 arg3 arg4
				     * @componentLinkServer arg1 arg2 arg3 arg4 arg5
				     * @componentLinkServer arg1 arg2|arg3|arg4
				     * @componentLinkServer default application/json
				     * @componentLinkServer 200 application/json
				     * @componentLinkServer type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLinkServer type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLinkServer implicit sec1 - -
				     * @componentLinkServer password sec1 - -
				     * @componentLinkServer clientCredentials sec1 - -
				     * @componentLinkServer authorizationCode sec1 - - -
				     * @componentLinkServer deviceAuthorization sec1 - - -
				     * @responseLinkOpId
				     * @responseLinkOpId - - - - - - -
				     * @responseLinkOpId arg1
				     * @responseLinkOpId arg1 arg2
				     * @responseLinkOpId arg1 arg2 arg3
				     * @responseLinkOpId arg1 arg2 arg3 arg4
				     * @responseLinkOpId arg1 arg2 arg3 arg4 arg5
				     * @responseLinkOpId arg1 arg2|arg3|arg4
				     * @responseLinkOpId default application/json
				     * @responseLinkOpId 200 application/json
				     * @responseLinkOpId type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseLinkOpId type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseLinkOpId implicit sec1 - -
				     * @responseLinkOpId password sec1 - -
				     * @responseLinkOpId clientCredentials sec1 - -
				     * @responseLinkOpId authorizationCode sec1 - - -
				     * @responseLinkOpId deviceAuthorization sec1 - - -
				     * @description
				     * @description - - - - - - -
				     * @description arg1
				     * @description arg1 arg2
				     * @description arg1 arg2 arg3
				     * @description arg1 arg2 arg3 arg4
				     * @description arg1 arg2 arg3 arg4 arg5
				     * @description arg1 arg2|arg3|arg4
				     * @description default application/json
				     * @description 200 application/json
				     * @description type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @description type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @description implicit sec1 - -
				     * @description password sec1 - -
				     * @description clientCredentials sec1 - -
				     * @description authorizationCode sec1 - - -
				     * @description deviceAuthorization sec1 - - -
				     * @componentLinkDesc
				     * @componentLinkDesc - - - - - - -
				     * @componentLinkDesc arg1
				     * @componentLinkDesc arg1 arg2
				     * @componentLinkDesc arg1 arg2 arg3
				     * @componentLinkDesc arg1 arg2 arg3 arg4
				     * @componentLinkDesc arg1 arg2 arg3 arg4 arg5
				     * @componentLinkDesc arg1 arg2|arg3|arg4
				     * @componentLinkDesc default application/json
				     * @componentLinkDesc 200 application/json
				     * @componentLinkDesc type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLinkDesc type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLinkDesc implicit sec1 - -
				     * @componentLinkDesc password sec1 - -
				     * @componentLinkDesc clientCredentials sec1 - -
				     * @componentLinkDesc authorizationCode sec1 - - -
				     * @componentLinkDesc deviceAuthorization sec1 - - -
				     * @pathParameter
				     * @pathParameter - - - - - - -
				     * @pathParameter arg1
				     * @pathParameter arg1 arg2
				     * @pathParameter arg1 arg2 arg3
				     * @pathParameter arg1 arg2 arg3 arg4
				     * @pathParameter arg1 arg2 arg3 arg4 arg5
				     * @pathParameter arg1 arg2|arg3|arg4
				     * @pathParameter default application/json
				     * @pathParameter 200 application/json
				     * @pathParameter type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @pathParameter type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @pathParameter implicit sec1 - -
				     * @pathParameter password sec1 - -
				     * @pathParameter clientCredentials sec1 - -
				     * @pathParameter authorizationCode sec1 - - -
				     * @pathParameter deviceAuthorization sec1 - - -
				     * @contactName
				     * @contactName - - - - - - -
				     * @contactName arg1
				     * @contactName arg1 arg2
				     * @contactName arg1 arg2 arg3
				     * @contactName arg1 arg2 arg3 arg4
				     * @contactName arg1 arg2 arg3 arg4 arg5
				     * @contactName arg1 arg2|arg3|arg4
				     * @contactName default application/json
				     * @contactName 200 application/json
				     * @contactName type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @contactName type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @contactName implicit sec1 - -
				     * @contactName password sec1 - -
				     * @contactName clientCredentials sec1 - -
				     * @contactName authorizationCode sec1 - - -
				     * @contactName deviceAuthorization sec1 - - -
				     * @responseHeaderExample
				     * @responseHeaderExample - - - - - - -
				     * @responseHeaderExample arg1
				     * @responseHeaderExample arg1 arg2
				     * @responseHeaderExample arg1 arg2 arg3
				     * @responseHeaderExample arg1 arg2 arg3 arg4
				     * @responseHeaderExample arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderExample arg1 arg2|arg3|arg4
				     * @responseHeaderExample default application/json
				     * @responseHeaderExample 200 application/json
				     * @responseHeaderExample type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderExample type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderExample implicit sec1 - -
				     * @responseHeaderExample password sec1 - -
				     * @responseHeaderExample clientCredentials sec1 - -
				     * @responseHeaderExample authorizationCode sec1 - - -
				     * @responseHeaderExample deviceAuthorization sec1 - - -
				     * @title
				     * @title - - - - - - -
				     * @title arg1
				     * @title arg1 arg2
				     * @title arg1 arg2 arg3
				     * @title arg1 arg2 arg3 arg4
				     * @title arg1 arg2 arg3 arg4 arg5
				     * @title arg1 arg2|arg3|arg4
				     * @title default application/json
				     * @title 200 application/json
				     * @title type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @title type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @title implicit sec1 - -
				     * @title password sec1 - -
				     * @title clientCredentials sec1 - -
				     * @title authorizationCode sec1 - - -
				     * @title deviceAuthorization sec1 - - -
				     * @responseEncodingItemEncoding
				     * @responseEncodingItemEncoding - - - - - - -
				     * @responseEncodingItemEncoding arg1
				     * @responseEncodingItemEncoding arg1 arg2
				     * @responseEncodingItemEncoding arg1 arg2 arg3
				     * @responseEncodingItemEncoding arg1 arg2 arg3 arg4
				     * @responseEncodingItemEncoding arg1 arg2 arg3 arg4 arg5
				     * @responseEncodingItemEncoding arg1 arg2|arg3|arg4
				     * @responseEncodingItemEncoding default application/json
				     * @responseEncodingItemEncoding 200 application/json
				     * @responseEncodingItemEncoding type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseEncodingItemEncoding type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseEncodingItemEncoding implicit sec1 - -
				     * @responseEncodingItemEncoding password sec1 - -
				     * @responseEncodingItemEncoding clientCredentials sec1 - -
				     * @responseEncodingItemEncoding authorizationCode sec1 - - -
				     * @responseEncodingItemEncoding deviceAuthorization sec1 - - -
				     * @responseHeaderRequired
				     * @responseHeaderRequired - - - - - - -
				     * @responseHeaderRequired arg1
				     * @responseHeaderRequired arg1 arg2
				     * @responseHeaderRequired arg1 arg2 arg3
				     * @responseHeaderRequired arg1 arg2 arg3 arg4
				     * @responseHeaderRequired arg1 arg2 arg3 arg4 arg5
				     * @responseHeaderRequired arg1 arg2|arg3|arg4
				     * @responseHeaderRequired default application/json
				     * @responseHeaderRequired 200 application/json
				     * @responseHeaderRequired type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseHeaderRequired type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseHeaderRequired implicit sec1 - -
				     * @responseHeaderRequired password sec1 - -
				     * @responseHeaderRequired clientCredentials sec1 - -
				     * @responseHeaderRequired authorizationCode sec1 - - -
				     * @responseHeaderRequired deviceAuthorization sec1 - - -
				     * @componentPathItem
				     * @componentPathItem - - - - - - -
				     * @componentPathItem arg1
				     * @componentPathItem arg1 arg2
				     * @componentPathItem arg1 arg2 arg3
				     * @componentPathItem arg1 arg2 arg3 arg4
				     * @componentPathItem arg1 arg2 arg3 arg4 arg5
				     * @componentPathItem arg1 arg2|arg3|arg4
				     * @componentPathItem default application/json
				     * @componentPathItem 200 application/json
				     * @componentPathItem type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentPathItem type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentPathItem implicit sec1 - -
				     * @componentPathItem password sec1 - -
				     * @componentPathItem clientCredentials sec1 - -
				     * @componentPathItem authorizationCode sec1 - - -
				     * @componentPathItem deviceAuthorization sec1 - - -
				     * @serverVariable
				     * @serverVariable - - - - - - -
				     * @serverVariable arg1
				     * @serverVariable arg1 arg2
				     * @serverVariable arg1 arg2 arg3
				     * @serverVariable arg1 arg2 arg3 arg4
				     * @serverVariable arg1 arg2 arg3 arg4 arg5
				     * @serverVariable arg1 arg2|arg3|arg4
				     * @serverVariable default application/json
				     * @serverVariable 200 application/json
				     * @serverVariable type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @serverVariable type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @serverVariable implicit sec1 - -
				     * @serverVariable password sec1 - -
				     * @serverVariable clientCredentials sec1 - -
				     * @serverVariable authorizationCode sec1 - - -
				     * @serverVariable deviceAuthorization sec1 - - -
				     * @pathDescription
				     * @pathDescription - - - - - - -
				     * @pathDescription arg1
				     * @pathDescription arg1 arg2
				     * @pathDescription arg1 arg2 arg3
				     * @pathDescription arg1 arg2 arg3 arg4
				     * @pathDescription arg1 arg2 arg3 arg4 arg5
				     * @pathDescription arg1 arg2|arg3|arg4
				     * @pathDescription default application/json
				     * @pathDescription 200 application/json
				     * @pathDescription type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @pathDescription type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @pathDescription implicit sec1 - -
				     * @pathDescription password sec1 - -
				     * @pathDescription clientCredentials sec1 - -
				     * @pathDescription authorizationCode sec1 - - -
				     * @pathDescription deviceAuthorization sec1 - - -
				     * @responseEncoding
				     * @responseEncoding - - - - - - -
				     * @responseEncoding arg1
				     * @responseEncoding arg1 arg2
				     * @responseEncoding arg1 arg2 arg3
				     * @responseEncoding arg1 arg2 arg3 arg4
				     * @responseEncoding arg1 arg2 arg3 arg4 arg5
				     * @responseEncoding arg1 arg2|arg3|arg4
				     * @responseEncoding default application/json
				     * @responseEncoding 200 application/json
				     * @responseEncoding type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @responseEncoding type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @responseEncoding implicit sec1 - -
				     * @responseEncoding password sec1 - -
				     * @responseEncoding clientCredentials sec1 - -
				     * @responseEncoding authorizationCode sec1 - - -
				     * @responseEncoding deviceAuthorization sec1 - - -
				     * @componentLinkOpRef
				     * @componentLinkOpRef - - - - - - -
				     * @componentLinkOpRef arg1
				     * @componentLinkOpRef arg1 arg2
				     * @componentLinkOpRef arg1 arg2 arg3
				     * @componentLinkOpRef arg1 arg2 arg3 arg4
				     * @componentLinkOpRef arg1 arg2 arg3 arg4 arg5
				     * @componentLinkOpRef arg1 arg2|arg3|arg4
				     * @componentLinkOpRef default application/json
				     * @componentLinkOpRef 200 application/json
				     * @componentLinkOpRef type=- scheme=- in=- name=- bearerFormat=- openIdConnectUrl=-
				     * @componentLinkOpRef type=a scheme=b in=c name=d bearerFormat=e openIdConnectUrl=f
				     * @componentLinkOpRef implicit sec1 - -
				     * @componentLinkOpRef password sec1 - -
				     * @componentLinkOpRef clientCredentials sec1 - -
				     * @componentLinkOpRef authorizationCode sec1 - - -
				     * @componentLinkOpRef deviceAuthorization sec1 - - -
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
				            b.method("GET");
				            return null;
				        }
				    }
				""";
		Parse.parse(edgeCode);

		OpenAPI errApi = Parse.parse("this is not java code {{");

		assertNotNull(errApi);
	}
}
