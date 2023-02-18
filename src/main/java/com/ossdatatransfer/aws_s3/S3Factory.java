package com.ossdatatransfer.aws_s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.ossdatatransfer.tool.LoggerUtil;
import com.ossdatatransfer.tool.MD5Tool;
import net.iharder.Base64;

/**
 * 亚马逊S3
 *
 * @author zm https://github.com/zhangmeng001/OSSDataTransfer
 * @version 1.0
 * @date 2022/12/15 015 17:21
 */
public class S3Factory extends LoggerUtil {
    private  AmazonS3 amazonS3;

    public S3Factory(S3ClientInfo clientInfo) {
        this.initS3Client(clientInfo);
    }

    public AmazonS3 getAmazonS3() {
        return amazonS3;
    }


    private void initS3Client(S3ClientInfo clientInfo){

        String flag =clientInfo.getFlag();
        if (ConfigUtil.CONSTANT_DECRYPT_FLAG.equals(flag)){//账户密码是明文
            //对明文进行加密
            this.initInstance(Base64.encodeBytes(clientInfo.getAccount().getBytes()),
                    MD5Tool.string2MD5(clientInfo.getPassword()),clientInfo.getEndpoint());
        }else if(ConfigUtil.CONSTANT_ENCRYPT_FLAG.equals(flag)){//账户密码是密文
            this.initInstance(clientInfo.getAccount(),clientInfo.getPassword(),clientInfo.getEndpoint());
        }
    }

    /**
     *初始化amazonS3 client 连接
     *
     * @param username 用户
     * @param pwd 密码
     * @param endpointName oss地址
     * @author zm
     * @date 2023/2/5 005 13:44
     * @return void
     */
    private void initInstance(String username, String pwd, String endpointName) {
        ClientConfiguration myClientConfig = new ClientConfiguration();
        myClientConfig.setProtocol(Protocol.HTTP);
        myClientConfig.setSignerOverride("S3SignerType");
//		myClientConfig.withMaxConnections(10);
//		myClientConfig.withTcpKeepAlive(true);

        AWSCredentials awsCredentials = new BasicAWSCredentials(username,pwd);
        amazonS3 = new AmazonS3Client(awsCredentials, myClientConfig);
        S3ClientOptions s3o = S3ClientOptions.builder().build();
        amazonS3.setEndpoint(endpointName);
        amazonS3.setS3ClientOptions(s3o);
        logger.info("amazonS3 client init successfully !");
    }



    public static void main(String[] args) {
        String target1 = "cmhsbGJzdXNlcg==";
        String target2 = "0444cb5c0f260204bf48af33edaa79bc";

        String s1 = Base64.encodeBytes("rhllbsuser".getBytes());
        String s2 = MD5Tool.string2MD5("TEST*rhllbsuser123");

        System.out.println(target1.equals(s1));
        System.out.println(target2.equals(s2));
    }

}
