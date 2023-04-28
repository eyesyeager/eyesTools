package com.eyes.eyesTools.starter.properties;

import lombok.Data;

/**
 * @author eyes
 * @date 2023/1/9 13:41
 */
@Data
public class SecurityProperties {
  // 单向散列算法
  private String hash_algorithm = "SHA1";

  // 单向散列次数
  private int hash_num = 1;

  // 对称加密算法
  private String symmetric_algorithm = "DES";

  // 对称加密密钥
  private String symmetric_key = "";

  // 解密前端字符串——IV
  private String front_iv = "";

  // 解密前端字符串——KEY
  private String front_key = "";
}
