package trybin.fdg.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.service.ReadDatabaseResourcesService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TryBin
 */
@Service
public class ReadDatabaseResourcesServiceFactory {

    @Autowired
    private final Map<String, ReadDatabaseResourcesService> readDatabaseResourcesServiceIns = new ConcurrentHashMap<>();

    public ReadDatabaseResourcesService getReadDatabaseResourcesServiceIns(String code) {
        ReadDatabaseResourcesService reportInstance = readDatabaseResourcesServiceIns.get(code + "ReadDatabaseResourcesService");
        if (reportInstance == null) {
            throw new RuntimeException("未定义 ReadDatabaseResourcesServiceInstance");
        }

        return reportInstance;
    }

}
