package com.luban.spring.demo.service;

import com.luban.spring.framework.*;

/**
 * @author ch
 * @date 2021-2-20
 */
@Component("orderService")
@Scope("prototype")
@Lazy
public class OrderService implements BeanNameAware , InitializingBean{

    @Autowired
    private UserService userService;

    // 想把该类在容器内的beanName传给此属性,此时就需要使用BeanNameAware
    private String beanName;

    private String userName;

    public UserService getUserService() {
        return userService;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return this.beanName;
    }

    public void afterPropertiesSet() {
        System.out.println("初始化 OrderService");
        this.userName = userService.getUserName();
    }
}
