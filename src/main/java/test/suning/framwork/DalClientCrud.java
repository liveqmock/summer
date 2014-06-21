/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: DalClientCrud.java
 * Author:   13071472
 * Date:     2014-6-21 上午9:58:01
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package test.suning.framwork;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import test.suning.framwork.bean.Users;

import com.suning.framework.dal.client.DalClient;

/**
 * 测试DalClient CRUD<br>
 * 
 * @author 13071472
 */
public class DalClientCrud {

    private DalClient dalClient;

    private static volatile Object syncObject = new Object();

    /**
     * @return the dalClient
     */
    public DalClient getDalClient() {

        synchronized (syncObject) {
            if (dalClient == null) {
                ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:dal-service.xml");
                dalClient = (DalClient) applicationContext.getBean("dalClient");
                return dalClient;
            } else {
                return dalClient;
            }
        }

    }

    public static void main(String[] args) {
        DalClientCrud crud = new DalClientCrud();
        DalClient client = crud.getDalClient();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("USER_ID", "1");
        Users user = client.queryForObject("test.select", paramMap, Users.class);
        if (user == null) {
            user = new Users();
            System.out.println("查询不到:新增");
            user.setUserId("1");
            user.setUserName("1");
            user.setUserPassword("D#32dw23");
            user.setUserAccount("1");
            user.setUserDesc("test");
            user.setEnabled(1);
            user.setIsSys(0);

            client.persist(user, String.class);
        } else {
            System.out.println(ReflectionToStringBuilder.toString(user));
            System.out.println("查询到:删除");
            client.execute("test.delete", paramMap);
        }
        System.out.println("再次查询");
        user = client.queryForObject("test.select", paramMap, Users.class);
        System.out.println(ReflectionToStringBuilder.toString(user));
        System.out.println("删除");
        client.execute("test.delete", paramMap);
        System.out.println("再次查询");
        user = client.queryForObject("test.select", paramMap, Users.class);
        System.out.println(ReflectionToStringBuilder.toString(user));
    }

    /**
     * @param dalClient the dalClient to set
     */
    public void setDalClient(DalClient dalClient) {
        this.dalClient = dalClient;
    }
}
