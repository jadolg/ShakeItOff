package com.akiel.shakeitoff;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final int ADMIN_INTENT = 15;
    private static final String description = "Administration Permission needed to lock your screen";
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private Switch aswitch;
    private SeekBar seekbar;
    private SharedPreferences preferences;

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
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        aswitch = (Switch) findViewById(R.id.switch2);
        aswitch.setChecked(isMyServiceRunning(getApplicationContext()));

        preferences = getSharedPreferences("shakeitoff", MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = preferences.edit();

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setProgress(preferences.getInt("sensitivity", 2));
        seekbar.setEnabled(!isMyServiceRunning(getApplicationContext()));
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preferencesEditor.putInt("sensitivity", progress);
                preferencesEditor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, MyAdminReceiver.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        aswitch.setChecked(isMyServiceRunning(getApplicationContext()));
        seekbar.setEnabled(!isMyServiceRunning(getApplicationContext()));
    }


    public void onClick(View view) {
        Log.i("main", "iniciando");
        boolean isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        preferences = getSharedPreferences("shakeitoff", MODE_PRIVATE);
        final SharedPreferences.Editor preferencesEditor = preferences.edit();

        if (isAdmin) {
            if (isMyServiceRunning(getApplicationContext())) {
                stopService(new Intent(this, ShakerService.class));
            } else {
                startService(new Intent(this, ShakerService.class));
            }
            boolean serviceRunning = isMyServiceRunning(getApplicationContext());
            preferencesEditor.putBoolean("enabled", serviceRunning);
            preferencesEditor.apply();
            Log.i("main", "saved state "+serviceRunning);
            aswitch.setChecked(serviceRunning);
            seekbar.setEnabled(!serviceRunning);
        } else {
            showInstallAdminAlert();
        }
    }

    private void showInstallAdminAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_admin_text));
        builder.setCancelable(true)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
                                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, description);
                                startActivityForResult(intent, ADMIN_INTENT);
                            }
                        }).setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //hacer algo en el cancelar?
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle(getString(R.string.confirm_admin));
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADMIN_INTENT) {
            if (resultCode == RESULT_OK) {
                if (isMyServiceRunning(getApplicationContext())) {
                    stopService(new Intent(this, ShakerService.class));
                } else {
                    startService(new Intent(this, ShakerService.class));
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.noadmin), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
