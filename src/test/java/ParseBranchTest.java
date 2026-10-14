import org.junit.Test;
import static org.junit.Assert.*;
import cli.Parse;
import openapi.*;

/**
 * Branch coverage unit tests for {@link cli.Parse}.
 */
public class ParseBranchTest {
	/**
	 * Verifies edge cases in server variable, discriminator, and XML line parsing.
	 */
	@Test
	public void testParseBranches() {
		String cli = "public class T {\n" + "    private static void printHelp() {\n"
				+ "        System.out.println(\"ServerVariableObject noServers\");\n"
				+ "        System.out.println(\"Server Variable Object noServers\");\n"
				+ "        System.out.println(\"Server Object url: \");\n"
				+ "        System.out.println(\"Component schemas S1\");\n"
				+ "        System.out.println(\"  Discriminator propertyName= mapping= defaultMapping=\");\n"
				+ "        System.out.println(\"  Discriminator propertyName=null mapping=null defaultMapping=null\");\n"
				+ "        System.out.println(\"  Discriminator propertyName=p mapping=a defaultMapping=d\");\n"
				+ "        System.out.println(\"  XML name= namespace= prefix= attribute= wrapped=\");\n"
				+ "        System.out.println(\"  XML name=null namespace=null prefix=null attribute=null wrapped=null\");\n"
				+ "        System.out.println(\"  XML name=n namespace=ns prefix=p attribute=false wrapped=false\");\n"
				+ "        System.out.println(\"  Discriminator propertyName=broken\");\n"
				+ "        System.out.println(\"  XML name=broken\");\n"
				+ "        System.out.println(\"Component securitySchemesFlowScope short\");\n"
				+ "        System.out.println(\"Component securitySchemesFlowScope missingSec implicit s d\");\n"
				+ "        System.out.println(\"Component securitySchemes secNoFlows type=oauth2 scheme=- in=- name=- bearerFormat=- openIdConnectUrl=- oauth2MetadataUrl=- deprecated=false\");\n"
				+ "        System.out.println(\"Component securitySchemesFlowScope secNoFlows implicit s d\");\n"
				+ "        System.out.println(\"Component securitySchemesFlow short\");\n"
				+ "        System.out.println(\"Component securitySchemesFlow secNoFlows unknownFlow - - - -\");\n"
				+ "        System.out.println(\"Component securitySchemesFlowScope secNoFlows unknownFlow s d\");\n"
				+ "        System.out.println(\"Component securitySchemesFlow secNoFlows implicit - - - -\");\n"
				+ "        System.out.println(\"Component securitySchemesFlowScope secNoFlows implicit s1 d1\");\n"
				+ "        System.out.println(\"Component securitySchemesFlowScope secNoFlows implicit s2 d2\");\n"
				+ "        System.out.println(\"Component linksParam short\");\n"
				+ "        System.out.println(\"Component links lnk1\");\n"
				+ "        System.out.println(\"Component linksParam lnk1 p1 v1\");\n"
				+ "        System.out.println(\"Component linksParam lnk1 p2 v2\");\n"
				+ "        System.out.println(\"Component short\");\n"
				+ "        System.out.println(\"Component unknownType k1\");\n"
				+ "        System.out.println(\"    OperationId: noOp\");\n"
				+ "        System.out.println(\"    Description: noOp\");\n"
				+ "        System.out.println(\"      RequestBodyNoSpace\");\n"
				+ "        System.out.println(\"      Example: noParam\");\n"
				+ "        System.out.println(\"      Example noParam\");\n"
				+ "        System.out.println(\"      ResponseNoSpace\");\n"
				+ "        System.out.println(\"        LinkParam noResp\");\n"
				+ "        System.out.println(\"Operation: get /edge\");\n"
				+ "        System.out.println(\"Callback: cb1\");\n"
				+ "        System.out.println(\"Callback: cb2\");\n"
				+ "        System.out.println(\"      RequestBodyNoRequestBodyYet a b\");\n"
				+ "        System.out.println(\"    --requestBody: rb [Content-Types: application/json]\");\n"
				+ "        System.out.println(\"      RequestBodyNoSpace\");\n"
				+ "        System.out.println(\"      RequestBodyContentItemSchema noSpace\");\n"
				+ "        System.out.println(\"      RequestBodyContentItemSchema unknown/ct string\");\n"
				+ "        System.out.println(\"      RequestBodyContentItemSchema application/json string\");\n"
				+ "        System.out.println(\"      RequestBodyContentItemSchema application/json integer\");\n"
				+ "        System.out.println(\"      RequestBodyContentPrefixEncoding application/json text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyContentPrefixEncoding application/json text/csv\");\n"
				+ "        System.out.println(\"      RequestBodyContentItemEncoding application/json text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyContentItemEncoding application/json text/csv\");\n"
				+ "        System.out.println(\"      RequestBodyContentUnknown application/json text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyEncoding short a\");\n"
				+ "        System.out.println(\"      RequestBodyEncoding unknown/ct k text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingPrefixEncoding application/json e1 text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingPrefixEncoding application/json e1 text/csv\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingItemEncoding application/json e1 text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingItemEncoding application/json e1 text/csv\");\n"
				+ "        System.out.println(\"      RequestBodyEncodingUnknown application/json e1 text/plain\");\n"
				+ "        System.out.println(\"      RequestBodyUnknownKind application/json e1\");\n"
				+ "        System.out.println(\"    --p1 : desc\");\n"
				+ "        System.out.println(\"    --p2 : desc\");\n"
				+ "        System.out.println(\"      Example noColon\");\n"
				+ "        System.out.println(\"      Example k0: noDashOrEq\");\n"
				+ "        System.out.println(\"      Example k0b: has - dashButNoEq\");\n"
				+ "        System.out.println(\"      Example k1: null - null = null\");\n"
				+ "        System.out.println(\"      Example k2: s - d = v\");\n"
				+ "        System.out.println(\"      ResponseNoResponsesYet 200 a b\");\n"
				+ "        System.out.println(\"    Returns 200: ok\");\n"
				+ "        System.out.println(\"      ResponseNoSpace\");\n"
				+ "        System.out.println(\"      ResponseHeader h1: d1\");\n"
				+ "        System.out.println(\"      Header h1: d1\");\n"
				+ "        System.out.println(\"      Header h2: d2\");\n"
				+ "        System.out.println(\"        LinkParam noLinksYet a b\");\n"
				+ "        System.out.println(\"      Link l1\");\n"
				+ "        System.out.println(\"      Link l2 operationId=op\");\n"
				+ "        System.out.println(\"        LinkParam short\");\n"
				+ "        System.out.println(\"        LinkParam missingLink k v\");\n"
				+ "        System.out.println(\"        LinkParam l1 k1 v1\");\n"
				+ "        System.out.println(\"        LinkParam l1 k2 v2\");\n" + "    }\n" + "}";
		OpenAPI api = Parse.parse(cli);
		assertNotNull(api);
		assertNotNull(api.components);
		Schema s1 = api.components.schemas.get("S1");
		assertNotNull(s1);
		assertNotNull(s1.discriminator);
		assertEquals("p", s1.discriminator.propertyName);
		assertEquals("d", s1.discriminator.defaultMapping);
		assertNotNull(s1.xml);
		assertEquals("n", s1.xml.name);
		assertEquals("ns", s1.xml.namespace);
		assertEquals("p", s1.xml.prefix);
		assertFalse(s1.xml.attribute);
		assertFalse(s1.xml.wrapped);
	}
}
