package trybin.fdg.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataGenerateUtilTest {
    @Test
    void myTest(){
        System.out.println(DateUtils.getDayAfterDate(DateUtils.getDate("1970-01-01"), 1));
    }
}