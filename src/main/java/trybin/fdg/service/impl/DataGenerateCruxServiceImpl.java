package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import trybin.fdg.enums.DATASOURCE_TYPE;
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

    @Value("${datasource.type}")
    private String datasourceType;

    @Autowired
    private DataGenerateService mysqlDataGenerateService;

    @Override
    public DataGenerateService getService() {
        DataGenerateService dataGenerationService = null;
        if (DATASOURCE_TYPE.MySQL.name().equals(datasourceType)){
            dataGenerationService = mysqlDataGenerateService;
        }
        if (ObjectUtils.isEmpty(dataGenerationService)) {
            throw new NullPointerException("传入的数据库类型不正确。");
        }
        return dataGenerationService;
    }
}
