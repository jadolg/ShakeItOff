package com.akiel.shakeitoff;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class Autostart extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg1) {
        SharedPreferences preferences = context.getSharedPreferences("shakeitoff", MODE_PRIVATE);
        if (preferences.getBoolean("enabled", false)) {
            Intent intent = new Intent(context, ShakerService.class);
            context.startForegroundService(intent);
            Log.i("Autostart", "started");
        } else
            Log.i("Autostart", "not needed");
    }
}