/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: DefaultDalClient.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: 默认客户端      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.client.support;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.util.Assert;

import com.suning.framework.dal.client.DalClient;
import com.suning.framework.dal.client.support.audit.SqlAuditor;
import com.suning.framework.dal.client.support.executor.DBType;
import com.suning.framework.dal.client.support.executor.MappedSqlExecutor;
import com.suning.framework.dal.exception.DalException;
import com.suning.framework.dal.parsing.ParsingException;
import com.suning.framework.dal.parsing.annotation.AnnotationSqlMapBuilder;
import com.suning.framework.dal.parsing.io.ResolverUtil;
import com.suning.framework.dal.parsing.xml.XmlSqlMapBuilder;

/**
 * 功能描述：默认客户端
 * @author 13092011
 */
public class DefaultDalClient implements DalClient, InitializingBean {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());
    
    protected Resource[] sqlMapConfigLocation;

    protected String entityPackage;

    protected Configuration configuration = new Configuration();;

    protected DataSource dataSource;

    protected SqlAuditor sqlAuditor;

    protected MappedSqlExecutor mappedSqlExecutor;

    protected boolean profileLongTimeRunningSql;

    protected long longTimeRunningSqlIntervalThreshold;

    public Resource[] getSqlMapConfigLocation() {
        return sqlMapConfigLocation;
    }

    public void setSqlMapConfigLocation(Resource[] sqlMapConfigLocation) {
        this.sqlMapConfigLocation = sqlMapConfigLocation;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean isProfileLongTimeRunningSql() {
        return profileLongTimeRunningSql;
    }

    public void setProfileLongTimeRunningSql(boolean profileLongTimeRunningSql) {
        this.profileLongTimeRunningSql = profileLongTimeRunningSql;
    }

    public long getLongTimeRunningSqlIntervalThreshold() {
        return longTimeRunningSqlIntervalThreshold;
    }

    public void setLongTimeRunningSqlIntervalThreshold(
            long longTimeRunningSqlIntervalThreshold) {
        this.longTimeRunningSqlIntervalThreshold = longTimeRunningSqlIntervalThreshold;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SqlAuditor getSqlAuditor() {
        return sqlAuditor;
    }

    public void setSqlAuditor(SqlAuditor sqlAuditor) {
        this.sqlAuditor = sqlAuditor;
    }

    @Override
    public Number persist(Object entity) {
        return persist(entity, Number.class);
    }

    @Override
    public <T> T persist(Object entity, Class<T> requiredType) {
        assertMapped(entity);
        return mappedSqlExecutor.persist(entity, requiredType);
    }

    @Override
    public int merge(Object entity) {
        assertMapped(entity);
        return mappedSqlExecutor.merge(entity);
    }

    @Override
    public int dynamicMerge(Object entity) {
        assertMapped(entity);
        return mappedSqlExecutor.dynamicMerge(entity);
    }

    @Override
    public int remove(Object entity) {
        assertMapped(entity);
        return mappedSqlExecutor.remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object entity) {
        assertMapped(entityClass);
        return mappedSqlExecutor.find(entityClass, entity);
    }

    @Override
    public <T> T queryForObject(String sqlId, Map<String, Object> paramMap,
            Class<T> requiredType) {
        return mappedSqlExecutor.queryForObject(sqlId, paramMap, requiredType);
    }

    @Override
    public <T> T queryForObject(String sqlId, Object param,
            Class<T> requiredType) {
        return mappedSqlExecutor.queryForObject(sqlId, param, requiredType);
    }

    @Override
    public <T> T queryForObject(String sqlId, Map<String, Object> paramMap,
            RowMapper<T> rowMapper) {
        return mappedSqlExecutor.queryForObject(sqlId, paramMap, rowMapper);
    }

    @Override
    public <T> T queryForObject(String sqlId, Object param,
            RowMapper<T> rowMapper) {
        return mappedSqlExecutor.queryForObject(sqlId, param, rowMapper);
    }

    @Override
    public Map<String, Object> queryForMap(String sqlId,
            Map<String, Object> paramMap) {
        return mappedSqlExecutor.queryForMap(sqlId, paramMap);
    }

    @Override
    public Map<String, Object> queryForMap(String sqlId, Object param) {
        return mappedSqlExecutor.queryForMap(sqlId, param);
    }

    @Override
    public <T> List<T> queryForList(String sqlId, Map<String, Object> paramMap,
            Class<T> requiredType) {
        return mappedSqlExecutor.queryForList(sqlId, paramMap, requiredType);
    }

    @Override
    public <T> List<T> queryForList(String sqlId, Object param,
            Class<T> requiredType) {
        return mappedSqlExecutor.queryForList(sqlId, param, requiredType);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sqlId,
            Map<String, Object> paramMap) {
        return mappedSqlExecutor.queryForList(sqlId, paramMap);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sqlId, Object param) {
        return mappedSqlExecutor.queryForList(sqlId, param);
    }

    @Override
    public <T> List<T> queryForList(String sqlId, Map<String, Object> paramMap,
            RowMapper<T> rowMapper) {
        return mappedSqlExecutor.queryForList(sqlId, paramMap, rowMapper);
    }

    @Override
    public <T> List<T> queryForList(String sqlId, Object param,
            RowMapper<T> rowMapper) {
        return mappedSqlExecutor.queryForList(sqlId, param, rowMapper);
    }

    @Override
    public int execute(String sqlId, Map<String, Object> paramMap) {
        return mappedSqlExecutor.execute(sqlId, paramMap);
    }

    @Override
    public Number execute4PrimaryKey(String sqlId, Map<String, Object> paramMap) {
        return mappedSqlExecutor.execute4PrimaryKey(sqlId, paramMap);
    }

    @Override
    public int execute(String sqlId, Object param) {
        return mappedSqlExecutor.execute(sqlId, param);
    }

    @Override
    public int[] batchUpdate(String sqlId, Map<String, Object>[] batchValues) {
        return mappedSqlExecutor.batchUpdate(sqlId, batchValues);
    }

    @Override
    public Map<String, Object> call(String sqlId, Map<String, Object> paramMap,
            List<SqlParameter> sqlParameters) {
        return mappedSqlExecutor.call(sqlId, paramMap, sqlParameters);
    }

    protected MappedSqlExecutor getMappedSqlExector() {
        return mappedSqlExecutor;
    }

  
    protected void assertMapped(Object entity) {
        if (entity == null) {
            throw new DalException("the entity can't null");
        }
        Class<? extends Object> entityClass = entity.getClass();
        assertMapped(entityClass);
    }

    /**
     * 功能描述：维护映射。<br>
     * 根据实体类型查询configuration对象中是否有该实体类的mappedStatement对象。<br>
     * 若有，则跳过；若没有，则扫描实体类，判断是否有TableRoute或Entity注解。<br>
     * 若有，则日志告警配置的entityPackage下不包含此实体类；若没有，则报错。<br>
     * 输入参数：实体bean<按照参数定义顺序> 
     * @param entityClass
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    protected void assertMapped(Class<?> entityClass) {
        if (entityClass == null) {
            throw new DalException("the entity can't null");
        }
        String sqlId = entityClass.getName() + ".insert";
        MappedStatement mappedStatement = configuration
                .getMappedStatement(sqlId);
        if (mappedStatement == null) {
            if (entityClass.isAnnotationPresent(Entity.class)) {
                logger.debug(
                        "Please configure the entityPackage for {} in order to it can scan the entity classes.",
                        entityClass.getName());
                new AnnotationSqlMapBuilder(configuration, entityClass).parse();
            } else {

                throw new DalException(
                        "The persist method is not support this pojo:"
                                + entityClass.getName());
            }
        }
    }

    /**
     * 描述：在bean初始化时执行
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(dataSource, "Property 'dataSource' is required");

        if (isProfileLongTimeRunningSql()) {
            Assert.isTrue(
                    longTimeRunningSqlIntervalThreshold > 0,
                    "'longTimeRunningSqlIntervalThreshold' should have a positive value " +
                    "if 'profileLongTimeRunningSql' is set to true");
        }

        //生成SqlMap
        buildSqlMap();

        DBType dbType = DatabaseTypeProvider.getDatabaseType(dataSource);
        logger.debug("this 'dataSource' database type is " + dbType);
        mappedSqlExecutor = new MappedSqlExecutor();
        mappedSqlExecutor.setConfiguration(configuration);
        mappedSqlExecutor.setDataSource(dataSource);
        mappedSqlExecutor.setDbType(dbType);
        mappedSqlExecutor.setSqlAuditor(sqlAuditor);
        mappedSqlExecutor
                .setProfileLongTimeRunningSql(isProfileLongTimeRunningSql());
        mappedSqlExecutor
                .setLongTimeRunningSqlIntervalThreshold(longTimeRunningSqlIntervalThreshold);

    }

    /**
     * 功能描述：生成SqlMap。<br>
     * 扫描entityPackage路径下的所有实体类，添加单表操作的sql语句。<br>
     * 并解析SqlMap配置文件，添加sql语句。<br>
     */
    private void buildSqlMap() {
        try {
            Set<Class<? extends Class<?>>> classSet = new HashSet<Class<? extends Class<?>>>();
            if (entityPackage != null && !"".equals(entityPackage)) {
                //如果entityPackage不为空，开始扫描路径下的所有实体类
                ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
                resolverUtil.setClassLoader(getClass().getClassLoader());
                
                //存在Entity注解
                resolverUtil.find(new ResolverUtil.AnnotatedWith(Entity.class), entityPackage);
                classSet.addAll(resolverUtil.getClasses());
                
                for (Class<?> entityClass : classSet) {
                    // 解析 Entity注解
                    new AnnotationSqlMapBuilder(configuration, entityClass)
                            .parse();
                }
            }
            if (sqlMapConfigLocation != null) {
                for (Resource resource : sqlMapConfigLocation) {
                    //解析sqlMap配置文件
                    new XmlSqlMapBuilder(resource.getInputStream(),
                            configuration, resource.getFilename()).parse();
                }
            }
        } catch (ParsingException e) {
            logger.error(this.getClass() + "Error occurred.  Cause: ", e);
            throw e;
        } catch (IOException e) {
            throw new ParsingException("Error occurred.  Cause: ", e);
        }
    }
}
