/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: RowMapperFactory.java
 * Author:   scc
 * Date:     2013-5-13 下午4:19:10
 * Description:映射类型工厂   
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.client.support.rowmapper;

import java.util.Date;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * 
 * 描述：映射类型工厂
 * @param <T> class
 * @author 12010065
 */
public class RowMapperFactory<T> {
    private Class<T> requiredType;

    public RowMapperFactory(Class<T> requiredType) {
        this.requiredType = requiredType;
    }

    public RowMapper<T> getRowMapper() {
        if (requiredType.equals(String.class) || Number.class.isAssignableFrom(requiredType)
                || requiredType.equals(Date.class)) {
            return new SingleColumnRowMapper<T>(requiredType);
        } else {
            return new BeanPropertyRowMapper<T>(requiredType);
        }
    }
}
