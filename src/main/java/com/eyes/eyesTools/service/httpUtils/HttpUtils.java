package com.eyes.eyesTools.service.httpUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author eyesYeager
 * @date 2023/1/17 14:49
 */
public class HttpUtils {
  private final RestTemplate restTemplate;

  public HttpUtils(RestTemplateBuilder builder) {
    this.restTemplate = builder.build();
    this.restTemplate.getInterceptors().add(new CustomClientHttpRequestInterceptor());
  }

  /**
   * 请求接口公共方法
   * @param url URL
   * @param method 请求方法
   * @return String
   */
  public String getResponse(String url, HttpMethod method) throws IOException {
    return getResponse(url, method, MediaType.APPLICATION_FORM_URLENCODED, null, null);
  }

  /**
   * 请求接口公共方法
   * @param url URL
   * @param method 请求方法
   * @param mediaType 媒体类型
   * @return String
   */
  public String getResponse(String url, HttpMethod method, MediaType mediaType) throws IOException {
    return getResponse(url, method, mediaType, null, null);
  }

  /**
   * 请求接口公共方法
   * @param url URL
   * @param method 请求方法
   * @param mediaType 媒体类型
   * @param requestParams 请求参数
   * @return String
   */
  public String getResponse(String url, HttpMethod method, MediaType mediaType, Map<String, String> requestParams) throws IOException {
    return getResponse(url, method, mediaType, requestParams, null);
  }

  /**
   * 请求接口公共方法
   * @param url URL
   * @param method 请求方法
   * @param mediaType 媒体类型
   * @param requestParams 请求参数
   * @param headerParams 请求头设置
   * @return String
   */
  public String getResponse(String url, HttpMethod method, MediaType mediaType, Map<String, String> requestParams, Map<String, String> headerParams) throws IOException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(mediaType);
    // 设置用户提交的header头数据
    if (Objects.nonNull(headerParams)) {
      headerParams.forEach(headers::set);
    }
    // 将请求头部和参数合成一个请求
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    if (Objects.nonNull(requestParams)) {
      if (method == HttpMethod.GET) {
        url = url + "?" + getUrlParamsByMap(requestParams);
      } else {
        requestParams.forEach((k, v) -> params.put(k, Collections.singletonList(v)));
      }
    }

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
    // 执行HTTP请求，将返回的结构使用spring ResponseEntity处理http响应
    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, method, requestEntity, byte[].class);
    if (Objects.isNull(responseEntity.getBody())) {
      return "";
    }
    String contentEncoding = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
    // gzip编码
    if ("gzip".equals(contentEncoding)) {
      // gzip解压服务器的响应体
      byte[] data = unGZip(new ByteArrayInputStream(responseEntity.getBody()));
      return new String(data);
    } else {
      // 其他编码暂时不做处理，有需求再说
      return new String(responseEntity.getBody());
    }
  }

  /**
   * Gzip解压缩
   */
  public static byte[] unGZip(InputStream inputStream) throws IOException {
    try (
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)
    ) {
      byte[] buf = new byte[4096];
      int len;
      while ((len = gzipInputStream.read(buf, 0, buf.length)) != -1) {
        byteArrayOutputStream.write(buf, 0, len);
      }
      return byteArrayOutputStream.toByteArray();
    }
  }

  /**
   * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
   * @param params 需要排序并参与字符拼接的参数组
   * @return 拼接后字符串
   */
  public static String getUrlParamsByMap(Map<String, String> params) throws UnsupportedEncodingException {
    List<String> keys = new ArrayList<>(params.keySet());
    Collections.sort(keys);
    StringBuilder preStr = new StringBuilder();
    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get(i);
      String value = params.get(key);
      value = URLEncoder.encode(value, "UTF-8");
      if (i == keys.size() - 1) { // 拼接时，不包括最后一个&字符
        preStr.append(key).append("=").append(value);
      } else {
        preStr.append(key).append("=").append(value).append("&");
      }
    }
    return preStr.toString();
  }
}
