package trybin.fdg.entity.batchconfig;

import lombok.Data;

import java.util.List;

/**
 * @author TryBin
 */
@Data
public class Group {
    List<Table> tableList;
    List<Value> valueList;
}
