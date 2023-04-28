package com.eyes.eyesTools.service.file;

import com.eyes.eyesTools.common.exception.CustomException;
import com.eyes.eyesTools.starter.EyesToolsProperties;
import com.eyes.eyesTools.starter.properties.FileProperties;
import com.eyes.eyesTools.utils.FileFormatUtils;
import com.eyes.eyesTools.utils.SpringContextUtils;
import com.eyes.eyesTools.utils.UUIDUtils;
import com.qiniu.common.QiniuException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * 文件上传类
 * TODO: 取消文件默认格式、改名FileUploadUtils、新增FileUtils用以处理文件（例如计算文件名）
 * @author eyes
 */
@Slf4j
public class FileUtils {
    private static final FileProperties config = SpringContextUtils.getBean(EyesToolsProperties.class).getFile();

    private FileUtils() {
        throw new UnsupportedOperationException("It is not recommended to instantiate this class. It is recommended to use static method calls");
    }

    /**
     * 小文件上传
     * @param multipartFile 文件
     * @param category 分类
     * @return URL
     */
    public static String sUpload(MultipartFile multipartFile, String category) throws CustomException {
        return sUpload(multipartFile, category, config.getQiniu().getBucket(), config.getQiniu().getRegion());
    }

    /**
     * 小文件上传
     * @param bytes 文件
     * @param category 分类
     * @return URL
     */
    public static String sUpload(byte[] bytes, String suffix, String category) throws CustomException {
        return sUpload(bytes, suffix, category, config.getQiniu().getBucket(), config.getQiniu().getRegion());
    }

    /**
     * 小文件上传
     * @param multipartFile 文件
     * @param category 分类
     * @param bucket 空间名
     * @return URL
     */
    public static String sUpload(MultipartFile multipartFile, String category, String bucket) throws CustomException {
        return sUpload(multipartFile, category, bucket, config.getQiniu().getRegion());
    }

    /**
     * 小文件上传
     * @param bytes 文件
     * @param category 分类
     * @param bucket 空间名
     * @return URL
     */
    public static String sUpload(byte[] bytes, String suffix, String category, String bucket) throws CustomException {
        return sUpload(bytes, suffix, category, bucket, config.getQiniu().getRegion());
    }

