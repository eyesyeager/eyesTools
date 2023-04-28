package com.eyes.eyesTools.starter.properties;

import lombok.Data;

/**
 * @author eyes
 * @date 2023/1/9 13:42
 */
@Data
public class FileProperties {
  private Qiniu qiniu = new Qiniu();

  private String folder_url;

  private String default_pic_suffix = ".png";

  @Data
  public static class Qiniu {
    private String access_key;

    private String secret_key;

    private String bucket;

    private String region;

    private String base_url;
  }
}
