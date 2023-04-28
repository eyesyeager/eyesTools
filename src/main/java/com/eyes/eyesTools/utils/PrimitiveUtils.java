package com.eyes.eyesTools.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * class原始类型工具类
 * @author eyes
 * @date 2023/1/11 9:44
 */

public class PrimitiveUtils {

  private static final ConcurrentMap<String, Class<?>> NAME_CLASS_CACHE = new ConcurrentHashMap<>();

  // void(V)
  public static final char JVM_VOID = 'V';

  // boolean(Z)
  public static final char JVM_BOOLEAN = 'Z';

  // byte(B)
  public static final char JVM_BYTE = 'B';

  // char(C)
  public static final char JVM_CHAR = 'C';

  // double(D)
  public static final char JVM_DOUBLE = 'D';

  // float(F)
  public static final char JVM_FLOAT = 'F';

  // int(I)
  public static final char JVM_INT = 'I';

  // long(J)
  public static final char JVM_LONG = 'J';

  // short(S)
  public static final char JVM_SHORT = 'S';

  private PrimitiveUtils() {
    throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
  }

  /**
   * 判断是否是原始类型
   * @param cls 待判断class
   * @return boolean
   */
  public static boolean isPrimitives(Class<?> cls) {
    if (cls.isArray()) {
      return isPrimitive(cls.getComponentType());
    }
    return isPrimitive(cls);
  }

  /**
   * 判断是否是原始类型
   * @param cls 待判断class
   * @return boolean
   */
  public static boolean isPrimitive(Class<?> cls) {
    return
        cls.isPrimitive() || cls == String.class
        || cls == Boolean.class || cls == Character.class
        || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
  }

  /**
   * 判断是否是Pojo
   * @param cls 待判断class
   * @return boolean
   */
  public static boolean isPojo(Class<?> cls) {
    return ! isPrimitives(cls) && ! Collection.class.isAssignableFrom(cls) && ! Map.class.isAssignableFrom(cls);
  }

  /**
   * 通过类名获取类
   * "boolean" => boolean.class
   * "java.util.Map[][]" => java.util.Map[][].class
   *
   * @param name 类名
   * @return Class 类
   */
  public static Class<?> name2class(String name) throws ClassNotFoundException {
    return name2class(getClassLoader(), name);
  }

  /**
   * 通过类名与类加载器获取类
   * "boolean" => boolean.class
   * "java.util.Map[][]" => java.util.Map[][].class
   *
   * @param cl 类加载器
   * @param name 类名
   * @return Class 类
   */
  private static Class<?> name2class(ClassLoader cl, String name) throws ClassNotFoundException {
    int c = 0, index = name.indexOf('[');
    if(index > 0) {
      c = (name.length() - index) / 2;
      name = name.substring(0, index);
    }

    if(c > 0) {
      StringBuilder sb = new StringBuilder();
      while( c-- > 0 )
        sb.append("[");

      switch (name) {
        case "void":
          sb.append(JVM_VOID);
          break;
        case "boolean":
          sb.append(JVM_BOOLEAN);
          break;
        case "byte":
          sb.append(JVM_BYTE);
          break;
        case "char":
          sb.append(JVM_CHAR);
          break;
        case "double":
          sb.append(JVM_DOUBLE);
          break;
        case "float":
          sb.append(JVM_FLOAT);
          break;
        case "int":
          sb.append(JVM_INT);
          break;
        case "long":
          sb.append(JVM_LONG);
          break;
        case "short":
          sb.append(JVM_SHORT);
          break;
        default:
          sb.append('L').append(name).append(';'); // "java.lang.Object" ==> "Ljava.lang.Object;"
          break;
      }
      name = sb.toString();
    } else {
      switch (name) {
        case "void":
          return void.class;
        case "boolean":
          return boolean.class;
        case "byte":
          return byte.class;
        case "char":
          return char.class;
        case "double":
          return double.class;
        case "float":
          return float.class;
        case "int":
          return int.class;
        case "long":
          return long.class;
        case "short":
          return short.class;
      }
    }

    if(cl == null){
      cl = getClassLoader();
    }
    Class<?> clazz = NAME_CLASS_CACHE.get(name);
    if(clazz == null){
      clazz = Class.forName(name, true, cl);
      NAME_CLASS_CACHE.put(name, clazz);
    }
    return clazz;
  }

