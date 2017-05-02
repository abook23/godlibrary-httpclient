package com.god.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.god.dialog.DownloadDialog;
import com.god.http.DownLoadUtils;
import com.god.listener.DownloadListener;

import java.io.File;

/**
 * Created by yzz on 2016/4/21.
 */
public class DownloadService extends Service {


    public static boolean isDownload = false;
    private Context context;
    private String url, dirsName, fileName;
    public IBinder mBuilder = new DownloadBinder();
    private NotificationManager mNotificationManager = null;
    private DownLoadUtils downLoadUtils;

    public DownloadService() {

    }

    public void setDownloadListener(DownloadListener downloadListener) {
        downLoadUtils.setDownloadListener(downloadListener);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (context == null) {
            context = getApplicationContext();
        }
        downLoadUtils = new DownLoadUtils(context);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBuilder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        flags = START_STICKY;
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        url = intent.getStringExtra(DownloadDialog.URL);
        dirsName = intent.getStringExtra(DownloadDialog.DIRS_NAME);
        fileName = intent.getStringExtra(DownloadDialog.FILE_NAME);
        if (url != null) {
            downLoadUtils.down(url, dirsName, fileName,false);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        isDownload = false;
    }

    /**
     * 取消
     */
    public void onCancel() {
        isDownload = false;
        downLoadUtils.cancel();
    }

    /**
     * 暂停、继续
     */
    public void onPause() {
        if (downLoadUtils.isPause()) {
            downLoadUtils.resume();
        } else {
            downLoadUtils.pause();
        }
    }

    /**
     * 安装apk
     *
     * @param file 要安装的apk的目录
     */
    public void install(File file) {
        if (file != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            context.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
            isDownload = false;
//		    如果没有android.os.Process.killProcess(android.os.Process.myPid());最后不会提示完成、打开。
//			如果没有i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);这一步的话，最后安装好了，点打开，是不会打开新版本应用的。
//          this.finish();
        }

    }

    public void setNotifacation(NotificationManager mNotificationManager) {
        this.mNotificationManager = mNotificationManager;
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}
