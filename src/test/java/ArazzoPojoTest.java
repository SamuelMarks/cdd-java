import static org.junit.Assert.*;

import arazzo.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Unit tests for all Arazzo 1.1.0 AST POJO classes, enums, and union wrappers.
 */
public class ArazzoPojoTest {
	/**
	 * Default constructor.
	 */
	public ArazzoPojoTest() {
	}

	/**
	 * Verifies structural equality, hashCode, toString, and extension helpers
	 * across all mutable Arazzo POJO classes.
	 *
	 * @throws Exception
	 *             If reflection or instantiation fails.
	 */
	@Test
	public void testAllArazzoPojos() throws Exception {
		Class<?>[] pojoClasses = new Class<?>[]{Arazzo.class, ArazzoInfo.class, SourceDescription.class, Workflow.class,
				Step.class, ReusableObject.class, ArazzoParameter.class, SuccessAction.class, FailureAction.class,
				ArazzoComponents.class, ExpressionType.class, Criterion.class, Selector.class, PayloadReplacement.class,
				ArazzoRequestBody.class};

		for (Class<?> clazz : pojoClasses) {
			Object inst1 = clazz.getDeclaredConstructor().newInstance();
			Object inst2 = clazz.getDeclaredConstructor().newInstance();
			assertEquals(inst1, inst1);
			assertEquals(inst1, inst2);
			assertEquals(inst1.hashCode(), inst2.hashCode());
			assertNotEquals(inst1, null);
			assertNotEquals(inst1, "string");
			assertNotNull(inst1.toString());

			try {
				java.lang.reflect.Method addExt = clazz.getMethod("addExtension", String.class, Object.class);
				java.lang.reflect.Method getExt = clazz.getMethod("getExtensions");
				addExt.invoke(inst1, "x-custom", "val");
				addExt.invoke(inst1, "ignored", "val");
				Map<?, ?> extMap = (Map<?, ?>) getExt.invoke(inst1);
				assertEquals("val", extMap.get("x-custom"));
				assertFalse(extMap.containsKey("ignored"));
				assertNotEquals(inst1, inst2);
				addExt.invoke(inst2, "x-custom", "val");
				assertEquals(inst1, inst2);
			} catch (NoSuchMethodException ignored) {
				// ReusableObject does not have extensions
			}

			for (Field f : clazz.getFields()) {
				if (f.getType() == String.class) {
					f.set(inst1, "diff");
					assertNotEquals(inst1, inst2);
					f.set(inst2, "diff");
					assertEquals(inst1, inst2);
					break;
				}
			}
		}
	}

	/**
	 * Verifies all Arazzo 1.1.0 enums ({@link SourceType}, {@link StepActionType},
	 * {@link ParameterIn}, {@link SuccessActionType}, {@link FailureActionType},
	 * and {@link CriterionEngineType}).
	 */
	@Test
	public void testAllEnums() {
		assertEquals(SourceType.OPENAPI, SourceType.fromString("openapi"));
		assertEquals(SourceType.ASYNCAPI, SourceType.fromString("ASYNCAPI"));
		assertEquals(SourceType.ARAZZO, SourceType.fromString("arazzo"));
		assertEquals("openapi", SourceType.OPENAPI.getValue());
		assertNull(SourceType.fromString(null));
		assertNull(SourceType.fromString("unknown"));

		assertEquals(StepActionType.SEND, StepActionType.fromString("send"));
		assertEquals(StepActionType.RECEIVE, StepActionType.fromString("RECEIVE"));
		assertEquals("send", StepActionType.SEND.getValue());
		assertNull(StepActionType.fromString(null));
		assertNull(StepActionType.fromString("unknown"));

		assertEquals(ParameterIn.PATH, ParameterIn.fromString("path"));
		assertEquals(ParameterIn.QUERY, ParameterIn.fromString("query"));
		assertEquals(ParameterIn.QUERYSTRING, ParameterIn.fromString("querystring"));
		assertEquals(ParameterIn.HEADER, ParameterIn.fromString("header"));
		assertEquals(ParameterIn.COOKIE, ParameterIn.fromString("cookie"));
		assertEquals("querystring", ParameterIn.QUERYSTRING.getValue());
		assertNull(ParameterIn.fromString(null));
		assertNull(ParameterIn.fromString("body"));

		assertEquals(SuccessActionType.END, SuccessActionType.fromString("end"));
		assertEquals(SuccessActionType.GOTO, SuccessActionType.fromString("goto"));
		assertEquals("goto", SuccessActionType.GOTO.getValue());
		assertNull(SuccessActionType.fromString(null));
		assertNull(SuccessActionType.fromString("retry"));

		assertEquals(FailureActionType.END, FailureActionType.fromString("end"));
		assertEquals(FailureActionType.RETRY, FailureActionType.fromString("retry"));
		assertEquals(FailureActionType.GOTO, FailureActionType.fromString("goto"));
		assertEquals("retry", FailureActionType.RETRY.getValue());
		assertNull(FailureActionType.fromString(null));
		assertNull(FailureActionType.fromString("invalid"));

		assertEquals(CriterionEngineType.SIMPLE, CriterionEngineType.fromString("simple"));
		assertEquals(CriterionEngineType.REGEX, CriterionEngineType.fromString("regex"));
		assertEquals(CriterionEngineType.JSONPATH, CriterionEngineType.fromString("jsonpath"));
		assertEquals(CriterionEngineType.XPATH, CriterionEngineType.fromString("xpath"));
		assertEquals(CriterionEngineType.JSONPOINTER, CriterionEngineType.fromString("jsonpointer"));
		assertEquals("jsonpointer", CriterionEngineType.JSONPOINTER.getValue());
		assertNull(CriterionEngineType.fromString(null));
		assertNull(CriterionEngineType.fromString("sql"));
	}

