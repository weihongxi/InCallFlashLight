package com.wind.weihongxi.incallflashlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by weihongxi on 2016/11/30 030.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    private final static String TAG = "whx.BootCompleted";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(context, InCallService.class);
        context.startService(it);
        Log.d(TAG, "onReceive: action:" + intent.getAction());
    }
}
