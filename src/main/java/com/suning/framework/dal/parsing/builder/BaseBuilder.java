/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: BaseBuilder.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: 基本生成类    
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.suning.framework.dal.client.support.Configuration;
/**
 * 描述：基本生成类
 * @author 13092011/jorgie
 */
public abstract class BaseBuilder {
    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 功能描述：是否为空，为空则赋默认值<br>
     * 返回值:  Boolean <说明> 
     */
    protected Boolean booleanValueOf(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    /**
     * 功能描述：是否为空，为空则赋默认值<br>
     * 返回值:  Integer <说明> 
     */
    protected Integer integerValueOf(String value, Integer defaultValue) {
        return value == null ? defaultValue : Integer.valueOf(value);
    }

    /**
     * 功能描述：是否为空，为空则赋默认值<br>
     * 返回值:  Set<String> <说明> 
     */
    protected Set<String> stringSetValueOf(String value, String defaultValue) {
        String value1 = value == null ? defaultValue : value;
        return new HashSet<String>(Arrays.asList(value1.split(",")));
    }

}
