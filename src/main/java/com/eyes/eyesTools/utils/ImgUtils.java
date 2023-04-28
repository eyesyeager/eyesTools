package com.eyes.eyesTools.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * 图像工具类
 * @author eyes
 * @date 2023/1/11 17:06
 */

public class ImgUtils {
  private ImgUtils() {
    throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
  }

  /**
   * BufferedImage转Base64(默认jpg格式)
   * @param bufferedImage BufferedImage
   * @return String
   */
  public static String bufferedImageToBase64(BufferedImage bufferedImage) {
    return bufferedImageToBase64(bufferedImage, "jpg");
  }

  /**
   * BufferedImage转Base64
   * @param bufferedImage BufferedImage
   * @param type 图片格式
   * @return String
   */
  public static String bufferedImageToBase64(BufferedImage bufferedImage, String type) {
    String base64Img = null;
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    try {
      ImageIO.write(bufferedImage, type, bs);
      base64Img = Base64.getEncoder().encodeToString(bs.toByteArray());
      bs.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return base64Img;
  }
}
