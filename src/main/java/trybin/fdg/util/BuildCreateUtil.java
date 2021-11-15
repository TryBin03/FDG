package trybin.fdg.util;

import java.util.Date;

/**
 * @author TryBin
 */
public class BuildCreateUtil {

    public static Date maxDate(Date date, String maxDateStr) {
        if (date.compareTo(DateUtils.getDate(maxDateStr)) == 0){
            date = DateUtils.getDate(maxDateStr);
        }
        return date;

    }
}
