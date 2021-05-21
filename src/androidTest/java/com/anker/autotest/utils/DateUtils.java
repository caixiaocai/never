package com.anker.autotest.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat ("yyyy-MM-dd-HH-mm-ss");

    public static String getDateTime() {
        Date date = new Date();
        return dateTimeFormat.format(date);
    }

    private static SimpleDateFormat simpleDateTimeFormat =
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.SIMPLIFIED_CHINESE);

    public static String getSystemTime() {
        Date date = new Date();
        return simpleDateTimeFormat.format(date);
    }

    public static String getSystemTime(Date date){
        return simpleDateTimeFormat.format(date);
    }

    public static String getSystemTime(long date){
        return simpleDateTimeFormat.format(date);
    }
}
