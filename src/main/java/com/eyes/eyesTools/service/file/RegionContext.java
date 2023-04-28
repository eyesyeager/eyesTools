package com.eyes.eyesTools.service.file;

import com.qiniu.storage.Region;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 七牛云机房配置类
 * @author eyes
 * @date 2023/1/9 16:06
 */
public class RegionContext {
  private RegionContext() {
    throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
  }

  /**
   * 根据名称获取region
   * @param region 机房名称
   * @return Region
   */
  public static Region getRegion(String region) {
    return MapContainer.map.get(region);
  }

  // 利用静态内部类保证线程安全
  private static class MapContainer {
    private static final Map<String, Region> map;

    static {
      Map<String, Region> tMap = new HashMap<>();
      tMap.put("region0", Region.region0());
      tMap.put("huadong", Region.huadong());
      tMap.put("qvmRegion0", Region.qvmRegion0());
      tMap.put("qvmHuadong", Region.qvmHuadong());
      tMap.put("region1", Region.region1());
      tMap.put("huabei", Region.huabei());
      tMap.put("qvmRegion1", Region.qvmRegion1());
      tMap.put("qvmHuabei", Region.qvmHuabei());
      tMap.put("region2", Region.region2());
      tMap.put("huanan", Region.huanan());
      tMap.put("regionNa0", Region.regionNa0());
      tMap.put("beimei", Region.beimei());
      tMap.put("regionAs0", Region.regionAs0());
      tMap.put("xinjiapo", Region.xinjiapo());
      tMap.put("regionFogCnEast1", Region.regionFogCnEast1());
      map = Collections.unmodifiableMap(tMap);
    }
  }
}
