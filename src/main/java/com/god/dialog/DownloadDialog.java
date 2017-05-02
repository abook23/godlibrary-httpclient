package com.god.dialog;

import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.god.http.R;
import com.god.listener.DownloadListener;
import com.god.service.DownloadService;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by yzz on 2016/4/21.
 */
public class DownloadDialog extends DialogFragment implements View.OnClickListener {

    private Button button_cancel, button_task;
    private TextView textView_content, textView_title;
    private String url, dirsName, fileName;

    public static String URL = "url";
    public static String DIRS_NAME = "dirsName";
    public static String FILE_NAME = "fileName";
    private Intent intent;
    private ProgressBar progressBar;
    private DownloadService downloadService;
    private float mSize, mMax;
    private Handler handler = new Handler();

    private UpdateView updateView;


    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder mBuilder;
    private Notification notification;
    private Class mclass;


    private int notifyId = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            url = getArguments().getString(URL);
            dirsName = getArguments().getString(DIRS_NAME);
            fileName = getArguments().getString(FILE_NAME);
            intent = new Intent();
            if (!DownloadService.isDownload) {

                intent.putExtra(URL, url);
                intent.putExtra(DIRS_NAME, dirsName);
                intent.putExtra(FILE_NAME, fileName);
                intent.setClass(getActivity(), DownloadService.class);
                getActivity().startService(intent);

            }
            if (mNotificationManager == null)
                initNotify(mclass);
            updateView = new UpdateView();

        }
    }

    private void initNotify(Class mclass) {
        mNotificationManager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getActivity());
        mBuilder.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                // .setPriority(Notification.PRIORITY_DEFAULT)// 优先级
                .setOngoing(false).setDefaults(0);// 震动，声音，闪光
                //.setSmallIcon(R.mipmap.logo);
        mNotificationManager.cancelAll();
        showProgressNotify();//显示进度都条
        initContentIntent(mclass);//点击通知栏 跳转

    }


    /**
     * 显示进度条
     */
    private void showProgressNotify() {
        mBuilder.setContentTitle(fileName).setContentText("0/0");
        mBuilder.setProgress(100, 0, false);
    }


    /**
     * 点击通知栏 跳转
     */
    private void initContentIntent(Class<?> startActivity) {
        if (startActivity !=null){
            Intent intent = new Intent(getActivity(), startActivity);
            PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
        }
    }

    public static DownloadDialog newInstance(String url, String dirsName, String fileName) {
        DownloadDialog fragment = new DownloadDialog();
        Bundle args = new Bundle();
        args.putString(URL, url);
        args.putString(DIRS_NAME, dirsName);
        args.putString(FILE_NAME, fileName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_down, container);
        button_cancel = (Button) view.findViewById(R.id.button_cancel);
        button_task = (Button) view.findViewById(R.id.button_task);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        textView_content = (TextView) view.findViewById(R.id.textView_content);
        textView_title = (TextView) view.findViewById(R.id.textView_title);
        button_cancel.setOnClickListener(this);
        button_task.setOnClickListener(this);
        intent.setClass(getActivity(), DownloadService.class);
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        return view;

    }

    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.DownloadBinder) service).getService();
            downloadService.setDownloadListener(downloadListener);
            downloadService.setNotifacation(mNotificationManager);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onClick(View v) {
        if (v == button_cancel) {
            dismiss();

            downloadService.onCancel();
            intent.setClass(getActivity(), DownloadService.class);
            getActivity().stopService(intent);
            mNotificationManager.cancelAll();
            mNotificationManager = null;

        } else if (v == button_task) {


            notification = mBuilder.build();

            notification.flags = Notification.FLAG_AUTO_CANCEL;
//            downloadService.startForeground(notifyId, notification);
            mNotificationManager.notify(notifyId, mBuilder.build());
            dismiss();
        }
    }


    private DownloadListener downloadListener = new DownloadListener() {
        int count;

        @Override
        public void onStart(float fileByteSize) {
            DownloadService.isDownload = true;
            mMax = fileByteSize / (1024 * 1024);

        }

        @Override
        public void onPause() {
            Log.v("onPause", "onPause");
        }

        @Override
        public void onResume() {
            Log.v("onResume", "onResume");
        }

        @Override
        public void onSize(float size, float maxSize) {
            mSize = size / (1024 * 1024);
            mMax = maxSize / (1024 * 1024);
            count++;
            if (count > 50) {
                count = 0;
                handler.postDelayed(updateView, 100);
            }
        }

        @Override
        public void onFail() {
            DownloadService.isDownload = false;
        }

        @Override
        public void onSuccess(File file) {
            DownloadService.isDownload = false;
            if (file.getName().endsWith(".apk"))
                downloadService.install(file);
        }

        @Override
        public void onCancel() {
            DownloadService.isDownload = false;
        }
    };

    public void setStartNotifyClass(Class mclass) {
        this.mclass = mclass;
    }


    public class UpdateView implements Runnable {
        private float size, max;

        public UpdateView() {
        }

        @Override
        public void run() {
            size = mSize;
            max = mMax;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
            String _size = decimalFormat.format(size);//format 返回的是字符串
            String _max = decimalFormat.format(max);
            if (isVisible()) {
//                mNotificationManager.cancel(notifyId);
                textView_title.setText("下载中....");
                progressBar.setIndeterminate(false);
                progressBar.setProgress((int) size);
                progressBar.setMax((int) max);
                textView_content.setText(_size + "/" + _max + " MB");
//                Log.d(TAG, "下载大小：" + _size + "/" + _max + " MB");
            } else {//通知栏
//                Log.d(TAG, "通知栏显示：" + _size + "/" + _max + "MB");
                mBuilder.setProgress((int) max, (int) size, false).setContentText(_size + "/" + _max + "MB"); // 这个方法是显示进度条
                mBuilder.setDefaults(0);
                if (mNotificationManager != null) {
                    mNotificationManager.notify(notifyId, mBuilder.build());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(conn);
    }
}
