package com.ossdatatransfer.aws_s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.ossdatatransfer.tool.LoggerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * S3工具类，提供上传、下载等操作
 *
 * @author zm https://github.com/zhangmeng001/OSSDataTransfer
 * @version 1.0
 * @date 2023/2/5 005 16:22
 */
public class S3Util extends LoggerUtil {
    private  AmazonS3 amazonS3;
    //private String bucketName;

    public S3Util(AmazonS3 amazonS3,String bucketName) {
        this.amazonS3 = amazonS3;
        //this.bucketName = bucketName;
    }


    //检测账户配置信息是否正确
    public boolean checkS3Config(S3ClientInfo clientInfo){
        return this.isExistBucket(clientInfo.getBucketName());
    }

    /**
     * 检测桶是否存在
     *
     * @return true 存在
     */
    public  boolean isExistBucket(String bucketName) {
        List<Bucket> buckets = amazonS3.listBuckets();
        for (Bucket bucket : buckets) {
            if (bucket.getName().equals(bucketName)) {
                return true;
            }
        }
        return false;
    }
    /**
     *
     *清理未上传的分段请求
     * @param bucketName
     * @author zm
     * @date 2021/8/18 018 14:03
     * @return void
     */
    private void abortMultipartUpload(String bucketName){
        ListMultipartUploadsRequest allMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
        MultipartUploadListing multipartUploadListing = amazonS3.listMultipartUploads(allMultipartUploadsRequest);
        List<MultipartUpload> uploads = multipartUploadListing.getMultipartUploads();

        // Display information about all in-progress multipart uploads.
        logger.info(uploads.size() + " multipart upload(s) in progress.");
        for (MultipartUpload u : uploads) {

            System.out.println("Upload in progress: Key = \"" + u.getKey() + "\", id = " + u.getUploadId());
            amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName,u.getKey(),u.getUploadId()));
        }
        logger.info("----清理完成。。。。。");
    }

    /**
     *
     *使用TransferManager进行上传，可根据需要分段上传，增加上传速度
     * TransferManager会根据文件大小，选择是否进行分块上传。
     * 当文件小于等于16M时，TransferManager会自动调用PutObject接口，
     * 否则TransferManager会自动对文件进行分块上传。
     * @param file 需要上传的文件
     * @param objKey 上传的对象key，即全路径名，形如：home/123.txt
     * @param bucketName 桶名称
     * @author zm
     * @date 2021/8/18 018 10:00
     * @return boolean
     */
    public  boolean transferManagerUpload(File file, String objKey,String bucketName){

        if (objKey.startsWith("/")) {
            objKey = objKey.substring(1);
        }

        //切片大小，单位：M
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withS3Client(amazonS3)
                .build();

        logger.info("开始上传: {} .......",file.getAbsolutePath());
        long start_time = System.currentTimeMillis();
        try {
            Upload xfer = transferManager.upload(bucketName, objKey, file);

            ListMultipartUploadsRequest allMultipartUploadsRequest = new ListMultipartUploadsRequest(bucketName);
            MultipartUploadListing multipartUploadListing = amazonS3.listMultipartUploads(allMultipartUploadsRequest);
            List<MultipartUpload> uploads = multipartUploadListing.getMultipartUploads();

            // Display information about all in-progress multipart uploads.
            logger.info(uploads.size() + " multipart upload(s) in progress...");
            for (MultipartUpload u : uploads) {
                logger.info("Upload in progress: Key = \"" + u.getKey() + "\", id = " + u.getUploadId());
            }

            xfer.waitForUploadResult();
            //  or block with Transfer.waitForCompletion()
            //XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException | InterruptedException e) {
            logger.error("{}上传失败: {}",e.getCause(),objKey);
            return false;
        }
        //transferManager.shutdownNow();
        long end_time = System.currentTimeMillis();
        logger.info("{}上传完成。。。耗时："+(end_time-start_time)+"ms",objKey);

        return true;
    }

    public void uploadDirFile(File dir,String bucketName){
        File[] childFile = dir.listFiles();
        for (File file : childFile) {
            if (file.isDirectory()) {
                uploadDirFile(file,bucketName);
                continue;
            }
            if (file.getName().equals("OSSTransfer.jar")||file.getName().equals("AWS_S3.properties")) {
                continue;
            }
            long start = System.currentTimeMillis();
            System.out.println(System.currentTimeMillis()+"==上传文件："+file.getAbsolutePath());
            this.transferManagerUpload(file, file.getAbsolutePath(),bucketName);
            long end = System.currentTimeMillis();
            System.out.println("上传耗时【"+(end-start)+"】ms.");
        }
    }

    /**
     *
     *使用TransferManager进行下载
     * @param file 需要上传的文件
     * @param objKey 上传的对象key，即全路径名，形如：home/123.txt
     * @author zm
     * @date 2021/8/18 018 10:00
     * @return boolean
     */
    public boolean transferManagerDownload(File file,String objKey,String bucketName){
        //切片大小，单位：M
        long mininumSize = 6*1024*1024;
        TransferManager transferManager = TransferManagerBuilder.standard()
                .withMultipartUploadThreshold(mininumSize)
                .withS3Client(amazonS3)
                .build();

        logger.info("开始下载: {} .......",file.getAbsolutePath());
        long start_time = System.currentTimeMillis();
        try {
            Download xfer = transferManager.download(bucketName, objKey, file);
            // loop with Transfer.isDone()
            //XferMgrProgress.showTransferProgress(xfer);
            //XferMgrProgress.waitForCompletion(xfer);
            xfer.waitForCompletion();
        } catch (AmazonServiceException | InterruptedException e) {
            logger.error("{}下载失败: {}",e.getCause(),objKey);
            return false;
        }
        //transferManager.shutdownNow();
        long end_time = System.currentTimeMillis();
        logger.info("{}下载完成，耗时："+(end_time-start_time)+"ms",objKey);

        return true;
    }



    /**
     * 删除对象
     * @param objectName  oss对象名称，如：data/241/、 data/241/123.zip、/data/241/、/data/241/123.zip
     * @return
     */
    public Boolean deleteObject(String objectName,String bucketName) {
        logger.info("--- 删除oss对象：{}",objectName);
        try {
            DeleteObjectRequest del = new DeleteObjectRequest(bucketName, objectName );
            amazonS3.deleteObject(del);
            logger.info("---删除文件【{}】成功------",objectName);
            return true;
        } catch (Exception e) {
            logger.error("删除文件【{}】失败，失败信息：{}",objectName,e.getCause());
            return false;
        }
    }

    /**
     *
     * 模糊删除，根据提供的路径及文件中包含的关键字，删除路径下文件名包含关键字的文件
     * @param prefixPath -文件路径，按oss路径规则，不以“/”开头，形如：data/241/
     * @param fileNameKeyWord
     * @author zm
     * @date 2021/8/19 019 17:03
     * @return boolean
     */
    public void deleteLike(String prefixPath,String fileNameKeyWord,String bucketName){
        List<String> fileNameList = listBucketObjects(prefixPath,bucketName);
        for (String fileName:fileNameList){
            if (fileName.contains(fileNameKeyWord)){
                deleteObject(fileName,bucketName);
            }
        }
    }

    /**
     *删除执行路径下的所有对象
     *
     * @param prefixPath 指定路径
     * @param bucketName 桶名
     * @author zm
     * @date 2023/2/5 005 16:58
     * @return void
     */
    public int deleteDir(String prefixPath,String bucketName){
        ObjectListing listObjects = amazonS3.listObjects(bucketName,prefixPath);

        List<S3ObjectSummary> objectSummaries = listObjects.getObjectSummaries();
        int size = objectSummaries.size();

        String objKey = "";
        for (int i = size-1; i >=0; i--) {
            objKey = objectSummaries.get(i).getKey();
            this.deleteObject(objKey,bucketName);
        }
        logger.info("删除完成，共删除{}个对象。",size);
        return size;
    }



    /**
     *
     * 获取包含指定前缀的对象，即：获取指定目录下所有文件路径和文件夹路径，包含执行路径本身
     * @param prefixPath 指定前缀,以“/”结尾
     * @author zm
     * @date 2021/7/28 028 11:17
     * @return List<String> oss对象名称，例如：data/241/、data/241/123.zip
     */
    public List<String> listBucketObjects(String prefixPath,String bucketName) {
        logger.info("-----获取指定前缀{}的oss对象",prefixPath);
        //对传入的path参数做简单处理
        if (prefixPath.startsWith("/")){
            prefixPath = prefixPath.substring(1,prefixPath.length());
        }

        if (!prefixPath.endsWith("/")){
            prefixPath = prefixPath +"/";
        }

        List<String> listName=new ArrayList<>();

        ObjectListing listObjects = amazonS3.listObjects(bucketName,prefixPath);
        List<S3ObjectSummary> objectSummaries = listObjects.getObjectSummaries();
        int size = objectSummaries.size();

        String ossPath = "";
        for (int i = 0; i < size; i++) {
            ossPath = objectSummaries.get(i).getKey();
            //以传参路径开头且长度大于参数的路径，则认为该路径是传参路径下的文件
            if(ossPath.startsWith(prefixPath) && !isDirectory(ossPath) && ossPath.length()>prefixPath.length() ){
                logger.info("----获取到的{}路径下的文件：{}","/"+prefixPath,ossPath);
                listName.add(ossPath);
            }
        }
        return listName;
    }

    /**
     *
     * 判断oss对象的路径是否为文件夹
     * @param objectPath oss对象的路径
     * @author zm
     * @date 2021/7/29 029 11:04
     * @return boolean
     */
    public static boolean isDirectory(String objectPath){
        //oss对象以“/”结尾的为文件夹
        if (objectPath.endsWith("/")){
            return true;
        }
        return false;
    }
}