	/**
	 * Verifies {@link Step#hasValidSingleTarget()} mutual exclusivity checks.
	 */
	@Test
	public void testStepSingleTargetValidation() {
		Step step = new Step();
		assertFalse(step.hasValidSingleTarget());
		step.operationId = "";
		step.operationPath = "";
		step.channelPath = "";
		step.workflowId = "";
		assertFalse(step.hasValidSingleTarget());

		step.operationId = "getPet";
		assertTrue(step.hasValidSingleTarget());

		step.operationPath = "#/paths/~1pet/get";
		assertFalse(step.hasValidSingleTarget());

		step.operationId = null;
		assertTrue(step.hasValidSingleTarget());

		step.operationPath = null;
		step.channelPath = "/pets/created";
		assertTrue(step.hasValidSingleTarget());

		step.channelPath = null;
		step.workflowId = "subWf";
		assertTrue(step.hasValidSingleTarget());
	}

	/**
	 * Verifies {@link PayloadValue}, {@link ReusableOr},
	 * {@link CriterionTypeOrExpression}, and {@link OutputValue} union wrappers.
	 */
	@Test
	public void testUnionWrappers() {
		PayloadValue pvNull = PayloadValue.ofNull();
		assertTrue(pvNull.isNull());
		assertFalse(pvNull.isString());
		assertEquals("null", pvNull.toString());
		assertEquals(pvNull.hashCode(), PayloadValue.ofNull().hashCode());
		assertEquals(pvNull, PayloadValue.ofString(null));
		assertEquals(pvNull, PayloadValue.ofNumber(null));
		assertEquals(pvNull, PayloadValue.ofBoolean(null));
		assertEquals(pvNull, PayloadValue.ofObject(null));
		assertEquals(pvNull, PayloadValue.ofArray(null));

		PayloadValue pvStr = PayloadValue.ofString("$inputs.id");
		assertTrue(pvStr.isString());
		assertEquals("$inputs.id", pvStr.toString());
		assertEquals(pvStr, pvStr);
		assertNotEquals(pvStr, "raw");
		assertNotEquals(pvStr, pvNull);
		assertNotEquals(pvStr, PayloadValue.ofString("other"));

		PayloadValue pvNum1 = PayloadValue.ofNumber(42);
		PayloadValue pvNum2 = PayloadValue.ofNumber(42.0);
		assertTrue(pvNum1.isNumber());
		assertEquals(pvNum1, pvNum2);
		assertEquals(pvNum1.hashCode(), pvNum2.hashCode());
		assertEquals("42", pvNum1.toString());
		assertNotEquals(pvNum1, PayloadValue.ofNumber(99));
		assertNotEquals(pvNum1, new PayloadValue(PayloadValue.Kind.NUMBER, null, null, null, null, null));
		assertNotEquals(new PayloadValue(PayloadValue.Kind.NUMBER, null, null, null, null, null), pvNum1);

		PayloadValue pvBool = PayloadValue.ofBoolean(true);
		assertTrue(pvBool.isBoolean());
		assertEquals("true", pvBool.toString());
		assertNotEquals(pvBool, PayloadValue.ofBoolean(false));

		Map<String, PayloadValue> map = new LinkedHashMap<>();
		map.put("k", pvStr);
		PayloadValue pvObj = PayloadValue.ofObject(map);
		assertTrue(pvObj.isObject());
		assertTrue(pvObj.toString().contains("k="));
		assertNotEquals(pvObj, PayloadValue.ofObject(Collections.emptyMap()));

		PayloadValue pvArr = PayloadValue.ofArray(Arrays.asList(pvStr, pvNum1));
		assertTrue(pvArr.isArray());
		assertTrue(pvArr.toString().contains("$inputs.id"));
		assertNotEquals(pvArr, PayloadValue.ofArray(Collections.emptyList()));

		ArazzoParameter param = new ArazzoParameter();
		param.name = "id";
		ReusableOr<ArazzoParameter> rItem = ReusableOr.ofItem(param);
		ReusableOr<ArazzoParameter> rRef = ReusableOr.ofReference("$components.parameters.id");
		ReusableObject ro = new ReusableObject();
		ro.reference = "$components.parameters.id";
		ReusableOr<ArazzoParameter> rRef2 = ReusableOr.ofReusable(ro);
		assertTrue(rItem.isItem());
		assertFalse(rItem.isReusable());
		assertEquals(param, rItem.getItem());
		assertNull(rItem.getReusable());
		assertTrue(rRef.isReusable());
		assertFalse(rRef.isItem());
		assertEquals(ro, rRef.getReusable());
		assertEquals(rRef, rRef2);
		assertEquals(rItem, ReusableOr.ofItem(param));
		assertNotEquals(rItem, ReusableOr.ofReusable(ro));
		assertNotEquals(rRef, ReusableOr.ofReference("$components.parameters.other"));
		assertEquals(rRef, rRef);
		assertNotEquals(rRef, "str");
		assertNotEquals(rRef, rItem);
		assertEquals(rRef.hashCode(), rRef2.hashCode());
		assertTrue(rRef.toString().contains("Reusable("));
		assertTrue(rItem.toString().contains("Item("));

		CriterionTypeOrExpression cteSimple = CriterionTypeOrExpression.ofSimple(CriterionEngineType.REGEX);
		ExpressionType et = new ExpressionType();
		et.type = CriterionEngineType.JSONPATH;
		et.version = "rfc9535";
		CriterionTypeOrExpression cteExpr = CriterionTypeOrExpression.ofExpressionType(et);
		CriterionTypeOrExpression cteEmpty = new CriterionTypeOrExpression(null, new ExpressionType());
		CriterionTypeOrExpression cteNull = new CriterionTypeOrExpression(null, null);
		assertTrue(cteSimple.isSimple());
		assertFalse(cteSimple.isExpressionType());
		assertEquals(CriterionEngineType.REGEX, cteSimple.resolveEngineType());
		assertTrue(cteExpr.isExpressionType());
		assertFalse(cteExpr.isSimple());
		assertEquals(CriterionEngineType.JSONPATH, cteExpr.resolveEngineType());
		assertEquals(CriterionEngineType.SIMPLE, cteEmpty.resolveEngineType());
		assertEquals(CriterionEngineType.SIMPLE, cteNull.resolveEngineType());
		assertEquals(cteSimple, cteSimple);
		assertNotEquals(cteSimple, "str");
		assertNotEquals(cteSimple, cteExpr);
		assertNotEquals(cteExpr, cteEmpty);
		assertEquals(cteExpr, CriterionTypeOrExpression.ofExpressionType(et));
		assertEquals(cteExpr.hashCode(), CriterionTypeOrExpression.ofExpressionType(et).hashCode());
		assertNotNull(cteSimple.toString());
		assertNotNull(cteExpr.toString());

		OutputValue ovExpr = OutputValue.ofExpression("$response.body#/id");
		Selector sel = new Selector();
		sel.context = "$response.body";
		sel.selector = "$.id";
		OutputValue ovSel = OutputValue.ofSelector(sel);
		assertTrue(ovExpr.isExpression());
		assertFalse(ovExpr.isSelector());
		assertTrue(ovSel.isSelector());
		assertFalse(ovSel.isExpression());
		assertEquals(ovExpr, ovExpr);
		assertEquals(ovExpr, OutputValue.ofExpression("$response.body#/id"));
		assertNotEquals(ovExpr, "str");
		assertNotEquals(ovExpr, ovSel);
		assertNotEquals(ovSel, OutputValue.ofSelector(new Selector()));
		assertEquals(ovSel, OutputValue.ofSelector(sel));
		assertEquals(ovSel.hashCode(), OutputValue.ofSelector(sel).hashCode());
		assertEquals("$response.body#/id", ovExpr.toString());
		assertNotNull(ovSel.toString());
	}
}
