package com.zhuang.flowable.util;

import java.util.Date;
import java.util.Map;

public class MapUtils {

    public static String getString(Map<String, Object> map, String key) {
        Object value = getObject(map,key);
        return value == null ? null : (String) value;
    }

    public static Date getDate(Map<String, Object> map, String key) {
        Object value = getObject(map,key);
        return value == null ? null : (Date) value;
    }

    public static Object getObject(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }

}
