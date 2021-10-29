package trybin.fdg.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import trybin.fdg.constant.Constant;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static final String FORMAT_YYYYMM = "yyyyMM";

    public static final String FORMAT_YYYYM_CN = "yyyy年M月";

    public static final String FORMAT_YYYYMMDD = "yyyyMMdd";

    public static final String FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String FORMAT_YYYY_MM = "yyyy-MM";

    public static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    public static final String FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String FORMAT_HH_MM_SS = "HH:mm:ss";

    public static final String FORMAT_YYYY = "YYYY";

    public static Date getMonthAfterDate(Date date, int monthAfter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, monthAfter);
        return calendar.getTime();
    }

    public static Date getDayAfterDate(Date date, int dayAfter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, dayAfter);
        return calendar.getTime();
    }
    public static Date getSecondAfterDate(Date date, int dayAfter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, dayAfter);
        return calendar.getTime();
    }

    public static Date getYearAfterDate(Date date, int dayAfter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, dayAfter);
        return calendar.getTime();
    }

    public static Date getDayStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getDayEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    public static Date getMonthStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date getMonthEndDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getDate(String dateStr, String format) {
        Date date = null;
        try {
            DateFormat df = new SimpleDateFormat(format);
            date = df.parse(dateStr);
        } catch (Exception e) {
            logger.debug("[" + dateStr + "]不能转换成[" + format + "]格式的日期");
        }
        return date;
    }

    public static Date getDate(String dateStr) {
        String format = FORMAT_YYYY_MM_DD;
        return getDate(dateStr, format);
    }

    public static String convertFormatDate(String dateStr, String dateFormat1, String dateFormat2) {
        if (StringUtils.isEmpty(dateStr) || StringUtils.isEmpty(dateFormat1) || StringUtils.isEmpty(dateFormat2))
            return dateStr;
        if (dateStr.length() != dateFormat1.length()) return dateStr;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat1);
            Date date = sdf.parse(dateStr);
            return formatDate(date, dateFormat2);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    public static String formatMonthStartDate(Date date) {
        String format = FORMAT_YYYY_MM_DD_HH_MM_SS;
        return formatMonthStartDate(date, format);
    }

    public static String formatMonthStartDate(Date date, String format) {
        Date monthStart = getMonthStartDate(date);
        return formatDate(monthStart, format);
    }

    public static String formatMonthEndDate(Date date) {
        String format = FORMAT_YYYY_MM_DD_HH_MM_SS;
        return formatMonthEndDate(date, format);
    }

    public static String formatMonthEndDate(Date date, String format) {
        Date monthEnd = getMonthStartDate(date);
        return formatDate(monthEnd, format);
    }

    public static String formatDate(Date date) {
        String format = FORMAT_YYYY_MM_DD_HH_MM_SS;
        return formatDate(date, format);
    }

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return Constant.STRING_EMPTY;
        }
        if (format == null || format.isEmpty()) format = FORMAT_YYYYMMDD;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

}
