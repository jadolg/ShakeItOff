package com.akiel.shakeitoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;


import static android.content.Context.MODE_PRIVATE;

public class Autostart extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg1) {
        SharedPreferences preferences = context.getSharedPreferences("shakeitoff", MODE_PRIVATE);
        if (preferences.getBoolean("enabled", false)) {
            Intent intent = new Intent(context, ShakerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
            Log.i("Autostart", "started");
        } else
        Log.i("Autostart", "not needed");
    }
}