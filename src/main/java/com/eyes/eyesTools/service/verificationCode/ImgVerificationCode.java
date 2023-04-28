package com.eyes.eyesTools.service.verificationCode;

import com.eyes.eyesTools.common.exception.CustomException;
import com.eyes.eyesTools.service.verificationCode.BO.ImgVerificationCodeBO;
import com.eyes.eyesTools.starter.EyesToolsProperties;
import com.eyes.eyesTools.starter.properties.VerificationCodeProperties;
import com.eyes.eyesTools.utils.ImgUtils;
import com.eyes.eyesTools.utils.RandomUtils;
import com.eyes.eyesTools.utils.SecurityUtils;
import com.eyes.eyesTools.utils.SpringContextUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * 图形验证码类
 * @author eyes
 * @date 2023/1/9 17:35
 */
@Slf4j
public class ImgVerificationCode {
  private static final Random RANDOM = new Random();

  private static final String KEY_SEPARATOR = "@.@";

  private ImgVerificationCode() {
    throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
  }

  private static class ConfigContainer {
    private static final VerificationCodeProperties.Img config = SpringContextUtils.getBean(EyesToolsProperties.class).getVerificationCode().getImg();
  }

  /**
   * 获取图形验证码
   * @return ImgVerificationCodeBO
   */
  public static ImgVerificationCodeBO getCode() throws CustomException {
    return getCode(
        ConfigContainer.config.getDigit(),
        ConfigContainer.config.getValidity(),
        ConfigContainer.config.getWidth(),
        ConfigContainer.config.getHeight(),
        ConfigContainer.config.getBackColor()
    );
  }

  /**
   * 获取图形验证码
   * @param digit 验证码位数
   * @param width 图片宽度
   * @param height 图片高度
   * @param backColor 图片背景色
   * @return ImgVerificationCodeBO
   */
  public static ImgVerificationCodeBO getCode(int digit, Long validity, int width, int height, Color backColor) throws CustomException {
    try {
      String randomString = getRandomString(digit);
      String base64Img = getBase64Image(randomString, width, height, backColor);
      // key由三段组成： 单向散列Code + 分隔符 + 双向加密Date
      String encryptedCode = SecurityUtils.oneWayHash(randomString.toLowerCase());
      String expireTime = String.valueOf(System.currentTimeMillis() + validity * 1000);
      String key = encryptedCode + KEY_SEPARATOR + SecurityUtils.disorderStr(expireTime);
      return new ImgVerificationCodeBO(base64Img, key);
    } catch (Exception e) {
      log.error("current time encryption error");
      throw new CustomException("程序错误");
    }
  }

  /**
   * 验证图形验证码
   * @param code 用户填写的验证码
   * @param key 验证key
   */
  public static void checkCode(String code, String key) throws CustomException {
    String[] split = key.split(KEY_SEPARATOR);
    // 验证key有效性
    if (split.length != 2) {
      throw new CustomException("key值不规范");
    }
    try {
      // 验证code
      if (!SecurityUtils.oneWayHash(code.toLowerCase()).equals(split[0])) {
        throw new CustomException("图形验证码错误");
      }
      // 验证有效期
      if (Long.parseLong(SecurityUtils.collateStr(split[1])) < System.currentTimeMillis()) {
        throw new CustomException("图形验证码已过期");
      }
    } catch (CustomException e) {
      throw new CustomException(e.getErrorCode(), e.getErrorMsg());
    } catch (Exception e) {
      throw new CustomException("key值不规范");
    }
  }

  /*
   **************************************************************************
   *                                辅助函数
   **************************************************************************
   */

  /**
   * 生成指定位数随机字符串
   * @param length 长度
   * @return 验证码
   */
  private static String getRandomString(int length) {
    String str = "abcdefghijkmnprstuvwxyABCDEFGHJKLMNPQRSTUVWXY23456789";
    return RandomUtils.getRandomStr(length, str);
  }

  /**
   * 生成base64图片验证码
   * @param code 验证码
   * @param width 图片宽度
   * @param height 图片高度
   * @param backColor 图片背景色
   * @return base64图片
   */
  private static String getBase64Image(String code, int width, int height, Color backColor) {
    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();

    // 初始化画布
    g2.setColor(backColor);
    g2.fillRect(0, 0, width, height);

    // 画干扰线(3~6条)
    drawLine(g2, 3 + RANDOM.nextInt(4), width, height);

    // 写入验证码
    drawString(g2, code, width, height);

    g2.dispose();

    return "data:image/jpg;base64," + ImgUtils.bufferedImageToBase64(bi);
  }

  /**
   * 获取随机颜色
   * @return Color
   */
  private static Color getRandomColor() {
    int r = RANDOM.nextInt(225);
    int g = RANDOM.nextInt(225);
    int b = RANDOM.nextInt(225);
    return new Color(r, g, b);
  }

  /**
   * 画干扰线
   * @param g2 画笔
   * @param nums 干扰线条数
   * @param width 画布宽度
   * @param height 画布高度
   */
  private static void drawLine(Graphics2D g2, int nums, int width, int height) {
    for (int i = 0; i < nums; i++) {
      g2.setColor(getRandomColor());
      g2.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextInt(width), RANDOM.nextInt(height));
    }
  }

  /**
   * 写入字符串
   * @param g2 画笔
   * @param code 待写入字符串
   * @param width 画布宽度
   * @param height 画布高度
   */
  private static void drawString(Graphics2D g2, String code, int width, int height) {
    int x = 0;
    int y = (int)Math.floor(height * 0.6);
    int step = (int)Math.floor(width / code.length() * 0.8);
    double ro = 0.05;

    Font font = new Font(null, Font.BOLD, 20);
    for (int i = 0; i < code.length(); i++) {
      // 设置字母颜色字体
      font.deriveFont(Font.BOLD, 30f);
      g2.setColor(getRandomColor());
      g2.setFont(font);
      String letter = String.valueOf(code.charAt(i));
      // 旋转字母
      if(RANDOM.nextBoolean()) {
        ro = -ro;
      }
      g2.rotate(ro);
      // 指定写入字母位置并写入
      x += step;
      g2.drawString(letter, x, y);
      g2.rotate(-ro);
    }
  }
}
