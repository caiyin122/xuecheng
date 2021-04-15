package com.xuecheng.test.fastdfs;


import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    //@Before
    public void init() throws IOException, MyException {
        ClientGlobal.initByTrackers("config/fastdfs-client.properties");
    }

    //上传文件
    @Test
    public void testUpload() {
        try {
            //加载fastdfs-client.properties文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            // 定义TrackerClient 用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            // 连接tracker
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            // 获取storage
            StorageServer storage = trackerClient.getStoreStorage(trackerServer);
            // 
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storage);
            // 向Storage中上传文件
            String filePath = "E:/3.jpg";
            String png = storageClient1.upload_file1(filePath, "jpg", null);
            if(StringUtils.isNotBlank(png)){
                System.out.println(png);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // 下载文件
    @Test
    public void testDownloadFile(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            byte[] bytes = storageClient1.download_file1("group1/M00/00/00/wKhqBWBfTbyAOJGbAAKgxdR5Axc878.jpg");
            File file = new File("E:/caiyin.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
