package trybin.fdg.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Component;

@Component
@MapperScan(basePackages = {"trybin.fdg.dao"})
public class MybatisConfig {
}
