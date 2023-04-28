package com.eyes.eyesTools.service.verificationCode;

import com.eyes.eyesTools.common.exception.CustomException;
import com.eyes.eyesTools.service.email.EmailSender;
import com.eyes.eyesTools.service.redis.RedisUtils;
import com.eyes.eyesTools.service.verificationCode.bind.EmailVerificationCodeTemplateBind;
import com.eyes.eyesTools.service.verificationCode.context.EmailCheckEnum;
import com.eyes.eyesTools.starter.EyesToolsProperties;
import com.eyes.eyesTools.starter.properties.VerificationCodeProperties;
import com.eyes.eyesTools.utils.RandomUtils;
import com.eyes.eyesTools.utils.SpringContextUtils;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮箱验证码类
 * 使用前必须配置enabled=true
 * 该类依赖于redisUtils，因此必须设置enabled-redis=true
 * @author eyes
 * @date 2023/1/9 17:36
 */
@Slf4j
public class EmailVerificationCode {
  private final VerificationCodeProperties.Email config;

  private final RedisUtils redisUtils;

  public EmailVerificationCode(EyesToolsProperties eyesToolsProperties, RedisUtils redisUtils) {
    this.config = eyesToolsProperties.getVerificationCode().getEmail();
    this.redisUtils = redisUtils;
  }

  // 因为EmailSender载入Spring容器的时间不确定，因此需要懒加载以保证能在有该Bean时将其拿到
  // 利用静态内部类实现懒加载的同时保证并发安全性
  private static class EmailSenderContainer {
    private static final EmailSender emailSender;

    static {
      EmailSender bean = null;
      boolean flag = false;
      try {
        bean = SpringContextUtils.getBean(EmailSender.class);
      } catch (Exception e) {
        flag = true;
        log.error("Before using EmailVerificationCode, you need to register the EyesEmailSender bean or pass in the EyesEmailSender instance.");
      }
      emailSender = flag ? null : bean;
    }
  }

  /**
   * 获取邮箱验证码（采用默认EyesMailSender）
   * @param key Redis key
   * @param email 目标邮箱
   * @param subject 邮件主题
   * @param template 邮件内容（HTML）
   */
  public void getCode(String key, String email, String subject, EmailVerificationCodeTemplateBind template) throws CustomException {
    getCode(key, email, subject, template, EmailSenderContainer.emailSender);
  }

  /**
   * 获取邮箱验证码
   * @param key Redis key
   * @param email 目标邮箱
   * @param subject 邮件主题
   * @param template 邮件内容（HTML）
   * @param emailSender 邮件发送器
   */
  public void getCode(String key, String email, String subject, EmailVerificationCodeTemplateBind template, EmailSender emailSender) throws CustomException {
    if (Objects.isNull(emailSender)) {
      throw new CustomException("EyesEmailSender can not be null");
    }

    String randomString = getRandomString(config.getDigit());

    // 将验证码存入redis
    if(!redisUtils.set(key, randomString, config.getValidity())) {
      throw new CustomException("Failed to save the verification code into Redis");
    }

    // 发送邮件
    emailSender.sendMail(email, subject, template.getTemplate(randomString));
  }

  // 验证邮箱验证码
  public EmailCheckEnum checkCode(String code, String key) {
    Object value = redisUtils.get(key);
    if(Objects.isNull(value)) {
      log.info("Email error or invalid verification code");
      return EmailCheckEnum.INVALID_CODE;
    }
    if(!value.toString().equalsIgnoreCase(code)) {
      log.info("Verification code error");
      return EmailCheckEnum.ERROR_CODE;
    }
    redisUtils.del(key);
    return EmailCheckEnum.SUCCESS;
  }

  // 生成指定位数随机字符串
  private static String getRandomString(int length) {
    String str = "abcdefghijkmnprstuvwxyABCDEFGHJKLMNPQRSTUVWXY23456789";
    return RandomUtils.getRandomStr(length, str);
  }
}
