package com.zhuang.flowable.util;

import java.util.HashMap;
import java.util.Map;

public class ParamsUtils {

    private static final String BUSINESS_DATA_KEY = "_businessData";

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

}
