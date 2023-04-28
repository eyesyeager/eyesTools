package com.eyes.eyesTools.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author eyesYeager
 * @date 2023/2/22 9:40
 */

public class FileFormatUtils {
  /**
   * InputStream转byte[]
   * @param inStream InputStream
   * @return byte[]
   */
  public static byte[] readInputStream(InputStream inStream) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[5 * 1024 * 1024];
    int len;
    while ((len = inStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, len);
    }
    inStream.close();
    return outStream.toByteArray();
  }

  /**
   * byte[]转MultipartFile
   * @param byteArr byte[]
   * @return MultipartFile
   */
  public static MultipartFile byteArr2multipartFile(byte[] byteArr) {
    return null;
  }

  /**
   * ByteBuffer转byte[]
   * @param buffer ByteBuffer
   * @return byte[]
   */
  public static byte[] byteBuffer2byteArr(ByteBuffer buffer) {
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes, 0, bytes.length);
    return bytes;
  }

  /**
   * MultipartFile转ByteBuffer
   * @param file MultipartFile
   * @return ByteBuffer
   */
  public static ByteBuffer multipartFile2byteBuffer(MultipartFile file) throws IOException {
    try {
      byte[] byteFile = file.getBytes();
      return ByteBuffer.wrap(byteFile);
    } catch (IOException e) {
      throw new IOException(e);
    }
  }
}
