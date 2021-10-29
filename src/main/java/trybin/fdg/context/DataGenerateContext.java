package trybin.fdg.context;

import lombok.Data;
import trybin.fdg.enums.DATASOURCE_TYPE;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: TryBin
 * @date: 2021/10/28 18:26:22
 * @version: 0.0.1
 */
@Data
public class DataGenerateContext {
    private String table;
    private String schema;

    private Long count;
    private Integer sqlValuesCount;
    private AtomicLong index;

    private DATASOURCE_TYPE datasourceType;
}
