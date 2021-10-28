package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.service.DataGenerateService;
import trybin.fdg.util.DataGenerateUtil;

import java.util.List;
import java.util.Set;

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

    @Override
    public void process() {
        Long index = 0L;
        DataGenerateContext dataGenerateContext = new DataGenerateContext();
        dataGenerateContext.setCount(count);
        dataGenerateContext.setSqlValuesCount(sqlValuesCount);
        dataGenerateContext.setIndex(index);
        dataGenerateContext.setTable(table);
        dataGenerateContext.setSchema(schema);

        String getColNameSql = "select COLUMN_NAME as COLNAME, IF(COLUMN_KEY = 'PRI',1,null) as KEYSEQ, DATA_TYPE as TYPENAME, CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) as UNSIGNED ) LENGTH from information_schema.columns where table_schema = '${REP0}' and table_name = '${REP1}' ORDER BY ORDINAL_POSITION";
        List<Columns> columnsVos = DataGenerateUtil.getColumnsVos(dataGenerateContext, getColNameSql);
        List<String> colName = DataGenerateUtil.getColName(columnsVos);
        Set<String> keys = DataGenerateUtil.getKeys(columnsVos);
        long dataForm = System.currentTimeMillis();
        List<String> insertSqlBach = DataGenerateUtil.createInsertSqlBach(colName, keys, dataGenerateContext);
        long dataTo = System.currentTimeMillis();
        log.info("数据生成完成，花费时间：{} s。",(dataTo - dataForm) / 1000L);

        // todo
        /*CreateDataConsumerServiceImpl createDataConsumerService = new CreateDataConsumerServiceImpl();
        createDataConsumerService.consumerProcess(insertSqlBach);*/
    }
}

