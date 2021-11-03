package trybin.fdg.entity.batchconfig;

import lombok.Data;

/**
 * @author 远弘163
 */
@Data
public class Value {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String value;
    private Boolean autoIncrement;
}
