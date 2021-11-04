package trybin.fdg.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import trybin.fdg.entity.batchconfig.Value;

import java.util.List;

/**
 * @author TryBin
 */
@Component
@Data
@ConfigurationProperties(prefix = "fdg.single-table")
@ToString
public class FdgSingleTableConfig {
    private String table;
    private String schema;
    private Long count;
    private List<Value> valueList;
}
