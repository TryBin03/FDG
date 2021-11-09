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
import trybin.fdg.factory.BuildCreateSqlServiceFactory;
import trybin.fdg.factory.DataRemoveServiceFactory;
import trybin.fdg.factory.ReadDatabaseResourcesServiceFactory;
import trybin.fdg.service.*;
import trybin.fdg.service.helper.ConfigAnalysisHelper;
import trybin.fdg.util.DataGenerateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author TryBin
 * @date: 2021/10/28 15:26:04
 * @version 0.0.1
 */
@Service("dataGenerateService")
@Slf4j
public class DataGenerateServiceImpl implements DataGenerateService {

    @Value("${fdg.submit-count}")
    private Long submitCount;

    @Value("${fdg.sql-values-count}")
    private int sqlValuesCount;

    @Value("${fdg.delete-old-data-flag}")
    private Boolean deleteOldDataFlag;

    @Value("${database.type}")
    private String datasourceType;

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

    @Autowired
    private ReadDatabaseResourcesServiceFactory readDatabaseResourcesServiceFactory;

    @Autowired
    private DataRemoveServiceFactory dataRemoveServiceFactory;

    @Autowired
    private BuildCreateSqlServiceFactory buildCreateSqlServiceFactory;

    @Override
    public void process() {
        DataGenerateContext dataGenerateContext = structureContext();
        List<Columns> columns = readDatabaseResourcesServiceFactory
                .getReadDatabaseResourcesServiceIns(dataGenerateContext.getDatasourceType().name())
                .getColumns(dataGenerateContext);
        // 删除旧数据
        if (deleteOldDataFlag) {
            dataRemoveServiceFactory
                    .getDataRemoveServiceIns(dataGenerateContext.getDatasourceType().name())
                    .process(DataGenerateUtil.getNotKey(columns), dataGenerateContext);
        }

        Set<String> keys = DataGenerateUtil.getKeys(columns);
        log.info("数据生成中...");
        long dataForm = System.currentTimeMillis();
        List<String> insertSqlBach = buildCreateSqlServiceFactory
                .getBuildCreateSqlServiceIns(dataGenerateContext.getDatasourceType().name())
                .execute(columns, keys, dataGenerateContext);
        log.info("数据生成完成，共生成 {} 条，花费时间：{} s。", dataGenerateContext.getIndex().get(), (System.currentTimeMillis() - dataForm) / 1000D);
        log.info("数据插入中...");
        long start = System.currentTimeMillis();
        DataGenerateUtil.insertBatch(sqlExecuteService, insertSqlBach);
        log.info("数据插入完成，共生成 {} 条，花费时间：{} s", dataGenerateContext.getIndex().get(), (System.currentTimeMillis() - start) / 1000D);
    }

    @Override
    public void batchProcess() {
        DataGenerateContext dataGenerateContext = structureBatchContext();

        // 删除旧数据
        if (deleteOldDataFlag) {
            dataRemoveServiceFactory
                    .getDataRemoveServiceIns(dataGenerateContext.getDatasourceType().name())
                    .batchProcess(dataGenerateContext);
        }

        log.info("数据生成中...");
        long dataForm = System.currentTimeMillis();
        List<String> insertSqlBach = new ArrayList<>();
        Map<String, Map<String, List<Columns>>> tableStructureContainer = dataGenerateContext.getTableStructureContainer();
        Map<String, Map<String, List<Columns>>> notKeyColumnsContainer = dataGenerateContext.getNotKeyColumnsContainer();
        Map<String, Map<String, List<Columns>>> keyColumnsContainer = dataGenerateContext.getKeyColumnsContainer();
        AtomicLong size = new AtomicLong(0);
        dataGenerateContext.getDataGenerateContextList().forEach(context->{
            insertSqlBach.addAll(buildCreateSqlServiceFactory.getBuildCreateSqlServiceIns(context.getDatasourceType().name()).execute(
                    tableStructureContainer.get(context.getSchema()).get(context.getTable()),
                    keyColumnsContainer.get(context.getSchema()).get(context.getTable()).stream().map(Columns::getColname).collect(Collectors.toSet()),
                    context));
            size.addAndGet(context.getIndex().get());
        });
        log.info("数据生成完成，共生成 {} 条，花费时间：{} s。", size, (System.currentTimeMillis() - dataForm) / 1000D);
        log.info("数据插入中...");
        long start = System.currentTimeMillis();
        DataGenerateUtil.insertBatch(sqlExecuteService, insertSqlBach);
        log.info("数据插入完成，共生成 {} 条，花费时间：{} s", size, (System.currentTimeMillis() - start) / 1000D);
    }

    private DataGenerateContext structureBatchContext() {
        DataGenerateContext dataGenerateContext = new DataGenerateContext();
        dataGenerateContext.setSqlValuesCount(sqlValuesCount);
        dataGenerateContext.setIndex(new AtomicLong(0));
        dataGenerateContext.setDatasourceType(DATASOURCE_TYPE.getType(datasourceType));
        readDatabaseResourcesServiceFactory
                .getReadDatabaseResourcesServiceIns(dataGenerateContext.getDatasourceType().name())
                .batchFindColumns(dataGenerateContext);
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
        dataGenerateContext.setDatasourceType(DATASOURCE_TYPE.getType(datasourceType));
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


}

