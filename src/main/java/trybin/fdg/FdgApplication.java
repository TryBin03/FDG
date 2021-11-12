package trybin.fdg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author TryBin
 */
@EnableCaching
@SpringBootApplication
public class FdgApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdgApplication.class, args);
    }

}
