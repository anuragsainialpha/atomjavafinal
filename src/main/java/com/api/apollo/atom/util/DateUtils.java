package com.api.apollo.atom.util;

import com.api.apollo.atom.constant.Constants;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

  public static Date formatDate(final String date, final String format) {
    try {
      return new SimpleDateFormat(format).parse(date.trim());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
//It used for storing expiry date in freight, since DB is converting the date to UTC
  public static Date formatDateToUTC(final String date, final String format) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat(format);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      return dateFormat.parse(date.trim());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String formatDate(final Date date, final String format) {
    try {
      return new SimpleDateFormat(format).format(date);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static boolean isDateValid(String dateToValidate, String dateFromat) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
    sdf.setLenient(false);
    try {
      Date date = sdf.parse(dateToValidate);
      // System.out.println(date);
    } catch (ParseException e) {
      return false;
    }
    return true;
  }

  public static Date atStartOfDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
    return localDateTimeToDate(startOfDay);
  }

  public static Date atEndOfDay(Date date) {
    LocalDateTime localDateTime = dateToLocalDateTime(date);
    LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
    return localDateTimeToDate(endOfDay);
  }

  private static LocalDateTime dateToLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  private static Date localDateTimeToDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static Long diffBetweenDates(Date newDate, Date oldDate) {
    return Duration.between(dateToLocalDateTime(newDate), dateToLocalDateTime(oldDate)).toHours();
  }

  public static Date setTimeToMidnight(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime( date );
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime();
  }

  public static Date setNextDayStart(Date date) {
    Calendar calendar = Calendar.getInstance();

    calendar.setTime( date );
    calendar.add(Calendar.DATE, 1);

    return setTimeToMidnight(calendar.getTime());
  }

  public static boolean isSameDay(Date date){
    if (date != null){
      return setTimeToMidnight(date).equals(setTimeToMidnight(new Date()));
    }
    return false;
  }

  //When user uploads dispatch date in DD/MM/YYYY or DD-MM-YYYY formats
  public static Date constantDateFormat(String dateString) {
    if(!StringUtils.isEmpty(dateString)){
      //formartString contains two date formats DD/MM/YYYY and DD-MM-YYYY
      for (String formatString : Constants.formatStrings)
      {
        try
        {
          // returns Date if given date matches with any of the formats else returns null
          return new SimpleDateFormat(formatString).parse(dateString);
        }
        catch (ParseException e) {}
      }
    }
    return null;
  }

  public static String getStartOfTheMonth(Date date){
    Calendar aCalendar = Calendar.getInstance();

    aCalendar.set(Calendar.DATE, 1);
//    aCalendar.set(Calendar.HOUR_OF_DAY, 0);
//    aCalendar.set(Calendar.MINUTE, 0);
//    aCalendar.set(Calendar.SECOND, 0);

    Date firstDateOfCurrentMonth = aCalendar.getTime();

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    sdf.setTimeZone(TimeZone.getTimeZone("IST"));

    return sdf.format(firstDateOfCurrentMonth);
  }
}
