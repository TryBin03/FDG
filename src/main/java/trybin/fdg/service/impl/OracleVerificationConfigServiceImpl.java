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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TryBin
 */
@Component("OracleVerificationConfigService")
@Slf4j
public class OracleVerificationConfigServiceImpl implements VerificationConfigService {
    @Autowired
    private SqlExecuteService sqlExecuteService;

    @org.springframework.beans.factory.annotation.Value("${database.username}")
    private String oracleUserName;

    @Override
    public List<String> execute(DataGenerateContext dataGenerateContext) {
        List<String> exceptionContainer = new ArrayList<>();
        Map<String, Long> tableContainer = dataGenerateContext.getTableContainer();
        String findVerificationTableSql =  "SELECT\n" +
                    "    OWNER||'.'||TABLE_NAME  TABLEID,\n" +
                    "    TABLE_NAME                               TABLENAME\n" +
                    "FROM\n" +
                    "    user_constraints\n" +
                    "WHERE INDEX_OWNER = '" + StringUtils.upperCase(oracleUserName) + "'";
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
                    "    '"+ StringUtils.upperCase(oracleUserName) +"'||'.'||TABLE_NAME||'.'||COLUMN_NAME COLUMNID,\n" +
                    "    COLUMN_NAME                                           COLUMNNAME,\n" +
                    "    DATA_TYPE\n" +
                    "FROM\n" +
                    "    user_tab_columns";
        List<VerificationColumns> verificationColumnsList = sqlExecuteService.selectList(findVerificationColumnSql, VerificationColumns.class);

        List<String> databaseColumnNames = verificationColumnsList.stream().map(VerificationColumns::getColumnId).collect(Collectors.toList());
        List<String> columnNames = new ArrayList<>(valuesContainer.keySet());
        columnNames.forEach((columnName) -> {
            if (!databaseColumnNames.contains(columnName)) {
                log.error("在数据库中没有找到 {} 列，请检查配置。", columnName);
                exceptionContainer.add("在数据库中没有找到 " + columnName + " 列，请检查配置。");
            }
        });
        return exceptionContainer;
    }

    @Override
    public Boolean isAdopt(List<String> exceptionContainer){
        return CollectionUtils.isNotEmpty(exceptionContainer);
    }
}
