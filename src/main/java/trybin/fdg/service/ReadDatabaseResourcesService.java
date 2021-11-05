package trybin.fdg.service;


import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;

import java.util.List;

/**
 * @author TryBin
 */
public interface ReadDatabaseResourcesService {

    void batchFindColumns(DataGenerateContext dataGenerateContext);

    List<Columns> getColumns(DataGenerateContext dataGenerateContext);
}
