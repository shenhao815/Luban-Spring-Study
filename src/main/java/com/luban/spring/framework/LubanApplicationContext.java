package com.luban.spring.framework;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ch
 * @date 2021-2-20
 *
 * 模仿注解方式
 */
public class LubanApplicationContext {

    private Class configClass;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();
    // 单例池
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>();

    public LubanApplicationContext(Class configClass) {
        this.configClass = configClass;

        // spring的启动
        // 1。解析ConfigClass得到包路径

        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String packagePath = componentScanAnnotation.value(); // 包路径

        // 2. 扫描类--->解析类上的注解---> BeanDefinition -->存入beanDefinitionMap
        packagePath = packagePath.replace(".", "/"); // com/luban/spring/demo/service
        ClassLoader classLoader = LubanApplicationContext.class.getClassLoader();// AppClassLoader --> classpath
        URL resource = classLoader.getResource(packagePath);
        File file = new File(resource.getFile());// 目录

        for (File f : file.listFiles()) {// 严谨一点应当使用递归
            String absolutePath = f.getAbsolutePath();

            String path = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
            path = path.replace("\\", ".");
            try {
                Class clazz = classLoader.loadClass(path);

                if (clazz.isAnnotationPresent(Component.class)) {
                    BeanDefinition beanDefinition = new BeanDefinition();
                    beanDefinition.setBeanClass(clazz);
                    Component componentAnnotation = (Component) clazz.getAnnotation(Component.class);
                    String beanName = componentAnnotation.value();

                    if (clazz.isAnnotationPresent(Lazy.class)) {
                        beanDefinition.setLazy(true);
                    } else{
                        beanDefinition.setLazy(false);
                    }
                    if (clazz.isAnnotationPresent(Scope.class)) {
                        Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                        String scope = scopeAnnotation.value();
                        beanDefinition.setScope(scope);
                    } else {
                        beanDefinition.setScope("singleton");
                    }
                    beanDefinitionMap.put(beanName, beanDefinition);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // 3。生成非懒加载的单例bean（对象）:bean的生命周期（实例化--->填充属性--->?--->存入单例池）
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            // 非懒加载的单例bean
            if (!beanDefinition.isLazy() && "singleton".equals(beanDefinition.getScope())) {
                // 创建bean对象
                Object object = createBean(beanDefinition);
                singletonObjects.put(beanName, object);
            }
        }

    }

    // bean的生命周期（实例化--->填充属性--->?）
    private Object createBean(BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        Object instance = null;
        try {
            instance = beanClass.getDeclaredConstructor().newInstance();

            // 填充属性

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return instance;
    }

    public Object getBean(String beanName){
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if ("singleton".equals(beanDefinition.getScope())) {
            Object bean = singletonObjects.get(beanName);
            return bean;
        } else {
            // 不是单例的，那就是原型的
            Object bean = createBean(beanDefinition);
            return bean;
        }
    }
}
