package com.wind.weihongxi.incallflashlight;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by weihongxi on 2016/11/29 029.
 */

public class InCallService extends Service {
    private static final String TAG = "whx.InCallService";
    public Camera mCamera;
    boolean isFlashLightOpen = false;
    int delayMillis = 100;

    private final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    private final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    //    private final String ACTION_WHX = "com.wind.weihongxi.inCallBroadcast";
    private final String ACTION_DESTROY = "com.wind.whx.action.SERVICE_DESTROY";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PHONE_STATE);
        filter.addAction(ACTION_NEW_OUTGOING_CALL);
//        filter.addAction(ACTION_WHX);
        filter.addAction(ACTION_DESTROY);
        registerReceiver(mInCallBroadcast, filter);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent destroy = new Intent(ACTION_DESTROY);
        sendBroadcast(destroy);
//        unregisterReceiver(mInCallBroadcast);
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    sleep(1000);
                    unregisterReceiver(mInCallBroadcast);
                    stopSelf();
                } catch (Exception e) {
                    Log.d(TAG, "onDestroy Exception.");
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }.start();
        mHandler.removeCallbacks(open);
        mCamera = null;
        Log.d(TAG, "onDestroy");
    }

    private BroadcastReceiver mInCallBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (action) {
                case ACTION_NEW_OUTGOING_CALL:
                    openFlashLight();
                    String outCallNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.d(TAG, "onReceive: OUT call number:" + outCallNumber);
                    break;
                case ACTION_DESTROY:
                    tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                    Log.d(TAG, "onReceive: onDestroy,action:" + action);
                    break;
                case ACTION_PHONE_STATE:
                    tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                    Log.d(TAG, "onReceive: else, action:" + action);
                    break;
                default:
                    Log.d(TAG, "onReceive: default,error");
                    break;
            }
        }

        PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    //空闲
                    case TelephonyManager.CALL_STATE_IDLE:
//                        enableFlashLight();
                        mHandler.removeCallbacks(open);
                        Log.d(TAG, "PhoneStateListener.IDLE");
                        break;
                    //摘机
                    case TelephonyManager.CALL_STATE_OFFHOOK:
//                        enableFlashLight();
                        mHandler.removeCallbacks(open);
                        Log.d(TAG, "PhoneStateListener.OFFHOOK");
                        break;
                    //来电
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(TAG, "PhoneStateListener.RINGING, incomingNumber:" + incomingNumber);
//                        enableFlashLight();
                        mHandler.post(open);
                        break;
                }
            }

        };
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    Runnable open = new Runnable() {
        @Override
        public void run() {
            openFlashLight();
            mHandler.postDelayed(open, delayMillis);
        }

    };

    public void openFlashLight(/*boolean enable*/) {
        if (mCamera == null) {
            mCamera = Camera.open();
            mCamera.startPreview();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
            Log.d(TAG, "enableFlashLight()");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isFlashLightOpen = false;
        }
    }

}
