package routes;

import java.util.Map;
import openapi.OpenAPI;
import openapi.Operation;
import openapi.PathItem;

/**
 * Serializes OpenAPI route, operation, parameter, requestBody, and response
 * metadata into Javadoc tags for {@link Emit}.
 */
public class RouteJavadocEmitter {

	/**
	 * Default constructor.
	 */
	public RouteJavadocEmitter() {
	}

	/**
	 * Checks whether a string is non-null and non-empty.
	 *
	 * @param s
	 *            The string to inspect.
	 * @return {@code true} if non-null and non-empty.
	 */
	public static boolean isNonEmpty(String s) {
		return s != null && !s.isEmpty();
	}

	/**
	 * Builds the class-level Javadoc comment block for a generated client class.
	 *
	 * @param title
	 *            The sanitized client title.
	 * @param model
	 *            The root {@link OpenAPI} model.
	 * @return The formatted class-level Javadoc comment string.
	 */
	public static String buildClassJavadoc(String title, OpenAPI model) {
		StringBuilder classDoc = new StringBuilder();
		classDoc.append("/**\n * Auto-generated client for ").append(title).append(".\n");
		if (model.openapi != null)
			classDoc.append(" * @openapiVersion ").append(model.openapi).append("\n");
		if (model.$self != null)
			classDoc.append(" * @openapiSelf ").append(model.$self).append("\n");
		if (model.jsonSchemaDialect != null)
			classDoc.append(" * @jsonSchemaDialect ").append(model.jsonSchemaDialect).append("\n");
		classDoc.append(" */\n");
		return classDoc.toString();
	}

	/**
	 * Appends operation-level Javadoc tags ({@code @callback}, {@code @pathRef},
	 * {@code @pathSummary}, {@code @pathDescription}, {@code @requestBody*},
	 * {@code @response*}, {@code @return}, and {@code @throws}) to a method Javadoc
	 * builder.
	 *
	 * @param docBuilder
	 *            The target Javadoc {@link StringBuilder}.
	 * @param path
	 *            The URL path template.
	 * @param op
	 *            The {@link Operation} definition.
	 * @param item
	 *            The parent {@link PathItem}.
	 */
	public static void appendOperationJavadocTags(StringBuilder docBuilder, String path, Operation op, PathItem item) {
		if (op.callbacks != null && !op.callbacks.isEmpty()) {
			for (Map.Entry<String, openapi.ReferenceOr<openapi.Callback>> cbEntry : op.callbacks.entrySet()) {
				openapi.Callback cb = cbEntry.getValue().value;
				for (Map.Entry<String, PathItem> piEntry : cb.pathItems.entrySet()) {
					String method = "POST";
					if (item.get != null)
						method = "GET";
					else if (item.put != null)
						method = "PUT";
					else if (item.delete != null)
						method = "DELETE";
					else if (item.patch != null)
						method = "PATCH";
					else if (item.query != null)
						method = "QUERY";
					docBuilder.append(" * @callback ").append(cbEntry.getKey()).append(" ").append(piEntry.getKey())
							.append(" ").append(method).append("\n");
				}
			}
		}

		if (isNonEmpty(item.ref)) {
			docBuilder.append(" * @pathRef ").append(path).append(" ").append(item.ref).append("\n");
		}
		if (isNonEmpty(item.summary)) {
			docBuilder.append(" * @pathSummary ").append(path).append(" ").append(item.summary.replace("\n", " "))
					.append("\n");
		}
		if (isNonEmpty(item.description)) {
			docBuilder.append(" * @pathDescription ").append(path).append(" ")
					.append(item.description.replace("\n", " ")).append("\n");
		}

		if (op.requestBody != null && op.requestBody.value != null) {
			openapi.RequestBody rb = op.requestBody.value;
			appendMediaTypeJavadocTags(docBuilder, "requestBody", "", rb.content);
		}

		if (op.responses != null && op.responses.statusCodes != null) {
			for (java.util.Map.Entry<String, openapi.ReferenceOr<openapi.Response>> rEntry : op.responses.statusCodes
					.entrySet()) {
				String statusCode = rEntry.getKey();
				openapi.Response r = rEntry.getValue().value;
				if (r != null) {
					appendMediaTypeJavadocTags(docBuilder, "response", statusCode + " ", r.content);
				}
			}
		}

		if (op.responses != null && op.responses.defaultResponse != null) {
			openapi.Response r = op.responses.defaultResponse.value;
			appendMediaTypeJavadocTags(docBuilder, "response", "default ", r.content);
		}

		docBuilder.append(" * @return HttpResponse\n");
		docBuilder.append(" * @throws IOException on error\n");
		docBuilder.append(" * @throws InterruptedException on error\n");
		docBuilder.append(" */\n");
	}

