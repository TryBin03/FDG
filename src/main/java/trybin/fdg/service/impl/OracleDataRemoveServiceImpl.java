package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.enums.DATA_TYPE;
import trybin.fdg.service.DataRemoveService;
import trybin.fdg.service.SqlExecuteService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author TryBin
 */
@Slf4j
@Component("OracleDataRemoveService")
public class OracleDataRemoveServiceImpl implements DataRemoveService {

    @Autowired
    private SqlExecuteService sqlExecuteService;

    @Override
    public void process(List<Columns> columnsNotKet, DataGenerateContext dataGenerateContext) {
        log.info("删除服务启动...");
        long start = System.currentTimeMillis();
        String removeSql = generateRemoveSql(dataGenerateContext.getSchema(), dataGenerateContext.getTable(), columnsNotKet,
                dataGenerateContext.getColumnContainer().get(dataGenerateContext.getSchema()).get(dataGenerateContext.getTable()));
        sqlExecuteService.delete(removeSql);
        log.info("删除完成，共耗时 {} s。", (System.currentTimeMillis() - start) / 1000D);
    }

    @Override
    public void batchProcess(DataGenerateContext dataGenerateContext) {
        log.info("删除服务启动...");
        long start = System.currentTimeMillis();
        Map<String, Map<String, List<Columns>>> notKeyColumnsContainer = dataGenerateContext.getNotKeyColumnsContainer();

        ExecutorService executorService = new ThreadPoolExecutor(
                5,
                15,
                5L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(70),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        try {
            CompletableFuture[] cfArr = dataGenerateContext.getDataGenerateContextList().stream().
                    map(context -> CompletableFuture
                            .runAsync(() -> {
                                String schema = context.getSchema();
                                String table = context.getTable();
                                String removeSql = generateRemoveSql(schema, table,
                                        notKeyColumnsContainer.get(schema).get(table),
                                        context.getColumnContainer().get(schema).get(table));
                                sqlExecuteService.delete(removeSql);
                            }, executorService)
                            .whenComplete((result, th) -> {
                            })).toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(cfArr).join();
        }finally {
            executorService.shutdown();
        }
        log.info("删除完成，共耗时 {} s。", (System.currentTimeMillis() - start) / 1000D);
    }

    private String generateRemoveSql(String schema, String table, List<Columns> columnsNotKet, Map<String, Value> userDefinedValueContainer) {
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM ").append(schema).append(".").append("\"").append(table).append("\"");
        sb.append(" WHERE ");
        columnsNotKet.forEach(column -> {
            sb.append("\"").append(column.getColname()).append("\"").append(" = ");
            String typename = column.getTypename();
            // 用户自定义值
            if (userDefinedValueContainer.containsKey(column.getColname())) {
                sb.append("'").append(userDefinedValueContainer.get(column.getColname()).getValue()).append("'");
            }
            // 排除时间类型
            else if (StringUtils.equalsIgnoreCase(DATA_TYPE.DATE.name(), typename)){
                sb.append("to_date('");
                sb.append("0021-11-09");
                sb.append("' , 'yyyy-mm-dd hh24:mi:ss')");
            } else if (StringUtils.containsAnyIgnoreCase(typename, DATA_TYPE.TIMESTAMP.name())){
                sb.append("to_timestamp('");
                sb.append("0021-11-09 00:00:00");
                sb.append("' , 'yyyy-mm-dd hh24:mi:ss')");
            }
            // 默认为 1
            else {
                sb.append("'").append("1").append("'");
            }
            sb.append(" AND ");
        });
        sb.delete(sb.length() - 4, sb.length());
        return sb.toString();
    }
}
