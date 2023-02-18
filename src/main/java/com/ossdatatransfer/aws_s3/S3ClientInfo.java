package com.ossdatatransfer.aws_s3;

/**
 * S3 client 初始化信息
 *
 * @author zm https://github.com/zhangmeng001/OSSDataTransfer
 * @version 1.0
 * @date 2023/2/5 005 13:47
 */
public class S3ClientInfo {
    public String flag;
    public String account;
    public String password;
    public String bucketName;
    public String endpoint;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
