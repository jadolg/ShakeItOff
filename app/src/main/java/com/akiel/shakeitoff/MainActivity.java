package com.akiel.shakeitoff;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

public class MainActivity extends AppCompatActivity {

    private static final int ADMIN_INTENT = 15;
    private static final String description = "Admin needed to lock your screen";
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private FloatingActionButton fab;

    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (ShakerService.class.getName().equals(
                    service.service.getClassName())) {
                Log.i("main", "Service running");
                return true;
            }
        }
        Log.i("main", "Service not running");
        return false;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton2);

        setBtnStatus(isMyServiceRunning(getApplicationContext()));

        mDevicePolicyManager = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, MyAdminReceiver.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBtnStatus(isMyServiceRunning(getApplicationContext()));
    }

    public void setBtnStatus(Boolean status){
        IconDrawable icon = null;
        if (status) {
            icon = new IconDrawable(this, FontAwesomeIcons.fa_toggle_on);
        } else {
            icon = new IconDrawable(this, FontAwesomeIcons.fa_toggle_off);
        }
        fab.setImageDrawable(icon.color(Color.WHITE));
    }

    public void onClick(View view){
        Log.i("main", "iniciando");
        boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        if (isAdmin) {
            if (isMyServiceRunning(getApplicationContext())){
                stopService(new Intent(this, ShakerService.class));
            } else {
                startService(new Intent(this, ShakerService.class));
            }
            setBtnStatus(isMyServiceRunning(getApplicationContext()));
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.noadmin), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,description);
            startActivityForResult(intent, ADMIN_INTENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADMIN_INTENT) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText(getApplicationContext(), "Registered As Admin", Toast.LENGTH_SHORT).show();
                if (isMyServiceRunning(getApplicationContext())){
                    stopService(new Intent(this, ShakerService.class));
                } else {
                    startService(new Intent(this, ShakerService.class));
                }
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.noadmin), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
