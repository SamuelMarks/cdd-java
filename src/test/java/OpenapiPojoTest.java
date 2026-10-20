import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import openapi.*;
import org.junit.Test;

/**
 * Strongly typed unit tests for all OpenAPI AST POJO classes, union wrappers,
 * {@link IdentifierSanitizer}, and {@link JavaTypeMapper}.
 */
public class OpenapiPojoTest {
	/**
	 * Default constructor.
	 */
	public OpenapiPojoTest() {
	}

	/**
	 * Asserts structural equality, inequality, hashCode, and toString invariants
	 * for a pair of equal instances and a modified instance.
	 *
	 * @param a
	 *            First instance.
	 * @param b
	 *            Equal second instance.
	 * @param diff
	 *            Modified distinct instance.
	 * @param <T>
	 *            The POJO type.
	 */
	private static <T> void assertPojoContract(T a, T b, T diff) {
		assertEquals(a, a);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertNotEquals(a, diff);
		assertNotEquals(a, null);
		assertNotEquals(a, "otherType");
		assertNotNull(a.toString());
	}

	/**
	 * Verifies all 31 OpenAPI model POJOs explicitly without reflection.
	 */
	@Test
	public void testPojos() {
		Callback cb1 = new Callback();
		Callback cb2 = new Callback();
		Callback cbDiff = new Callback();
		cbDiff.addExtension("x-ext", "extVal");
		cbDiff.addPathItem("http://expr", new PathItem());
		cbDiff.addPathItem("x-ignored", new PathItem());
		cbDiff.addPathItem(null, new PathItem());
		cbDiff.addPathItem("http://expr", null);
		cbDiff.addExtension("invalid", "val");
		assertEquals(1, cbDiff.getExtensions().size());
		assertEquals(1, cbDiff.pathItems.size());
		assertPojoContract(cb1, cb2, cbDiff);

		Components comp1 = new Components();
		Components comp2 = new Components();
		Components compDiff = new Components();
		compDiff.addExtension("x-test", "v");
		compDiff.addExtension("invalid", "v");
		assertNotNull(compDiff.getExtensions());
		assertPojoContract(comp1, comp2, compDiff);

		Contact ct1 = new Contact();
		Contact ct2 = new Contact();
		Contact ctDiff = new Contact();
		ctDiff.name = "Alice";
		ctDiff.addExtension("x-test", "v");
		ctDiff.addExtension("invalid", "v");
		assertNotNull(ctDiff.getExtensions());
		assertPojoContract(ct1, ct2, ctDiff);

		Discriminator d1 = new Discriminator();
		Discriminator d2 = new Discriminator();
		Discriminator dDiff = new Discriminator();
		dDiff.propertyName = "kind";
		dDiff.addExtension("x-test", "v");
		dDiff.addExtension("invalid", "v");
		assertNotNull(dDiff.getExtensions());
		assertPojoContract(d1, d2, dDiff);

		Encoding enc1 = new Encoding();
		Encoding enc2 = new Encoding();
		Encoding encDiff = new Encoding();
		encDiff.contentType = "application/json";
		encDiff.addExtension("x-test", "v");
		encDiff.addExtension("invalid", "v");
		assertNotNull(encDiff.getExtensions());
		assertPojoContract(enc1, enc2, encDiff);

		Example ex1 = new Example();
		Example ex2 = new Example();
		Example exDiff = new Example();
		exDiff.summary = "Sample";
		exDiff.addExtension("x-test", "v");
		exDiff.addExtension("invalid", "v");
		assertNotNull(exDiff.getExtensions());
		assertPojoContract(ex1, ex2, exDiff);

		ExternalDocumentation ed1 = new ExternalDocumentation();
		ExternalDocumentation ed2 = new ExternalDocumentation();
		ExternalDocumentation edDiff = new ExternalDocumentation();
		edDiff.url = "https://example.com";
		edDiff.addExtension("x-test", "v");
		edDiff.addExtension("invalid", "v");
		assertNotNull(edDiff.getExtensions());
		assertPojoContract(ed1, ed2, edDiff);

		Header h1 = new Header();
		Header h2 = new Header();
		Header hDiff = new Header();
		hDiff.description = "X-Rate-Limit";
		hDiff.addExtension("x-test", "v");
		hDiff.addExtension("invalid", "v");
		assertNotNull(hDiff.getExtensions());
		assertPojoContract(h1, h2, hDiff);

		Info info1 = new Info();
		Info info2 = new Info();
		Info infoDiff = new Info();
		infoDiff.title = "Petstore";
		infoDiff.addExtension("x-test", "v");
		infoDiff.addExtension("invalid", "v");
		assertNotNull(infoDiff.getExtensions());
		assertPojoContract(info1, info2, infoDiff);

		Items it1 = new Items();
		Items it2 = new Items();
		Items itDiff = new Items();
		itDiff.type = "string";
		itDiff.addExtension("x-test", "v");
		itDiff.addExtension("invalid", "v");
		assertNotNull(itDiff.getExtensions());
		assertPojoContract(it1, it2, itDiff);

		License lic1 = new License();
		License lic2 = new License();
		License licDiff = new License();
		licDiff.name = "MIT";
		licDiff.addExtension("x-test", "v");
		licDiff.addExtension("invalid", "v");
		assertNotNull(licDiff.getExtensions());
		assertPojoContract(lic1, lic2, licDiff);

		Link lnk1 = new Link();
		Link lnk2 = new Link();
		Link lnkDiff = new Link();
		lnkDiff.operationId = "getUser";
		lnkDiff.addExtension("x-test", "v");
		lnkDiff.addExtension("invalid", "v");
		assertNotNull(lnkDiff.getExtensions());
		assertPojoContract(lnk1, lnk2, lnkDiff);

		MediaType mt1 = new MediaType();
		MediaType mt2 = new MediaType();
		MediaType mtDiff = new MediaType();
		mtDiff.example = common.PayloadValue.ofString("example");
		mtDiff.addExtension("x-test", "v");
		mtDiff.addExtension("invalid", "v");
		assertNotNull(mtDiff.getExtensions());
		assertPojoContract(mt1, mt2, mtDiff);

		OAuthFlow of1 = new OAuthFlow();
		OAuthFlow of2 = new OAuthFlow();
		OAuthFlow ofDiff = new OAuthFlow();
		ofDiff.tokenUrl = "https://auth.example.com/token";
		ofDiff.addExtension("x-test", "v");
		ofDiff.addExtension("invalid", "v");
		assertNotNull(ofDiff.getExtensions());
		assertPojoContract(of1, of2, ofDiff);

		OAuthFlows ofs1 = new OAuthFlows();
		OAuthFlows ofs2 = new OAuthFlows();
		OAuthFlows ofsDiff = new OAuthFlows();
		ofsDiff.implicit = ofDiff;
		ofsDiff.addExtension("x-test", "v");
		ofsDiff.addExtension("invalid", "v");
		assertNotNull(ofsDiff.getExtensions());
		assertPojoContract(ofs1, ofs2, ofsDiff);

		OpenAPI api1 = new OpenAPI();
		OpenAPI api2 = new OpenAPI();
		OpenAPI apiDiff = new OpenAPI();
		apiDiff.openapi = "3.2.0";
		apiDiff.addExtension("x-test", "v");
		apiDiff.addExtension("invalid", "v");
		assertNotNull(apiDiff.getExtensions());
		assertPojoContract(api1, api2, apiDiff);

		Operation op1 = new Operation();
		Operation op2 = new Operation();
		Operation opDiff = new Operation();
		opDiff.operationId = "listPets";
		opDiff.addExtension("x-test", "v");
		opDiff.addExtension("invalid", "v");
		assertNotNull(opDiff.getExtensions());
		assertPojoContract(op1, op2, opDiff);

		Parameter p1 = new Parameter();
		Parameter p2 = new Parameter();
		Parameter pDiff = new Parameter();
		pDiff.name = "limit";
		pDiff.addExtension("x-test", "v");
		pDiff.addExtension("invalid", "v");
		assertNotNull(pDiff.getExtensions());
		assertPojoContract(p1, p2, pDiff);

		PathItem pi1 = new PathItem();
		PathItem pi2 = new PathItem();
		PathItem piDiff = new PathItem();
		piDiff.summary = "Pets endpoint";
		piDiff.addExtension("x-test", "v");
		piDiff.addExtension("invalid", "v");
		assertNotNull(piDiff.getExtensions());
		assertPojoContract(pi1, pi2, piDiff);

		Paths paths1 = new Paths();
		Paths paths2 = new Paths();
		Paths pathsDiff = new Paths();
		pathsDiff.addExtension("x-ext", "extVal");
		pathsDiff.addExtension("x-bool", true);
		pathsDiff.addExtension("x-num", 42);
		pathsDiff.addPathItem("/path", new PathItem());
		pathsDiff.addPathItem("invalid", new PathItem());
		pathsDiff.addPathItem(null, new PathItem());
		pathsDiff.addPathItem("/path", null);
		pathsDiff.addExtension("invalid", "val");
		assertEquals(3, pathsDiff.getExtensions().size());
		assertEquals(1, pathsDiff.pathItems.size());
		assertPojoContract(paths1, paths2, pathsDiff);

		Reference ref1 = new Reference();
		Reference ref2 = new Reference();
		Reference refDiff = new Reference();
		refDiff.ref = "#/components/schemas/Pet";
		refDiff.addExtension("x-test", "v");
		refDiff.addExtension("invalid", "v");
		assertNotNull(refDiff.getExtensions());
		assertPojoContract(ref1, ref2, refDiff);

		RequestBody rb1 = new RequestBody();
		RequestBody rb2 = new RequestBody();
		RequestBody rbDiff = new RequestBody();
		rbDiff.description = "Body";
		rbDiff.addExtension("x-test", "v");
		rbDiff.addExtension("invalid", "v");
		assertNotNull(rbDiff.getExtensions());
		assertPojoContract(rb1, rb2, rbDiff);

		Response resp1 = new Response();
		Response resp2 = new Response();
		Response respDiff = new Response();
		respDiff.description = "OK";
		respDiff.addExtension("x-test", "v");
		respDiff.addExtension("invalid", "v");
		assertNotNull(respDiff.getExtensions());
		assertPojoContract(resp1, resp2, respDiff);

		Responses resps1 = new Responses();
		Responses resps2 = new Responses();
		Responses respsDiff = new Responses();
		respsDiff.addStatusCode("default", new Response());
		respsDiff.addStatusCode("default", ReferenceOr.ofValue(new Response()));
		respsDiff.addStatusCode("default", (Response) null);
		respsDiff.addStatusCode("default", (ReferenceOr<Response>) null);
		respsDiff.addStatusCode(null, new Response());
		respsDiff.addStatusCode("x-ignored", new Response());
		respsDiff.addExtension("x-ext", "extVal");
		respsDiff.addStatusCode("200", new Response());
		respsDiff.addStatusCode("201", ReferenceOr.ofValue(new Response()));
		respsDiff.addExtension("invalid", "val");
		assertEquals(1, respsDiff.getExtensions().size());
		assertEquals(2, respsDiff.statusCodes.size());
		assertNotNull(respsDiff.defaultResponse);
		assertPojoContract(resps1, resps2, respsDiff);

		Schema s1 = new Schema();
		Schema s2 = new Schema();
		Schema sDiff = new Schema();
		sDiff.type = SchemaType.ofSingle("string");
		sDiff.addExtension("x-test", "v");
		sDiff.addExtension("invalid", "v");
		assertNotNull(sDiff.getExtensions());
		assertPojoContract(s1, s2, sDiff);

		SecurityRequirement sr1 = new SecurityRequirement();
		SecurityRequirement sr2 = new SecurityRequirement();
		SecurityRequirement srDiff = new SecurityRequirement();
		srDiff.addRequirement("req", Collections.singletonList("scope"));
		assertEquals(1, srDiff.getRequirements().size());
		assertPojoContract(sr1, sr2, srDiff);

		SecurityScheme ss1 = new SecurityScheme();
		SecurityScheme ss2 = new SecurityScheme();
		SecurityScheme ssDiff = new SecurityScheme();
		ssDiff.type = "http";
		ssDiff.addExtension("x-test", "v");
		ssDiff.addExtension("invalid", "v");
		assertNotNull(ssDiff.getExtensions());
		assertPojoContract(ss1, ss2, ssDiff);

		Server srv1 = new Server();
		Server srv2 = new Server();
		Server srvDiff = new Server();
		srvDiff.url = "https://api.example.com";
		srvDiff.addExtension("x-test", "v");
		srvDiff.addExtension("invalid", "v");
		assertNotNull(srvDiff.getExtensions());
		assertPojoContract(srv1, srv2, srvDiff);

		ServerVariable sv1 = new ServerVariable();
		ServerVariable sv2 = new ServerVariable();
		ServerVariable svDiff = new ServerVariable();
		svDiff.defaultValue = "v1";
		svDiff.addExtension("x-test", "v");
		svDiff.addExtension("invalid", "v");
		assertNotNull(svDiff.getExtensions());
		assertPojoContract(sv1, sv2, svDiff);

		Tag tag1 = new Tag();
		Tag tag2 = new Tag();
		Tag tagDiff = new Tag();
		tagDiff.name = "pets";
		tagDiff.addExtension("x-test", "v");
		tagDiff.addExtension("invalid", "v");
		assertNotNull(tagDiff.getExtensions());
		assertPojoContract(tag1, tag2, tagDiff);

		XML xml1 = new XML();
		XML xml2 = new XML();
		XML xmlDiff = new XML();
		xmlDiff.name = "pet";
		xmlDiff.addExtension("x-test", "v");
		xmlDiff.addExtension("invalid", "v");
		assertNotNull(xmlDiff.getExtensions());
		assertPojoContract(xml1, xml2, xmlDiff);

		// ReferenceOr
		ReferenceOr<String> valWrapper = ReferenceOr.ofValue("hello");
		ReferenceOr<String> valWrapper2 = ReferenceOr.ofValue("hello");
		ReferenceOr<String> refWrapper = ReferenceOr.ofRef("#/components/schemas/Foo");
		Reference refObj = new Reference();
		refObj.ref = "#/components/schemas/Foo";
		ReferenceOr<String> refWrapper2 = ReferenceOr.ofRef(refObj);
		ReferenceOr<String> refWrapperDiff = ReferenceOr.ofRef("#/components/schemas/Bar");
		assertTrue(valWrapper.isValue());
		assertFalse(valWrapper.isRef());
		assertTrue(refWrapper.isRef());
		assertFalse(refWrapper.isValue());
		assertEquals(valWrapper, valWrapper);
		assertEquals(valWrapper, valWrapper2);
		assertNotEquals(valWrapper, refWrapper);
		assertNotEquals(refWrapper, refWrapperDiff);
		assertNotEquals(valWrapper, "notReferenceOr");
		assertEquals(valWrapper.hashCode(), valWrapper2.hashCode());
		assertEquals("Value(hello)", valWrapper.toString());
		assertEquals("Ref(#/components/schemas/Foo)", refWrapper.toString());
		assertNotNull(refWrapper2);

		// SchemaType
		SchemaType singleType = SchemaType.ofSingle("string");
		SchemaType singleType2 = new SchemaType("string");
		SchemaType nullSingle = new SchemaType((String) null);
		SchemaType multiType = SchemaType.ofMultiple(Arrays.asList("string", "null"));
		SchemaType emptyMulti = new SchemaType((java.util.List<String>) null);
		assertFalse(singleType.isMultiType());
		assertTrue(multiType.isMultiType());
		assertNull(nullSingle.single);
		assertNull(emptyMulti.single);
		assertEquals(singleType, singleType);
		assertEquals(singleType, singleType2);
		assertNotEquals(singleType, multiType);
		assertNotEquals(singleType, nullSingle);
		assertNotEquals(singleType, "notSchemaType");
		assertEquals(singleType.hashCode(), singleType2.hashCode());
		assertEquals("string", singleType.toString());
		assertEquals("[string, null]", multiType.toString());

		// ExclusiveBound
		ExclusiveBound ebBool = ExclusiveBound.ofBoolean(true);
		ExclusiveBound ebBool2 = ExclusiveBound.ofBoolean(true);
		ExclusiveBound ebNum = ExclusiveBound.ofNumber(10);
		ExclusiveBound ebNum2 = ExclusiveBound.ofNumber(20);
		assertTrue(ebBool.isBoolean());
		assertFalse(ebBool.isNumber());
		assertTrue(ebNum.isNumber());
		assertFalse(ebNum.isBoolean());
		assertEquals(ebBool, ebBool);
		assertEquals(ebBool, ebBool2);
		assertNotEquals(ebBool, ebNum);
		assertNotEquals(ebNum, ebNum2);
		assertNotEquals(ebBool, "notExclusiveBound");
		assertEquals(ebBool.hashCode(), ebBool2.hashCode());
		assertEquals("true", ebBool.toString());
		assertEquals("10", ebNum.toString());

		// AdditionalProperties
		AdditionalProperties boolAp = AdditionalProperties.ofBoolean(true);
		AdditionalProperties boolAp2 = AdditionalProperties.ofBoolean(true);
		Schema apSchema = new Schema();
		AdditionalProperties schemaAp = AdditionalProperties.ofSchema(apSchema);
		AdditionalProperties refAp = AdditionalProperties.ofRefOrSchema(ReferenceOr.ofRef("#/components/schemas/Bar"));
		assertTrue(boolAp.isBoolean());
		assertFalse(boolAp.isSchema());
		assertTrue(schemaAp.isSchema());
		assertFalse(schemaAp.isBoolean());
		assertEquals(boolAp, boolAp);
		assertEquals(boolAp, boolAp2);
		assertNotEquals(boolAp, schemaAp);
		assertNotEquals(schemaAp, refAp);
		assertNotEquals(boolAp, "notAp");
		assertEquals(boolAp.hashCode(), boolAp2.hashCode());
		assertEquals("true", boolAp.toString());
		assertTrue(refAp.toString().contains("Ref("));

		// IdentifierSanitizer & JavaTypeMapper
		assertEquals("abc_123", IdentifierSanitizer.sanitizeIdentifier("abc_123!"));
		assertEquals("", IdentifierSanitizer.sanitizeIdentifier(null));
		assertEquals("abc123", IdentifierSanitizer.sanitizeAlphanumeric("abc_123!"));
		assertEquals("", IdentifierSanitizer.sanitizeAlphanumeric(null));
		assertEquals("", IdentifierSanitizer.escape(null));
		assertEquals("Object", JavaTypeMapper.toJavaType((Object) "notSchema", api1, false));
		assertEquals("Object", JavaTypeMapper.toJavaType((Object) sDiff, api1, false).equals("String") ? "Object" : "");
		assertEquals("Object", JavaTypeMapper.toJavaType((ReferenceOr<Schema>) null, api1, false));
		assertEquals("Object", JavaTypeMapper.toJavaType((Schema) null, api1, false));
		assertEquals("Pet",
				JavaTypeMapper.toJavaType(ReferenceOr.<Schema>ofRef("#/components/schemas/Pet"), api1, false));
		assertEquals("String", JavaTypeMapper.toJavaType(ReferenceOr.ofValue(sDiff), api1, false));
		assertEquals("String", TypeMapper.toJavaType(sDiff, api1, false));
		assertEquals("String", TypeMapper.toJavaType(ReferenceOr.ofValue(sDiff), api1, false));
		assertEquals("String", TypeMapper.toJavaType((Object) ReferenceOr.ofValue(sDiff), api1, false));
		assertEquals("String", TypeMapper.toJavaType((Object) sDiff, api1, false));

		// ReferenceOr.resolveRefString
		Schema inlineRefSchema = new Schema();
		inlineRefSchema.$ref = "#/components/schemas/Inline";
		assertEquals("#/components/schemas/Foo", refWrapper.resolveRefString());
		assertNull(ReferenceOr.ofRef(new Reference()).resolveRefString());
		assertEquals("#/components/schemas/Inline", ReferenceOr.ofValue(inlineRefSchema).resolveRefString());
		assertNull(valWrapper.resolveRefString());

		// HttpVerb, ParameterLocation, ParameterStyle, SecuritySchemeType
		for (HttpVerb v : HttpVerb.values()) {
			assertEquals(v, HttpVerb.fromString(v.name()));
			assertEquals(v, HttpVerb.fromString(v.specName));
		}
		assertNull(HttpVerb.fromString(null));
		assertNull(HttpVerb.fromString("CUSTOM"));
		assertEquals(HttpVerb.POST, HttpVerb.fromStringOrDefault("POST", HttpVerb.GET));
		assertEquals(HttpVerb.GET, HttpVerb.fromStringOrDefault("CUSTOM", HttpVerb.GET));

		for (ParameterLocation loc : ParameterLocation.values()) {
			assertEquals(loc, ParameterLocation.fromString(loc.value));
		}
		assertNull(ParameterLocation.fromString(null));
		assertNull(ParameterLocation.fromString("unknown"));

		for (ParameterStyle st : ParameterStyle.values()) {
			assertEquals(st, ParameterStyle.fromString(st.value));
		}
		assertNull(ParameterStyle.fromString(null));
		assertNull(ParameterStyle.fromString("unknown"));

		for (SecuritySchemeType sct : SecuritySchemeType.values()) {
			assertEquals(sct, SecuritySchemeType.fromString(sct.value));
		}
		assertNull(SecuritySchemeType.fromString(null));
		assertNull(SecuritySchemeType.fromString("unknown"));

		Parameter typedParam = new Parameter();
		typedParam.setLocation(ParameterLocation.QUERY);
		assertEquals(ParameterLocation.QUERY, typedParam.getLocation());
		typedParam.setLocation(null);
		assertNull(typedParam.getLocation());
		typedParam.setParameterStyle(ParameterStyle.FORM);
		assertEquals(ParameterStyle.FORM, typedParam.getParameterStyle());
		typedParam.setParameterStyle(null);
		assertNull(typedParam.getParameterStyle());

		Header typedHeader = new Header();
		typedHeader.setParameterStyle(ParameterStyle.SIMPLE);
		assertEquals(ParameterStyle.SIMPLE, typedHeader.getParameterStyle());
		typedHeader.setParameterStyle(null);
		assertNull(typedHeader.getParameterStyle());

		Encoding typedEncoding = new Encoding();
		typedEncoding.setParameterStyle(ParameterStyle.SPACE_DELIMITED);
		assertEquals(ParameterStyle.SPACE_DELIMITED, typedEncoding.getParameterStyle());
		typedEncoding.setParameterStyle(null);
		assertNull(typedEncoding.getParameterStyle());

		SecurityScheme typedSec = new SecurityScheme();
		typedSec.setSchemeType(SecuritySchemeType.API_KEY);
		assertEquals(SecuritySchemeType.API_KEY, typedSec.getSchemeType());
		typedSec.setSchemeType(null);
		assertNull(typedSec.getSchemeType());
		typedSec.setLocation(ParameterLocation.HEADER);
		assertEquals(ParameterLocation.HEADER, typedSec.getLocation());
		typedSec.setLocation(null);
		assertNull(typedSec.getLocation());

		PathItem fullPi = new PathItem();
		assertNull(fullPi.getOperation((HttpVerb) null));
		assertNull(fullPi.getOperation((String) null));
		assertNull(fullPi.getOperation("COPY"));
		fullPi.setOperation((HttpVerb) null, new Operation());
		fullPi.setOperation((String) null, new Operation());
		for (HttpVerb v : HttpVerb.values()) {
			Operation op = new Operation();
			op.operationId = v.specName;
			fullPi.setOperation(v.name(), op);
			assertEquals(op, fullPi.getOperation(v));
			assertEquals(op, fullPi.getOperation(v.specName));
		}
		assertEquals(HttpVerb.values().length, fullPi.getOperationsMap().size());
		Operation copyOp = new Operation();
		fullPi.setOperation("COPY", copyOp);
		fullPi.setOperation("MOVE", copyOp);
		assertEquals(copyOp, fullPi.getOperation("COPY"));

		assertEquals("", common.JavadocMetadataCodec.encodeUnderscoreToken(null));
		assertEquals("", common.JavadocMetadataCodec.decodeUnderscoreToken(null));
		String rawToken = "a_b c%d";
		String encodedToken = common.JavadocMetadataCodec.encodeUnderscoreToken(rawToken);
		assertEquals("a%5Fb_c%25d", encodedToken);
		assertEquals(rawToken, common.JavadocMetadataCodec.decodeUnderscoreToken(encodedToken));

		// CddException
		CddException cddEx1 = new CddException(CddException.ErrorCode.PARSE_ERROR, "bad input");
		assertEquals(CddException.ErrorCode.PARSE_ERROR, cddEx1.errorCode);
		assertEquals("bad input", cddEx1.getMessage());
		RuntimeException cause = new RuntimeException("io");
		CddException cddEx2 = new CddException(CddException.ErrorCode.RESOLUTION_ERROR, "unresolved", cause);
		assertEquals(CddException.ErrorCode.RESOLUTION_ERROR, cddEx2.errorCode);
		assertEquals(cause, cddEx2.getCause());
		assertEquals(CddException.ErrorCode.VALIDATION_ERROR, CddException.ErrorCode.valueOf("VALIDATION_ERROR"));
	}
}
