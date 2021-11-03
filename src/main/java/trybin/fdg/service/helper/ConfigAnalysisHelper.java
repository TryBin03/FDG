package trybin.fdg.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.batchconfig.Group;
import trybin.fdg.entity.batchconfig.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TryBin
 */
@Component
@Slf4j
public class ConfigAnalysisHelper {
    @Autowired
    private Environment environment;

    public String getProperty(String key){
        return environment.getProperty(key);
    }

    public <T> T getProperty(String key, Class<T> targetType){
        return environment.getProperty(key, targetType);
    }

    public List<DataGenerateContext> analysis(List<Group> groupList) {
        if (ObjectUtils.isEmpty(groupList)) {
            log.error("所传入的配置为空，请检查配置。");
            throw new NullPointerException("所传入的配置为空，请检查配置。");
        }
        List<DataGenerateContext> dataGenerateContextList = new ArrayList<>();

        Map<String, Long> cruxTableMap = new HashMap<>();
        groupList.forEach(group -> {
            Map<String, Long> tableMap = group.getTableList().stream().collect(Collectors.toMap(s->s.getSchemaName()+"."+s.getTableName(), Table::getCount));
            if (cruxTableMap.keySet().containsAll(tableMap.keySet())) {
                log.error("不同组内传入了相同表，请检查配置。");
            }
        });

        groupList.forEach(group->{
            group.getTableList().forEach(table -> {
                DataGenerateContext dataGenerateContext = new DataGenerateContext();
                dataGenerateContext.setSchema(table.getSchemaName());
                dataGenerateContext.setTable(table.getTableName());
                dataGenerateContext.setCount(table.getCount());

                dataGenerateContextList.add(dataGenerateContext);
            });
        });
        return dataGenerateContextList;
    }
}
