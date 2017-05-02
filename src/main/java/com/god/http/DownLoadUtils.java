package com.god.http;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.god.listener.DownloadListener;

import java.io.File;

/**
 * Created by abook23 on 2015/12/7.
 */
public class DownLoadUtils {

    private final String TAG = "DownLoadUtils";
    private DownLoad downLoad;
    private Context mContext;

    public DownLoadUtils(Context context) {
        this.mContext =context;
        downLoad = new DownLoad();
    }

    /**
     * 取消 队列中的任务
     * 真正现在的线程 无法终止
     */
    public synchronized void cancelTask() {
        Log.d(TAG, "取消下载");
        downLoad.cancelTask();
    }

    public boolean isPause() {
        return downLoad.isPause();
    }

    public void pause() {
        downLoad.pause();
    }

    public void cancel() {
        downLoad.cancel();
    }

    public void resume() {
        downLoad.resume();
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        downLoad.setDownloadListener(downloadListener);
    }

    public void down(String url) {
        downLoad.down(url, getDownLoadRootPath(url, null, null), false);
    }

    public void down(String url, String dirsName) {
        downLoad.down(url, getDownLoadRootPath(url, dirsName, null), false);
    }

    public void down(String url, String dirsName, String fileName, boolean overlap) {
        downLoad.down(url, getDownLoadRootPath(url, dirsName, fileName), overlap);
    }

    public void down(String url, String dirsName, String fileName, boolean overlap, DownloadListener downloadListener) {
        downLoad.down(url, getDownLoadRootPath(url, dirsName, fileName), overlap, downloadListener);
    }

    private String getDownLoadRootPath(String url, String dirsName, String fileName) {
        String path = getDownloadDir(mContext);
        if (dirsName!=null && dirsName.length()>0)
            path += File.separator + dirsName;
        if (fileName!=null && fileName.length()>0) {
            fileName = File.separator + fileName;
        } else {
            fileName = File.separator + url.substring(url.lastIndexOf("/") + 1);
        }
        path += fileName;
        return path.replace("//", "/");
    }


    public static String getSDPath(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    public static String getDownloadDir(Context context) {
        return getSDPath(context) + File.separator + "Download";
    }

}
