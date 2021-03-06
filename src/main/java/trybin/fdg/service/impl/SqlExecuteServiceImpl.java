package trybin.fdg.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trybin.fdg.dao.SqlExecuteMapper;
import trybin.fdg.entity.OracleColumns;
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
@Service("sqlExecuteService")
public class SqlExecuteServiceImpl implements SqlExecuteService {

    private boolean ignorePropertyNotFitFlg = false;

    @Autowired
    private SqlExecuteMapper sqlExecuteMapper;

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
        return sqlExecuteMapper.insert(statement);
    }

    @Override
    public Integer delete(String statement) {
        return sqlExecuteMapper.delete(statement);
    }

    @Override
    public Integer update(String statement) {
        return sqlExecuteMapper.update(statement);
    }

    @Override
    public List<Map<String, Object>> selectList(String statement) {
        return sqlExecuteMapper.selectList(statement);
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
                                        log.warn(className+":["+mapKey+"]"+propType+"-->"+valueType+"????????????????????????????????????????????????");
                                    }
                                } catch (InvocationTargetException | NoSuchMethodException | BeanConvertException e) {
                                    e.printStackTrace();
                                    if (e instanceof BeanConvertException) {
                                        throw (BeanConvertException)e;
                                    } else {
                                        throw new BeanConvertException("???????????????Map??????????????????Bean??????????????????", e);
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
        log.debug("??????Map?????????????????????????????????{}??????", end-start);
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
     * ????????????????????????
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
            String errMsg = "?????????["+className+"]?????????["+mapKey+"]?????????["+propType.getName()+"]???????????????????????????["+valueType.getName()+"]?????????????????????????????????";
            log.error(errMsg);
            if (!ignorePropertyNotFitFlg) {
                throw new BeanConvertException(errMsg);
            }
        }
        return fitClassFlg;
    }

    // ??????????????????
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
     * ???????????????class?????????????????????Number??????
     *
     * @param clazz
     * @return ????????????
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
        // ????????????????????????????????????
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
