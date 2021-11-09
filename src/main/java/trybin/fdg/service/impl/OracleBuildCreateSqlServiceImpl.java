package trybin.fdg.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.enums.MySQL_DATA_TYPE;
import trybin.fdg.service.BuildCreateSqlService;
import trybin.fdg.util.DataGenerateUtil;
import trybin.fdg.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author TryBin
 */
@Component("OracleBuildCreateSqlService")
public class OracleBuildCreateSqlServiceImpl implements BuildCreateSqlService {

    @Override
    public List<String> execute(List<Columns> colNames, Set<String> keys, DataGenerateContext dataGenerateContext) {
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

    public String createInsertSql(List<Columns> columns, Set<String> keys, DataGenerateContext dataGenerateContext, Map<String, Value> userDefinedValueContainer) {
        Long count = dataGenerateContext.getCount();
        Integer sqlValuesCount = dataGenerateContext.getSqlValuesCount();
        AtomicLong index = dataGenerateContext.getIndex();

        StringBuffer sqlSb = new StringBuffer();
        // 拼装字段
        sqlSb.append("INSERT ALL ");

        StringBuilder sqlSbPrefix = new StringBuilder();
        // 拼装字段
        sqlSbPrefix.append("INTO ")
                .append("\"").append(dataGenerateContext.getTable()).append("\"").append(" (");
        columns.forEach(colName->sqlSbPrefix.append("\"").append(colName.getColname()).append("\"").append(", "));
        sqlSbPrefix.delete(sqlSbPrefix.length() - 2, sqlSbPrefix.length());
        sqlSbPrefix.append(") ");
        sqlSbPrefix.append("VALUES ");

        // 拼装值
        for (int i = 0; i < sqlValuesCount; i++) {
            if (index.get() == count) {
                break;
            }
            sqlSb.append(sqlSbPrefix);
            sqlSb.append("(");
            for (Columns column : columns) {
                String typename = column.getTypename();
                // 主键
                if (!(CollectionUtils.isEmpty(userDefinedValueContainer)) && userDefinedValueContainer.containsKey(column.getColname())){
                    sqlSb.append("'").append(userDefinedValueContainer.get(column.getColname()).getValue()).append("'");
                } else if (keys.contains(column.getColname())) {
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATE.name(), typename)){
                        sqlSb.append("to_date('");
                        sqlSb.append(DateUtils.formatDate(DateUtils.getDayAfterDate(DateUtils.getDate("0021-11-09"),index.intValue()), DateUtils.FORMAT_YYYY_MM_DD));
                        sqlSb.append("' , 'yyyy-mm-dd hh24:mi:ss')");
                    }else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIMESTAMP.name(), typename)){
                        sqlSb.append("to_timestamp('");
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("0021-11-09 00:00:00"),index.intValue()),DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                        sqlSb.append("' , 'yyyy-mm-dd hh24:mi:ss')");
                    }
                    else {
                        sqlSb.append("'").append(DataGenerateUtil.getIndex(index.get(),column.getLength())).append("'");
                    }
                }else {
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATE.name(), typename)){
                        sqlSb.append("to_date('");
                        sqlSb.append("0021-11-09");
                        sqlSb.append("' , 'yyyy-mm-dd hh24:mi:ss')");
                    } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIMESTAMP.name(), typename)){
                        sqlSb.append("to_timestamp('");
                        sqlSb.append("0021-11-09 00:00:00");
                        sqlSb.append("' , 'yyyy-mm-dd hh24:mi:ss')");
                    }
                    // 默认插入 1
                    else {
                        sqlSb.append("'1'");
                    }
                }
                sqlSb.append(", ");
            }
            // 去除字符“, ”
            sqlSb.delete(sqlSb.length() - 2,sqlSb.length());
            sqlSb.append(")");
            index.set(1 + index.get());
        }
        sqlSb.append(" SELECT * FROM dual");
        dataGenerateContext.setIndex(index);
        return sqlSb.toString();
    }
}
