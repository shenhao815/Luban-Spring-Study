package com.luban.spring.demo.service;

import com.luban.spring.framework.BeanPostProcessor;
import com.luban.spring.framework.Component;

@Component
public class Luban1BeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前1。。。");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后1。。。");
        return bean;
    }
}
