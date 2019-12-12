package com.pheuture.playlists.utils;

import android.content.Context;

import com.pheuture.playlists.R;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConstants;

public class CalenderUtils {

    public static boolean isSameYear(Calendar calendar){
        Calendar calendarCurrent=Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR);
    }

    public static boolean isSameMonth(Calendar calendar){
        Calendar calendarCurrent=Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == calendarCurrent.get(Calendar.MONTH);
    }

    public static boolean isYesterday(Calendar calendar){
        Calendar calendarYesterday=Calendar.getInstance();
        calendarYesterday.add(Calendar.DAY_OF_MONTH,-1);
        return calendar.get(Calendar.YEAR) == calendarYesterday.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == calendarYesterday.get(Calendar.MONTH)
                && calendar.get(Calendar.DAY_OF_MONTH) == calendarYesterday.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isSameDay(Calendar calendar){
        Calendar calendarToday=Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH)
                && calendar.get(Calendar.DAY_OF_MONTH) == calendarToday.get(Calendar.DAY_OF_MONTH);
    }

    public static String getFormattedDate(Calendar calendar){
        if (isSameYear(calendar)){
            return calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH,
                    Calendar.SHORT, Locale.getDefault()).toUpperCase();

        } else {
            return calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()).toUpperCase() + " " + calendar.get(Calendar.YEAR);
        }
    }

    public static String getFormattedDate1(Context context, Calendar calendar){
        if (isSameDay(calendar)){
            return context.getString(R.string.today);

        }else if (isYesterday(calendar)){
            return context.getString(R.string.yesterday);

        }else if (isSameYear(calendar)){
            return calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()).toUpperCase();

        } else {
            return calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()).toUpperCase() + " " + calendar.get(Calendar.YEAR);
        }
    }

    public static String getFormattedDateTime(Context context, Calendar calendar){
        if (isSameDay(calendar)){
            return change0To12(calendar.get(Calendar.HOUR)) + ":"
                    + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE))
                    + " " + calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());

        }else if (isYesterday(calendar)){
            return context.getString(R.string.yesterday);

        }else if (isSameYear(calendar)){
            return calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()).toUpperCase();

        } else {
            return calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                    Locale.getDefault()).toUpperCase() + " " + calendar.get(Calendar.YEAR);
        }
    }

    public static String getFullDateTime(Calendar calendar){
        if (isSameDay(calendar)){
            return change0To12(calendar.get(Calendar.HOUR)) + ":"
                    + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE))
                    + " " + calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault())
                    + R.string.formated_today;

        }else if (isYesterday(calendar)){
            return change0To12(calendar.get(Calendar.HOUR)) + ":"
                    + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE)) + " "
                    + calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault())
                    + R.string.formated_yesterday;

        }else if (isSameYear(calendar)){
            return change0To12(calendar.get(Calendar.HOUR)) + ":"
                    + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE)) + " "
                    + calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault())
                    + " \u2022 " + calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase();

        } else {
            return change0To12(calendar.get(Calendar.HOUR)) + ":"
                    + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE)) + " "
                    + calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault())
                    + " \u2022 " + calendar.get(Calendar.DAY_OF_MONTH) + " "
                    + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase()
                    + " " + calendar.get(Calendar.YEAR);
        }
    }

    public static String getFullDate(Calendar calendar){
        return addZeroPrefixIfOneDigitValue(calendar.get(Calendar.DAY_OF_MONTH)) + "/"
                + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);
    }

    public static String getFullTime(Calendar calendar){
        return change0To12(calendar.get(Calendar.HOUR)) + ":"
                + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE)) + " "
                + calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault());
    }

    public static String getFullTimeWithSecAndMillis(Calendar calendar){
        return change0To12(calendar.get(Calendar.HOUR)) + ":"
                + addZeroPrefixIfOneDigitValue(calendar.get(Calendar.MINUTE)) + " "
                + calendar.get(Calendar.SECOND) + " "
                + calendar.get(Calendar.MILLISECOND) + " "
                + calendar.getDisplayName(Calendar.AM_PM, Calendar.LONG, Locale.getDefault());
    }

    public static int change0To12(int value){
        return value == 0 ? 12 : value;
    }

    public static String addZeroPrefixIfOneDigitValue(int a) {
        StringBuilder stringBuilder = new StringBuilder();
        String data = String.valueOf(a);

        if (data.length() == 1){
            stringBuilder.append("0");
            stringBuilder.append(data);

        }else {
            stringBuilder.append(a);
        }
        return stringBuilder.toString();
    }

    public static String getFormattedTimeDuration(long playDurationInMillis) {
        long diffInHours = TimeUnit.MILLISECONDS.toHours(playDurationInMillis);
        long diffInMin = TimeUnit.MILLISECONDS.toMinutes((diffInHours>0)?((diffInHours * 60 * 1000) - playDurationInMillis):playDurationInMillis);

        return diffInHours + "h " + diffInMin + "m";
    }
}
