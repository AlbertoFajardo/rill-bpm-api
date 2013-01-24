package org.rill.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateUtils {

	// FIXME MENGRAN. Not thread-safe
	public static final DateFormat DATE_FORMAT_YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * Get start time / end time of some day.
	 * 
	 * @param date
	 *            some date
	 * @return array[start time, end time]
	 */
	public static Date[] getDayStartAndEndDate(Date date) {
		Date[] dates = new Date[2];
		Calendar C = Calendar.getInstance();
		C.setTime(date);
		C.set(Calendar.HOUR_OF_DAY, 0);
		C.set(Calendar.MINUTE, 0);
		C.set(Calendar.SECOND, 0);
		C.set(Calendar.MILLISECOND, 0);
		dates[0] = C.getTime();
		C.set(Calendar.HOUR_OF_DAY, 23);
		C.set(Calendar.MINUTE, 59);
		C.set(Calendar.SECOND, 59);
		C.set(Calendar.MILLISECOND, 99);
		dates[1] = C.getTime();
		return dates;
	}
	
	public static String formatDateYYYYMMDD(Date target) {
		
		if (target == null) {
			throw new NullPointerException();
		}
		return DATE_FORMAT_YYYYMMDD.format(target);
	}
}
