import org.junit.Test;
import cli.Parse;
import openapi.*;

public class ParseBranchTest {
	@Test
	public void testParseBranches() {
		// Line 119: Server Object url: (parts.length == 0 is impossible with split(" ")
		// but let's try just "Server Object url: " or similar)
		// Let's test basic paths
		String cli = "public class T {\n" + "  private static void printHelp() {\n"
				+ "    System.out.println(\"Server Object url: \");\n"
				+ "    System.out.println(\"Server Variable Object noServers\");\n" + // 138: api.servers == null /
																						// empty
				"    System.out.println(\"Component schemas S1\");\n"
				+ "    System.out.println(\"  Discriminator propertyName= mapping= defaultMapping=\");\n"
				+ "    System.out.println(\"  Discriminator propertyName=null mapping=null defaultMapping=null\");\n"
				+ "    System.out.println(\"  Discriminator propertyName=p mapping=a defaultMapping=d\");\n" + // malformed
																												// mapping
																												// a (no
																												// '=')
				"    System.out.println(\"  XML name= namespace= prefix= attribute= wrapped=\");\n"
				+ "    System.out.println(\"  XML name=null namespace=null prefix=null attribute=null wrapped=null\");\n"
				+ "    System.out.println(\"  XML name=n namespace=ns prefix=p attribute=false wrapped=false\");\n"
				+ "  }\n" + "}";
		Parse.parse(cli);
	}
}
