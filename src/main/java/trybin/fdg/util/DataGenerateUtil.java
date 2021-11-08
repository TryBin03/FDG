package trybin.fdg.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.enums.MySQL_DATA_TYPE;
import trybin.fdg.exception.DataGenerateException;
import trybin.fdg.service.SqlExecuteService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author TryBin
 * @date: 2021/10/28 18:25:42
 * @version 0.0.1
 */
@Slf4j
public class DataGenerateUtil {

    public static String perfectFindColumnsSql(DataGenerateContext dataGenerateConText, String getColNameSql) {
        List<String> values = new ArrayList<>(2);
        values.add(dataGenerateConText.getSchema());
        values.add(dataGenerateConText.getTable());
        return StringUtil.replace(getColNameSql, values);
    }

    public static List<String> getColName(List<Columns> columnsVos) {
        return columnsVos
                .stream().map(Columns::getColname).collect(Collectors.toList());
    }

    public static Set<String> getKeys(List<Columns> columnsVos) {
        return columnsVos.stream()
                .filter(s -> null != s.getKeyseq())
                .map(Columns::getColname).collect(Collectors.toSet());
    }

    public static List<Columns> getNotKey(List<Columns> columnsVos) {
        return columnsVos.stream()
                .filter(s -> null == s.getKeyseq())
                .collect(Collectors.toList());
    }

