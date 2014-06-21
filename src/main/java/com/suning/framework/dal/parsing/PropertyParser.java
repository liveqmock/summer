/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: PropertyParser.java
 * Author:   13092011
 * Date:     2013-12-1 下午4:48:08
 * Description: 属性解析类   
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing;

import java.util.Properties;
/**
 * 描述：属性解析类
 * @author 13092011
 */
public class PropertyParser {

    public static String parse(String string, Properties variables) {
        VariableTokenHandler handler = new VariableTokenHandler(variables);
        GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
        return parser.parse(string);
    }

    /**
     * 描述：变量处理器
     * @author 作者 13092011
     */
    private static class VariableTokenHandler implements TokenHandler {
        private Properties variables;

        public VariableTokenHandler(Properties variables) {
            this.variables = variables;
        }

        public String handleToken(String content) {
            if (variables != null && variables.containsKey(content)) {
                return variables.getProperty(content);
            }
            return "${" + content + "}";
        }
    }
}
