package com.god.http;

import com.god.listener.HttpRequestListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by My on 2016/7/11. s
 */
public class HttpThreadUtils {
    private HttpThreadPool httpThreadPool;
    private HttpRequestListener mListener;

    private Map<String, Object> params = new HashMap<>();
    private HttpThreadPool.HttpModel httpModel = HttpThreadPool.HttpModel.get;
    private String url;
    private Class<?> jsonBean;

    public HttpThreadPool getInstance() {
        if (httpThreadPool == null) {
            httpThreadPool = new HttpThreadPool() {
                @Override
                protected Object doInBackground(String result) {
                    if (jsonBean == null)
                        return null;
                    try {
                        String type = jsonBean.getSimpleName();
                        if ("String".equals(type) || "Long".equals(type)
                                || "Float".equals(type) || "Boolean".equals(type) || "Integer".equals(type))
                            return result;
                        return new Gson().fromJson(result, jsonBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
        return httpThreadPool;
    }

    public <T> HttpThreadUtils setHttpRequestListener(Class<T> jsonBean, HttpRequestListener<T> listener) {
        this.mListener = listener;
        this.jsonBean = jsonBean;
        return this;
    }

    public HttpThreadUtils addParams(String key, String value) {
        params.put(key, value);
        return this;
    }

    public HttpThreadUtils setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public static HttpThreadUtils get(final String url) {
        HttpThreadUtils httpThreadUtils = new HttpThreadUtils();
        httpThreadUtils.url = url;
        httpThreadUtils.httpModel = HttpThreadPool.HttpModel.get;
        return httpThreadUtils;
    }

    public static HttpThreadUtils post(final String url) {
        HttpThreadUtils httpThreadUtils = new HttpThreadUtils();
        httpThreadUtils.httpModel = HttpThreadPool.HttpModel.post;
        httpThreadUtils.url = url;
        return httpThreadUtils;
    }

    public HttpThreadUtils post(final String url, Map<String, Object> params) {
        this.url = url;
        this.params = params;
        httpModel = HttpThreadPool.HttpModel.post;
        return this;
    }


    @SuppressWarnings("unchecked")
    public void execute() {
        getInstance();
        httpThreadPool.setHttpRequestListener(mListener);
        httpThreadPool.httpModel = httpModel;
        httpThreadPool.setUrl(url);
        httpThreadPool.setParams(params);
        httpThreadPool.execute();
    }

    public void cancel() {
        httpThreadPool.cancel();
    }

}
