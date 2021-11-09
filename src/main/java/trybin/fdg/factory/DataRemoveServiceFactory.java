package trybin.fdg.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.service.DataRemoveService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TryBin
 */
@Service
public class DataRemoveServiceFactory {

    @Autowired
    private final Map<String, DataRemoveService> dataRemoveServiceIns = new ConcurrentHashMap<>();

    public DataRemoveService getDataRemoveServiceIns(String code) {
        DataRemoveService reportInstance = dataRemoveServiceIns.get(code + "DataRemoveService");
        if (reportInstance == null) {
            throw new RuntimeException("未定义 DataRemoveServiceInstance");
        }

        return reportInstance;
    }

}
