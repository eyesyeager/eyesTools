package com.eyes.eyesTools.service.file;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;

/**
 * 七牛云工具类
 * @author eyes
 */
@Slf4j
public class QiniuUtils {
    private final String access_key;
    private final String secret_key;

    public QiniuUtils(String access_key, String secret_key) {
        this.access_key = access_key;
        this.secret_key = secret_key;
    }

    /**
     * 小文件上传（数据流）
     * @param multipartFile 文件
     * @param fileName 文件名
     * @param bucket 空间名
     * @return 是否成功
     */
    public boolean sUpload2Qiniu(MultipartFile multipartFile, String fileName, String bucket, Region region) throws IOException {
        return sUpload2Qiniu(multipartFile.getBytes(), fileName, bucket, region);
    }

    /**
     * 小文件上传(数据流)
     * @param file 文件
     * @param fileName 文件名
     * @param bucket 空间名
     * @return 是否成功
     */
    public boolean sUpload2Qiniu(byte[] file, String fileName, String bucket, Region region) throws QiniuException {
        Configuration cfg = new Configuration(region);
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(file);
        Auth auth = Auth.create(access_key, secret_key);
        String upToken = auth.uploadToken(bucket);
        return uploadManager.put(byteInputStream, fileName, upToken,null, null).isOK();
    }

    /**
     * 大文件上传
     * @param localFilePath 本地缓存路径
     * @param fileName 文件名
     * @param bucket 空间名
     */
    public boolean bUpload2Qiniu(String localFilePath, String fileName, String bucket, Region region) throws IOException {
        Configuration cfg = new Configuration(region);
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        cfg.resumableUploadMaxConcurrentTaskCount = 10;
        Auth auth = Auth.create(access_key, secret_key);
        String upToken = auth.uploadToken(bucket);
        String localTempDir = Paths.get(System.getenv("java.io.tmpdir"), bucket).toString();

        FileRecorder fileRecorder = new FileRecorder(localTempDir);
        UploadManager uploadManager = new UploadManager(cfg, fileRecorder);
        return uploadManager.put(localFilePath, fileName, upToken).isOK();
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @param bucket 空间名
     */
    public boolean deleteFileFromQiniu(String fileName, String bucket, Region region) throws QiniuException {
        Configuration cfg = new Configuration(region);
        Auth auth = Auth.create(access_key, secret_key);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        return bucketManager.delete(bucket, fileName).isOK();
    }
}
