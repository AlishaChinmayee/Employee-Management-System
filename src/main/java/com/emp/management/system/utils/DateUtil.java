package com.emp.management.system.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static Date convertStringToDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
            sdf.setLenient(false);
            Date ret = sdf.parse(dateStr.trim());
            if (sdf.format(ret).equals(dateStr.trim()))
                return ret;
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    
    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
        return dateFormat.format(date);
    }

    public static boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
            sdf.setLenient(false);
            Date ret = sdf.parse(dateStr.trim());
            return sdf.format(ret).equals(dateStr.trim());
        } catch (Exception e) {
            return false;
        }
    }
}