  public static ClassLoader getClassLoader(){
    return getClassLoader(PrimitiveUtils.class);
  }

  /**
   * 根据class获取class加载器
   * @param cls class
   * @return class loader class加载器
   */
  public static ClassLoader getClassLoader(Class<?> cls) {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Throwable ex) {
      // Cannot access thread context ClassLoader - falling back to system class loader...
    }
    if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = cls.getClassLoader();
    }
    return cl;
  }

  private static final Map<String, PrimitiveInfo<?>> PRIMITIVES = new HashMap<String, PrimitiveInfo<?>>();

  static {
    addPrimitive(boolean.class, "Z", Boolean.class, "booleanValue", false);
    addPrimitive(short.class, "S", Short.class, "shortValue", (short) 0);
    addPrimitive(int.class, "I", Integer.class, "intValue", 0);
    addPrimitive(long.class, "J", Long.class, "longValue", 0L);
    addPrimitive(float.class, "F", Float.class, "floatValue", 0F);
    addPrimitive(double.class, "D", Double.class, "doubleValue", 0D);
    addPrimitive(char.class, "C", Character.class, "charValue", '\0');
    addPrimitive(byte.class, "B", Byte.class, "byteValue", (byte) 0);
    addPrimitive(void.class, "V", Void.class, null, null);
  }

  private static <T> void addPrimitive(Class<T> type, String typeCode, Class<T> wrapperType, String unwrapMethod, T defaultValue) {
    PrimitiveInfo<T> info = new PrimitiveInfo<T>(type, typeCode, wrapperType, unwrapMethod, defaultValue);

    PRIMITIVES.put(type.getName(), info);
    PRIMITIVES.put(wrapperType.getName(), info);
  }

  /** 代表一个primitive类型的信息。 */
  @SuppressWarnings("unused")
  private static class PrimitiveInfo<T> {
    final Class<T> type;
    final String   typeCode;
    final Class<T> wrapperType;
    final String   unwrapMethod;
    final T        defaultValue;

    public PrimitiveInfo(Class<T> type, String typeCode, Class<T> wrapperType, String unwrapMethod, T defaultValue) {
      this.type = type;
      this.typeCode = typeCode;
      this.wrapperType = wrapperType;
      this.unwrapMethod = unwrapMethod;
      this.defaultValue = defaultValue;
    }
  }

  /**
   * 取得primitive类型的wrapper。如果不是primitive，则原样返回。
   * <p>
   * 例如：
   * <p/>
   * <pre>
   * ClassUtil.getPrimitiveWrapperType(int.class) = Integer.class;
   * ClassUtil.getPrimitiveWrapperType(int[].class) = int[].class;
   * ClassUtil.getPrimitiveWrapperType(int[][].class) = int[][].class;
   * ClassUtil.getPrimitiveWrapperType(String[][].class) = String[][].class;
   * </pre>
   * <p/>
   * </p>
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getWrapperTypeIfPrimitive(Class<T> type) {
    if (type.isPrimitive()) {
      return ((PrimitiveInfo<T>) PRIMITIVES.get(type.getName())).wrapperType;
    }

    return type;
  }

  /**
   * 取得primitive类型的默认值。如果不是primitive，则返回<code>null</code>。
   * <p>
   * 例如：
   * <p/>
   * <pre>
   * ClassUtil.getPrimitiveDefaultValue(int.class) = 0;
   * ClassUtil.getPrimitiveDefaultValue(boolean.class) = false;
   * ClassUtil.getPrimitiveDefaultValue(char.class) = '\0';
   * </pre>
   * <p/>
   * </p>
   */
  @SuppressWarnings("unchecked")
  public static <T> T getPrimitiveDefaultValue(Class<T> type) {
    PrimitiveInfo<T> info = (PrimitiveInfo<T>) PRIMITIVES.get(type.getName());
    if (info != null) {
      return info.defaultValue;
    }
    return null;
  }

}