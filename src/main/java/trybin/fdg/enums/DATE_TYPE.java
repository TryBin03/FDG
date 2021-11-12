package trybin.fdg.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>DB 时间类型</p>
 * <ul>
 *     <li>
 *         <b>MySQL</b>
 *         - DATE,TIME,YEAR,DATETIME,TIMESTAMP
 *     </li>
 *     <li>
 *         <b>Oracle</b>
 *         - DATE,TIMESTAMP
 *     </li>
 *     <li>
 *         <b>SqlServer</b>
 *         - DATE,TIME,DATETIME,TIMESTAMP,DATETIME2,DATETIMEOFFSET,SMALLDATETIME
 *     </li>
 *
 * </ul>
 * @author TryBin
 */
public enum DATE_TYPE {
    DATE,
    TIME,
    YEAR,
    DATETIME,
    TIMESTAMP,
    DATETIME2,
    DATETIMEOFFSET,
    SMALLDATETIME;

    public static Boolean contains(String typeStr){
        for (DATE_TYPE value : DATE_TYPE.values()) {
            return StringUtils.equalsIgnoreCase(value.name(), typeStr);
        }
        return false;
    }
}
