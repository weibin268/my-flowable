package com.zhuang.flowable.constant;

import java.util.HashMap;
import java.util.Map;

public class ProcessMainVariableNames {

    private static final String NAME_PREFIX = "_";

    public static final String PROC_TITLE = NAME_PREFIX + "procTitle";

    public static final String PROC_TYPE = NAME_PREFIX + "procType";

    public static final String PROC_DEF_KEY = NAME_PREFIX + "procDefKey";

    public static final String PROC_CREATE_TIME = NAME_PREFIX + "procCreateTime";

    public static final String PROC_CREATE_USER_ID = NAME_PREFIX + "procCreateUserId";

    public static final String PROC_CREATE_USER = NAME_PREFIX + "procCreateUser";

    public static final String PROC_BUSINESS_KEY = NAME_PREFIX + "procBusinessKey";

    public static Map<String, Object> getProcessVariables(Map<String, Object> params) {
        if (params == null) return null;
        Map result = new HashMap();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getKey().startsWith(NAME_PREFIX)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
