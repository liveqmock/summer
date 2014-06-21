/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: SimpleSqlAuditor.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: 简单SQL跟踪器，实现接口的audit跟踪方法
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.client.support.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 描述：简单SQL跟踪器<br>
 * 实现接口的audit跟踪方法
 * @author 13092011/jorgie
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class SimpleSqlAuditor implements SqlAuditor {

    private transient final Logger logger = LoggerFactory.getLogger(SimpleSqlAuditor.class);

    /**
     * 实现sql跟踪器的audit方法，具体是打出告警日志<br>
     * 输入参数：sql，参数，sql执行毫秒数<按照参数定义顺序> 
     * @param sql，param，interval
     * 返回值:  <说明> 
     * @return 
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    public void audit(String sql, Object param, long interval) {
        logger.warn("SQL Statement [{}] with parameter object [{}] ran out of the normal time range, " +
            "it consumed [{}] milliseconds.",new Object[] { sql, param, interval });
    }

}
