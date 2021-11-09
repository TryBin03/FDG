package trybin.fdg.service;

import com.github.pagehelper.Page;
import trybin.fdg.entity.OracleColumns;

import java.util.List;
import java.util.Map;

/**
 * @author: TryBin
 * @date: 2021/10/28 22:49:29
 * @version: 0.0.1
 */
public interface SqlExecuteService {
    Integer insert(String statement);

    Integer delete(String statement);

    Integer update(String statement);

    List<Map<String, Object>> selectList(String statement);

    <T> List<T> selectList(String statement, Class<T> clazz);

    public <T> Page<T> selectListWithPage(String statement, int pageNum, int pageSize, String orderBy, Class<T> clazz);
}
