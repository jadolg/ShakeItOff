package com.akiel.shakeitoff;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

public class Shaker implements SensorListener {
    private static final int BASE_FORCE_THRESHOLD = 350;
    private static final int TIME_THRESHOLD = 200;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 1000;
    private static final int SHAKE_COUNT = 3;

    private SensorManager mSensorMgr;
    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private final Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;
    private final SharedPreferences preferences;

    public Shaker(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences("shakeitoff", Context.MODE_PRIVATE);
        resume();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    public void resume() {
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            mSensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void onAccuracyChanged(int sensor, int accuracy) {
    }

    public void pause() {
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            mSensorMgr = null;
        }
    }

    public void onSensorChanged(int sensor, float[] values) {
        if (sensor != SensorManager.SENSOR_ACCELEROMETER) return;
        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD) {
            long diff = now - mLastTime;
            float speed = Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000;

            int sensitivity = preferences.getInt("sensitivity", 2);

            int FORCE_THRESHOLD = BASE_FORCE_THRESHOLD;
            switch (sensitivity) {
                case 0:
                    FORCE_THRESHOLD = BASE_FORCE_THRESHOLD * 4;
                    break;
                case 1:
                    FORCE_THRESHOLD = Math.round(BASE_FORCE_THRESHOLD * 2);
                    break;
            }

            if (speed > FORCE_THRESHOLD) {
                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener != null) {
                        mShakeListener.onShake();
                    }
                }
                mLastForce = now;
            }
            mLastTime = now;
            mLastX = values[SensorManager.DATA_X];
            mLastY = values[SensorManager.DATA_Y];
            mLastZ = values[SensorManager.DATA_Z];
        }
    }

    public interface OnShakeListener {
        public void onShake();
    }

}