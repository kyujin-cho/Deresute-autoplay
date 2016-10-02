package com.example.kyujin.simulatetouch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.zeromq.ZMQ;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText ipText;
    Intent intent;

    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.stopButton).setOnClickListener(this);
        ipText = (EditText) findViewById(R.id.ipText);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                startOverlayWindowService(this);
                break;
            case R.id.stopButton:
                if(intent != null)
                    stopService(intent);
                break;
        }
    }

    void startTouchService() {
        Log.i("MainActivity", "Permission granted... Starting service");
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = (displayMetrics.widthPixels < displayMetrics.heightPixels) ? displayMetrics.widthPixels : displayMetrics.heightPixels;
        intent = new Intent(MainActivity.this, MatchTimingService.class);
        intent.putExtra("ip_addr", ipText.getText().toString());
        intent.putExtra("width", width);
        Log.i("MainActivity", "Width: " + width);
        startService(intent);
    }

    public void startOverlayWindowService(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            /** request permission via start activity for result */
        }
        startTouchService();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        Log.i("MainActivity", "ActivityResult");
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
//             if so check once again if we have permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
                startTouchService();
            } else
                Log.i("MainActivity", "Permission not granted!");
        }  else
            Log.i("MainActivity", "Not a valid call");
    }

}

