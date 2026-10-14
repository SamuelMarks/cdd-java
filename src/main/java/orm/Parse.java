package orm;

import openapi.OpenAPI;
import openapi.Schema;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses JPA/Hibernate entities to OpenAPI representation.
 */
public class Parse {
	/** Default constructor. */
	public Parse() {
	}

	/**
	 * Parses Java source files into an OpenAPI model.
	 *
	 * @param sourceCode
	 *            The Java source.
	 * @return The parsed OpenAPI object.
	 */
	public static OpenAPI parse(String sourceCode) {
		OpenAPI api = new OpenAPI();
		api.openapi = "3.2.0";
		api.components = new openapi.Components();
		api.components.schemas = new HashMap<>();

		try {
			CompilationUnit cu = StaticJavaParser.parse(sourceCode);

			for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
				if (classDecl.isInterface() || !classDecl.getAnnotationByName("Entity").isPresent()) {
					continue;
				}

				String className = classDecl.getNameAsString();
				Schema schema = new Schema();
				schema.type = "object";

				classDecl.getAnnotationByName("Table").ifPresent(ann -> {
					if (ann instanceof NormalAnnotationExpr) {
						for (MemberValuePair mvp : ((NormalAnnotationExpr) ann).getPairs()) {
							if (mvp.getNameAsString().equals("name")) {
								schema.addExtension("x-table-name", mvp.getValue().toString().replace("\"", ""));
							}
						}
					}
				});

				Map<String, Object> properties = new HashMap<>();
				java.util.List<String> requiredList = new java.util.ArrayList<>();
				for (FieldDeclaration fieldDecl : classDecl.getFields()) {
					for (VariableDeclarator varDecl : fieldDecl.getVariables()) {
						Type type = varDecl.getType();
						String name = varDecl.getNameAsString();
						boolean isNullable = true;

						for (AnnotationExpr ann : fieldDecl.getAnnotations()) {
							String annName = ann.getNameAsString();
							if ((annName.equals("Column") || annName.equals("JoinColumn"))
									&& ann instanceof NormalAnnotationExpr) {
								for (MemberValuePair mvp : ((NormalAnnotationExpr) ann).getPairs()) {
									if (mvp.getNameAsString().equals("name")) {
										String colName = mvp.getValue().toString().replace("\"", "");
										if (annName.equals("JoinColumn") && colName.endsWith("_id")) {
											name = colName.substring(0, colName.length() - 3);
										} else {
											name = colName;
										}
									} else if (mvp.getNameAsString().equals("nullable")
											&& "false".equals(mvp.getValue().toString())) {
										isNullable = false;
									}
								}
							}
						}

						Schema propSchema = new Schema();
						resolveType(type, propSchema);

						if (fieldDecl.getAnnotationByName("Id").isPresent()) {
							propSchema.addExtension("x-primary-key", true);
						}
						if (fieldDecl.getAnnotationByName("GeneratedValue").isPresent()) {
							propSchema.addExtension("x-generated-value", true);
						}
						if (fieldDecl.getAnnotationByName("ManyToOne").isPresent()) {
							propSchema.addExtension("x-relationship", "many-to-one");
						}
						if (fieldDecl.getAnnotationByName("OneToMany").isPresent()) {
							propSchema.addExtension("x-relationship", "one-to-many");
						}
						if (!isNullable) {
							requiredList.add(name);
						}

						properties.put(name, propSchema);
					}
				}
				schema.properties = properties;
				if (!requiredList.isEmpty()) {
					schema.required = requiredList;
				}
				api.components.schemas.put(className, schema);
			}
		} catch (Exception e) {
			java.util.logging.Logger.getLogger(Parse.class.getName())
					.fine("Skipping unparseable ORM source: " + e.getMessage());
		}
		return api;
	}

	/**
	 * Maps a Java AST {@link Type} from an ORM entity into its corresponding
	 * OpenAPI {@link Schema}.
	 *
	 * @param type
	 *            The JavaParser AST {@link Type}.
	 * @param propSchema
	 *            The target OpenAPI {@link Schema} to populate.
	 */
	private static void resolveType(Type type, Schema propSchema) {
		openapi.TypeMapper.toSchema(type, propSchema, true);
	}
}
