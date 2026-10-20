import static org.junit.Assert.*;

import arazzo.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Strongly typed unit tests for all Arazzo 1.1.0 AST POJO classes, enums, and
 * union wrappers.
 */
public class ArazzoPojoTest {
	/**
	 * Default constructor.
	 */
	public ArazzoPojoTest() {
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
		assertNotEquals(a, "string");
		assertNotNull(a.toString());
	}

	/**
	 * Verifies structural equality, hashCode, toString, and extension helpers
	 * across all 15 mutable Arazzo POJO classes explicitly without reflection.
	 */
	@Test
	public void testAllArazzoPojos() {
		Arazzo a1 = new Arazzo();
		Arazzo a2 = new Arazzo();
		Arazzo aDiff = new Arazzo();
		aDiff.$self = "https://example.com/arazzo.json";
		aDiff.addExtension("x-custom", "val");
		aDiff.addExtension("ignored", "val");
		assertEquals("val", aDiff.getExtensions().get("x-custom"));
		assertFalse(aDiff.getExtensions().containsKey("ignored"));
		assertPojoContract(a1, a2, aDiff);

		ArazzoInfo info1 = new ArazzoInfo();
		ArazzoInfo info2 = new ArazzoInfo();
		ArazzoInfo infoDiff = new ArazzoInfo();
		infoDiff.title = "Flow";
		infoDiff.addExtension("x-custom", "val");
		infoDiff.addExtension("ignored", "val");
		assertNotNull(infoDiff.getExtensions());
		assertPojoContract(info1, info2, infoDiff);

		SourceDescription sd1 = new SourceDescription();
		SourceDescription sd2 = new SourceDescription();
		SourceDescription sdDiff = new SourceDescription();
		sdDiff.name = "petstore";
		sdDiff.addExtension("x-custom", "val");
		sdDiff.addExtension("ignored", "val");
		assertNotNull(sdDiff.getExtensions());
		assertPojoContract(sd1, sd2, sdDiff);

		Workflow wf1 = new Workflow();
		Workflow wf2 = new Workflow();
		Workflow wfDiff = new Workflow();
		wfDiff.workflowId = "wf1";
		wfDiff.addExtension("x-custom", "val");
		wfDiff.addExtension("ignored", "val");
		assertNotNull(wfDiff.getExtensions());
		assertPojoContract(wf1, wf2, wfDiff);

		Step st1 = new Step();
		Step st2 = new Step();
		Step stDiff = new Step();
		stDiff.stepId = "st1";
		stDiff.addExtension("x-custom", "val");
		stDiff.addExtension("ignored", "val");
		assertNotNull(stDiff.getExtensions());
		assertPojoContract(st1, st2, stDiff);

		ReusableObject ro1 = new ReusableObject();
		ReusableObject ro2 = new ReusableObject();
		ReusableObject roDiff = new ReusableObject();
		roDiff.reference = "$components.parameters.id";
		assertPojoContract(ro1, ro2, roDiff);

		ArazzoParameter ap1 = new ArazzoParameter();
		ArazzoParameter ap2 = new ArazzoParameter();
		ArazzoParameter apDiff = new ArazzoParameter();
		apDiff.name = "petId";
		apDiff.addExtension("x-custom", "val");
		apDiff.addExtension("ignored", "val");
		assertNotNull(apDiff.getExtensions());
		assertPojoContract(ap1, ap2, apDiff);

		SuccessAction sa1 = new SuccessAction();
		SuccessAction sa2 = new SuccessAction();
		SuccessAction saDiff = new SuccessAction();
		saDiff.name = "done";
		saDiff.addExtension("x-custom", "val");
		saDiff.addExtension("ignored", "val");
		assertNotNull(saDiff.getExtensions());
		assertPojoContract(sa1, sa2, saDiff);

		FailureAction fa1 = new FailureAction();
		FailureAction fa2 = new FailureAction();
		FailureAction faDiff = new FailureAction();
		faDiff.name = "retry";
		faDiff.addExtension("x-custom", "val");
		faDiff.addExtension("ignored", "val");
		assertNotNull(faDiff.getExtensions());
		assertPojoContract(fa1, fa2, faDiff);

		ArazzoComponents comp1 = new ArazzoComponents();
		ArazzoComponents comp2 = new ArazzoComponents();
		ArazzoComponents compDiff = new ArazzoComponents();
		compDiff.addExtension("x-custom", "val");
		compDiff.addExtension("ignored", "val");
		assertNotNull(compDiff.getExtensions());
		assertPojoContract(comp1, comp2, compDiff);

		ExpressionType et1 = new ExpressionType();
		ExpressionType et2 = new ExpressionType();
		ExpressionType etDiff = new ExpressionType();
		etDiff.version = "rfc9535";
		etDiff.addExtension("x-custom", "val");
		etDiff.addExtension("ignored", "val");
		assertNotNull(etDiff.getExtensions());
		assertPojoContract(et1, et2, etDiff);

		Criterion c1 = new Criterion();
		Criterion c2 = new Criterion();
		Criterion cDiff = new Criterion();
		cDiff.condition = "$statusCode == 200";
		cDiff.addExtension("x-custom", "val");
		cDiff.addExtension("ignored", "val");
		assertNotNull(cDiff.getExtensions());
		assertPojoContract(c1, c2, cDiff);

		Selector sel1 = new Selector();
		Selector sel2 = new Selector();
		Selector selDiff = new Selector();
		selDiff.selector = "$.id";
		selDiff.addExtension("x-custom", "val");
		selDiff.addExtension("ignored", "val");
		assertNotNull(selDiff.getExtensions());
		assertPojoContract(sel1, sel2, selDiff);

		PayloadReplacement pr1 = new PayloadReplacement();
		PayloadReplacement pr2 = new PayloadReplacement();
		PayloadReplacement prDiff = new PayloadReplacement();
		prDiff.target = "/id";
		prDiff.addExtension("x-custom", "val");
		prDiff.addExtension("ignored", "val");
		assertNotNull(prDiff.getExtensions());
		assertPojoContract(pr1, pr2, prDiff);

		ArazzoRequestBody rb1 = new ArazzoRequestBody();
		ArazzoRequestBody rb2 = new ArazzoRequestBody();
		ArazzoRequestBody rbDiff = new ArazzoRequestBody();
		rbDiff.contentType = "application/json";
		rbDiff.addExtension("x-custom", "val");
		rbDiff.addExtension("ignored", "val");
		assertNotNull(rbDiff.getExtensions());
		assertPojoContract(rb1, rb2, rbDiff);
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
