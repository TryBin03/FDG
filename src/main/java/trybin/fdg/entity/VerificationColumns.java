package trybin.fdg.entity;

import lombok.Data;

/**
 * @author TryBin
 */
@Data
public class VerificationColumns <T>{
    private String columnId;
    private String columnName;
    private String dataType;
    private T length;
}
