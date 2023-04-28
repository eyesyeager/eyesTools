package com.eyes.eyesTools.utils;

import java.util.UUID;

/**
 * UUID工具类
 * @author eyes
 * @date 2023/1/13 10:49
 */
public class UUIDUtils {
  private UUIDUtils() {
    throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
  }

  public static String getUUid() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
