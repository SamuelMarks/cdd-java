package functions;

import openapi.OpenAPI;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * Emits functions to language source preserving lexical layout.
 */
public class Emit {
    /** Default constructor. */
    public Emit() {}

    /**
     * Emits Java code for standalone functions.
     * @param model The OpenAPI model.
     * @param existingSource Existing Java code to preserve formatting, or null if new.
     * @return Generated Java source.
     */
    public static String emit(OpenAPI model, String existingSource) {
        if (existingSource != null && !existingSource.trim().isEmpty()) {
            CompilationUnit cu = StaticJavaParser.parse(existingSource);
            LexicalPreservingPrinter.setup(cu);
            return LexicalPreservingPrinter.print(cu);
        } else {
            return "// Helper Functions Generated\n";
        }
    }
}
