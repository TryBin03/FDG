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
@Component("MySQLBuildCreateSqlService")
public class MysqlBuildCreateSqlServiceImpl implements BuildCreateSqlService {

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
                        sqlSb.append(DataGenerateUtil.getIndex(index.get(),column.getLength()));
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
}
