package com.eyes.eyesTools.starter.properties;

import lombok.Data;

/**
 * @author eyes
 * @date 2023/1/10 9:32
 */
@Data
public class EmailProperties {
  private Sender sender = new Sender();
  private Receiver receiver = new Receiver();

  @Data
  public static class Sender {
    // 是否启用
    private boolean enabled = false;
    // 邮件服务器
    private String host;
    // 协议
    private String transportType = "smtp";
    // 发件邮箱
    private String fromEmail;
    // 邮箱授权码
    private String authCode;
    // 发件人名称
    private String senderName;
  }

  @Data
  public static class Receiver {
    // 是否启用
    private boolean enabled = false;
    // 邮件服务器
    private String host;
    // 协议
    private String transportType;
    // 收件邮箱
    private String toEmail;
    // 邮箱授权码
    private String authCode;
  }
}
