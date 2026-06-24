
import org.junit.Test;
public class StringFuzzTest {
	@Test
	public void fuzzRoutes() {
		try {
			routes.Parse.parse("/** \n* @HEAD test \n*/ class T { void m() { HEAD(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @HEAD test \n*/ class T { void m() { obj.HEAD(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @method test \n*/ class T { void m() { method(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @method test \n*/ class T { void m() { obj.method(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @header test \n*/ class T { void m() { header(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @header test \n*/ class T { void m() { obj.header(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @query test \n*/ class T { void m() { query(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @query test \n*/ class T { void m() { obj.query(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @QUERY test \n*/ class T { void m() { QUERY(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @QUERY test \n*/ class T { void m() { obj.QUERY(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @DELETE test \n*/ class T { void m() { DELETE(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @DELETE test \n*/ class T { void m() { obj.DELETE(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @application/json test \n*/ class T { void m() { application/json(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @application/json test \n*/ class T { void m() { obj.application/json(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Authorization test \n*/ class T { void m() { Authorization(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Authorization test \n*/ class T { void m() { obj.Authorization(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @description test \n*/ class T { void m() { description(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @description test \n*/ class T { void m() { obj.description(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @licenseName test \n*/ class T { void m() { licenseName(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @licenseName test \n*/ class T { void m() { obj.licenseName(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @licenseUrl test \n*/ class T { void m() { licenseUrl(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @licenseUrl test \n*/ class T { void m() { obj.licenseUrl(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @) || pName.equals( test \n*/ class T { void m() { ) || pName.equals((); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse
					.parse("/** \n* @) || pName.equals( test \n*/ class T { void m() { obj.) || pName.equals((); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @) test \n*/ class T { void m() { )(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @) test \n*/ class T { void m() { obj.)(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @PUT test \n*/ class T { void m() { PUT(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @PUT test \n*/ class T { void m() { obj.PUT(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @jsonSchemaDialect test \n*/ class T { void m() { jsonSchemaDialect(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse
					.parse("/** \n* @jsonSchemaDialect test \n*/ class T { void m() { obj.jsonSchemaDialect(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @version test \n*/ class T { void m() { version(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @version test \n*/ class T { void m() { obj.version(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @type test \n*/ class T { void m() { type(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @type test \n*/ class T { void m() { obj.type(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @http test \n*/ class T { void m() { http(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @http test \n*/ class T { void m() { obj.http(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @OPTIONS test \n*/ class T { void m() { OPTIONS(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @OPTIONS test \n*/ class T { void m() { obj.OPTIONS(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @bearer test \n*/ class T { void m() { bearer(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @bearer test \n*/ class T { void m() { obj.bearer(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @requestBody test \n*/ class T { void m() { requestBody(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @requestBody test \n*/ class T { void m() { obj.requestBody(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @POST test \n*/ class T { void m() { POST(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @POST test \n*/ class T { void m() { obj.POST(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @path test \n*/ class T { void m() { path(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @path test \n*/ class T { void m() { obj.path(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @TRACE test \n*/ class T { void m() { TRACE(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @TRACE test \n*/ class T { void m() { obj.TRACE(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Client test \n*/ class T { void m() { Client(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Client test \n*/ class T { void m() { obj.Client(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @ Path:  test \n*/ class T { void m() {  Path: (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @ Path:  test \n*/ class T { void m() { obj. Path: (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Method:  test \n*/ class T { void m() { Method: (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Method:  test \n*/ class T { void m() { obj.Method: (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @GET test \n*/ class T { void m() { GET(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @GET test \n*/ class T { void m() { obj.GET(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse
					.parse("/** \n* @Successful response test \n*/ class T { void m() { Successful response(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse(
					"/** \n* @Successful response test \n*/ class T { void m() { obj.Successful response(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @200 test \n*/ class T { void m() { 200(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @200 test \n*/ class T { void m() { obj.200(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @3.2.0 test \n*/ class T { void m() { 3.2.0(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @3.2.0 test \n*/ class T { void m() { obj.3.2.0(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @openapiSelf test \n*/ class T { void m() { openapiSelf(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @openapiSelf test \n*/ class T { void m() { obj.openapiSelf(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @body test \n*/ class T { void m() { body(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @body test \n*/ class T { void m() { obj.body(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @{$1} test \n*/ class T { void m() { {$1}(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @{$1} test \n*/ class T { void m() { obj.{$1}(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @contactUrl test \n*/ class T { void m() { contactUrl(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @contactUrl test \n*/ class T { void m() { obj.contactUrl(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @PATCH test \n*/ class T { void m() { PATCH(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @PATCH test \n*/ class T { void m() { obj.PATCH(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @ : parts[3]) :  test \n*/ class T { void m() {  : parts[3]) : (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @ : parts[3]) :  test \n*/ class T { void m() { obj. : parts[3]) : (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @termsOfService test \n*/ class T { void m() { termsOfService(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @termsOfService test \n*/ class T { void m() { obj.termsOfService(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @licenseIdentifier test \n*/ class T { void m() { licenseIdentifier(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse
					.parse("/** \n* @licenseIdentifier test \n*/ class T { void m() { obj.licenseIdentifier(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @,  test \n*/ class T { void m() { , (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @,  test \n*/ class T { void m() { obj., (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @summary test \n*/ class T { void m() { summary(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @summary test \n*/ class T { void m() { obj.summary(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @0.0.3 test \n*/ class T { void m() { 0.0.3(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @0.0.3 test \n*/ class T { void m() { obj.0.0.3(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @uri test \n*/ class T { void m() { uri(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @uri test \n*/ class T { void m() { obj.uri(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @) ?  test \n*/ class T { void m() { ) ? (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @) ?  test \n*/ class T { void m() { obj.) ? (); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @globalTag test \n*/ class T { void m() { globalTag(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @globalTag test \n*/ class T { void m() { obj.globalTag(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @string test \n*/ class T { void m() { string(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @string test \n*/ class T { void m() { obj.string(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @authorization test \n*/ class T { void m() { authorization(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @authorization test \n*/ class T { void m() { obj.authorization(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @{ test \n*/ class T { void m() { {(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @{ test \n*/ class T { void m() { obj.{(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @contactEmail test \n*/ class T { void m() { contactEmail(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @contactEmail test \n*/ class T { void m() { obj.contactEmail(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @openapiVersion test \n*/ class T { void m() { openapiVersion(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @openapiVersion test \n*/ class T { void m() { obj.openapiVersion(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @BearerAuth test \n*/ class T { void m() { BearerAuth(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @BearerAuth test \n*/ class T { void m() { obj.BearerAuth(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @? test \n*/ class T { void m() { ?(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @? test \n*/ class T { void m() { obj.?(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @title test \n*/ class T { void m() { title(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @title test \n*/ class T { void m() { obj.title(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @} test \n*/ class T { void m() { }(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @} test \n*/ class T { void m() { obj.}(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @contactName test \n*/ class T { void m() { contactName(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @contactName test \n*/ class T { void m() { obj.contactName(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Extracted API test \n*/ class T { void m() { Extracted API(); } }");
		} catch (Exception e) {
		}
		try {
			routes.Parse.parse("/** \n* @Extracted API test \n*/ class T { void m() { obj.Extracted API(); } }");
		} catch (Exception e) {
		}

	}
	@Test
	public void fuzzCli() {
		try {
			cli.Parse.parse("/** \n* @true test \n*/ class T { void m() { true(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @true test \n*/ class T { void m() { obj.true(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @options test \n*/ class T { void m() { options(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @options test \n*/ class T { void m() { obj.options(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @Component  test \n*/ class T { void m() { Component (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @Component  test \n*/ class T { void m() { obj.Component (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @query test \n*/ class T { void m() { query(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @query test \n*/ class T { void m() { obj.query(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @  test \n*/ class T { void m() {  (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @  test \n*/ class T { void m() { obj. (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @).matcher( test \n*/ class T { void m() { ).matcher((); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @).matcher( test \n*/ class T { void m() { obj.).matcher((); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @callbacks test \n*/ class T { void m() { callbacks(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @callbacks test \n*/ class T { void m() { obj.callbacks(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @responses test \n*/ class T { void m() { responses(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @responses test \n*/ class T { void m() { obj.responses(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @Operation Object  test \n*/ class T { void m() { Operation Object (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @Operation Object  test \n*/ class T { void m() { obj.Operation Object (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @ or  test \n*/ class T { void m() {  or (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @ or  test \n*/ class T { void m() { obj. or (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @mediaTypes test \n*/ class T { void m() { mediaTypes(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @mediaTypes test \n*/ class T { void m() { obj.mediaTypes(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @head test \n*/ class T { void m() { head(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @head test \n*/ class T { void m() { obj.head(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @patch test \n*/ class T { void m() { patch(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @patch test \n*/ class T { void m() { obj.patch(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @headers test \n*/ class T { void m() { headers(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @headers test \n*/ class T { void m() { obj.headers(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @get test \n*/ class T { void m() { get(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @get test \n*/ class T { void m() { obj.get(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse(
					"/** \n* @Component securitySchemesFlowScope  test \n*/ class T { void m() { Component securitySchemesFlowScope (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse(
					"/** \n* @Component securitySchemesFlowScope  test \n*/ class T { void m() { obj.Component securitySchemesFlowScope (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @parameters test \n*/ class T { void m() { parameters(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @parameters test \n*/ class T { void m() { obj.parameters(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @- test \n*/ class T { void m() { -(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @- test \n*/ class T { void m() { obj.-(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @put test \n*/ class T { void m() { put(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @put test \n*/ class T { void m() { obj.put(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @requestBodies test \n*/ class T { void m() { requestBodies(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @requestBodies test \n*/ class T { void m() { obj.requestBodies(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @).replace( test \n*/ class T { void m() { ).replace((); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @).replace( test \n*/ class T { void m() { obj.).replace((); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @?)? test \n*/ class T { void m() { ?)?(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @?)? test \n*/ class T { void m() { obj.?)?(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @post test \n*/ class T { void m() { post(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @post test \n*/ class T { void m() { obj.post(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @,  test \n*/ class T { void m() { , (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @,  test \n*/ class T { void m() { obj., (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @pathItems test \n*/ class T { void m() { pathItems(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @pathItems test \n*/ class T { void m() { obj.pathItems(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @Operation:  test \n*/ class T { void m() { Operation: (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @Operation:  test \n*/ class T { void m() { obj.Operation: (); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @links test \n*/ class T { void m() { links(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @links test \n*/ class T { void m() { obj.links(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @trace test \n*/ class T { void m() { trace(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @trace test \n*/ class T { void m() { obj.trace(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @delete test \n*/ class T { void m() { delete(); } }");
		} catch (Exception e) {
		}
		try {
			cli.Parse.parse("/** \n* @delete test \n*/ class T { void m() { obj.delete(); } }");
		} catch (Exception e) {
		}

	}
}
