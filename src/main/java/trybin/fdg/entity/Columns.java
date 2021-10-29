package trybin.fdg.entity;

import lombok.Data;

import java.math.BigInteger;

/**
 * @author: TryBin
 * @date: 2021/10/28 18:15:18
 * @version: 0.0.1
 */
@Data
public class Columns {
    private String colname;
    private Long keyseq;
    private String typename;
    private BigInteger length;
    private Long scale;
    private String idEntity;
}
