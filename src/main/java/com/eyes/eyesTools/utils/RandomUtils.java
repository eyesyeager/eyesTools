package com.eyes.eyesTools.utils;

import java.util.Random;

/**
 * 随机数工具类
 * @author eyes
 * @date 2023/1/9 17:28
 */
public class RandomUtils {
  private static final Random RANDOM = new Random();

  private RandomUtils() {
    throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
  }

  public static String getRandomStr(int length, String str) {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < length; i++){
      int number = RANDOM.nextInt(str.length());
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }
}
