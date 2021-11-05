package trybin.fdg.service.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.batchconfig.Group;
import trybin.fdg.entity.batchconfig.Table;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.exception.DataGenerateException;

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

    public void batchAnalysis(List<Group> groupList, DataGenerateContext dataGenerateContext) {
        if (ObjectUtils.isEmpty(groupList)) {
            log.error("所传入的配置为空，请检查配置。");
            throw new NullPointerException("所传入的配置为空，请检查配置。");
        }

        Map<String, Long> tableContainer = new HashMap<>();
        // Map<Schema, Map<Table, Map<column, value>>>
        Map<String, Map<String, Map<String, Value>>> columnContainer = new HashMap<>();
        Map<String, Value> valueContainer = new HashMap<>();

        groupList.forEach(group -> {
            Map<String, Long> tableSmallContainer = group.getTableList().stream().collect(Collectors.toMap(s->s.getSchemaName()+"."+s.getTableName(), Table::getCount));
            if (tableContainer.keySet().containsAll(tableSmallContainer.keySet())) {
                log.error("不同组内传入了相同表，请检查配置。");
                throw new DataGenerateException("不同组内传入了相同表，请检查配置。");
            }
            tableContainer.putAll(tableSmallContainer);
            // todo 过滤没有填写表名的
            Map<String, List<Value>> valueGroupBySchemaName = group.getValueList().stream().collect(Collectors.groupingBy(Value::getSchemaName));
            valueGroupBySchemaName.forEach((sk,sv)->{
                Map<String, List<Value>> valueGroupByTableName = sv.stream().collect(Collectors.groupingBy(Value::getTableName));
                Map<String, Map<String, Value>> tableMap = new HashMap<>(valueGroupByTableName.size());
                valueGroupByTableName.forEach((tk,tv)->{
                    Map<String, List<Value>> valueGroupByColumnName = tv.stream().collect(Collectors.groupingBy(Value::getColumnName));
                    Map<String, Value> columnMap = new HashMap<>(valueGroupByColumnName.size());
                    valueGroupByColumnName.forEach((ck,cv)->{
                        int size = cv.size();
                        if (size > 1) {
                            log.error("解析自定义列时，发现您在 {} 表传入了 {} 个重复列 {} ，请检查配置。", tk, size, ck);
                            throw new DataGenerateException("解析自定义列时，发现您在 "+ tk +" 表传入了 "+ size +" 个重复列 "+ ck +" ，请检查配置。");
                        }
                        columnMap.put(ck, cv.get(0));
                        valueContainer.put(sk + "." + tk + "." + ck, cv.get(0));
                    });
                    tableMap.put(tk, columnMap);
                });
                columnContainer.put(sk,tableMap);
            });
        });

        dataGenerateContext.setTableContainer(tableContainer);
        dataGenerateContext.setColumnContainer(columnContainer);
        dataGenerateContext.setValuesContainer(valueContainer);
    }

    public void analysis(List<Value> valueList, DataGenerateContext dataGenerateContext){
        if (CollectionUtils.isEmpty(valueList)) {
            log.error("所传入的配置为空，请检查配置。");
            throw new NullPointerException("所传入的配置为空，请检查配置。");
        }

        Map<String, Long> tableContainer = new HashMap<>();
        final String tableId = dataGenerateContext.getSchema() + "." + dataGenerateContext.getTable();
        tableContainer.put(tableId, dataGenerateContext.getCount());

        Map<String, Map<String, Map<String, Value>>> columnContainer = new HashMap<>();
        Map<String, Value> valueContainer = new HashMap<>();

        Map<String, List<Value>> valueGroupByColumnName = valueList.stream().collect(Collectors.groupingBy(Value::getColumnName));
        Map<String, Value> columnMap = new HashMap<>(valueGroupByColumnName.size());
        valueGroupByColumnName.forEach((ck,cv)->{
            int size = cv.size();
            if (size > 1) {
                log.error("解析自定义列时，发现您在 {} 表传入了 {} 个重复列 {} ，请检查配置。", tableId, size, ck);
                throw new DataGenerateException("解析自定义列时，发现您在 "+ tableId +" 表传入了 "+ size +" 个重复列 "+ ck +" ，请检查配置。");
            }
            columnMap.put(ck, cv.get(0));
            valueContainer.put(tableId + "." + ck, cv.get(0));

            Map<String, Map<String, Value>> tableMap = new HashMap<>(1);
            tableMap.put(dataGenerateContext.getTable(), columnMap);
            columnContainer.put(dataGenerateContext.getSchema(),tableMap);
        });

        dataGenerateContext.setTableContainer(tableContainer);
        dataGenerateContext.setColumnContainer(columnContainer);
        dataGenerateContext.setValuesContainer(valueContainer);
    }
}
