package com.ossdatatransfer;

import com.amazonaws.services.s3.AmazonS3;
import com.ossdatatransfer.aws_s3.ConfigUtil;
import com.ossdatatransfer.aws_s3.S3ClientInfo;
import com.ossdatatransfer.aws_s3.S3Factory;
import com.ossdatatransfer.aws_s3.S3Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * 程序主启动类
 *
 * @author zm https://github.com/zhangmeng001/OSSDataTransfer
 * @version 1.0
 * @date 2023/2/5 005 12:57
 */
public class MainApp {
    private static boolean IS_CONFIG_LOADING = false;
    public static Logger logger = LogManager.getLogger(MainApp.class);
    public static void main(String[] args) {
        //1、判断是否存在配置文件，不存在提示按步骤配置
        ConfigUtil configUtil = new ConfigUtil();
        logger.info("检查初始化配置文件....");
        S3ClientInfo s3ClientInfo = new S3ClientInfo();

        while (true) {
            if (configUtil.if_exst_config()){
                if (!IS_CONFIG_LOADING){
                    s3ClientInfo = configUtil.loadConfig();
                }
                //3、测试配置文件是否配置正确
                AmazonS3 amazonS3 = new S3Factory(s3ClientInfo).getAmazonS3();
                S3Util s3Util = new S3Util(amazonS3, s3ClientInfo.getBucketName());
                boolean ifConfigOk = s3Util.checkS3Config(s3ClientInfo);

                if (ifConfigOk) {
                    //4、打印工具头
                    printHeader();
                    //5、选择操作序号
                    choseOperation(s3Util,s3ClientInfo.getBucketName());
                }else {
                    System.out.println("");
                    System.out.println("===========【oss账户信息配置错误，无法连接，请重新配置！】=======================");
                    //2、初始化文件配置
                    initConfig(configUtil);
                }
            }else {
                System.out.println("");
                System.out.println("===========【还未进行初始化配置，请先进行初始化配置！】=======================");
                //2、初始化文件配置
                initConfig(configUtil);
            }
        }

    }

