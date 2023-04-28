package com.eyes.eyesTools.starter;

import com.eyes.eyesTools.context.ConfigContext;
import com.eyes.eyesTools.starter.properties.EmailProperties;
import com.eyes.eyesTools.starter.properties.FileProperties;
import com.eyes.eyesTools.starter.properties.SecurityProperties;
import com.eyes.eyesTools.starter.properties.VerificationCodeProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author eyes
 * @date 2023/1/6 9:50
 */
@Data
@Component
@ConfigurationProperties(ConfigContext.PACKAGE_ROOT)
public class EyesToolsProperties {
  // 是否启用redis
  private boolean enabledRedis = false;

  // 是否启用httpUtils
  private boolean enabledHttpUtils = false;

  @NestedConfigurationProperty
  private SecurityProperties security = new SecurityProperties();

  @NestedConfigurationProperty
  private EmailProperties email = new EmailProperties();

  @NestedConfigurationProperty
  private FileProperties file = new FileProperties();

  @NestedConfigurationProperty
  private VerificationCodeProperties verificationCode = new VerificationCodeProperties();
}
