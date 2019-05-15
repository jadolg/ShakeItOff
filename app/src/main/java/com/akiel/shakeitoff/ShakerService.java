package com.akiel.shakeitoff;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
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
                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                if (pm.isScreenOn()) {
                    Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(100);
                    mDevicePolicyManager.lockNow();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyit();
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);

        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyit() {
        /*
         * Este método asegura que el servicio permanece en el área de notificación
         * */
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        String notificationChannel = createNotificationChannel("shakeitoff", "shakeitoff");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannel);

        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(R.drawable.shakeit).setTicker("shake").setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.intro)).build();
        startForeground(1337, notification);
    }

}
