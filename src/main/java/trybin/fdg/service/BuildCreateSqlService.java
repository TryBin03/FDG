package trybin.fdg.service;

import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;

import java.util.List;
import java.util.Set;

/**
 * @author TryBin
 */
public interface BuildCreateSqlService {
    List<String> execute(List<Columns> colNames, Set<String> keys, DataGenerateContext dataGenerateContext);
}
