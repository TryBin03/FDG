package trybin.fdg.context;

import lombok.Data;
import trybin.fdg.entity.batchconfig.Table;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.enums.DATASOURCE_TYPE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author TryBin
 * @date: 2021/10/28 18:26:22
 * @version 0.0.1
 */
@Data
public class DataGenerateContext {
    private String table;
    private String schema;

    private Long count;
    private Integer sqlValuesCount;
    private AtomicLong index;

    private DATASOURCE_TYPE datasourceType;

    private List<DataGenerateContext> dataGenerateContextList;
    private List<Table> tableList;

    private Map<String, Map<String, Map<String, Value>>> columnContainer;
    private Map<String, Long>  tableContainer;
    private Map<String, Value> valuesContainer;
}
