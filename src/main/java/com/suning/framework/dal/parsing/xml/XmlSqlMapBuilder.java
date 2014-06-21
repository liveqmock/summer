/*
 * Copyright (C), 2002-2013, 苏宁易购电子商务有限公司
 * FileName: XmlSqlMapBuilder.java
 * Author:   13092011
 * Date:     2013-12-1 下午5:03:34
 * Description: SqlMap配置文件解析类     
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.framework.dal.parsing.xml;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.suning.framework.dal.client.support.Configuration;
import com.suning.framework.dal.client.support.MappedStatement;
import com.suning.framework.dal.client.support.SqlCommandType;
import com.suning.framework.dal.parsing.ParsingException;
import com.suning.framework.dal.parsing.XNode;
import com.suning.framework.dal.parsing.XPathParser;
import com.suning.framework.dal.parsing.builder.BaseBuilder;

/**
 * 描述：SqlMap配置文件解析类
 * @author 作者 13092011
 */
public class XmlSqlMapBuilder extends BaseBuilder {

    private XPathParser parser;
    private String resource;
    private String currentNamespace;

    public XmlSqlMapBuilder(InputStream inputStream, Configuration configuration, String resource) {
        this(new XPathParser(inputStream, false, configuration.getVariables(), new XmlSqlMapEntityResolver()),
                configuration, resource);
    }

    private XmlSqlMapBuilder(XPathParser parser, Configuration configuration, String resource) {
        super(configuration);
        this.parser = parser;
        this.resource = resource;
    }

    /**
     * 功能描述：解析sqlMap标签
     */
    public void parse() {
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(parser.evalNode("/sqlMap"));
            configuration.addLoadedResource(resource);
        }
    }

    /**
     * 功能描述：解析sqlMap下的五个子标签：sql、select、insert、update、delete<br>
     * 输入参数：xml节点<按照参数定义顺序> 
     * @param context
     * 返回值:  <说明> 
     * @return 返回值
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    private void configurationElement(XNode context) {
        try {
            String namespace = context.getStringAttribute("namespace","");
            if (namespace.equals("")) {
                throw new ParsingException("sqlMap's namespace cannot be empty");
            }
            setCurrentNamespace(namespace);
            addStatements(context.evalNodes("sql|select|insert|update|delete"));
        } catch (Exception e) {
            throw new ParsingException("Error parsing sqlMap XML. " + resource + " Cause: " + e, e);
        }
    }

    /**
     * 功能描述：具体解析每个子标签<br>
     * 每个sql处理id属性外，还可配置fetchSize、jdbcTimeout和maxRows<br>
     * 输入参数：list集合，元素是xml节点<按照参数定义顺序> 
     * @param list
     * 返回值:  <说明> 
     * @return 返回值
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    private void addStatements(List<XNode> list) {
        for (XNode statementsNode : list) {
            String id = statementsNode.getStringAttribute("id");
            Integer fetchSize = statementsNode.getIntAttribute("fetchSize");
            Integer timeout = statementsNode.getIntAttribute("jdbcTimeout");
            Integer maxRows = statementsNode.getIntAttribute("maxRows");
            
            StringBuilder sqlBuilder = new StringBuilder();
            NodeList children = statementsNode.getNode().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                XNode child = statementsNode.newXNode(children.item(i));
                String nodeName = child.getNode().getNodeName();
                if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
                        || child.getNode().getNodeType() == Node.TEXT_NODE) {
                    String data = child.getStringBody("");
                    sqlBuilder.append(data);
                } else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) {
                    throw new ParsingException("Unknown element <" + nodeName + "> in SQL statement.");
                }
            }
            String sqlSource = sqlBuilder.toString();
            sqlSource = sqlSource.trim().replace('\n', ' ');
            
            if (id == null || id.equals("")) {
                throw new ParsingException(this.resource + " element " +statementsNode.getName() + 
                        "'s id cannot be empty");
            }
            if (sqlSource == null || sqlSource.equals("")) {
                throw new ParsingException(this.resource + " sql sql statment['id'="+id+"] is an empty sql.");
            }
            
            String nodeNode = statementsNode.getName();

            //解析后的sql属性封装成sql描述对象
            MappedStatement ms = new MappedStatement();
            ms.setConfiguration(configuration);
            ms.setId(applyCurrentNamespace(id));
            ms.setFetchSize(fetchSize == null ? 0 : fetchSize);
            ms.setTimeout(timeout == null ? 0 : timeout);
            ms.setMaxRows(maxRows == null ? 0 : maxRows);
            ms.setSqlSource(sqlSource);

            //支持多个标签
            if ("sql".equals(nodeNode)) {
                ms.setSqlCommandType(SqlCommandType.UNKNOWN);
            } else if ("select".equals(nodeNode)) {
                ms.setSqlCommandType(SqlCommandType.SELECT);
            } else if ("insert".equals(nodeNode)) {
                ms.setSqlCommandType(SqlCommandType.INSERT);
            } else if ("update".equals(nodeNode)) {
                ms.setSqlCommandType(SqlCommandType.UPDATE);
            } else if ("delete".equals(nodeNode)) {
                ms.setSqlCommandType(SqlCommandType.DELETE);
            }
            configuration.addMappedStatement(ms);
        }
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new ParsingException("The sqlmap element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new ParsingException("Wrong namespace. Expected '" + this.currentNamespace + "' but found '"
                    + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
    }

    /**
     * 功能描述：生成sqlId<br>
     * 若前面没有namespace则加上当前namespace<br>
     * 输入参数：sqlId<按照参数定义顺序> 
     * @param sqlId
     * 返回值:  sqlId <说明> 
     * @return String
     * @throw 异常描述
     * @see 需要参见的其它内容
     */
    private String applyCurrentNamespace(String id) {
        if (id.startsWith(currentNamespace + ".")){
            return id;     
        }
        else{
            return currentNamespace + "." + id;
        }
    }

}
