package com.exchange.ross.exchangeapp.Utils;

import com.exchange.ross.exchangeapp.core.entities.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ross on 4/7/15.
 */
public class DateUtils {

    public static Date dateSinceToday(int numberOfDays) {
        //get the date in a specified number of days
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, numberOfDays - 1);
        date = c.getTime();
        return date;
    }

    public static String dateSinceTodayWithString(int numberOfDays) {
        //get the date in a specified number of days
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, numberOfDays);
        date = c.getTime();

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
    public static String meetingTimeFromEvent(Event event) {
        int startDateLength = event.getStartDate().length();
        String start = event.getStartDate().substring(startDateLength - 8, startDateLength - 3);

        int endDateLength = event.getStartDate().length();
        int endLength = event.getEndDate().length();

        String end = event.getEndDate().substring(endLength - 8, endDateLength - 3);
        return start + " - " + end;
    }

    public static String dayOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day + "";
    }

    public static String dayOfWeekOfDate(Date date) {
        DateFormat format = new SimpleDateFormat("EEEE");
        String day = format.format(date);
        return day.toUpperCase();

    }

    public static String monthYearOfDate(Date date) {
        DateFormat format = new SimpleDateFormat("MMMM yyyy");
        String monthYear = format.format(date);
        return monthYear;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    public static Date dateWithDaySinceNow(int numberOfDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
        return calendar.getTime();
    }
}
