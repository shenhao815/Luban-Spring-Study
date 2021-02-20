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

        OrderService orderService = (OrderService) applicationContext.getBean("orderService");
        UserService userService = orderService.getUserService();
        UserService userService1 = orderService.getUserService();
        System.out.println(orderService);
        System.out.println(userService);
        System.out.println(userService1);
        System.out.println(orderService.getBeanName());
    }

}
