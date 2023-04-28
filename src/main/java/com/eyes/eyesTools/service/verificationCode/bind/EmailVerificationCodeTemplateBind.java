package com.eyes.eyesTools.service.verificationCode.bind;

/**
 * 邮件验证码HTML模板约束
 * @author eyes
 * @date 2023/1/17 10:18
 */
public interface EmailVerificationCodeTemplateBind {

  /**
   * 获取邮件验证码模板
   * @param code 验证码
   * @return HTML
   */
  String getTemplate(String code);
}
