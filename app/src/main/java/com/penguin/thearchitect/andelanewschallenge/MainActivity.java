package com.penguin.thearchitect.andelanewschallenge;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askForSystemOverlayPermission();
        }
        startService();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    errorToast();
                    finish();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void errorToast() {
        Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
    }


    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }


    void createService()
    {
        Intent mIntent = new Intent(MainActivity.this,WebService.class);
        startService(mIntent);
    }

    public void startService()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startService(new Intent(MainActivity.this, FloatingViewService.class));
            finish();
        } else if (Settings.canDrawOverlays(this)) {
            startService(new Intent(MainActivity.this, FloatingViewService.class));
            finish();
        } else {
            askForSystemOverlayPermission();
            Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }
    }
}
