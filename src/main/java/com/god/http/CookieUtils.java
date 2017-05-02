package com.god.http;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.impl.client.BasicCookieStoreHC4;

import java.util.List;

/**
 * Created by My on 2016/10/14.
 */

public class CookieUtils {

    private static final String SPF_NAME = "cookie_preferences";
    private static final String COOKIE_NAME = "cookie_name";

    /**
     * http 登陆之后
     *
     * @param context
     */
    public static void saveCookie(Context context) {
        SharedPreferences spf = context.getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        List<Cookie> cookies = HttpUtils.getCookie();
        String value = gson.toJson(cookies);
        SharedPreferences.Editor edit = spf.edit();
        edit.putString(COOKIE_NAME, value);
        edit.apply();
    }

    /**
     * http 请求之前
     *
     * @param context
     * @deprecated
     */
    public static BasicCookieStoreHC4 getCookie(Context context) {
        SharedPreferences spf = context.getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);
        String value = spf.getString(COOKIE_NAME, null);
        if (value != null) {
            List<Cookie> cookies = new Gson().fromJson(value, new TypeToken<List<Cookie>>() {
            }.getType());
            HttpUtils.setCookie(cookies);
        }
        return HttpUtils.cookieStore;
    }

    /**
     * http 请求之前
     *
     * @param context
     */
    public static BasicCookieStoreHC4 initCookie(Context context) {
        return getCookie(context);
    }
}
