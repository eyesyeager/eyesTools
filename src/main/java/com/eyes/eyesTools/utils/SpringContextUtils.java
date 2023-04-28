package com.eyes.eyesTools.utils;

import java.util.Map;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author eyes
 * @date 2023/1/9 10:48
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {
  private static final AppContainer APP_CONTAINER = new AppContainer();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    APP_CONTAINER.setApplicationContext(applicationContext);
  }

  /**
   * 通过clazz,从spring容器中获取bean
   */
  public static <T> T getBean(Class<T> clazz) {
    return getApplicationContext().getBean(clazz);
  }

  /**
   * 获取某一类型的bean集合
   */
  public static <T> Map<String, T> getBeans(Class<T> clazz) {
    return getApplicationContext().getBeansOfType(clazz);
  }

  /**
   * 通过name和clazz,从spring容器中获取bean
   */
  public static <T> T getBean(String name, Class<T> clazz) {
    return getApplicationContext().getBean(name, clazz);
  }
  /**
   * 静态内部类，用于存放ApplicationContext
   */
  @Data
  private static class AppContainer {
    private ApplicationContext applicationContext;
  }

  /**
   * 获取ApplicationContext
   */
  private static ApplicationContext getApplicationContext() {
    return APP_CONTAINER.getApplicationContext();
  }
}