	/**
	 * Appends Javadoc tags describing a map of {@link openapi.MediaType} entries
	 * for either a request body or a response.
	 *
	 * @param docBuilder
	 *            The target Javadoc {@link StringBuilder}.
	 * @param tagPrefix
	 *            Either {@code "requestBody"} or {@code "response"}.
	 * @param statusPrefix
	 *            Empty string for request bodies, or {@code "<statusCode> "} for
	 *            responses.
	 * @param contentMap
	 *            The media type map to serialize into Javadoc tags.
	 */
	public static void appendMediaTypeJavadocTags(StringBuilder docBuilder, String tagPrefix, String statusPrefix,
			Map<String, openapi.MediaType> contentMap) {
		if (contentMap == null) {
			return;
		}
		for (java.util.Map.Entry<String, openapi.MediaType> mEntry : contentMap.entrySet()) {
			String mtStr = mEntry.getKey();
			openapi.MediaType mt = mEntry.getValue();
			docBuilder.append(" * @").append(tagPrefix).append("Content ").append(statusPrefix).append(mtStr)
					.append("\n");
			if (mt.schema != null && mt.schema.value.type != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentSchema ").append(statusPrefix).append(mtStr)
						.append(" ").append(mt.schema.value.type.single).append("\n");
			}
			if (mt.itemSchema != null && mt.itemSchema.value.type != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentItemSchema ").append(statusPrefix)
						.append(mtStr).append(" ").append(mt.itemSchema.value.type.single).append("\n");
			}
			if (mt.prefixEncoding != null) {
				for (openapi.Encoding enc : mt.prefixEncoding) {
					if (enc.contentType != null) {
						docBuilder.append(" * @").append(tagPrefix).append("ContentPrefixEncoding ")
								.append(statusPrefix).append(mtStr).append(" ").append(enc.contentType).append("\n");
					}
				}
			}
			if (mt.itemEncoding != null && mt.itemEncoding.contentType != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentItemEncoding ").append(statusPrefix)
						.append(mtStr).append(" ").append(mt.itemEncoding.contentType).append("\n");
			}
			if (mt.encoding != null) {
				for (java.util.Map.Entry<String, openapi.Encoding> encEntry : mt.encoding.entrySet()) {
					String encKey = encEntry.getKey();
					openapi.Encoding enc = encEntry.getValue();
					if (enc.contentType != null) {
						docBuilder.append(" * @").append(tagPrefix).append("Encoding ").append(statusPrefix)
								.append(mtStr).append(" ").append(encKey).append(" ").append(enc.contentType)
								.append("\n");
					}
					if (enc.prefixEncoding != null) {
						for (openapi.Encoding pEnc : enc.prefixEncoding) {
							if (pEnc.contentType != null) {
								docBuilder.append(" * @").append(tagPrefix).append("EncodingPrefixEncoding ")
										.append(statusPrefix).append(mtStr).append(" ").append(encKey).append(" ")
										.append(pEnc.contentType).append("\n");
							}
						}
					}
					if (enc.itemEncoding != null && enc.itemEncoding.contentType != null) {
						docBuilder.append(" * @").append(tagPrefix).append("EncodingItemEncoding ").append(statusPrefix)
								.append(mtStr).append(" ").append(encKey).append(" ")
								.append(enc.itemEncoding.contentType).append("\n");
					}
				}
			}
			if (mt.example != null) {
				docBuilder.append(" * @").append(tagPrefix).append("ContentExample ").append(statusPrefix).append(mtStr)
						.append(" ").append(mt.example).append("\n");
			}
			if (mt.examples != null) {
				for (java.util.Map.Entry<String, openapi.ReferenceOr<openapi.Example>> exEntry : mt.examples
						.entrySet()) {
					openapi.Example ex = exEntry.getValue().value;
					String exValue = ex.value != null ? ex.value.toString() : "";
					String exSummary = ex.summary != null ? ex.summary : "";
					String exDesc = ex.description != null ? ex.description : "";
					docBuilder.append(" * @").append(tagPrefix).append("ContentExamples ").append(statusPrefix)
							.append(mtStr).append(" ").append(exEntry.getKey()).append(" ")
							.append(exSummary.replace(" ", "_")).append("|").append(exDesc.replace(" ", "_"))
							.append("|").append(exValue).append("\n");
				}
			}
		}
	}
}
