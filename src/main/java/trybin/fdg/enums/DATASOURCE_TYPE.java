package trybin.fdg.enums;

import org.apache.commons.lang3.StringUtils;
import trybin.fdg.exception.DataGenerateException;

/**
 * @author: TryBin
 * @date: 2021/10/28 15:23:13
 * @version: 0.0.1
 */
public enum DATASOURCE_TYPE {
    MySQL,
    DB2,
    Oracle,
    SqlServer;

    public static DATASOURCE_TYPE getType(String datasourceType){
        for (DATASOURCE_TYPE value : DATASOURCE_TYPE.values()) {
            if (StringUtils.equalsIgnoreCase(value.name(), datasourceType)) {
                return value;
            }
        }
        throw new DataGenerateException("传入的数据库类型不正确。");
    }
}
