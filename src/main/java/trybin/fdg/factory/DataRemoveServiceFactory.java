package trybin.fdg.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.service.DataRemoveService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TryBin
 */
@Service
@Slf4j
public class DataRemoveServiceFactory {

    @Autowired
    private final Map<String, DataRemoveService> dataRemoveServiceIns = new ConcurrentHashMap<>();

    public DataRemoveService getDataRemoveServiceIns(String code) {
        DataRemoveService reportInstance = dataRemoveServiceIns.get(code + "DataRemoveService");
        if (reportInstance == null) {
            log.error("未定义 DataRemoveServiceInstance");
            throw new RuntimeException("未定义 DataRemoveServiceInstance");
        }

        return reportInstance;
    }

}
