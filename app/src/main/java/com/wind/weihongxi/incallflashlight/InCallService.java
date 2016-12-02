package com.wind.weihongxi.incallflashlight;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by weihongxi on 2016/11/29 029.
 */

public class InCallService extends AccessibilityService {
    private static final String TAG = "whx.InCallService";
    public Camera mCamera;
    int delayMillis = 100;
    private CameraManager mCameraManager;
    private String mCameraId;
    boolean versionM;
    Context mContext;

    private final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    private final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    //    private final String ACTION_WHX = "com.wind.weihongxi.inCallBroadcast";
    private final String ACTION_DESTROY = "com.wind.whx.action.SERVICE_DESTROY";

    //for test
    private final String ACTION_SHINING_FLASH_LIGHT = "com.wind.weihongxi.SHINING_FLASH_LIGHT";
    private final String ACTION_STOP_SHINING_FLASH_LIGHT = "com.wind.weihongxi.ACTION_STOP_SHINING_FLASH_LIGHT";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
/*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            versionM = false;
        } else {
            versionM = true;
        }
        if (versionM) {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
*/
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PHONE_STATE);
        filter.addAction(ACTION_NEW_OUTGOING_CALL);
//        filter.addAction(ACTION_WHX);
        filter.addAction(ACTION_DESTROY);
        filter.addAction(ACTION_SHINING_FLASH_LIGHT);
        filter.addAction(ACTION_STOP_SHINING_FLASH_LIGHT);
        registerReceiver(mInCallBroadcast, filter);
        Log.d(TAG, "onCreate");

    }

    /*
           @Override
            public IBinder onBind(Intent intent) {
                Log.d(TAG, "onBind");
                return null;
            }
        */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent destroy = new Intent(ACTION_DESTROY);
        sendBroadcast(destroy);
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
            Log.d(TAG, "mInCallBroadcast.onReceive: action:" + action);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (action) {
                case ACTION_NEW_OUTGOING_CALL:
//                    openFlashLight();
                    String outCallNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.d(TAG, "OUT call number:" + outCallNumber);
                    break;
                case ACTION_PHONE_STATE:
                    tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                    break;
                case ACTION_DESTROY:
                    tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                    break;
                //for test flashlight begin
                case ACTION_SHINING_FLASH_LIGHT:
                    mHandler.post(open);
                    break;
                case ACTION_STOP_SHINING_FLASH_LIGHT:
                    mHandler.removeCallbacks(open);
                    break;
                //for test end
                default:
                    Log.e(TAG, "onReceive: default,error");
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
//            openFlashLight();
            FlashLight.openFlashLight(mContext);
            mHandler.postDelayed(open, delayMillis);
        }

    };

 /*   public void openFlashLight() {
        try {
            if (versionM) {
                mCameraManager.setTorchMode(mCameraId, true);
                mCameraManager.setTorchMode(mCameraId, false);
                Log.d(TAG, "openFlashLight.>=M");
            } else {
                if (mCamera == null) {
                    mCamera = Camera.open();
                    mCamera.startPreview();
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                    Log.d(TAG, "enableFlashLight().<M");
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;

                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, "openFlashLight:EXCEPTION;" + Log.getStackTraceString(e));
        }


    }*/

}

