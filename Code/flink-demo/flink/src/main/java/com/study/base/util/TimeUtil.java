package com.study.base.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    /**
     * 时间戳转 String
     * @param timestamp
     * @return
     */
    public static String toString(long timestamp) {
        return sdf.format(new Date(timestamp));
    }
}
