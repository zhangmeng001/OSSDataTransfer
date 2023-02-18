package com.ossdatatransfer.aws_s3;

import com.ossdatatransfer.tool.LoggerUtil;

import java.io.*;
import java.util.Properties;

/**
 * 配置文件工具类
 *
 * @author zm https://github.com/zhangmeng001/OSSDataTransfer
 * @version 1.0
 * @date 2023/2/5 005 11:51
 */
public class ConfigUtil extends LoggerUtil {

    //明文
    public static final String CONSTANT_DECRYPT_FLAG = "0";
    //密文
    public static final String CONSTANT_ENCRYPT_FLAG = "1";

    public static final String CONSTANT_CONFIG_FILE = "config.properties";


    /**
     *生成配置文件
     *
     * @param flg 账号密码是明文密文，0-明文，1-密文
     * @param acount 账户
     * @param pwd 密码
     * @param bucketName 桶名称
     * @param endpointName oss地址
     * @author zm
     * @date 2023/2/5 005 12:19
     * @return void
     */
    public void geneateConfig(String flg,String acount,String pwd,String bucketName,String endpointName)
            throws IOException {
        Properties properties=new Properties();
        properties.setProperty("oss.flag",flg);
        properties.setProperty("oss.acount",acount);
        properties.setProperty("oss.password",pwd);
        properties.setProperty("oss.bucketName",bucketName);
        properties.setProperty("oss.endpointName",endpointName);
        //properties.setProperty("oss.ignore",ignoreStr);

        properties.store(new FileWriter(CONSTANT_CONFIG_FILE), "This is a AWS S3 config properties");
        logger.info("config properties init successfully！");
    }

    public boolean if_exst_config() throws NullPointerException{
        File file=new File(CONSTANT_CONFIG_FILE);
        return file.exists();
    }

    public S3ClientInfo loadConfig()  {
        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(CONSTANT_CONFIG_FILE);
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        S3ClientInfo clientinfo = new S3ClientInfo();
        clientinfo.setFlag(prop.getProperty("oss.flag"));
        clientinfo.setAccount(prop.getProperty("oss.acount"));
        clientinfo.setPassword(prop.getProperty("oss.password"));
        clientinfo.setBucketName(prop.getProperty("oss.bucketName"));
        clientinfo.setEndpoint(prop.getProperty("oss.endpointName"));

        logger.info("config properties load successfully！");

        return clientinfo;
    }


    public static void main(String[] args) throws IOException {
        ConfigUtil configUtil = new ConfigUtil();
        configUtil.geneateConfig("1","cmhsbGJzdXNlcg==","0444cb5c0f260204bf48af33edaa79bc","rhllbs",
                "test.osstest.zm.com.cn");
        System.out.println("输出完成");
        System.out.println(configUtil.if_exst_config());
    }
}
