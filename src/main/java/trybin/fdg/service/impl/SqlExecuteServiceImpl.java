package trybin.fdg.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import trybin.fdg.dao.SqlExecuteMapper;
import trybin.fdg.service.SqlExecuteService;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author: TryBin
 * @date: 2021/10/28 22:50:11
 * @version: 0.0.1
 */
@Slf4j
public class SqlExecuteServiceImpl implements SqlExecuteService {

    private boolean ignorePropertyNotFitFlg = false;

    @Autowired
    private SqlExecuteMapper sqlSiteExecuteMapper;

    class BeanConvertException extends RuntimeException {
        private static final long serialVersionUID = -2855351755428576526L;

        public BeanConvertException(String messages) {
            super(messages);
        }

        public BeanConvertException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    public Integer insert(String statement) {
        return sqlSiteExecuteMapper.insert(statement);
    }

    @Override
    public Integer delete(String statement) {
        return sqlSiteExecuteMapper.delete(statement);
    }

    @Override
    public Integer update(String statement) {
        return sqlSiteExecuteMapper.update(statement);
    }

    @Override
    public List<Map<String, Object>> selectList(String statement) {
        return sqlSiteExecuteMapper.selectList(statement);
    }


    @Override
    public <T> List<T> selectList(String statement, Class<T> clazz) {
        List<Map<String, Object>> selectMapList = selectList(statement);
        List<T> resultList = convertMapsToBeans(selectMapList, clazz);
        return resultList;
    }

    @Override
    public <T> Page<T> selectListWithPage(String statement, int pageNum, int pageSize, String orderBy, Class<T> clazz) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        Page<T> list = (Page<T>)selectList(statement, clazz);
        return list;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> convertMapsToBeans(List<Map<String, Object>> mapList, Class<T> clazz) {
        long start = System.currentTimeMillis();
        Map<String, Class<?>> propMap = new HashMap<>();
        List<T> beanList = new ArrayList<>();
        if (mapList.getClass() == Page.class) {
            Page<Map<String, Object>> mapPage = (Page<Map<String, Object>>) mapList;
            beanList = new Page<>(mapPage.getPageNum(), mapPage.getPageSize());
            Page<T> page = (Page<T>) beanList;
            page.setTotal(mapPage.getTotal());
            page.setReasonable(mapPage.getReasonable());
            page.setPageSizeZero(mapPage.getPageSizeZero());
        }
        boolean isDbType = isDataBaseType(clazz);
        if (isDbType) {
            for (Map<String, Object> obj : mapList) {
                boolean findFlg = false;
                for (Map.Entry<String, Object> entry : obj.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (obj.size() == 1) {
                        propMap.put(key, clazz);
                        findFlg = true;
                    } else if (value != null) {
                        Class<?> propType = clazz;
                        Class<?> valueType = value.getClass();
                        if (propType.isAssignableFrom(valueType)) {
                            propMap.put(key, valueType);
                            findFlg = true;
                            break;
                        }
                    }
                }
                if (findFlg) {
                    break;
                }
            }
        } else {
            for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(clazz)) {
                propMap.put(pd.getName(), pd.getPropertyType());
            }
        }
        for(Map<String, Object> obj : mapList) {
            if (obj == null) {
                continue;
            }
            try {
                T bean = null;
                if (hasZeroParameterConstructor(clazz)) {
                    bean = clazz.newInstance();
                }
                String className = clazz.getName();
                for (Map.Entry<String, Object> entry : obj.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String lowerCamelCaseKey = lowerCamelCase(key);
                    if (value != null) {
                        for (String mapKey : propMap.keySet()) {
                            if (mapKey.equalsIgnoreCase(key)
                                    || mapKey.equalsIgnoreCase(lowerCamelCaseKey)) {
                                Class<?> propType = propMap.get(mapKey);
                                Class<?> valueType = value.getClass();
                                try {
                                    boolean fitClassFlg = checkClazz(className, mapKey, propType, valueType);
                                    if (fitClassFlg) {
                                        if (isDbType) {
                                            bean = (T)value;
                                        } else {
                                            PropertyUtils.setProperty(bean, mapKey, value);
                                        }
                                    } else {
                                        log.warn(className+":["+mapKey+"]"+propType+"-->"+valueType+"，没有找到相对应的类型，转换失败");
                                    }
                                } catch (InvocationTargetException | NoSuchMethodException | BeanConvertException e) {
                                    e.printStackTrace();
                                    if (e instanceof BeanConvertException) {
                                        throw (BeanConvertException)e;
                                    } else {
                                        throw new BeanConvertException("数据库返回Map转换为指定的Bean列表时出错！", e);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                beanList.add(bean);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        log.debug("执行Map转换对象操作，共耗时：{}毫秒", end-start);
        return beanList;
    }

    private boolean hasZeroParameterConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查类型是否一致
     * @param className
     * @param mapKey
     * @param propType
     * @param valueType
     * @return
     * @throws BeanConvertException
     */
    private boolean checkClazz(String className, String mapKey, Class<?> propType, Class<?> valueType) throws BeanConvertException {
        boolean fitClassFlg = false;
        if (propType.isAssignableFrom(valueType)) {
            fitClassFlg = true;
        } else {
            fitClassFlg = false;
            String errMsg = "实体类["+className+"]中属性["+mapKey+"]类型为["+propType.getName()+"]，而数据库返回类型["+valueType.getName()+"]，类型不一致无法处理。";
            log.error(errMsg);
            if (!ignorePropertyNotFitFlg) {
                throw new BeanConvertException(errMsg);
            }
        }
        return fitClassFlg;
    }

    // 小驼峰命名法
    private String lowerCamelCase(String str) {
        str = str.toLowerCase();
        String result = "";
        String[] names = str.split("_");
        for (int i = 0; i < names.length; i++) {
            if (i == 0) {
                result += names[i].substring(0, 1).toLowerCase() + names[i].substring(1);
            } else {
                result += names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
            }
        }
        return result;
    }


    /**
     * 判断传入的class是否可以转换为Number类型
     *
     * @param clazz
     * @return 判断结果
     */
    private boolean isNumber(Class<?> clazz) {
        return (clazz != null)
                && ((Byte.TYPE.isAssignableFrom(clazz))
                || (Short.TYPE.isAssignableFrom(clazz))
                || (Integer.TYPE.isAssignableFrom(clazz))
                || (Long.TYPE.isAssignableFrom(clazz))
                || (Float.TYPE.isAssignableFrom(clazz))
                || (Double.TYPE.isAssignableFrom(clazz))
                || (Number.class.isAssignableFrom(clazz)));
    }

    private boolean isDataBaseType(Class<?> convertClass) {
        // 判断是否为数据库映射类型
        if (String.class.isAssignableFrom(convertClass)
                || Boolean.class.isAssignableFrom(convertClass)
                || isNumber(convertClass)
                || Character.class.isAssignableFrom(convertClass)
                || Date.class.isAssignableFrom(convertClass)
                || Calendar.class.isAssignableFrom(convertClass)
                || java.sql.Clob.class.isAssignableFrom(convertClass)
                || java.sql.Blob.class.isAssignableFrom(convertClass)) {
            return true;
        }
        return false;
    }
}
