package trybin.fdg.entity.batchconfig;

import lombok.Data;

/**
 * @author TryBin
 */
@Data
public class Table {
    private String schemaName;
    private String tableName;
    private Long count;
}
