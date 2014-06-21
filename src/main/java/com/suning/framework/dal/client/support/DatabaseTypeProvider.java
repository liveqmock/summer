/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: DatabaseTypeProvider.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: 数据库类型提供方   
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.client.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suning.framework.dal.client.support.executor.DBType;
/**
 * 描述：数据库类型提供方
 * @author 13092011/jorgie
 */
public class DatabaseTypeProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseTypeProvider.class);

    private static Properties properties;

    /**
     * 功能描述：获取数据库类型<br>
     * 输入参数：数据源<按照参数定义顺序> 
     * @param dataSource
     * 返回值:  数据库类型 <说明> 
     * @return DBType
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public static DBType getDatabaseType(DataSource dataSource) {
        if (dataSource == null){
            throw new NullPointerException("dataSource cannot be null");
        }
        try {
            String databaseName = getDatabaseName(dataSource);
            if(databaseName.toLowerCase().contains("db2")) {
                return DBType.DB2;
            } else if(databaseName.toLowerCase().contains("mysql")) {
                return DBType.MYSQL;
            } else if(databaseName.toLowerCase().contains("oracle")) {
                return DBType.ORACLE;
            }
        } catch (Exception e) {
            logger.error("Could not get a databaseId from dataSource", e);
        }
        return null;
    }

    /**
     * 功能描述：获取数据库名字<br>
     * 输入参数：数据源<按照参数定义顺序> 
     * @param dataSource
     * 返回值:  数据库名称 <说明> 
     * @return String
     * @throw SQLException
     * @see 需要参见的其它内容
     */
    private static String getDatabaseName(DataSource dataSource) throws SQLException {
        String productName = getDatabaseProductName(dataSource);
        if (properties != null) {
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                if (productName.contains((String) property.getKey())) {
                    return (String) property.getValue();
                }
            }
            return null; // no match, return null
        }
        return productName;
    }

    /**
     * 功能描述：获取数据库结果名称<br>
     * 输入参数：数据源<按照参数定义顺序> 
     * @param dataSource
     * 返回值:  数据库名称 <说明> 
     * @return String
     * @throw SQLException
     * @see 需要参见的其它内容
     */
    private static String getDatabaseProductName(DataSource dataSource) throws SQLException {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            return metaData.getDatabaseProductName();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    // ignored
                }
            }
        }
    }

}
