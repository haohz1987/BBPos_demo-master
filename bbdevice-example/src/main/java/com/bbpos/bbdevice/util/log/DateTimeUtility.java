package com.bbpos.bbdevice.util.log;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtility
{
    public static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
    public static final long MILLISECONDS_PER_HOUR = 1000 * 60 * 60;
    public static final long MILLISECONDS_PER_MIN = 1000 * 60;
    private static int _localTimeZoneOffsetInMilliseconds = TimeZone.getDefault().getRawOffset();
    private static final String _standardFormat = "yyyy-MM-dd HH:mm:ss";

    public static Date covertStringToDate(String date)
    {
        return covertStringToDate(date, _standardFormat);
    }

    public static Date covertStringToDate(String date, String format)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        if (TextUtils.isEmpty(date))
        {
            return null;
        }
        else
        {
            try
            {
                return simpleDateFormat.parse(date);
            }
            catch (ParseException e)
            {
                throw new ApplicationException("DateTimeUtility.convertStringToDate", "Failed to date string.", e);
            }
        }
    }

    public static Calendar convertDateToCalendar(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Date convertUtcToLocal(Date utcDate)
    {
        return new Date(utcDate.getTime() + _localTimeZoneOffsetInMilliseconds);
    }

    public static Date convertLocalToUtc(Date localDate)
    {
        return new Date(localDate.getTime() - _localTimeZoneOffsetInMilliseconds);
    }

    public static String getDateTimeString(Date date)
    {
        return getDateTimeString(date, _standardFormat);
    }

    public static String getDateTimeString(Date date, String format)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static long getDateDiffInDays(Date date1, Date date2)
    {
        return getDateDiffInMilliSeconds(date1, date2) / MILLISECONDS_PER_DAY;
    }

    public static long getDateDiffInMilliSeconds(Date date1, Date date2)
    {
        Calendar day1 = convertDateToCalendar(date1);
        Calendar day2 = convertDateToCalendar(date2);
        return (day1.getTimeInMillis() - day2.getTimeInMillis());
    }

    public static int getDaysOfCurrentMonth()
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    public static int getDaysOfYearMonth(int year, int month)
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }
}
