package com.zhuang.flowable.handler;

public class MyHandlerTag {

    private static final String HANDLER_TAG_PREFIX = "$";
    private static final String KEY_VALUE_SEPARATOR = ":";

    private String key;
    private String value;

    public MyHandlerTag(String tag) {
        String[] tagArray = tag.split(KEY_VALUE_SEPARATOR);
        key = tagArray[0];
        value = tagArray.length > 1 ? tagArray[1] : null;
    }

    public static boolean isMyHandlerTag(String tag) {
        return tag.startsWith(HANDLER_TAG_PREFIX);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
