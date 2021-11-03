package trybin.fdg.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import trybin.fdg.entity.batchconfig.Group;

import java.util.List;

/**
 * @author TryBin
 */
@Component
@Getter
@ConfigurationProperties(prefix = "fdg.batch")
@ToString
public class FdgBatchConfig {
    private List<Group> groupList;
}
