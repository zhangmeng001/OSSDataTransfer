package com.ossdatatransfer;

/**
 * TODO
 *
 * @author zm https://github.com/zhangmeng001/OSSDataTransfer
 * @version 1.0
 * @date 2022/12/15 015 17:12
 */
public abstract class  OSSFatory {
    public static  Object OSSSession ;

    //初始化session
    public abstract Object getOSSSession();

    //上传
    public abstract Object doUpload();

    //下载
    public abstract Object doDownload();

    //删除
    public abstract Object doDelete();

    //查询
    public abstract Object doQuery();
}
