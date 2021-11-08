package trybin.fdg.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import trybin.fdg.service.DataGenerateCruxService;
import trybin.fdg.service.DataGenerateService;

/**
 * @author TryBin
 * @date: 2021/10/27 18:34:22
 * @version 0.0.1
 */
@Component
@Slf4j
public class CommandLineRunnerImpl implements CommandLineRunner {

    @Value("${test.value:qwer}")
    private String testStr;

    @Value("${fdg.batch-flag}")
    private Boolean batchFlag;

    @Autowired
    private DataGenerateCruxService dataGenerateCruxService;

    @Override
    public void run(String... args) {
        log.info("FDG 服务启动...");
        long start = System.currentTimeMillis();
        log.info(testStr);
        DataGenerateService service = dataGenerateCruxService.getService();
        if (batchFlag) {
            service.batchProcess();
        }else {
            service.process();
        }
        log.info("FDG 服务结束，共花费 {} s。", (System.currentTimeMillis() - start) / 1000D);
    }
}
