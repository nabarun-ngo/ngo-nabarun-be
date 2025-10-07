package ngo.nabarun.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

public class DateUtil {
	public static Date getDayOfCurrentMonth(int day) {
		return getDayOfCurrentMonth(day,new Date());
	}
	
	public static Date getDayOfCurrentMonth(int day,Date today) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}

	public static Date getLastDayOfCurrentMonth() {
		return getLastDayOfCurrentMonth(new Date());
	}
	
	public static Date getLastDayOfCurrentMonth(Date today) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
	
	/**
	 * Utility function to convert java Date to TimeZone format
	 * 
	 * @param date
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static String formatDateToString(Date date, String format, String timeZone) {
		if (date == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		if (timeZone == null || "".equalsIgnoreCase(timeZone.trim())) {
			timeZone = Calendar.getInstance().getTimeZone().getID();
		}
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		return sdf.format(date);
	}
	
	public static boolean isCurrentMonth(Date givenDate) {
		return isCurrentMonth(givenDate,new Date());
	}

	public static boolean isCurrentMonth(Date givenDate,Date today) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTime(givenDate);
		cal2.setTime(today);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

	public static List<String> getMonthsBetween(Date startDate, Date endDate, String format) {
		List<String> list = new ArrayList<String>();
		Calendar beginCalendar = Calendar.getInstance();
		Calendar finishCalendar = Calendar.getInstance();
		beginCalendar.setTime(startDate);
		finishCalendar.setTime(endDate);
		DateFormat formaterYd = new SimpleDateFormat(format);
		while (beginCalendar.before(finishCalendar)) {
			list.add(formaterYd.format(beginCalendar.getTime()));
			beginCalendar.add(Calendar.MONTH, 1);
		}
		return list;
	}

	public static List<String> getMonthsBetween(Date startDate, Date endDate) {

		return getMonthsBetween(startDate, endDate, "MMMM yyyy");
	}

	public static Date getFormattedDate(String dateStr, String format) {
		if (dateStr == null || StringUtils.isBlank(dateStr)) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		try {
			return formaterYd.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getFormattedDateString(Date date, String format) {
		if (date == null) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		return formaterYd.format(date);
	}

	public static String getFormattedDateString(Date date) {
		return getFormattedDateString(date, "MMMM yyyy");
	}

	public static Date addDaysToDate(Date date, int days) {
		return addSecondsToDate(date, days * 86400); // One day to 86400 seconds
	}

	public static Date addSecondsToDate(Date date, int seconds) {
		if (date == null) {
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}
}
