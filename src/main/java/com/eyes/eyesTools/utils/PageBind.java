package com.eyes.eyesTools.utils;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页工具类
 * @author eyes
 * @date 2023/1/11 9:25
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBind<T> {
  private Integer page;

  private Integer total;

  private List<T> data;
}
