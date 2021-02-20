package com.luban.spring.demo.service;

import com.luban.spring.framework.Component;
import com.luban.spring.framework.Scope;

/**
 * @author ch
 * @date 2021-2-20
 */
@Component("userService")
@Scope("prototype")
public class UserService {

    public String getUserName(){
        return null;
    }

}