    public static String createInsertSql(List<Columns> columns, Set<String> keys, DataGenerateContext dataGenerateContext, Map<String, Value> userDefinedValueContainer) {
        Long count = dataGenerateContext.getCount();
        Integer sqlValuesCount = dataGenerateContext.getSqlValuesCount();
        AtomicLong index = dataGenerateContext.getIndex();

        StringBuffer sqlSb = new StringBuffer();
        // 拼装字段
        sqlSb.append("INSERT INTO ")
                .append(dataGenerateContext.getTable()).append(" (");
        columns.forEach(colName->sqlSb.append(colName.getColname()).append(", "));
        sqlSb.delete(sqlSb.length() - 2, sqlSb.length());
        sqlSb.append(") ");
        sqlSb.append("VALUES ");

        // 拼装值
        for (int i = 0; i < sqlValuesCount; i++) {
            if (index.get() == count) {
                break;
            }
            sqlSb.append("(");
            for (Columns column : columns) {
                sqlSb.append("'");
                String typename = column.getTypename();
                // 主键
                if (!(CollectionUtils.isEmpty(userDefinedValueContainer)) && userDefinedValueContainer.containsKey(column.getColname())){
                    Value value = userDefinedValueContainer.get(column.getColname());
                // todo 暂时支持用户自定义数据增

//                    if (value.getAutoIncrement()){
//                        sqlSb.append(getIndex(value.getValue(), column.getLength()));
//                    }else {
//                        sqlSb.append(value.getValue());
//                    }
                        sqlSb.append(value.getValue());
                } else if (keys.contains(column.getColname())) {
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATE.name(), typename)){
                        sqlSb.append(DateUtils.formatDate(DateUtils.getDayAfterDate(DateUtils.getDate("1970-01-01"),index.intValue()), DateUtils.FORMAT_YYYY_MM_DD));
                    } else if(StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIME.name(), typename)) {
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("00:00:00"),index.intValue()), DateUtils.FORMAT_HH_MM_SS));
                    } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATETIME.name(), typename)){
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("1970-01-01 00:00:00"),index.intValue()),DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                    }else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIMESTAMP.name(), typename)){
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("1970-01-01 08:00:01"),index.intValue()),DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                    } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.YEAR.name(), typename)){
                        sqlSb.append(DateUtils.formatDate(DateUtils.getYearAfterDate(DateUtils.getDate("1970"),index.intValue()), DateUtils.FORMAT_YYYY));
                    }
                    else {
                        sqlSb.append(getIndex(index.get(),column.getLength()));
                    }
                }else {
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATE.name(), typename)){
                        sqlSb.append("1970-01-01");
                    } else if(StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIME.name(), typename)) {
                        sqlSb.append("00:00:00");
                    } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATETIME.name(), typename)){
                        sqlSb.append("1970-01-01 00:00:00");
                    } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIMESTAMP.name(), typename)){
                        sqlSb.append("1970-01-01 08:00:01");
                    }
                    else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.YEAR.name(), typename)){
                        sqlSb.append("1970");
                    }
                    // 默认插入 1
                    else {
                        sqlSb.append("1");
                    }
                }
                sqlSb.append("', ");
            }
            // 去除字符“, ”
            sqlSb.delete(sqlSb.length() - 2,sqlSb.length());
            sqlSb.append("), ");
            index.set(1 + index.get());
        }
        sqlSb.delete(sqlSb.length() - 2,sqlSb.length());
        dataGenerateContext.setIndex(index);
        return sqlSb.toString();
    }

    public static List<String> createInsertSqlBach(List<Columns> colNames, Set<String> keys, DataGenerateContext dataGenerateContext) {
        Long count = dataGenerateContext.getCount();
        Integer sqlValuesCount = dataGenerateContext.getSqlValuesCount();
        Map<String, Value> userDefinedValueContainer = dataGenerateContext.getColumnContainer().get(dataGenerateContext.getSchema()).get(dataGenerateContext.getTable());

        Long realCount = count / sqlValuesCount;
        if (count % sqlValuesCount != 0) {
            realCount++;
        }
        List<String> insertSqlBach = new ArrayList<>();
        for (Long i = 0L; i < realCount; i++) {
            insertSqlBach.add(createInsertSql(colNames, keys, dataGenerateContext, userDefinedValueContainer));
        }
        return insertSqlBach;
    }

    private static String getIndex(Long index, BigInteger length){
        return getIndex(String.valueOf(index), length);
    }

    private static String getIndex(String index, BigInteger length){
        if (length == null) {
            throw new DataGenerateException("字段长度为Null。");
        }
        int indexLength = index.length();
        String timeMillis = String.valueOf(System.currentTimeMillis());
        if (length.bitLength() > timeMillis.length() + indexLength){
            return index + timeMillis;
        }
        if (length.bitLength() > indexLength){
            return index + StringUtils.right(timeMillis,length.bitLength() - indexLength);
        }
        return StringUtils.right(index,length.bitLength() == 1 ? length.bitLength() : length.bitLength()-1);
    }

    public static void insertBatch(SqlExecuteService sqlExecuteService, List<String> sqlBatch){
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
            CompletableFuture[] cfArr = sqlBatch.stream().
                    map(sql -> CompletableFuture
                            .runAsync(() -> sqlExecuteService.insert(sql), executorService)
                            .whenComplete((result, th) -> {
                            })).toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(cfArr).join();
        }finally {
            executorService.shutdown();
        }
    }

    public static List<DataGenerateContext> buildTask(DataGenerateContext dataGenerateContext) {
        List<DataGenerateContext> dataGenerateContextArrayList = new ArrayList<>();
        dataGenerateContext.getTableContainer().forEach((k, v)->{
            String[] split = k.split("\\.");
            DataGenerateContext context = new DataGenerateContext();
            context.setSchema(split[0]);
            context.setTable(split[1]);
            context.setCount(v);
            context.setDatasourceType(dataGenerateContext.getDatasourceType());
            context.setColumnContainer(dataGenerateContext.getColumnContainer());
            context.setSqlValuesCount(dataGenerateContext.getSqlValuesCount());
            context.setIndex(new AtomicLong(0));
            dataGenerateContextArrayList.add(context);
        });
        Map<String, Map<String, Map<String, Value>>> columnContainer = dataGenerateContext.getColumnContainer();

        return dataGenerateContextArrayList;
    }
}
