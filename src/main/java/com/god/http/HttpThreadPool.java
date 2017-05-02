package com.god.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.god.listener.HttpRequestListener;
import com.god.util.ExecutorServiceUtils;

import org.apache.http.impl.client.BasicCookieStoreHC4;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by My on 2016/5/27.
 */
public abstract class HttpThreadPool<Result> {
    private static ExecutorServiceUtils executorServiceUtils;
    private int requestCode;
    private HttpRequestListener<Result> httpRequestListener;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Boolean> mThreadUrl = new HashMap<>();//线程池中的网络请求
    private String url;
    private HttpInfo httpInfo;
    protected HttpModel httpModel = HttpModel.get;

    public HttpThreadPool setHttpRequestListener(HttpRequestListener<Result> mListener) {
        this.httpRequestListener = mListener;
        return this;
    }

    public HttpThreadPool setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    private static ExecutorServiceUtils getInstance() {
        if (executorServiceUtils == null)
            executorServiceUtils = ExecutorServiceUtils.initialize();
        return executorServiceUtils;
    }

    protected HttpThreadPool<Result> setModel(HttpModel httpModel) {
        this.httpModel = httpModel;
        return this;
    }

    public HttpThreadPool<Result> setUrl(String url) {
        this.url = url;
        return this;
    }


    public HttpThreadPool<Result> addParams(String key, String value) {
        params.put(key, value);
        httpModel = HttpModel.post;
        return this;
    }

    public HttpThreadPool<Result> setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public HttpThreadPool<Result> post(final String url) {
        httpModel = HttpModel.post;
        this.url = url;
        return this;
    }

    public HttpThreadPool<Result> post(final String url, Map<String, Object> params) {
        this.url = url;
        this.params = params;
        httpModel = HttpModel.post;
        return this;
    }

    public HttpThreadPool<Result> get(final String url) {
        this.url = url;
        httpModel = HttpModel.get;
        return this;
    }

    public void setCookie(BasicCookieStoreHC4 cookieStore) {
        HttpUtils.cookieStore = cookieStore;
    }

    public BasicCookieStoreHC4 getCookie() {
        return HttpUtils.cookieStore;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onPostExecute((Result) msg.obj);
        }
    };

    public void execute() {
        mThreadUrl.put(url, true);
        onPreExecute();
        getInstance().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (mThreadUrl.containsKey(url)) {
                    switch (httpModel) {
                        case post:
                            httpInfo = HttpUtils.post(url, params);
                            break;
                        case get:
                            httpInfo = HttpUtils.get(url, params);
                            break;
                        default:
                            break;
                    }
                    // Result result = JSON.parseObject(httpInfo.getResult(), new TypeReference<Result>(){}.getType());
                    // Result result = new Gson().fromJson(httpInfo.getResult(), resultClass);
                    //  Result result = new Gson().fromJson("", new TypeToken<Result>() {}.getType());
                    if (mThreadUrl.containsKey(url)) {//有可能网络请求回来，用户已经取消了
                        Result result = doInBackground(httpInfo.getResult());
                        doInBackground(httpInfo);
                        handler.obtainMessage(1, result).sendToTarget();
                    }
                }
                mThreadUrl.remove(url);
            }
        });
    }

    public void cancel() {
        mThreadUrl.put(url, false);
    }

    public void cancelAll() {
        getInstance().cancelTask();
    }

    private void setHttpResultSuccessCallBack(Result result, int requestCode) {
        if (httpRequestListener != null) {
            httpRequestListener.onHttpSuccess(result, requestCode);
        }
    }

    private void setHttpResultErrorCallBack(String msg, int httpCode, int resultCode) {
        if (httpRequestListener != null) {
            httpRequestListener.onHttpError(msg, httpCode, resultCode);
        }
    }

    /**
     * 线程执行之前
     */
    protected void onPreExecute() {
    }

    /**
     * 线程中
     *
     * @param result http 请求返回数据
     * @return Model
     */
    protected abstract Result doInBackground(String result);

    protected void doInBackground(HttpInfo httpInfo) {
    }

    protected Result onPostExecute() {
        return null;
    }

    /**
     * 线程执行完成
     */
    protected void onPostExecute(Result r) {
        Result result = onPostExecute();
        if (result == null)
            result = r;
        if (httpRequestListener != null)
            if (httpInfo.getHttpCode() == 200) {
                setHttpResultSuccessCallBack(result, requestCode);
            } else {
                setHttpResultErrorCallBack(HttpCodeType.getHttpMsg(httpInfo.getHttpCode()),
                        httpInfo.getHttpCode(), requestCode);
            }
    }

    public enum HttpModel {
        get, post
    }
}
