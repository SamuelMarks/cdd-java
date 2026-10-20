package common;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * Shared JavaParser AST and {@link LexicalPreservingPrinter} merge utilities
 * used across code emitters ({@code classes}, {@code orm}, {@code dao},
 * {@code seeder}, {@code routes}, {@code mocks}, {@code tests},
 * {@code functions}, and {@code workflows}).
 */
public final class AstMergeUtils {
	/**
	 * Private constructor to prevent instantiation.
	 */
	private AstMergeUtils() {
	}

	/**
	 * Parses an existing Java source string and configures
	 * {@link LexicalPreservingPrinter} on the resulting {@link CompilationUnit}.
	 *
	 * @param existingSource
	 *            The existing Java source code.
	 * @return The parsed and lexical-preserving {@link CompilationUnit}.
	 */
	public static CompilationUnit parseLexicalPreserving(String existingSource) {
		CompilationUnit cu = StaticJavaParser.parse(existingSource);
		LexicalPreservingPrinter.setup(cu);
		return cu;
	}

	/**
	 * Checks whether the given class declaration already contains a method with
	 * {@code methodName}.
	 *
	 * @param classDecl
	 *            The {@link ClassOrInterfaceDeclaration} to inspect.
	 * @param methodName
	 *            The method name to look for.
	 * @return {@code true} if a method with {@code methodName} exists;
	 *         {@code false} otherwise.
	 */
	public static boolean hasMethod(ClassOrInterfaceDeclaration classDecl, String methodName) {
		for (MethodDeclaration m : classDecl.getMethods()) {
			if (m.getNameAsString().equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given class declaration already contains a field with
	 * {@code fieldName}.
	 *
	 * @param classDecl
	 *            The {@link ClassOrInterfaceDeclaration} to inspect.
	 * @param fieldName
	 *            The field name to look for.
	 * @return {@code true} if a field with {@code fieldName} exists; {@code false}
	 *         otherwise.
	 */
	public static boolean hasField(ClassOrInterfaceDeclaration classDecl, String fieldName) {
		return classDecl.getFieldByName(fieldName).isPresent();
	}
}
