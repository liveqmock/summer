/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: FreeMakerParser.java
 * Author:   12010065
 * Date:     2013-5-13 下午4:19:10
 * Description: FreeMaker解析  
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.suning.framework.dal.exception.DalException;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * FreeMaker解析<br>
 * 
 * @author 12010065
 * 
 */
public class FreeMakerParser {
    private static final String DEFAULT_TEMPLATE_KEY = "default_template_key";
    private static final String DEFAULT_TEMPLATE_EXPRESSION = "default_template_expression";
    private static final Configuration CONFIGURER = new Configuration();
    
    /**
     * 表达式缓存
     */
    private static Map<String, Template> expressionCache = new HashMap<String, Template>();

    public static String process(String expression, Object root) {
        StringReader reader = null;
        StringWriter out = null;
        Template template = null;
        try {
            if (expressionCache.get(expression) != null) {
                template = expressionCache.get(expression);
            }
            if (template == null) {
                template = createTemplate(DEFAULT_TEMPLATE_EXPRESSION, new StringReader(expression));
                expressionCache.put(expression, template);
            }
            out = new StringWriter();
            template.process(root, out);
            return out.toString();
        } catch (Exception e) {
            throw new DalException(e);
        } finally {
            if (reader != null) {
                reader.close();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static Template createTemplate(String templateKey, StringReader reader) throws IOException {
        Template template = new Template(DEFAULT_TEMPLATE_KEY, reader, CONFIGURER);
        template.setNumberFormat("#");
        return template;
    }
}
