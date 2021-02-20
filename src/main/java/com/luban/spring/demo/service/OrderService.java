package com.luban.spring.demo.service;

import com.luban.spring.framework.Autowired;
import com.luban.spring.framework.Component;
import com.luban.spring.framework.Lazy;
import com.luban.spring.framework.Scope;

/**
 * @author ch
 * @date 2021-2-20
 */
@Component("orderService")
@Scope("prototype")
@Lazy
public class OrderService {

    @Autowired
    private UserService userService;

    public UserService getUserService() {
        return userService;
    }
}
