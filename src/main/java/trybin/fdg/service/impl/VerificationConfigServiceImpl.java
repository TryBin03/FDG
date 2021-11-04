package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.VerificationColumns;
import trybin.fdg.entity.VerificationTable;
import trybin.fdg.entity.batchconfig.Value;
import trybin.fdg.service.SqlExecuteService;
import trybin.fdg.service.VerificationConfigService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TryBin
 */
@Service("verificationConfigService")
@Slf4j
public class VerificationConfigServiceImpl implements VerificationConfigService {
    @Autowired
    SqlExecuteService sqlExecuteService;

    @Override
    public List<String> execute(DataGenerateContext dataGenerateContext) {
        List<String> exceptionContainer = new ArrayList<>();

        Map<String, Long> tableContainer = dataGenerateContext.getTableContainer();

        String findVerificationTableql = "select DISTINCT CONCAT_WS('.', TABLE_SCHEMA, TABLE_NAME) TABLEID, TABLE_NAME TABLENAME from information_schema.columns where table_schema not in ('information_schema','mysql','performance_schema')";
        List<VerificationTable> verificationTableList = sqlExecuteService.selectList(findVerificationTableql, VerificationTable.class);
        List<String> databaseTableNames = verificationTableList.stream().map(VerificationTable::getTableId).collect(Collectors.toList());
        List<String> tableNames = new ArrayList<>(tableContainer.keySet());
        if (!databaseTableNames.containsAll(tableNames)) {
            tableNames.forEach((tableName)->{
                if (databaseTableNames.contains(tableName)) {
                    log.error("在数据库中没有找到 {} 表，请检查配置。", tableName);
                    exceptionContainer.add("在数据库中没有找到 "+ tableName +" 表，请检查配置。");
                }
            });
        }

        Map<String, Value> valuesContainer = dataGenerateContext.getValuesContainer();
        String findVerificationColumnSql = "select CONCAT_WS('.', TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME) COLUMNID, COLUMN_NAME COLUMNNAME, DATA_TYPE from information_schema.columns where table_schema not in ('information_schema','mysql','performance_schema')";
        List<VerificationColumns> verificationColumnsList = sqlExecuteService.selectList(findVerificationColumnSql, VerificationColumns.class);

        List<String> databaseColumnNames = verificationColumnsList.stream().map(VerificationColumns::getColumnId).collect(Collectors.toList());
        List<String> columnNames = new ArrayList<>(valuesContainer.keySet());
        if (!databaseColumnNames.containsAll(columnNames)) {
            columnNames.forEach((columnName)->{
                if (databaseTableNames.contains(columnName)) {
                    log.error("在数据库中没有找到 {} 列，请检查配置。", columnName);
                    exceptionContainer.add("在数据库中没有找到 "+ columnName +" 列，请检查配置。");
                }
            });
        }

        // todo
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
