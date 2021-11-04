package trybin.fdg.service;

import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;

import java.util.List;

public interface DataRemoveService {
    void process(List<Columns> key, DataGenerateContext dataGenerateContext);
}
