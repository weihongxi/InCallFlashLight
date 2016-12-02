package com.wind.weihongxi.incallflashlight;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

/**
 * Created by weihongxi on 2016/12/2 002.
 */

public class FlashLight {

    private static final String TAG = "whx.FlashLight";

    public static Camera mCamera;
    private static CameraManager mCameraManager;
    private static String mCameraId;
   static boolean  versionM;

    public static void openFlashLight(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            versionM = false;
        } else {
            versionM = true;
        }
        if (versionM) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

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

    }
}
