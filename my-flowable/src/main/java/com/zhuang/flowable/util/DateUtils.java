package com.zhuang.flowable.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhuang on 10/5/2017.
 */
public class DateUtils {

    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    public static Date parseDate(String date) {
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("error parseDate !", e);
        }
    }

    public static long parseTimeMillis(String date) {
        return parseDate(date).getTime();
    }

    public static String parseString(Date date) {
        return simpleDateFormat.format(date);
    }

}
