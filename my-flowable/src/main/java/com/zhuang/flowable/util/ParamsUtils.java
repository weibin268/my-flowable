package com.zhuang.flowable.util;

import java.util.HashMap;
import java.util.Map;

public class ParamsUtils {

    public static final String BUSINESS_DATA_KEY = "businessData";
    public static final String CHOICE_KEY = "choice";

    public static Map<String, Object> getVariables(Map<String, Object> params) {
        if (params == null) return null;
        Map result = new HashMap();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getKey().equals(BUSINESS_DATA_KEY)) continue;
        }
        return result;
    }

    public static String getBusinessData(Map<String, Object> params) {
        if (params == null) return null;
        Object o = params.get(BUSINESS_DATA_KEY);
        return o == null ? null : o.toString();
    }

    public static void setBusinessData(Map<String, Object> params, String businessData) {
        if (params == null) return;
        params.put(BUSINESS_DATA_KEY, businessData);
    }

    public static String getChoice(Map<String, Object> params) {
        Object objChoice = params.get(CHOICE_KEY);
        return objChoice == null ? "" : objChoice.toString();
    }

    public static void setChoice(Map<String, Object> params, String choice) {
        if (params == null) return;
        params.put(CHOICE_KEY, choice);
    }
}
