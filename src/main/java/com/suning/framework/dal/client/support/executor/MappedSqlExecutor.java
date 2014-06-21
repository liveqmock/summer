/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: MappedSqlExecutor.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: sql执行器。最终调用NamedParameterJdbcTemplate中的数据操作方法
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.client.support.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.object.GenericStoredProcedure;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.suning.framework.dal.client.support.Configuration;
import com.suning.framework.dal.client.support.MappedStatement;
import com.suning.framework.dal.client.support.audit.SqlAuditor;
import com.suning.framework.dal.client.support.rowmapper.RowMapperFactory;
import com.suning.framework.dal.util.DalUtils;
import com.suning.framework.dal.util.ValueParser;
/**
 * 描述：sql执行器<br>
 * 最终调用NamedParameterJdbcTemplate中的数据操作方法
 * @author 13092011/jorgie
 */
public class MappedSqlExecutor extends JdbcTemplate {
    
    /** sql执行超过设置时间后，日志中打印的字符串 */
    public static final String SQL_AUDIT_LOGMESSAGE = "SQL Statement [{}] with parameter object [{}] " +
            "ran out of the normal time range, it consumed [{}] milliseconds.";
    
    private static Logger logger = LoggerFactory.getLogger(MappedSqlExecutor.class);

    protected Configuration configuration;
    
    /** 是否监控sql语句执行 */
    protected boolean profileLongTimeRunningSql;
    
    /** 监控sql语句执行时间的阀值 */
    protected long longTimeRunningSqlIntervalThreshold;
    
    protected ExecutorService sqlAuditorExecutor;
    
    protected NamedParameterJdbcTemplate execution = new NamedParameterJdbcTemplate(this);

    private DBType dbType;

    private SqlAuditor sqlAuditor;

    private String logPrefix;
    

    public boolean isProfileLongTimeRunningSql() {
        return profileLongTimeRunningSql;
    }

    public void setProfileLongTimeRunningSql(boolean profileLongTimeRunningSql) {
        this.profileLongTimeRunningSql = profileLongTimeRunningSql;
    }

    public long getLongTimeRunningSqlIntervalThreshold() {
        return longTimeRunningSqlIntervalThreshold;
    }

