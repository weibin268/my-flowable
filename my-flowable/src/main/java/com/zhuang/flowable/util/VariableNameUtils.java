package com.zhuang.flowable.util;

public class VariableNameUtils {

    private static final String START_SUFFIX = "_START";
    private static final String END_SUFFIX = "_END";

    public static String toStartName(String name) {
        return name + START_SUFFIX;
    }

    public static String toEndName(String name) {
        return name + END_SUFFIX;
    }
}
