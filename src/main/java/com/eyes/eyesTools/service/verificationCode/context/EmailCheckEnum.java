package com.eyes.eyesTools.service.verificationCode.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author eyes
 * @date 2023/1/10 0:03
 */
@Getter
@AllArgsConstructor
public enum EmailCheckEnum {
  // 验证码正确
  SUCCESS,

  // 邮箱错误或者验证码失效
  INVALID_CODE,

  // 验证码错误
  ERROR_CODE
}
