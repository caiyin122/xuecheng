package com.xuecheng.filesystem.service;

/**
 * @Author cai
 * @Date 2021/3/16 18:58
 */

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Service
public class FileSystemService {

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    @Autowired
    private FileSystemRepository fileSystemRepository;

    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata){

            // 2. 将文件ID和其他的文件信息保存到MongoDB中
            String fileID = uploadFile(multipartFile);
            if(StringUtils.isBlank(fileID)){
                ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
            }
            FileSystem fileSystem = new FileSystem();
            fileSystem.setFileId(fileID);
            fileSystem.setFilePath(fileID);
            fileSystem.setFiletag(filetag);
            fileSystem.setBusinesskey(businesskey);
            if(StringUtils.isNotBlank(metadata)){
                try {
                    Map map = JSON.parseObject(metadata, Map.class);
                    fileSystem.setMetadata(map);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            fileSystem.setFileName(multipartFile.getOriginalFilename());
            fileSystem.setFileSize(multipartFile.getSize());
            fileSystem.setFileType(multipartFile.getContentType());

            fileSystemRepository.save(fileSystem);
            return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }

    /**
     *  文件上传
     * @param multipartFile 文件
     * @return 文件ID
     */
    private String uploadFile(MultipartFile multipartFile){
        // 初始化fastDFS环境
        initFastdfsConfig();

        // 1.将文件上传到FastDFS中
        TrackerClient trackerClient = new TrackerClient();
        try {
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            StorageServer storage = trackerClient.getStoreStorage(trackerServer);
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storage);
            if(multipartFile == null){
                ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
            }else {
                byte[] bytes = multipartFile.getBytes();
                // 文件的原始名称
                String originalFilename = multipartFile.getOriginalFilename();
                // 拿到 文件的扩展名
                String extName = originalFilename.substring(originalFilename.indexOf(".") + 1);
                return storageClient1.upload_file1(bytes, extName, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加载fdfs的初始化
     */
    private void initFastdfsConfig() {
        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);

        } catch (Exception e) {
            e.printStackTrace();
            // 抛出异常
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

}
