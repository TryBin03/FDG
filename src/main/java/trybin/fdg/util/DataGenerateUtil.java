package trybin.fdg.util;

import org.springframework.beans.factory.annotation.Autowired;
import trybin.fdg.context.DataGenerateContext;
import trybin.fdg.entity.Columns;
import trybin.fdg.service.SqlExecuteService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: TryBin
 * @date: 2021/10/28 18:25:42
 * @version: 0.0.1
 */
public class DataGenerateUtil {

    public static List<Columns> getColumnsVos(DataGenerateContext dataGenerateConText, String getColNameSql) {
        List<String> values = new ArrayList<>(2);
        values.add(dataGenerateConText.getSchema());
        values.add(dataGenerateConText.getTable());
        getColNameSql = StringUtil.replace(getColNameSql, values);
//        try {
            return null;
//            return sqlExecuteService.selectList(getColNameSql, Columns.class);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        throw new NullPointerException();
    }

    public static List<String> getColName(DataGenerateContext dataGenerateConText, String getColNameSql) {
        return getColumnsVos(dataGenerateConText, getColNameSql)
                .stream().map(Columns::getColname).collect(Collectors.toList());
    }

    public static List<String> getColName(List<Columns> columnsVos) {
        return columnsVos
                .stream().map(Columns::getColname).collect(Collectors.toList());
    }

    public static Set<String> getKeys(List<Columns> columnsVos) {
        return columnsVos.stream()
                .filter(s -> null != s.getKeyseq())
                .map(Columns::getColname).collect(Collectors.toSet());
    }

    public static Set<String> getKeys(DataGenerateContext dataGenerateConText, String getColNameSql) {
        return getColumnsVos(dataGenerateConText, getColNameSql)
                .stream().filter(s -> null != s.getKeyseq())
                .map(Columns::getColname).collect(Collectors.toSet());
    }

    public static String createInsertSql(List<String> colNames, Set<String> keys, DataGenerateContext dataGenerateContext) {
        Long count = dataGenerateContext.getCount();
        Integer sqlValuesCount = dataGenerateContext.getSqlValuesCount();
        Long index = dataGenerateContext.getIndex();

        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("INSERT INTO ")
                .append(dataGenerateContext.getTable()).append(" (");
        colNames.forEach(colName->sqlSb.append(colName).append(", "));
        sqlSb.delete(sqlSb.length() - 2, sqlSb.length());
        sqlSb.append(") ");
        sqlSb.append("VALUES ");

        for (int i = 0; i < sqlValuesCount; i++) {
            sqlSb.append("(");
            for (String colName : colNames) {
                sqlSb.append("'");
                // 含有业务意义的值
                if (keys.contains(colName)) {
                    synchronized (index){
                        sqlSb.append(index);
                    }
                }else {
                    sqlSb.append("1");
                }
                sqlSb.append("', ");
            }
            sqlSb.delete(sqlSb.length() - 2,sqlSb.length());
            sqlSb.append("), ");
            index++;
        }
        sqlSb.delete(sqlSb.length() - 2,sqlSb.length());
        synchronized (index){
            dataGenerateContext.setIndex(index);
        }
        return sqlSb.toString();
    }

    public static List<String> createInsertSqlBach(List<String> colNames, Set<String> keys, DataGenerateContext dataGenerateContext) {
        Long count = dataGenerateContext.getCount();
        Integer sqlValuesCount = dataGenerateContext.getSqlValuesCount();
        Long index = dataGenerateContext.getIndex();

        Long realCount = count / sqlValuesCount;
        if (count % sqlValuesCount != 0) {
            realCount++;
        }
        List<String> insertSqlBach = new ArrayList<>();
        for (Long i = 0L; i < realCount; i++) {
            insertSqlBach.add(createInsertSql(colNames, keys, dataGenerateContext));
        }

        return insertSqlBach;
    }
}
