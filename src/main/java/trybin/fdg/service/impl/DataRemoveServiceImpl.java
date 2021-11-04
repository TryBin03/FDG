package trybin.fdg.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.enums.MySQL_DATA_TYPE;
import trybin.fdg.service.DataRemoveService;
import trybin.fdg.service.SqlExecuteService;

import java.util.List;

@Slf4j
@Service("dataRemoveService")
public class DataRemoveServiceImpl implements DataRemoveService {

    @Autowired
    private SqlExecuteService sqlExecuteService;

    @Override
    public void process(List<Columns> columnsNotKet, DataGenerateContext dataGenerateContext) {
        log.info("删除服务启动...");
        long start = System.currentTimeMillis();
        String removeSql = generateRemoveSql(dataGenerateContext.getSchema(), dataGenerateContext.getTable(), columnsNotKet);
        sqlExecuteService.delete(removeSql);
        log.info("删除完成，共耗时 {} s。", (System.currentTimeMillis() - start) / 1000D);
    }

    private String generateRemoveSql(String schema, String table, List<Columns> columnsNotKet) {
        StringBuffer sb = new StringBuffer();
        sb.append("DELETE FROM ").append(schema).append(".").append(table);
        sb.append(" WHERE ");
        columnsNotKet.forEach(column -> {
            sb.append(column.getColname()).append(" = ").append("'");
            // 排除时间类型
            String typename = column.getTypename();
            if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATE.name(), typename)) {
                sb.append("1970-01-01");
            } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIME.name(), typename)) {
                sb.append("00:00:00");
            } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.DATETIME.name(), typename) || StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.TIMESTAMP.name(), typename)) {
                sb.append("1970-01-01 00:00:00");
            } else if (StringUtils.equalsIgnoreCase(MySQL_DATA_TYPE.YEAR.name(), typename)) {
                sb.append("1970");
            }
            // 默认为 1
            else {
                sb.append("1");
            }
            sb.append("'").append(" AND ");
        });
        sb.delete(sb.length() - 4, sb.length());
        return sb.toString();
    }
}
