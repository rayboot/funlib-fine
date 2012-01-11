package com.funlib.download;

/**
 * 下载状态
 * @author feng
 *
 */
public class DownloadStatus {

    public static final int STATUS_PAUSE                				=       2;  /** 暂停 */
    public static final int STATUS_DOWNLOADING          				=       3;  /** 正在下载 */
    public static final int STATUS_WAITTING             				=       4;  /** 等待下载 */
    public static final int STATUS_COMPLETE             				=       5;  /** 下载完成 */
    public static final int STATUS_UNKNOWN              				=       6;  /** 未知错误 */
    public static final int STATUS_CANCELED             				=       7;  /** 取消下载任务 */
    public static final int STATUS_NOT_EXISTS           				=       8;  /** 下载任务不存在,可以添加下载 */
    public static final int STATUS_DOWNLOAD_FILE_NOT_FOUND             	=       9;  /** 文件不存在 */
    public static final int STATUS_STARTDOWNLOADING						=		10; /** 将要开始下载 */
    public static final int STATUS_RW_FILE_ERROR						=		11;	/** 文件读写错误 */
    public static final int STATUS_NET_ERROR							=		12; /** 网络连接失败 */
    
}
