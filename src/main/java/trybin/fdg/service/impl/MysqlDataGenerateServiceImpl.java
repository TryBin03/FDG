package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import trybin.fdg.config.FdgBatchConfig;
import trybin.fdg.config.FdgSingleTableConfig;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.enums.DATASOURCE_TYPE;
import trybin.fdg.exception.DataGenerateException;
import trybin.fdg.service.DataGenerateService;
import trybin.fdg.service.DataRemoveService;
import trybin.fdg.service.SqlExecuteService;
import trybin.fdg.service.VerificationConfigService;
import trybin.fdg.service.helper.ConfigAnalysisHelper;
import trybin.fdg.util.DataGenerateUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author TryBin
 * @date: 2021/10/28 15:26:04
 * @version 0.0.1
 */
@Service("mysqlDataGenerateService")
@Slf4j
public class MysqlDataGenerateServiceImpl implements DataGenerateService {

    @Value("${fdg.submit-count}")
    private Long submitCount;

    @Value("${fdg.sql-values-count}")
    private int sqlValuesCount;

    @Value("${fdg.delete-old-data-flag}")
    private Boolean deleteOldDataFlag;

    @Autowired
    private SqlExecuteService sqlExecuteService;

    @Autowired
    private DataRemoveService dataRemoveService;

    @Autowired
    private ConfigAnalysisHelper configAnalysisHelper;

    @Autowired
    private FdgBatchConfig fdgBachConfig;

    @Autowired
    private FdgSingleTableConfig fdgSingleTableConfig;

    @Autowired
    private VerificationConfigService verificationConfigService;

    @Override
    public void process() {
        DataGenerateContext dataGenerateContext = structureContext();

        List<Columns> columns = getColumns(dataGenerateContext);
        // 删除旧数据
        if (deleteOldDataFlag) {
            dataRemoveService.process(DataGenerateUtil.getNotKey(columns), dataGenerateContext);
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

    @Override
    public void batchProcess() {
        DataGenerateContext dataGenerateContext = structureBatchContext();
    }

    private DataGenerateContext structureBatchContext() {
        DataGenerateContext dataGenerateContext = new DataGenerateContext();
        dataGenerateContext.setSqlValuesCount(sqlValuesCount);
        dataGenerateContext.setIndex(new AtomicLong(0));
        dataGenerateContext.setDatasourceType(DATASOURCE_TYPE.MySQL);
        // 解析
        configAnalysisHelper.batchAnalysis(fdgBachConfig.getGroupList(), dataGenerateContext);
        // 校验
        if (verificationConfigService.isAdopt(verificationConfigService.execute(dataGenerateContext))) {
            log.error("配置校验未通过，请检查配置。");
            throw new DataGenerateException("配置校验未通过，请检查配置。");
        }
        dataGenerateContext.setDataGenerateContextList(DataGenerateUtil.buildTask(dataGenerateContext));
        return dataGenerateContext;
    }

    private DataGenerateContext structureContext() {
        DataGenerateContext dataGenerateContext = new DataGenerateContext();
        dataGenerateContext.setDatasourceType(DATASOURCE_TYPE.MySQL);
        dataGenerateContext.setSqlValuesCount(sqlValuesCount);
        dataGenerateContext.setIndex(new AtomicLong(0));
        dataGenerateContext.setCount(fdgSingleTableConfig.getCount());
        dataGenerateContext.setTable(fdgSingleTableConfig.getTable());
        dataGenerateContext.setSchema(fdgSingleTableConfig.getSchema());
        // 解析
        configAnalysisHelper.analysis(fdgSingleTableConfig.getValueList(), dataGenerateContext);
        // 校验
        if (verificationConfigService.isAdopt(verificationConfigService.execute(dataGenerateContext))) {
            log.error("配置校验未通过，请检查配置。");
            throw new DataGenerateException("配置校验未通过，请检查配置。");
        }
        return dataGenerateContext;
    }

    @Override
    public List<Columns> getColumns(DataGenerateContext dataGenerateContext) {
        String getColNameSql = "select COLUMN_NAME as COLNAME, IF(COLUMN_KEY = 'PRI',1,null) as KEYSEQ, DATA_TYPE as TYPENAME, CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) as UNSIGNED ) LENGTH from information_schema.columns where table_schema = '${REP0}' and table_name = '${REP1}' ORDER BY ORDINAL_POSITION";
        String findColumnsSql = DataGenerateUtil.perfectFindColumnsSql(dataGenerateContext, getColNameSql);
        return sqlExecuteService.selectList(findColumnsSql, Columns.class);
    }
}

