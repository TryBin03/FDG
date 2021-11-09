package trybin.fdg.entity;

import lombok.Data;

/**
 * @author TryBin
 * @date: 2021/10/28 18:15:18
 * @version 0.0.1
 */
@Data
public class Columns<T> {
    private String schemaName;
    private String tableName;
    private String colname;
    private T keyseq;
    private String typename;
    private T length;
    private Long scale;
    private String idEntity;
}
