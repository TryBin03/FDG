package trybin.fdg.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.exception.DataGenerateException;
import trybin.fdg.service.SqlExecuteService;

import java.math.BigDecimal;
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

    public static <T> String getIndex(Long index, T length){
        return getIndex(String.valueOf(index), length);
    }

    public static <T> String getIndex(String index, T length){
        if (length == null) {
            throw new DataGenerateException("字段长度为Null。");
        }
        int indexLength = index.length();
        String timeMillis = String.valueOf(System.currentTimeMillis());
        if (length instanceof BigInteger){
            BigInteger length1 = (BigInteger) length;
            if (length1.bitLength() > timeMillis.length() + indexLength){
                return index + timeMillis;
            }
            if (length1.bitLength() > indexLength){
                return index + StringUtils.right(timeMillis, length1.bitLength() - indexLength);
            }
            return StringUtils.right(index, length1.bitLength() == 1 ? length1.bitLength() : length1.bitLength() - 1);
        }else if (length instanceof BigDecimal){
            BigDecimal length1 = (BigDecimal) length;
            if (length1.intValueExact() > timeMillis.length() + indexLength){
                return index + timeMillis;
            }
            if (length1.intValueExact() > indexLength){
                return index + StringUtils.right(timeMillis, length1.intValueExact() - indexLength);
            }
            return StringUtils.right(index, length1.intValueExact() == 1 ? length1.intValueExact() : length1.intValueExact() - 1);
        }else if (length instanceof Integer){
            int length1 = (Integer) length;
            if (length1 > timeMillis.length() + indexLength){
                return index + timeMillis;
            }
            if (length1 > indexLength){
                String s = index + StringUtils.right(timeMillis, length1 - indexLength);
                if (Long.parseLong(s) > Integer.MAX_VALUE) {
                    s = StringUtils.left(s, s.length() - 1);
                }
                return s;
            }
            return StringUtils.right(index, length1 == 1 ? length1 : length1 - 1);
        }
        throw new DataGenerateException("getIndex 函数 中 传入数据类型不正确，传入数据类型为"+ length.getClass());
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
