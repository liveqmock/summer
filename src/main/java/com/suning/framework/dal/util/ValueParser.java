/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: ValueParser.java
 * Author:   12010065
 * Date:     2013-5-13 下午4:19:10
 * Description: 对象解析      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.util;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * 对象解析<br> 
 *
 * @author 12010065
 *
 */
public class ValueParser {
    private static Logger logger = LoggerFactory.getLogger(ValueParser.class);

    /**
     * 功能描述：解析方法<br>
     * 根据实体类中的Column注解转为map
     * 输入参数：Object类型<按照参数定义顺序> 
     * @param entity
     * 返回值:  map类型 <说明> 
     * @return  Map<String, Object>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public static Map<String, Object> parser(Object entity) {
        Map<String, Object> values = new HashMap<String, Object>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String key = field.getName();
                Object value = null;
                try {
                    field.setAccessible(true);
                    value = field.get(entity);
                    if (value instanceof java.util.Date) {
                        value = dateFormat(column, (Date) value);
                    }
                } catch (Exception e) {
                    logger.debug("reflect error.[" + field + "]", e);
                } finally {
                    field.setAccessible(false);
                }
                values.put(key, value);
            }
        }

        return values;
    }

    /**
     * 功能描述：日期类型转换
     * 输入参数：字段注解，日期类型<按照参数定义顺序> 
     * @param column，date
     * 返回值:  Object类型 <说明> 
     * @return Object
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    private static Object dateFormat(Column column, Date date) {
        if (date != null && !"".equals(column.columnDefinition())) {
            SimpleDateFormat format = new SimpleDateFormat(column.columnDefinition());
            return format.format(date);
        }
        return date;
    }
}
