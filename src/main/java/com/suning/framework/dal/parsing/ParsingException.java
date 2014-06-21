/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: ParsingException.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: 解析异常类    
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing;

/**
 * 描述：解析异常类
 * @author 13092011
 */
public class ParsingException extends RuntimeException {
    private static final long serialVersionUID = -176685891441325943L;

    public ParsingException() {
        super();
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
