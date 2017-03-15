package com.akiel.shakeitoff;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by akiel on 3/15/17.
 */

public class ShakerService extends Service  {


    private Shaker mShaker;
    private DevicePolicyManager mDevicePolicyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
        mDevicePolicyManager = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("servicio", "iniciando");
        mShaker = new Shaker(this);
        mShaker.setOnShakeListener(new Shaker.OnShakeListener() {

            @Override
            public void onShake() {
                Toast.makeText(getApplicationContext(),"here",Toast.LENGTH_SHORT);
                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
                mDevicePolicyManager.lockNow();
            }
        });

//        return super.onStartCommand(intent, flags, startId);
        notifyit();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mShaker.pause();
        super.onDestroy();
    }

    public void notifyit() {
        /*
         * Este método asegura que el servicio permanece en el área de notificación
		 * */
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.shakeit).setTicker("shake").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.intro)).build();
        startForeground(1337, notification);
    }

}
