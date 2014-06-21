/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: TokenHandler.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description:处理接口     
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing;
/**
 * 描述：处理接口
 * @author 13092011
 */
public interface TokenHandler {
    
    /** 处理方法 */
    String handleToken(String content);
}

