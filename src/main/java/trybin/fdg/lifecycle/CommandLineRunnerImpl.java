package trybin.fdg.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author: TryBin
 * @date: 2021/10/27 18:34:22
 * @version: 0.0.1
 */
@Component
@Slf4j
public class CommandLineRunnerImpl implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("FDG 服务启动。");
    }
}
