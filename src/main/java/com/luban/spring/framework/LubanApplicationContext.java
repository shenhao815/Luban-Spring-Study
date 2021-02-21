package com.luban.spring.framework;

import javax.lang.model.element.VariableElement;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    // BeanPostProcessor集合
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


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


        // 遍历扫描的包路径，此处应当使用递归
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


                    // BeanPostProcessor.class.isAssignableFrom(clazz) 判断这个class是否实现了某个接口
                    // instanceof 判断这个对象是否是继承或实现了某个类
                    if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                        try {
                            BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            beanPostProcessorList.add(instance);
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
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
                Object object = createBean(beanName,beanDefinition);
                singletonObjects.put(beanName, object);
            }
        }

    }

    // bean的生命周期（实例化--->填充属性--->Aware(相当于回调)--->初始化）
    private Object createBean(String beanName,BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        Object instance = null;
        try {
            instance = beanClass.getDeclaredConstructor().newInstance();

            // 填充属性
            for (Field field : beanClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    //
                    String name = field.getName();
                    Object bean = getBean(name);

                    field.setAccessible(true);
                    field.set(instance,bean);
                }
            }

            // Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }


            // 初始化之前调用后置处理器
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // 初始化之后调用后置处理器
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            // AOP --> BeanPostProcessor


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

            // 假设A类依懒B类,如果先加载A类，此时B类还未创建，这时又要给A类赋值B类，
            // 此时就需要下面的判断
            if (bean == null) {
                bean = createBean(beanName,beanDefinition);
            }

            return bean;
        } else {
            // 不是单例的，那就是原型的
            Object bean = createBean(beanName,beanDefinition);
            return bean;
        }
    }
}
