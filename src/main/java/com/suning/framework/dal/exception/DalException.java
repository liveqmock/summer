/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: DalException.java
 * Author:   12010065
 * Date:     2013-5-13 下午4:19:10
 * Description: Dal异常类      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.exception;

/**
 * 
 * Dal异常类<br>
 * @author 12010065
 * 
 */
public class DalException extends RuntimeException {
    /**
     */
    private static final long serialVersionUID = 1L;

    public DalException() {
    }

    public DalException(String msg) {
        super(msg);
    }

    public DalException(Throwable exception) {
        super(exception);
    }

    public DalException(String mag, Exception exception) {
        super(mag, exception);
    }
}
