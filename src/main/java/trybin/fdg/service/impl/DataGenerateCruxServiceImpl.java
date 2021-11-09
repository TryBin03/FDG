package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.service.DataGenerateCruxService;
import trybin.fdg.service.DataGenerateService;

/**
 * @author TryBin
 * @date 2021/10/27 18:13:15
 * @version 0.0.1
 */
@Slf4j
@Service("dataGenerateCruxService")
public class DataGenerateCruxServiceImpl implements DataGenerateCruxService {

    @Autowired
    private DataGenerateService mysqlDataGenerateService;

    @Override
    public DataGenerateService getService() {
        return mysqlDataGenerateService;
    }
}
