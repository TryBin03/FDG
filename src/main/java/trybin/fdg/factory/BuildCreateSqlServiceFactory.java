package trybin.fdg.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.service.BuildCreateSqlService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TryBin
 */
@Service
@Slf4j
public class BuildCreateSqlServiceFactory {

    @Autowired
    private final Map<String, BuildCreateSqlService> buildCreateSqlServiceIns = new ConcurrentHashMap<>();

    public BuildCreateSqlService getBuildCreateSqlServiceIns(String code) {
        BuildCreateSqlService reportInstance = buildCreateSqlServiceIns.get(code + "BuildCreateSqlService");
        if (reportInstance == null) {
            log.error("未定义 BuildCreateSqlServiceInstance");
            throw new RuntimeException("未定义 BuildCreateSqlServiceInstance");
        }

        return reportInstance;
    }

}
