package trybin.fdg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author: TryBin
 * @date: 2021/10/28 18:15:18
 * @version: 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OracleColumns extends Columns{
    private BigDecimal length;
    private BigDecimal keyseq;
}
