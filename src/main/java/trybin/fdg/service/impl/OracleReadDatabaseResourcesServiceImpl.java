package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.entity.OracleColumns;
import trybin.fdg.service.ReadDatabaseResourcesService;
import trybin.fdg.util.DataGenerateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TryBin
 */
@Slf4j
@Component(value = "OracleReadDatabaseResourcesService")
public class OracleReadDatabaseResourcesServiceImpl implements ReadDatabaseResourcesService {

    @Autowired
    SqlExecuteServiceImpl sqlExecuteService;

    @Override
    public void batchFindColumns(DataGenerateContext dataGenerateContext) {
        String batchFindNotKeyColumnsSql = "select TABLE_SCHEMA as SCHEMANAME, TABLE_NAME as TABLENAME, COLUMN_NAME as COLNAME, IF(COLUMN_KEY = 'PRI',1,null) as KEYSEQ, DATA_TYPE as TYPENAME, CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) as UNSIGNED ) LENGTH from information_schema.columns where table_schema not in ('information_schema','mysql','performance_schema') ORDER BY ORDINAL_POSITION";
        List<Columns> columns = sqlExecuteService.selectList(batchFindNotKeyColumnsSql, Columns.class);
        Map<String, Map<String, List<Columns>>> tableStructureContainer = new HashMap<>();
        Map<String, Map<String, List<Columns>>> keyColumnsContainer = new HashMap<>();
        Map<String, Map<String, List<Columns>>> notKeyColumnsContainer = new HashMap<>();
        Map<String, List<Columns>> valueGroupBySchemaName = columns.stream().collect(Collectors.groupingBy(Columns::getSchemaName));
        valueGroupBySchemaName.forEach((sk, sv)->{
            Map<String, List<Columns>> valueGroupByTableName = sv.stream().collect(Collectors.groupingBy(Columns::getTableName));
            tableStructureContainer.put(sk,valueGroupByTableName);
            keyColumnsContainer.put(sk, sv.stream().filter(s-> s.getKeyseq() != null
                    && s.getKeyseq() instanceof Long ? (Long) s.getKeyseq() == 1L : "1".equals((String) s.getKeyseq())).collect(Collectors.groupingBy(Columns::getTableName)));
            notKeyColumnsContainer.put(sk, sv.stream().filter(s-> s.getKeyseq() == null).collect(Collectors.groupingBy(Columns::getTableName)));
        });
        dataGenerateContext.setTableStructureContainer(tableStructureContainer);
        dataGenerateContext.setNotKeyColumnsContainer(notKeyColumnsContainer);
        dataGenerateContext.setKeyColumnsContainer(keyColumnsContainer);
    }


    @Override
    public List<Columns> getColumns(DataGenerateContext dataGenerateContext) {
        String getColNameSql  = "SELECT\n" +
                    "    utc.COLUMN_NAME                          AS COLNAME,\n" +
                    "    DECODE(uc.constraint_type, 'P', 1, NULL) AS KEYSEQ,\n" +
                    "    DATA_TYPE                                AS TYPENAME,\n" +
                    "    DATA_LENGTH                              AS LENGTH\n" +
                    "FROM\n" +
                    "    user_tab_columns utc\n" +
                    "LEFT JOIN\n" +
                    "    user_cons_columns ucc\n" +
                    "ON\n" +
                    "    utc.TABLE_NAME = ucc.TABLE_NAME\n" +
                    "AND utc.COLUMN_NAME = ucc.COLUMN_NAME\n" +
                    "LEFT JOIN\n" +
                    "    user_constraints uc\n" +
                    "ON\n" +
                    "    ucc.constraint_name = uc.constraint_name\n" +
                    "WHERE\n" +
                    "   utc.TABLE_NAME = '${REP1}'  AND\n" +
                    "    uc.index_name IS NOT NULL\n" +
                    "OR  (utc.TABLE_NAME = 'test_1' AND\n" +
                    "        uc.index_name IS NULL\n" +
                    "    AND ucc.constraint_name IS NULL)";
        String findColumnsSql = DataGenerateUtil.perfectFindColumnsSql(dataGenerateContext, getColNameSql);
        return new ArrayList<>(sqlExecuteService.selectList(findColumnsSql, OracleColumns.class));
    }

}
