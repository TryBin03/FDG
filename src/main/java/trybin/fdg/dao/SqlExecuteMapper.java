package trybin.fdg.dao;

import java.util.List;
import java.util.Map;

/**
 * @author: TryBin
 * @date: 2021/10/28 22:42:46
 * @version: 0.0.1
 */
public interface SqlExecuteMapper {
    Integer insert(String statement);

    Integer delete(String statement);

    Integer update(String statement);

    List<Map<String, Object>> selectList(String statement);

    String selectOne(String statement);
}
