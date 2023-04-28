package com.eyes.eyesTools.service.verificationCode.BO;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author eyes
 * @date 2023/1/9 17:53
 */
@Data
@AllArgsConstructor
public class ImgVerificationCodeBO {
  private final String base64Img;

  private final String key;
}