    /**
     * 小文件上传
     * @param multipartFile 文件
     * @param category 分类
     * @param bucket 空间名
     * @param region 机房名
     * @return URL
     */
    public static String sUpload(MultipartFile multipartFile, String category, String bucket, String region) throws CustomException {
        QiniuUtils qiniuUtils = new QiniuUtils(config.getQiniu().getAccess_key(), config.getQiniu().getSecret_key());
        String fileName = getRandomFileName(multipartFile, category);
        try {
            if (!qiniuUtils.sUpload2Qiniu(multipartFile, fileName, bucket, RegionContext.getRegion(region))) {
                throw new CustomException("File upload failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("File upload failed");
        }
        return config.getQiniu().getBase_url() + fileName;
    }

    /**
     * 小文件上传
     * @param bytes 文件
     * @param category 分类
     * @param bucket 空间名
     * @param region 机房名
     * @return URL
     */
    public static String sUpload(byte[] bytes, String suffix, String category, String bucket, String region) throws CustomException {
        QiniuUtils qiniuUtils = new QiniuUtils(config.getQiniu().getAccess_key(), config.getQiniu().getSecret_key());
        String fileName = category + "/" + UUIDUtils.getUUid() + suffix;
        try {
            if (!qiniuUtils.sUpload2Qiniu(bytes, fileName, bucket, RegionContext.getRegion(region))) {
                throw new CustomException("File upload failed");
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            throw new CustomException("File upload failed");
        }
        return config.getQiniu().getBase_url() + fileName;
    }

    /**
     * 大文件上传
     * @param multipartFile 文件
     * @param category 分类
     * @return URL
     */
    public static String bUpload(MultipartFile multipartFile, String category) throws CustomException {
        return bUpload(multipartFile, category, config.getQiniu().getBucket());
    }

    /**
     * 大文件上传
     * @param multipartFile 文件
     * @param category 分类
     * @param bucket 空间名
     * @return URL
     */
    public static String bUpload(MultipartFile multipartFile, String category, String bucket) throws CustomException {
        return bUpload(multipartFile, category, bucket, config.getQiniu().getRegion());
    }

    /**
     * 大文件上传
     * @param multipartFile 文件
     * @param category 分类
     * @param bucket 空间名
     * @param region 机房名
     * @return URL
     */
    public static String bUpload(MultipartFile multipartFile, String category, String bucket, String region) throws CustomException {
        String fileName = getRandomFileName(multipartFile, category);

        File folder = new File(config.getFolder_url());
        File file = new File(folder, fileName);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("File save failed");
        }

        QiniuUtils qiniuUtils = new QiniuUtils(config.getQiniu().getAccess_key(), config.getQiniu().getSecret_key());
        try {
            if (!qiniuUtils.bUpload2Qiniu(config.getFolder_url() + "\\" + fileName, fileName, bucket, RegionContext.getRegion(region))) {
                throw new CustomException("File upload failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("File upload failed");
        }

        if (!file.exists() || !file.isFile()) {
            log.warn("Cannot delete local file! File does not exist or it is a folder!");
        } else {
            boolean delete = file.delete();
            if(!delete) log.warn("Local file deletion failed!");
        }

        return config.getQiniu().getBase_url() + fileName;
    }

    /**
     * 根据URL上传文件（小文件）
     * @param netAddress URL地址
     * @param category 分类
     * @return URL
     */
    public static String uploadByUrl(String netAddress, String category) throws CustomException {
        return uploadByUrl(netAddress, category, config.getQiniu().getBucket());
    }

    /**
     * 根据URL上传文件（小文件）
     * @param netAddress URL地址
     * @param category 分类
     * @param bucket 空间名
     * @return URL
     */
    public static String uploadByUrl(String netAddress, String category, String bucket) throws CustomException {
        return uploadByUrl(netAddress, category, bucket, config.getQiniu().getRegion());
    }

    /**
     * 根据URL上传文件（小文件）
     * @param netAddress URL地址
     * @param category 分类
     * @param bucket 空间名
     * @param region 机房名
     * @return URL
     */
    public static String uploadByUrl(String netAddress, String category, String bucket, String region) throws CustomException {
        try {
            String fileName = category + "/" + UUIDUtils.getUUid() + config.getDefault_pic_suffix();
            URL url = new URL(netAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10 * 1000);
            InputStream is = conn.getInputStream();

            byte[] data = FileFormatUtils.readInputStream(is);
            QiniuUtils qiniuUtils = new QiniuUtils(config.getQiniu().getAccess_key(), config.getQiniu().getSecret_key());
            if (!qiniuUtils.sUpload2Qiniu(data, fileName, bucket, RegionContext.getRegion(region))) {
                throw new CustomException("File upload failed");
            }

            return config.getQiniu().getBase_url() + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("File upload failed");
        }
    }

    /**
     * 删除指定文件
     * @param fileName 文件名
     * @param category 文件目录
     */
    public static void delFile(String fileName, String category) throws CustomException {
        delFile(fileName, category, config.getQiniu().getBucket());
    }

    /**
     * 删除指定文件
     * @param fileName 文件名
     * @param category 文件目录
     * @param bucket 空间名
     */
    public static void delFile(String fileName, String category, String bucket) throws CustomException {
        delFile(fileName, category, bucket, config.getQiniu().getRegion());
    }

    /**
     * 删除指定文件
     * @param fileName 文件名
     * @param category 文件目录
     * @param bucket 空间名
     * @param region 机房名
     */
    public static void delFile(String fileName, String category, String bucket, String region) throws CustomException {
        QiniuUtils qiniuUtils = new QiniuUtils(config.getQiniu().getAccess_key(), config.getQiniu().getSecret_key());
        try {
            if (!qiniuUtils.deleteFileFromQiniu(category + "/" + fileName, bucket, RegionContext.getRegion(region))) {
                throw new CustomException("File delete failed");
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            throw new CustomException("File delete failed");
        }
    }

    /*
     *****************************************************************************
     *                                  辅助函数
     *****************************************************************************
     */

    /**
     * 计算文件名称
     * @param multipartFile MultipartFile
     * @param category String
     * @return String
     */
    private static String getRandomFileName(MultipartFile multipartFile, String category) {
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = config.getDefault_pic_suffix();
        if (!Objects.isNull(originalFilename)) {
            int lastIndexOf = originalFilename.lastIndexOf(".");
            suffix = originalFilename.substring(lastIndexOf);
        }
        return category + "/" + UUIDUtils.getUUid() + suffix;
    }
}