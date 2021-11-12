package trybin.fdg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import trybin.fdg.service.impl.GlobalIndexServiceImpl;

@SpringBootTest
class FdgApplicationTests {
    @Autowired
    GlobalIndexServiceImpl globalIndexService;

    @Test
    void contextLoads() {
        String deposit = globalIndexService.deposit(99L);
        System.out.println(deposit);
    }

}
