/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: XmlSqlMapEntityResolver.java
 * Author:   13092011
 * Date:     2013-12-2 下午6:13:04
 * Description: sqlMap实体类解析器
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * 描述：sqlMap实体类解析器
 * @author 13092011/jorgie
 */
public class XmlSqlMapEntityResolver implements EntityResolver {

    private static final Map<String, String> doctypeMap = new HashMap<String, String>();

    private static final String SNF_DAL_MAPPER_DOCTYPE = "-//code.cnsuning.com//DTD sqlMap//EN"
            .toUpperCase(Locale.ENGLISH);
    
    private static final String SNF_DAL_MAPPER_URL = "http://code.cnsuning.com/dtd/snf-dal-sql.dtd"
            .toUpperCase(Locale.ENGLISH);

    private static final String SNF_DAL_MAPPER_DTD = "META-INF/snf-dal-sql.dtd";

    static {
        doctypeMap.put(SNF_DAL_MAPPER_URL, SNF_DAL_MAPPER_DTD);
        doctypeMap.put(SNF_DAL_MAPPER_DOCTYPE, SNF_DAL_MAPPER_DTD);
    }

    /*
     * Converts a public DTD into a local one
     * @param publicId Unused but required by EntityResolver interface
     * @param systemId The DTD that is being requested
     * @return The InputSource for the DTD
     * @throws org.xml.sax.SAXException If anything goes wrong
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        if (publicId != null){
            publicId = publicId.toUpperCase(Locale.ENGLISH);
        }
        if (systemId != null){
            systemId = systemId.toUpperCase(Locale.ENGLISH);
        }

        InputSource source = null;
        try {
            String path = doctypeMap.get(publicId);
            source = getInputSource(path, source);
            if (source == null) {
                path = doctypeMap.get(systemId);
                source = getInputSource(path, null);
            }
        } catch (Exception e) {
            throw new SAXException(e.toString());
        } 
        return source;
    }

    private InputSource getInputSource(String path, InputSource source) {
        if (path != null) {
            InputStream in;
            try {
                in = this.getClass().getResourceAsStream(path);
                source = new InputSource(in);
            } catch (Exception e) {
                // ignore, null is ok
            }
        }
        return source;
    }

}