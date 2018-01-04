package com.team2.wechat.utils;


import android.content.Context;
import android.widget.Toast;

import com.team2.wechat.MainActivity;

/**
 * Created by wjsay on 2017/12/25.
 */

public class ToastUtils {
    private static Context context;
    private static Context getContext() {
        if(context == null) {
            context = MainActivity.context;
        }
        return context;
    }
    public static void showShortToast(String msg) {

        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    public static void showToast(String msg) {
        showShortToast(msg); //默认showShortToast
    }
}
