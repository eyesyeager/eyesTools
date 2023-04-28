package com.eyes.eyesTools.starter;

import com.eyes.eyesTools.common.exception.CustomException;
import com.eyes.eyesTools.context.ConfigContext;
import com.eyes.eyesTools.service.email.EmailSender;
import com.eyes.eyesTools.service.httpUtils.HttpUtils;
import com.eyes.eyesTools.service.redis.RedisUtils;
import com.eyes.eyesTools.service.verificationCode.EmailVerificationCode;
import com.eyes.eyesTools.utils.SpringContextUtils;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @author eyes
 * @date 2023/1/6 10:08
 */
@Configuration
@EnableConfigurationProperties(EyesToolsProperties.class)
public class EyesToolsAutoConfiguration {

  @Resource
  private EyesToolsProperties eyesToolsProperties;

  @Bean
  @ConditionalOnMissingBean(SpringContextUtils.class)
  public SpringContextUtils springContextUtils() {
    return new SpringContextUtils();
  }

  @Bean
  @ConditionalOnProperty(prefix = ConfigContext.PACKAGE_ROOT, name = "enabled-http-utils", havingValue = "true")
  public HttpUtils httpUtils(RestTemplateBuilder builder) {
    return new HttpUtils(builder);
  }

  @Bean
  @ConditionalOnProperty(prefix = ConfigContext.PACKAGE_ROOT, name = "enabled-redis", havingValue = "true")
  public RedisUtils redisUtils(RedisConnectionFactory redisConnectionFactory) {
    return new RedisUtils(redisConnectionFactory);
  }

  @Bean
  @ConditionalOnProperty(prefix = ConfigContext.PACKAGE_ROOT + ".email.sender", name = "enabled", havingValue = "true")
  public EmailSender eyesEmailSender() throws CustomException {
    return new EmailSender();
  }

  @Bean
  @ConditionalOnBean(RedisUtils.class)
  @ConditionalOnProperty(prefix = ConfigContext.PACKAGE_ROOT + ".verification-code.email", name = "enabled", havingValue = "true")
  public EmailVerificationCode emailVerificationCode(RedisUtils redisUtils) {
    return new EmailVerificationCode(eyesToolsProperties, redisUtils);
  }
}