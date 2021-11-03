package trybin.fdg.service;

import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;

import java.util.List;

/**
 * @author TryBin
 * @date: 2021/10/27 18:10:39
 * @version 0.0.1
 */
public interface DataGenerateService {
    void process();

    void batchProcess();

    List<Columns> getColumns(DataGenerateContext dataGenerateContext);
}
