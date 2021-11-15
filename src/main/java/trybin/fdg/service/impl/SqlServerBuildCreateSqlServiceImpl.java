package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.enums.DATE_TYPE;
import trybin.fdg.service.BuildCreateSqlService;
import trybin.fdg.service.SqlExecuteService;
import trybin.fdg.util.BuildCreateUtil;
import trybin.fdg.util.DataGenerateUtil;
import trybin.fdg.util.DateUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author TryBin
 */
@Component("SqlServerBuildCreateSqlService")
@Slf4j
public class SqlServerBuildCreateSqlServiceImpl implements BuildCreateSqlService {
    @Autowired
    private SqlExecuteService sqlExecuteService;

    @Override
    public List<String> execute(List<Columns> colNames, Set<String> keys, DataGenerateContext dataGenerateContext) {
        Long count = dataGenerateContext.getCount();
        Integer sqlValuesCount = dataGenerateContext.getSqlValuesCount();
        Map<String, Value> userDefinedValueContainer = dataGenerateContext.getColumnContainer().get(dataGenerateContext.getSchema()).get(dataGenerateContext.getTable());

        Long realCount = count / sqlValuesCount;
        if (count % sqlValuesCount != 0) {
            realCount++;
        }
        List<String> insertSqlBach = new ArrayList<>(100000);
        for (Long i = 0L; i < realCount; i++) {
            insertSqlBach.add(createInsertSql(colNames, keys, dataGenerateContext, userDefinedValueContainer));
            if (insertSqlBach.size() >= 100000){
                log.info("考虑超过最大堆栈，临时插入数据....");
                long start = System.currentTimeMillis();
                DataGenerateUtil.insertBatch(sqlExecuteService, insertSqlBach);
                log.info("临时插入完成，耗时 {} s。", (System.currentTimeMillis() - start) / 1000D);
                insertSqlBach = new ArrayList<>(100000);
            }
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
                String typename = column.getTypename();
                // 用户自定义
                if (!(CollectionUtils.isEmpty(userDefinedValueContainer)) && userDefinedValueContainer.containsKey(column.getColname())){
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATE.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(userDefinedValueContainer.get(column.getColname()).getValue());
                        sqlSb.append("' AS DATE)");
                    } else if(StringUtils.equalsIgnoreCase(DATE_TYPE.TIME.name(), typename)) {
                        sqlSb.append("CAST('");
                        sqlSb.append(userDefinedValueContainer.get(column.getColname()).getValue());
                        sqlSb.append("' AS TIME)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIME.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(userDefinedValueContainer.get(column.getColname()).getValue());
                        sqlSb.append("' AS DATETIME)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIME2.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(userDefinedValueContainer.get(column.getColname()).getValue());
                        sqlSb.append("' AS DATETIME2)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIMEOFFSET.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(userDefinedValueContainer.get(column.getColname()).getValue());
                        sqlSb.append("' AS DATETIMEOFFSET)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.SMALLDATETIME.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(userDefinedValueContainer.get(column.getColname()).getValue());
                        sqlSb.append("' AS SMALLDATETIME)");
                    }
                    else {
                        sqlSb.append("'").append(userDefinedValueContainer.get(column.getColname()).getValue()).append("'");
                    }
                // 主键
                } else if (keys.contains(column.getColname())) {
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATE.name(), typename)){
                        sqlSb.append("CAST('");
                        Date newDate = DateUtils.getDayAfterDate(DateUtils.getDate("1753-01-01"), index.intValue());
                        sqlSb.append(DateUtils.formatDate(BuildCreateUtil.maxDate(newDate, "9999-12-31"), DateUtils.FORMAT_YYYY_MM_DD));
                        sqlSb.append("' AS DATE)");
                    } else if(StringUtils.equalsIgnoreCase(DATE_TYPE.TIME.name(), typename)) {
                        sqlSb.append("CAST('");
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("00:00:00"),index.intValue()), DateUtils.FORMAT_HH_MM_SS));
                        sqlSb.append("' AS TIME)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIME.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("1753-01-01 00:00:00"),index.intValue()),DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                        sqlSb.append("' AS DATETIME)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIME2.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("0001-01-01 00:00:00"),index.intValue()),DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                        sqlSb.append("' AS DATETIME2)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIMEOFFSET.name(), typename)){
                        sqlSb.append("CAST('");
                        sqlSb.append(DateUtils.formatDate(DateUtils.getSecondAfterDate(DateUtils.getDate("1753-01-01 00:00:00"),index.intValue()),DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                        sqlSb.append("' AS DATETIMEOFFSET)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.SMALLDATETIME.name(), typename)){
                        sqlSb.append("CAST('");
                        Date newDate = DateUtils.getSecondAfterDate(DateUtils.getDate("1900-01-01 00:00:00"), index.intValue());
                        sqlSb.append(DateUtils.formatDate(BuildCreateUtil.maxDate(newDate, "2079-06-06 23:59:59"), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
                        sqlSb.append("' AS SMALLDATETIME)");
                    }
                    else {
//                        sqlSb.append("'").append(DataGenerateUtil.getIndex(index.get(),column.getLength())).append("'");
                        sqlSb.append("'").append(index.get()).append("'");
                    }
                // 系统默认
                }else {
                    // 排除时间类型
                    if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATE.name(), typename)){
                        sqlSb.append("CAST('1970-01-01' AS DATE)");
                    } else if(StringUtils.equalsIgnoreCase(DATE_TYPE.TIME.name(), typename)) {
                        sqlSb.append("CAST('00:00:00' AS TIME)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIME.name(), typename)){
                        sqlSb.append("CAST('1970-01-01 00:00:00' AS DATETIME)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIME2.name(), typename)){
                        sqlSb.append("CAST('1970-01-01 00:00:00' AS DATETIME2)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.DATETIMEOFFSET.name(), typename)){
                        sqlSb.append("CAST('1970-01-01 00:00:00' AS DATETIMEOFFSET)");
                    } else if (StringUtils.equalsIgnoreCase(DATE_TYPE.SMALLDATETIME.name(), typename)){
                        sqlSb.append("CAST('1970-01-01 00:00:00' AS SMALLDATETIME)");
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
            sqlSb.append("), ");
            index.set(1 + index.get());
        }
        sqlSb.delete(sqlSb.length() - 2,sqlSb.length());
        dataGenerateContext.setIndex(index);
        return sqlSb.toString();
    }
}
