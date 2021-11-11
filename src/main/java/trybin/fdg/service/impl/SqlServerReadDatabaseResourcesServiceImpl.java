package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.service.ReadDatabaseResourcesService;
import trybin.fdg.util.DataGenerateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TryBin
 */
@Slf4j
@Component(value = "SqlServerReadDatabaseResourcesService")
public class SqlServerReadDatabaseResourcesServiceImpl implements ReadDatabaseResourcesService {

    @Autowired
    SqlExecuteServiceImpl sqlExecuteService;

    @Override
    public void batchFindColumns(DataGenerateContext dataGenerateContext) {
        String batchFindNotKeyColumnsSql = "SELECT\n" +
                "    col.TABLE_SCHEMA                                        AS SCHEMANAME,\n" +
                "    col.TABLE_NAME                                           AS TABLENAME,\n" +
                "    col.COLUMN_NAME                                            AS COLNAME,\n" +
                "    IIF ( keycol.COLUMN_NAME IS NULL, NULL, 1)                   AS KEYSEQ,\n" +
                "    col.DATA_TYPE                                                   TYPENAME,\n" +
                "    COALESCE(col.CHARACTER_MAXIMUM_LENGTH,col.NUMERIC_PRECISION)    LENGTH\n" +
                "FROM\n" +
                "    INFORMATION_SCHEMA.COLUMNS col\n" +
                "LEFT JOIN\n" +
                "    INFORMATION_SCHEMA.KEY_COLUMN_USAGE keycol\n" +
                "ON\n" +
                "    col.TABLE_NAME = keycol.TABLE_NAME\n" +
                "AND col.COLUMN_NAME = keycol.COLUMN_NAME\n" +
                "ORDER BY\n" +
                "    col.TABLE_NAME";
        List<Columns> columns = sqlExecuteService.selectList(batchFindNotKeyColumnsSql, Columns.class);
        Map<String, Map<String, List<Columns>>> tableStructureContainer = new HashMap<>();
        Map<String, Map<String, List<Columns>>> keyColumnsContainer = new HashMap<>();
        Map<String, Map<String, List<Columns>>> notKeyColumnsContainer = new HashMap<>();
        Map<String, List<Columns>> valueGroupBySchemaName = columns.stream().collect(Collectors.groupingBy(Columns::getSchemaName));
        valueGroupBySchemaName.forEach((sk, sv)->{
            Map<String, List<Columns>> valueGroupByTableName = sv.stream().collect(Collectors.groupingBy(Columns::getTableName));
            tableStructureContainer.put(sk,valueGroupByTableName);
            keyColumnsContainer.put(sk, sv.stream().filter(s-> s.getKeyseq() != null
                    && s.getKeyseq() instanceof Long ? (Long) s.getKeyseq() == 1L : String.valueOf(s.getKeyseq()).equals("1")).collect(Collectors.groupingBy(Columns::getTableName)));
            notKeyColumnsContainer.put(sk, sv.stream().filter(s-> s.getKeyseq() == null).collect(Collectors.groupingBy(Columns::getTableName)));
        });
        dataGenerateContext.setTableStructureContainer(tableStructureContainer);
        dataGenerateContext.setNotKeyColumnsContainer(notKeyColumnsContainer);
        dataGenerateContext.setKeyColumnsContainer(keyColumnsContainer);
    }


    @Override
    public List<Columns> getColumns(DataGenerateContext dataGenerateContext) {
        String getColNameSql = "SELECT\n" +
                "    col.COLUMN_NAME                                           AS COLNAME,\n" +
                "    IIF ( keycol.COLUMN_NAME IS NULL, NULL, 1)                   AS KEYSEQ,\n" +
                "    col.DATA_TYPE                                                   TYPENAME,\n" +
                "    COALESCE(col.CHARACTER_MAXIMUM_LENGTH,col.NUMERIC_PRECISION)    LENGTH\n" +
                "FROM\n" +
                "    INFORMATION_SCHEMA.COLUMNS col\n" +
                "LEFT JOIN\n" +
                "    INFORMATION_SCHEMA.KEY_COLUMN_USAGE keycol\n" +
                "ON\n" +
                "    col.TABLE_NAME = keycol.TABLE_NAME\n" +
                "AND col.COLUMN_NAME = keycol.COLUMN_NAME\n" +
                "WHERE\n" +
                "    col.TABLE_NAME = '${REP0}'\n" +
                "AND col.TABLE_SCHEMA = '${REP1}'\n" +
                "ORDER BY\n" +
                "    col.TABLE_NAME";
        String findColumnsSql = DataGenerateUtil.perfectFindColumnsSql(dataGenerateContext, getColNameSql);
        return sqlExecuteService.selectList(findColumnsSql, Columns.class);
    }
}
