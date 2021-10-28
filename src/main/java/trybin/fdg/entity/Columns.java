package trybin.fdg.entity;

import lombok.Data;

/**
 * @author: TryBin
 * @date: 2021/10/28 18:15:18
 * @version: 0.0.1
 */
@Data
public class Columns {
    private String colname;
    private Integer keyseq;
    private String typename;
    private Integer length;
    private Integer scale;
    private String idEntity;
}
