package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.VerificationColumns;
import trybin.fdg.entity.VerificationTable;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.service.SqlExecuteService;
import trybin.fdg.service.VerificationConfigService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TryBin
 */
@Component("MySQLVerificationConfigService")
@Slf4j
public class MysqlVerificationConfigServiceImpl implements VerificationConfigService {
    @Autowired
    SqlExecuteService sqlExecuteService;

    @Override
    public List<String> execute(DataGenerateContext dataGenerateContext) {
        List<String> exceptionContainer = new ArrayList<>();

        Map<String, Long> tableContainer = dataGenerateContext.getTableContainer();

        String findVerificationTableSql  = "select DISTINCT CONCAT_WS('.', TABLE_SCHEMA, TABLE_NAME) TABLEID, TABLE_NAME TABLENAME from information_schema.columns where table_schema not in ('information_schema','mysql','performance_schema')";

        List<VerificationTable> verificationTableList = sqlExecuteService.selectList(findVerificationTableSql, VerificationTable.class);
        List<String> databaseTableNames = verificationTableList.stream().map(VerificationTable::getTableId).collect(Collectors.toList());
        List<String> tableNames = new ArrayList<>(tableContainer.keySet());
        tableNames.forEach((tableName) -> {
            if (!databaseTableNames.contains(tableName)) {
                log.error("在数据库中没有找到 {} 表，请检查配置。", tableName);
                exceptionContainer.add("在数据库中没有找到 " + tableName + " 表，请检查配置。");
            }
        });

        Map<String, Value> valuesContainer = dataGenerateContext.getValuesContainer();
        String findVerificationColumnSql = "SELECT\n" +
                "    CONCAT_WS('.', TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME) COLUMNID,\n" +
                "    COLUMN_NAME                                           COLUMNNAME,\n" +
                "    DATA_TYPE,\n" +
                "    CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) AS UNSIGNED )    LENGTH\n" +
                "FROM\n" +
                "    information_schema.columns\n" +
                "WHERE\n" +
                "    table_schema NOT IN ('information_schema',\n" +
                "                         'mysql',\n" +
                "                         'performance_schema')";
        List<VerificationColumns> verificationColumnsList = sqlExecuteService.selectList(findVerificationColumnSql, VerificationColumns.class);

        List<String> databaseColumnNames = verificationColumnsList.stream().map(VerificationColumns::getColumnId).collect(Collectors.toList());
        Map<String, VerificationColumns> verificationColumnsContainer = verificationColumnsList.stream().collect(Collectors.toMap(VerificationColumns::getColumnId, verificationColumns -> verificationColumns));
        List<String> columnNames = new ArrayList<>(valuesContainer.keySet());
        columnNames.forEach((columnName) -> {
            String tableName = StringUtils.left(columnName, columnName.lastIndexOf("."));
            Long count = tableContainer.get(tableName);
            if (databaseColumnNames.contains(columnName)) {
                Object length = verificationColumnsContainer.get(columnName).getLength();
                if (length != null) {
                    int i = ((BigInteger) length).intValue();
                    if (count > Math.pow(10 ,i)){
                        log.error("所传入数据量 {} ，超过 {} 列最大长度 {}。", count, columnName, i);
                        exceptionContainer.add("所传入数据量 "+ count +" ，超过 "+ columnName +" 列最大长度 "+ i +"。");
                    }
                    if (i < valuesContainer.get(columnName).getValue().length()) {
                        log.error("所传入的 {} 列，超出所设置最大长度，请检查配置。", columnName);
                        exceptionContainer.add("所传入的 " + columnName + " 列，超出所设置最大长度，请检查配置。");
                    }
                }
            }else {
                log.error("在数据库中没有找到 {} 列，请检查配置。", columnName);
                exceptionContainer.add("在数据库中没有找到 " + columnName + " 列，请检查配置。");
            }
        });

        // todo 检察用户传来的值是否合格
        /*Map<String, String> verificationColumns = verificationColumnsList.stream().collect(Collectors.toMap(VerificationColumns::getColumnId, VerificationColumns::getDataType));
        columnNames.forEach((columnName)->{
            String value = valuesContainer.get(columnName).getValue();
            String dataType = verificationColumns.get(columnName);
            if (StringUtils.isEmpty(value)){
                log.error("您没有为 {} 列设置值，请检查配置。", columnName);
                throw new DataGenerateException("您没有为 "+ columnName +" 列设置值，请检查配置。");
            } else if (StringUtils.isAlpha(value)) {

            } else if (StringUtils.isNumeric(value)){

            }
        });*/
        return exceptionContainer;
    }

    @Override
    public Boolean isAdopt(List<String> exceptionContainer){
        return CollectionUtils.isNotEmpty(exceptionContainer);
    }
}
