package trybin.fdg.service;

import trybin.fdg.entity.Columns;

import java.util.List;

public interface DataRemoveService {
    void process(List<Columns> key);
}
