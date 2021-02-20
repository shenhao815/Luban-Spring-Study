package com.luban.spring.demo;

import com.luban.spring.demo.service.OrderService;
import com.luban.spring.demo.service.UserService;
import com.luban.spring.framework.LubanApplicationContext;

/**
 * @author ch
 * @date 2021-2-20
 */
public class Test {

    public static void main(String[] args) {
        // 启动spring
        LubanApplicationContext applicationContext = new LubanApplicationContext(AppConfig.class);

        UserService orderService = (UserService) applicationContext.getBean("userService");
        UserService orderService1 = (UserService) applicationContext.getBean("userService");
        //UserService userService = orderService.getUserService();
        System.out.println(orderService);
        System.out.println(orderService1);
    }

}
