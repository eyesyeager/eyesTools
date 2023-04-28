package com.eyes.eyesTools.starter.properties;

import java.awt.Color;
import lombok.Data;

/**
 * @author eyes
 * @date 2023/1/9 13:42
 */
@Data
public class VerificationCodeProperties {
  private Img img = new Img();
  private Email email = new Email();
  private Sms sms = new Sms();

  @Data
  public static class Img {
    // 验证码位数
    private int digit = 4;
    // 验证码失效时间，单位为s
    private Long validity = 5 * 60 * 60L;
    // 图片宽度
    private int width = 140;
    // 图片高度
    private int height = 40;
    // 图片背景色
    private Color backColor = Color.white;
  }

  @Data
  public static class Email {
    // 是否启用
    private boolean enabled = false;

    // 验证码位数
    private int digit = 6;

    // 验证码失效时间，单位为s
    private Long validity = 5 * 60L;
  }

  @Data
  public static class Sms {
    // 是否启用
    private boolean enabled = false;

    // 验证码位数
    private int digit = 8;

    // 验证码失效时间，单位为s
    private Long validity = 5 * 60L;
  }
}
