package com.changgou.file.util;

import com.changgou.file.pojo.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

/**
 * 文件管理的操作类
 */
public class FileUtil {

    /**
     * 加载配置文件
     */
    static {
        try {
            //加载配置文件
            ClassPathResource classPathResource = new ClassPathResource("fdfs_client.conf");
            //加载到全局变量中
            ClientGlobal.init(classPathResource.getPath());
        }catch (Exception e){

        }
    }


    /**
     * 文件上传
     */
    public static String[] upload(FastDFSFile fastDFSFile){

        NameValuePair[] meta_list = new NameValuePair[2];

        meta_list[0] = new NameValuePair("拍摄的手机","华为Mate30 Pro 土豪金豪华版");
        meta_list[1] = new NameValuePair("author",fastDFSFile.getAuthor());

        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //文件上传
            /**
             * 1.文件的字节码
             * 2.文件的拓展名
             * 3.附加参数
             */
            String[] strings = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);

            return strings;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件的下载
     */
    public static InputStream download(String groupName, String fileName){
        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //文件下载
            byte[] bytes = storageClient.download_file(groupName, fileName);
            //返回输入流
            return new ByteArrayInputStream(bytes);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件的信息
     */
    public static FileInfo getFileInfo(String groupName, String fileName){
        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取文件的信息
            FileInfo file_info = storageClient.get_file_info(groupName, fileName);
            //返回
            return file_info;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件的删除
     */
    public static int deleteFile(String groupName, String fileName){
        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取文件的信息
            int i = storageClient.delete_file(groupName, fileName);

            return i;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 测试方法
     * @param args
     */
    public static void main(String[] args) throws Exception{
//        //下载文件获取输入流
//        InputStream is = download("group1", "M00/00/00/wKjThF-Ca6SAPLZsAHvcaCij3PU132.png");
//        //定义一个输出流
//        File file = new File("d:/123.jpg");
//        OutputStream os = new FileOutputStream(file);
//        byte[] buffer = new byte[1024];
//        //读文件
//        while (is.read(buffer) != -1){
//            os.write(buffer);
//        }
//        //释放资源
//        is.close();
//        os.close();
        deleteFile("group1", "M00/00/00/wKjThF-Ca6SAPLZsAHvcaCij3PU132.png");
    }
}
