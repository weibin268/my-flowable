package com.zhuang.flowable.util;

import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;

import java.util.Map;
import java.util.Map.Entry;

public class FlowableJuelUtils {

	public static boolean evaluateBooleanResult(String expression, Map<String, Object> params) {
		ExpressionFactory factory = new ExpressionFactoryImpl();
		SimpleContext context = new SimpleContext();
		for (Entry<String, Object> entry : params.entrySet()) {
			if (expression.contains(entry.getKey())) {
				context.setVariable(entry.getKey(), factory.createValueExpression(entry.getValue(), entry.getValue().getClass()));
			}
		}
		ValueExpression e = factory.createValueExpression(context, expression, boolean.class);
		return (Boolean) e.getValue(context);
	}

}