    private static void initConfig(ConfigUtil configUtil)  {
        Scanner in = new Scanner(System.in);

        System.out.println("请输入oss 账号(回车确认)：");
        String account = in.nextLine();

        System.out.println("请输入oss 密码(回车确认)：");
        String pwd = in.nextLine();

        System.out.println("请选择账户/密码是不是加密格式，0-未加密，1-加密(回车确认)：");
        String flag = in.nextLine();

        System.out.println("请输入oss桶名称(回车确认)：");
        String bucketName = in.nextLine();

        System.out.println("请输入oss地址(回车确认)：");
        String endpointName = in.nextLine();

        try {
            configUtil.geneateConfig(flag,account,pwd,bucketName,endpointName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printHeader() {
        System.out.println("");
        System.out.println("");
        System.out.println("##################################################");
        System.out.println("                                                  ");
        System.out.println("		AWS S3 Transfer 2.0                      ");
        System.out.println("			author:zm                        ");
        System.out.println("                                                ");
        System.out.println("	       使用此工具，你可以进行如下操作：          ");
        System.out.println("			1-备份           ");
        System.out.println("			2-迁移            ");
        System.out.println("			3-恢复（下载）     ");
        System.out.println("			4-删除            ");
        System.out.println("			0-退出            ");
        System.out.println("                              ");
        System.out.println("################################################");
    }

    private static void choseOperation(S3Util s3Util,String bucketName){
        Scanner in = new Scanner(System.in);
        List<String> filePathList = new ArrayList<>();

        String srcPath;
        String targetPath;
        String ossOject;

        while(true){
            System.out.println("");
            System.out.println("");
            System.out.println("请输入需要进行的操作序号(回车确认)：");
            String operateCode = in.nextLine();

            switch (operateCode) {
                case "1":
                    System.out.println("=====【备份数据到oss】========");
                    System.out.println("请输入源路径(回车确认)：");
                    srcPath = in.nextLine();

                    System.out.println("请输入目标路径(回车确认)：");
                    targetPath = in.nextLine();

                    //备份数据逻辑
                    System.out.println("进行数据备份,将源路径下的文件按原有层级关系备份到目标路径下.......");
                    getCurrentPatFileNameList(filePathList, srcPath);

                    for(String filePath:filePathList){
                        ossOject = filePath.replace(srcPath,targetPath);
                        s3Util.transferManagerUpload(new File(filePath), ossOject,bucketName);
                    }

                    logger.info("备份完成！！！");

                    System.exit(0);
                    break;
                case "2":
                    //移动数据逻辑;
                    System.out.println("=====【移动数据到oss】========");
                    System.out.println("请输入源路径(回车确认)：");
                    srcPath = in.nextLine();

                    System.out.println("请输入目标路径(回车确认)：");
                    targetPath = in.nextLine();

                    //备份数据逻辑
                    System.out.println("进行数据迁移,将源路径下的文件按原有层级关系迁移到目标路径下.......");
                    getCurrentPatFileNameList(filePathList, srcPath);

                    for(String filePath:filePathList){
                        ossOject = filePath.replace(srcPath,targetPath);
                        s3Util.transferManagerUpload(new File(filePath), ossOject,bucketName);
                    }

                    logger.info("迁移完成！！！");
                    System.exit(0);

                    break;
                case "3":
                    //回退数据逻辑;
                    System.out.println("=====【回退oss数据到服务器】========");
                    System.out.println("=====【数据回退目前仅支持整个文件夹的数据回退，不支持单文件的回退】====");
                    System.out.println("请输入oss对象源路径(回车确认)：");
                    srcPath = in.nextLine();

                    System.out.println("请输入目标路径(回车确认)：");
                    targetPath = in.nextLine();

                    //备份数据逻辑
                    System.out.println("进行数据恢复,将源路径下的文件按原有层级关系迁移到目标路径下.......");
                    List<String> list = s3Util.listBucketObjects(srcPath,bucketName);

                    for(String objKey: list){
                        if (s3Util.isDirectory(objKey)) {
                            continue;
                        }
                        ossOject = objKey.replace(srcPath,targetPath);
                        s3Util.transferManagerDownload(new File(ossOject), objKey,bucketName);
                    }

                    logger.info("回退备份数据完成！！！");

                    System.exit(0);

                    break;
                case "4":
                    //删除数据逻辑;
                    System.out.println("=====【删除oss数据】========");
                    System.out.println("请输入删除路径(回车确认)：");
                    targetPath = in.nextLine();
                    System.out.println("确定删除【"+targetPath+"】路径及其下的文件？(Y/N):");
                    operateCode = in.nextLine();
                    if ("Y".equalsIgnoreCase(operateCode)) {
                        //进行删除操作
                        if (s3Util.isDirectory(targetPath)) {
                            s3Util.deleteDir(targetPath,bucketName);
                        }else {
                            s3Util.deleteObject(targetPath,bucketName);
                        }
                    }else if("N".equalsIgnoreCase(operateCode)) {
                        System.out.println("【取消】删除操作！！");
                    }
                    printHeader();

                    break;
                case "0":
                    //退出
                    System.out.println("退出");

                    System.exit(0);
                    break;
                default:
                    System.err.println("无效的操作序号！！！！");
                    printHeader();

                    break;
            }
        }
    }

    private static void getCurrentPatFileNameList(List<String> list , String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            try {
                for(File subfile:subFiles){
                    getCurrentPatFileNameList(list , subfile.getCanonicalPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{//需要排除的文件
            //if ( !"OSSTransfer.jar".equals(file.getName()) &&  !"AWS_S3.properties".equals(file.getName()) && !"ossTransfer.log".equals(file.getName())) {
            //    list.add(file.getCanonicalPath());
            //}
        }

    }
}