    public void setLongTimeRunningSqlIntervalThreshold(long longTimeRunningSqlIntervalThreshold) {
        this.longTimeRunningSqlIntervalThreshold = longTimeRunningSqlIntervalThreshold;
    }

    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
    }

    public DataSource getDataSource() {
        return super.getDataSource();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }
    
    public SqlAuditor getSqlAuditor() {
        return sqlAuditor;
    }

    public void setSqlAuditor(SqlAuditor sqlAuditor) {
        this.sqlAuditor = sqlAuditor;
    }

    /**
     * 功能描述：插入操作<br>
     * 输入参数：实体对象 
     * @param entity
     * 返回值:  数值类型 <说明> 
     * @return Number
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public Number persist(Object entity) {
        return persist(entity, Number.class);
    }
    
    /**
     * 功能描述：插入操作<br>
     * 输入参数：实体对象，主键类型 
     * @param entity,requiredType
     * 返回值:  主键类型 <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    @SuppressWarnings("unchecked")
    public <T> T persist(Object entity, Class<T> requiredType) {
        long startTimestamp = System.currentTimeMillis();
        String insertSQL = null;
        Map<String, Object> paramMap = null;
        try {
            Class<? extends Object> entityClass = entity.getClass();
            String sqlId = entityClass.getName() + ".insert";
            //根据sqlId获取对应的sql描述
            MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
            //将mappedStatement中的其他参数配置到JdbcTemplate
            this.applyStatementSettings(mappedStatement);
            //将实体对象转换为map
            paramMap = ValueParser.parser(entity);
            insertSQL = mappedStatement.getBoundSql(paramMap);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            logMessage("persist", insertSQL, paramMap);
             // 使用默认数据库来查询序列
            if (mappedStatement.getKeyGenerator() != null) { // 支持序列
                Object seq = queryBySequence(mappedStatement.getKeyGenerator(), false);
                paramMap.put(mappedStatement.getIdProperty(), seq);
            }
            
            execution.update(insertSQL, new MapSqlParameterSource(paramMap), keyHolder);
            
            Object key = paramMap.get(mappedStatement.getIdProperty());
            if (key == null || (key instanceof Number && ((Number) key).doubleValue() == 0.0d)) {
                DalUtils.setProperty(entity, mappedStatement.getIdProperty(), keyHolder.getKey());
                return (T) keyHolder.getKey();
            }
            logMessage("persist", insertSQL, paramMap);
            return (T) key;
        }finally {
            if (isProfileLongTimeRunningSql()) {//sql审计
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { insertSQL, paramMap, interval });
                    executeSqlAuditorIfNecessary(insertSQL, paramMap, interval);
                }
            }
        }
        
    }

    /**
     * 功能描述：查询序列
     * @param needUpdate ：DB2为false；MySql为true
     * @return Object
     */
    public Object queryBySequence(String sequence, boolean needUpdate) {
        if (needUpdate) {
            execution.update(sequence, new HashMap<String, Object>());
            Map<String, Object> result = execution.queryForMap("select last_insert_id() as seq",
                    new HashMap<String, Object>());
            return result.get("seq");
        }
        Map<String, Object> result = execution.queryForMap(sequence, new HashMap<String, Object>());
        return result.get("1");//获取第一列
    }

    /**
     * 功能描述：修改操作<br>
     * 输入参数：实体对象
     * @param entity
     * 返回值:  执行成功的记录条数 <说明> 
     * @return int
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public int merge(Object entity) {
        long startTimestamp = System.currentTimeMillis();
        Map<String, Object> paramMap = null;
        String updateSql = null;
        try {
            Class<? extends Object> entityClass = entity.getClass();
            String sqlId = entityClass.getName() + ".update";
            MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(mappedStatement);
            paramMap = ValueParser.parser(entity);
            updateSql = mappedStatement.getBoundSql(paramMap);
            logMessage("merge", updateSql, paramMap);
            int result = execution.update(updateSql, paramMap);
            logMessage("merge", updateSql, paramMap);
            return result;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { updateSql, paramMap, interval });
                    executeSqlAuditorIfNecessary(updateSql, paramMap, interval);
                }
            }
        }
    }

    /**
     * 功能描述：动态修改操作<br>
     * 输入参数：实体对象
     * @param entity
     * 返回值:  执行成功的记录条数 <说明> 
     * @return int
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public int dynamicMerge(Object entity) {
        long startTimestamp = System.currentTimeMillis();
        Map<String, Object> paramMap = null;
        String updateSql = null;
        try {
            Class<? extends Object> entityClass = entity.getClass();
            String sqlId = entityClass.getName() + ".updateDynamic";
            MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(mappedStatement);
            
            paramMap = ValueParser.parser(entity);
            updateSql = parserDynamicMergeSQL(mappedStatement.getBoundSql(paramMap));
            logMessage("dynamicMerge", updateSql, paramMap);
            int result = execution.update(updateSql, paramMap);
            logMessage("dynamicMerge", updateSql, paramMap);

            return result;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { updateSql, paramMap, interval });
                    executeSqlAuditorIfNecessary(updateSql, paramMap, interval);
                }
            }
        }
        
    }
    
    /**
     * 功能描述：除去SQL语句中where前的逗号<br>
     */
    public String parserDynamicMergeSQL(String sql){
        String[] split = sql.split("WHERE");
        String sql2 = split[1];
        String sql1 = split[0];
        StringBuffer  sb  = new StringBuffer(sql1.trim());
        sb.deleteCharAt(sb.length()-1);
        String newSql = sb.toString()+" WHERE "+sql2;
        return newSql;
    }

    /**
     * 功能描述：删除操作<br>
     * 输入参数：实体对象 
     * @param entity
     * 返回值: 执行成功的记录条数 <说明> 
     * @return int
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public int remove(Object entity) {
        long startTimestamp = System.currentTimeMillis();
        Map<String, Object> paramMap = null;
        String removeSql = null;
        try {
            Class<? extends Object> entityClass = entity.getClass();
            String sqlId = entityClass.getName() + ".delete";
            MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(mappedStatement);
            
            paramMap = ValueParser.parser(entity);
            //processTableRoute(paramMap);
            removeSql = mappedStatement.getBoundSql(paramMap);
            logMessage("remove", removeSql, paramMap);
            int result = execution.update(removeSql, paramMap);
            logMessage("remove", removeSql, paramMap);

            return result;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { removeSql, paramMap, interval });
                    executeSqlAuditorIfNecessary(removeSql, paramMap, interval);
                }
            }
        }
        
    }

    /**
     * 功能描述：查询操作<br>
     * 输入参数：实体类型，实体类
     * @param entityClass,entity
     * 返回值:  实体类型 <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> T find(Class<T> entityClass, Object entity) {
        long startTimestamp = System.currentTimeMillis();
        Map<String, Object> paramMap = null;
        String selectSQL = null;
        try {
            String sqlId = entityClass.getName() + ".select";
            MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);

            paramMap = ValueParser.parser(entity);
            selectSQL = mappedStatement.getBoundSql(paramMap);
       
            logMessage("find", selectSQL, paramMap);
            List<T> resultList = execution.query(selectSQL, paramMap, 
                    new RowMapperFactory<T>(entityClass).getRowMapper());
            logMessage("find", selectSQL, paramMap);

            return singleResult(resultList);
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { selectSQL, paramMap, interval });
                    executeSqlAuditorIfNecessary(selectSQL, paramMap, interval);
                }
            }
        }
        
    }
    

    /**
     * 功能描述：根据sql查询对象<br>
     * 输入参数：sqlId，实体参数，实体类型
     * @param sqlId, param，requiredType
     * 返回值:  实体类型 <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> T queryForObject(String sqlId, Object param, Class<T> requiredType) {
        //实体参数会先转为map，再调用重载方法
        return this.queryForObject(sqlId, DalUtils.convertToMap(param), requiredType);
    }

    /**
     * 功能描述：根据sql查询对象<br>
     * 输入参数：sqlId，map类型的参数，实体类型
     * @param sqlId, paramMap, requiredType
     * 返回值:  实体类型 <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> T queryForObject(String sqlId, Map<String, Object> paramMap, Class<T> requiredType) {
        //实体参数会先转为map，实体类型转为实体映射，再调用重载方法
        return this.queryForObject(sqlId, paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
    }

    /**
     * 功能描述：根据sql查询对象<br>
     * 输入参数：sqlId，实体对象，实体映射
     * @param sqlId, param, rowMapper
     * 返回值:  实体类型 <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> T queryForObject(String sqlId, Object param, RowMapper<T> rowMapper) {
        //实体类型转为实体映射，再调用重载方法
        return this.queryForObject(sqlId, DalUtils.convertToMap(param), rowMapper);
    }

    /**
     * 功能描述：根据sql查询对象<br>
     * 输入参数：sqlId，map类型的参数，实体映射
     * @param sqlId, paramMap, rowMapper
     * 返回值:  实体类型 <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> T queryForObject(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper) {
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try {
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            logMessage("queryForObject", sql, paramMap);
            List<T> resultList = execution.query(sql, paramMap, rowMapper);
            logMessage("queryForObject", sql, paramMap);
            return singleResult(resultList);
        } finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, paramMap, interval });
                    executeSqlAuditorIfNecessary(sql, paramMap, interval);
                }
            }
        }
    }
    
    
    /**
     * 功能描述：根据sql查询map<br>
     * 输入参数：sqlId，实体对象 
     * @param  sqlId,param
     * 返回值:  Map类型 <说明> 
     * @return Map<String, Object>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public Map<String, Object> queryForMap(String sqlId, Object param) {
        return this.queryForMap(sqlId, DalUtils.convertToMap(param));
    }

    /**
     * 功能描述：根据sql查询map<br>
     * 输入参数：sqlId，map类型的参数
     * @param  sqlId,paramMap
     * 返回值:  Map类型 <说明> 
     * @return Map<String, Object>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public Map<String, Object> queryForMap(String sqlId, Map<String, Object> paramMap) {
        //processTableRoute(paramMap);
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try {
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            logMessage("queryForMap", sql, paramMap);
            Map<String, Object> map = singleResult(execution.queryForList(sql, paramMap));
            logMessage("queryForMap", sql, paramMap);
            return map;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, paramMap, interval });
                    executeSqlAuditorIfNecessary(sql, paramMap, interval);
                }
            }
        }
        
    }
    
    
    /**
     * 功能描述：根据sql查询list，其中list中元素类型自由指定<br>
     * 输入参数：sqlId，实体对象，实体类型
     * @param  sqlId,param，requiredType
     * 返回值:  list类型，元素为实体类型 <说明> 
     * @return List<T>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> List<T> queryForList(String sqlId, Object param, Class<T> requiredType) {
        return queryForList(sqlId, DalUtils.convertToMap(param), requiredType);
    }

    /**
     * 功能描述：根据sql查询list，其中list中元素类型自由指定<br>
     * 输入参数：sqlId，map类型的参数，实体类型
     * @param  sqlId,paramMap，requiredType
     * 返回值:  list类型，元素为实体类型 <说明> 
     * @return List<T>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> List<T> queryForList(String sqlId, Map<String, Object> paramMap, Class<T> requiredType) {
        return this.queryForList(sqlId, paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
    }

    /**
     * 功能描述：根据sql查询list，其中list中元素类型自由指定<br>
     * 输入参数：sqlId，实体对象，实体映射
     * @param  sqlId,param，rowMapper
     * 返回值:  list类型，元素为实体类型 <说明> 
     * @return List<T>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> List<T> queryForList(String sqlId, Object param, RowMapper<T> rowMapper) {
        return queryForList(sqlId, DalUtils.convertToMap(param), rowMapper);
    }
    
    /**
     * 功能描述：根据sql查询list，其中list中元素类型自由指定<br>
     * 输入参数：sqlId，map类型的参数，实体映射
     * @param  sqlId,paramMap，rowMapper
     * 返回值:  list类型，元素为实体类型 <说明> 
     * @return List<T>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public <T> List<T> queryForList(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper) {
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try {
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            
            logMessage("queryForList(3 paramter)", sql, paramMap);
            List<T> list = execution.query(sql, DalUtils.mapIfNull(paramMap), rowMapper);
            logMessage("queryForList(3 paramter)", sql, paramMap);
            return list;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, paramMap, interval });
                    executeSqlAuditorIfNecessary(sql, paramMap, interval);
                }
            }
        }
        
    }

    
    /**
     * 功能描述：根据sql查询list，其中list中元素为map类型<br>
     * 输入参数：sqlId，实体对象
     * @param  sqlId,param
     * 返回值:  list类型，元素为map类型 <说明> 
     * @return List<Map<String, Object>>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public List<Map<String, Object>> queryForList(String sqlId, Object param) {
        return queryForList(sqlId, DalUtils.convertToMap(param));
    }

    /**
     * 功能描述：根据sql查询list，其中list中元素为map类型<br>
     * 输入参数：sqlId，map类型的参数
     * @param  sqlId,paramMap
     * 返回值:  list类型，元素为map类型 <说明> 
     * @return List<Map<String, Object>>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public List<Map<String, Object>> queryForList(String sqlId, Map<String, Object> paramMap) {
        //processTableRoute(paramMap);
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try{
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            
            logMessage("queryForList(2 paramter)", sql, paramMap);
            List<Map<String, Object>> list = execution.queryForList(sql,
                    DalUtils.mapIfNull(paramMap));
            logMessage("queryForList(2 paramter)", sql, paramMap);
            return list;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, paramMap, interval });
                    executeSqlAuditorIfNecessary(sql, paramMap, interval);
                }
            }
        }
        
    }

    /**
     * 功能描述：根据sql执行自定义操作，返回成功记录数<br>
     * 输入参数：sqlId，实体参数
     * @param  sqlId,param
     * 返回值:  执行成功的记录条数 <说明> 
     * @return int
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public int execute(String sqlId, Object param) {
        return this.execute(sqlId, DalUtils.convertToMap(param));
//        return this.execute(sqlId, ValueParser.parser(param));
    }

    /**
     * 功能描述：根据sql执行自定义操作，返回成功记录数<br>
     * 输入参数：sqlId，map类型的参数
     * @param  sqlId,paramMap
     * 返回值:  执行成功的记录条数 <说明> 
     * @return int
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public int execute(String sqlId, Map<String, Object> paramMap) {
        return this.execute4PrimaryKey(sqlId, paramMap, null).intValue();
    }

    /**
     * 功能描述：根据sql执行自定义操作，返回成功记录数<br>
     * 输入参数：sqlId，map类型的参数
     * @param  sqlId,paramMap
     * 返回值:  执行成功的记录条数 <说明> 
     * @return Numer
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public Number execute4PrimaryKey(String sqlId, Map<String, Object> paramMap) {
        return this.execute4PrimaryKey(sqlId, paramMap, new GeneratedKeyHolder());
    }

    /**
     * 功能描述：根据sql执行自定义操作，返回成功记录数<br>
     * 输入参数：sqlId，map类型的对象，keyHoler对象
     * @param  sqlId,paramMap，keyHolder
     * 返回值:  执行成功的记录条数 <说明> 
     * @return Number
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    private Number execute4PrimaryKey(String sqlId, Map<String, Object> paramMap, KeyHolder keyHolder) {
        //processTableRoute(paramMap);
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try{
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            
            if (keyHolder != null) {
                execution.update(sql,
                        new MapSqlParameterSource(DalUtils.mapIfNull(paramMap)), keyHolder);
                logMessage("execute", sql, paramMap);
                return keyHolder.getKey();
            } else {
                int result = execution.update(sql, DalUtils.mapIfNull(paramMap));
                logMessage("execute", sql, paramMap);
                return result;
            }
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, paramMap, interval });
                    executeSqlAuditorIfNecessary(sql, paramMap, interval);
                }
            }
        }
    }

    /**
     * 功能描述：根据sql批量操作<br>
     * 输入参数：sqlId，map类型的对象数组
     * @param  sqlId, batchValues
     * 返回值:  执行成功的记录条数 <说明> 
     * @return int[] 
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public int[] batchUpdate(String sqlId, Map<String, Object>[] batchValues) {
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if (batchValues != null && batchValues.length != 0 && batchValues[0] != null) {
                paramMap = batchValues[0];
            }
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            logMessage("batchUpdate", sql, String.valueOf(batchValues == null ? 0 : batchValues.length));
            int[] result = execution.batchUpdate(sql, batchValues);
            logMessage("batchUpdate", sql, String.valueOf(batchValues == null ? 0 : batchValues.length));

            return result;
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, batchValues, interval });
                    executeSqlAuditorIfNecessary(sql, batchValues, interval);
                }
            }
        }
        
    }

    /**
     * 功能描述：存储过程调用<br>
     * 存储过程调用时，需要加上schema<br>
     * 输入参数：sqlId，map类型的参数，list集合（元素为SqlParameter类型）
     * @param  sqlId, paramMap，sqlParameters
     * 返回值:  map类型 <说明> 
     * @return  Map<String, Object>
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public Map<String, Object> call(String sqlId, Map<String, Object> paramMap, List<SqlParameter> sqlParameters) {
        long startTimestamp = System.currentTimeMillis();
        String sql = null;
        try {
            Map<String, Object> paramMapTmp = DalUtils.mapIfNull(paramMap);
            MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
            this.applyStatementSettings(stmt);
            sql = stmt.getBoundSql(paramMap);
            
            logMessage("call procedure", sql, paramMapTmp);
            GenericStoredProcedure storedProcedure = new GenericStoredProcedure();
            storedProcedure.setDataSource(this.getDataSource());
            storedProcedure.setSql(sql);
            for (SqlParameter sqlParameter : sqlParameters) {
                storedProcedure.declareParameter(sqlParameter);
            }
            logMessage("call", sql, paramMapTmp);
            return storedProcedure.execute(paramMapTmp);
        }finally {
            if (isProfileLongTimeRunningSql()) {
                long interval = System.currentTimeMillis() - startTimestamp;
                if (interval > getLongTimeRunningSqlIntervalThreshold()) {
                    logger.warn(SQL_AUDIT_LOGMESSAGE,new Object[] { sqlId, paramMap, interval });
                    executeSqlAuditorIfNecessary(sql, paramMap, interval);
                }
            }
        }
        
    }

    /**
     * 功能描述：日志信息
     */
    private void logMessage(String method, String sql, Object object) {
        if (logger.isDebugEnabled()) {
            String target = this.logPrefix == null ? "" : "execute the sql in " + this.logPrefix;
            logger.debug(method + " method {} SQL:{}",target, sql);
            logger.debug(method + " method {} parameter:[{}]",target, object);
        }
    }


    /**
     * 功能描述：将MappedStatement中的其他参数配置到JdbcTemplate
     */
    protected void applyStatementSettings(MappedStatement stmt) {
        int fetchSize = stmt.getFetchSize();
        if (fetchSize > 0) {
            this.setFetchSize(fetchSize);
        }
        int timeout = stmt.getTimeout() > 0 ? stmt.getTimeout() : configuration.getDefaultStatementTimeout();
        this.setQueryTimeout(timeout);
        int maxRows = stmt.getMaxRows();
        if (maxRows > 0) {
            this.setMaxRows(maxRows);
        }
    }

    /**
     * 功能描述：单一结果集<br>
     * 如果结果集中存在多条记录，返回第一条<br>
     * 输入参数：list集合，元素为T
     * @param resultList
     * 返回值:  T <说明> 
     * @return T
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    private <T> T singleResult(List<T> resultList) {
        if (resultList != null) {
            int size = resultList.size();
            if (size > 0) {
                if (logger.isDebugEnabled() && size > 1) {
                    logger.debug("Incorrect result size: expected " + 1 + ", actual " + size
                            + " return the first element.");
                }
                return resultList.get(0);
            }
            if (size == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Incorrect result size: expected " + 1 + ", actual " + size);
                }
                return null;
            }
        }
        return null;
    }
    /**
     * if executing sql out of user specified interval time, then execute the SqlAuditor
     * @param sql
     * @param paramMap
     * @param interval
     */
    private void executeSqlAuditorIfNecessary(final String sql, final Object paramMap, final long interval) {
        createSqlAuditorExecutorIfNecessary();
        if(sqlAuditor != null) {
            this.sqlAuditorExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    sqlAuditor.audit(sql, paramMap, interval);
                    
                }
            });
        }
    }
    
    private void createSqlAuditorExecutorIfNecessary() {
        
        if (sqlAuditor != null  && sqlAuditorExecutor == null) {
            
            int maxSize = Runtime.getRuntime().availableProcessors();
            ThreadFactory tf = new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "MappedSqlExecutor's sql auditor executor.");
                    t.setDaemon(true);
                    return t;
                }
            };
            BlockingQueue<Runnable> queueToUse = new LinkedBlockingQueue<Runnable>();
            sqlAuditorExecutor = new ThreadPoolExecutor(1, maxSize, 60L, TimeUnit.SECONDS, queueToUse, tf,
                    new ThreadPoolExecutor.DiscardPolicy());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (sqlAuditorExecutor == null) {
                        return;
                    }
                    try {
                        sqlAuditorExecutor.awaitTermination(5L, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        logger.warn("interrupted when shuting down the sql auditor executor:\n{}", e);
                    }
                }
            });
        }
        
        
    }

}
