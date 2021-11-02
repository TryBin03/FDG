package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.enums.DATASOURCE_TYPE;
import trybin.fdg.service.DataGenerateService;
import trybin.fdg.service.DataRemoveService;
import trybin.fdg.service.SqlExecuteService;
import trybin.fdg.util.DataGenerateUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: TryBin
 * @date: 2021/10/28 15:26:04
 * @version: 0.0.1
 */
@Service("mysqlDataGenerateService")
@Slf4j
public class MysqlDataGenerateServiceImpl implements DataGenerateService {

    @Value("${fdg.table}")
    private String table;

    @Value("${fdg.schema}")
    private String schema;

    @Value("${fdg.count}")
    private Long count;

    @Value("${fdg.submit-count}")
    private Long submitCount;

    @Value("${fdg.sql-values-count}")
    private int sqlValuesCount;

    @Value("${fdg.delete-old-data-flg}")
    private Boolean deleteOldDataFlg;

    @Autowired
    private SqlExecuteService sqlExecuteService;

    @Autowired
    private DataRemoveService dataRemoveService;

    @Override
    public void process() {
        DataGenerateContext dataGenerateContext = structureContext();

        List<Columns> columns = getColumns(dataGenerateContext);
        // 删除旧数据
        if (deleteOldDataFlg) {
            dataRemoveService.process(DataGenerateUtil.getNotKey(columns));
        }

        long start = System.currentTimeMillis();
        Set<String> keys = DataGenerateUtil.getKeys(columns);
        log.info("数据生成中...");
        long dataForm = System.currentTimeMillis();
        List<String> insertSqlBach = DataGenerateUtil.createInsertSqlBach(columns, keys, dataGenerateContext);
        int size = insertSqlBach.size();
        log.info("数据生成完成，共生成 {} 条，花费时间：{} s。", size, (System.currentTimeMillis() - dataForm) / 1000D);
        log.info("数据插入中...");
        DataGenerateUtil.insertBatch(sqlExecuteService, insertSqlBach);
        log.info("数据插入完成，共生成 {} 条，花费时间：{} s", size, (System.currentTimeMillis() - start) / 1000D);
    }

    private DataGenerateContext structureContext() {
        DataGenerateContext dataGenerateContext = new DataGenerateContext();
        dataGenerateContext.setCount(count);
        dataGenerateContext.setSqlValuesCount(sqlValuesCount);
        dataGenerateContext.setIndex(new AtomicLong(0));
        dataGenerateContext.setTable(table);
        dataGenerateContext.setSchema(schema);
        dataGenerateContext.setDatasourceType(DATASOURCE_TYPE.MySQL);
        return dataGenerateContext;
    }

    @Override
    public List<Columns> getColumns(DataGenerateContext dataGenerateContext) {
        String getColNameSql = "select COLUMN_NAME as COLNAME, IF(COLUMN_KEY = 'PRI',1,null) as KEYSEQ, DATA_TYPE as TYPENAME, CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) as UNSIGNED ) LENGTH from information_schema.columns where table_schema = '${REP0}' and table_name = '${REP1}' ORDER BY ORDINAL_POSITION";
        String findColumnsSql = DataGenerateUtil.perfectFindColumnsSql(dataGenerateContext, getColNameSql);
        return sqlExecuteService.selectList(findColumnsSql, Columns.class);
    }
}

