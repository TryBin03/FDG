package trybin.fdg.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.service.ReadDatabaseResourcesService;
import trybin.fdg.service.VerificationConfigService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TryBin
 */
@Service
@Slf4j
public class VerificationConfigServiceFactory {

    @Autowired
    private final Map<String, VerificationConfigService> verificationConfigServiceIns = new ConcurrentHashMap<>();

    public VerificationConfigService getVerificationConfigServiceInsIns(String code) {
        VerificationConfigService reportInstance = verificationConfigServiceIns.get(code + "VerificationConfigService");
        if (reportInstance == null) {
            log.error("未定义 VerificationConfigServiceInstance");
            throw new RuntimeException("未定义 VerificationConfigServiceInstance");
        }

        return reportInstance;
    }

}
