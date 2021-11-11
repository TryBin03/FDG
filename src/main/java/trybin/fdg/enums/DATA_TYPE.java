package trybin.fdg.enums;

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
public enum DATA_TYPE {
    DATE,
    TIME,
    YEAR,
    DATETIME,
    TIMESTAMP,
    DATETIME2,
    DATETIMEOFFSET,
    SMALLDATETIME;
}
