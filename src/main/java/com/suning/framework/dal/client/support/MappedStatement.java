/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: MappedStatement.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: sql描述     
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.client.support;

import java.util.Map;

import com.suning.framework.dal.parsing.FreeMakerParser;

/**
 * 描述：sql描述
 * @author 作者 13092011
 */
public final class MappedStatement {

    private String resource;
    private Configuration configuration;
    private String id;
    private String idProperty;
    




    private String keyGenerator;
    private int fetchSize;
    private int maxRows;
    private int timeout; 
    private boolean isRead;
    private String sqlSource;
    private String dsName;
    private SqlCommandType sqlCommandType;
    
    
    
    public String getIdProperty() {
        return idProperty;
    }




    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }
    
    
    

    public String getResource() {
        return resource;
    }




    public void setResource(String resource) {
        this.resource = resource;
    }




    public Configuration getConfiguration() {
        return configuration;
    }




    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }




    public String getId() {
        return id;
    }




    public void setId(String id) {
        this.id = id;
    }




    public Integer getFetchSize() {
        return fetchSize;
    }




    public Integer getTimeout() {
        return timeout;
    }



    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }



    public boolean isRead() {
        return isRead;
    }




    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }




    public String getSqlSource() {
        return sqlSource;
    }




    public void setSqlSource(String sqlSource) {
        this.sqlSource = sqlSource;
    }




    public String getDsName() {
        return dsName;
    }




    public void setDsName(String dsName) {
        this.dsName = dsName;
    }




    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }




    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }




    public String getBoundSql(Map<String,Object> parameterMap) {
        return FreeMakerParser.process(this.sqlSource, parameterMap);
    }




    public Integer getMaxRows() {
        return maxRows;
    }




    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }



    public String getKeyGenerator() {
        return keyGenerator;
    }




    public void setKeyGenerator(String keyGenerator) {
        this.keyGenerator = keyGenerator;
    }




    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }




    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
    

}
