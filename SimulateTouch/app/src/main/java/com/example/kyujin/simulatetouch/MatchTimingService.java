package com.example.kyujin.simulatetouch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.zeromq.ZMQ;

/**
 * Created by kyujin on 29/09/2016.
 */

public class MatchTimingService extends Service implements View.OnTouchListener {
    WindowManager wm;
    ImageView imageView;

    View topView;

    String ip;
    int width;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        ip = intent.getStringExtra("ip_addr");
        width = intent.getIntExtra("width", 0);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.line);
        imageView.setOnTouchListener(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;

        params.x = 0;
        params.y = width / 3;
        wm.addView(imageView, params);

//        topView = new View(this);
//        WindowManager.LayoutParams topParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
//        topParams.gravity = Gravity.TOP;
//        topParams.x = 0;
//        topParams.y = 0;
//        topParams.width = 0;
//        topParams.height = 0;
//        wm.addView(topView, topParams);
    }

    @Override
    public void onDestroy() {
        if(imageView != null)
            wm.removeView(imageView);
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            new SendMessageTask("START%" + System.currentTimeMillis() + "%" + width, ip).execute();
            Log.d("MatchTimingService", "Touched");
        }
        return false;
    }

    protected class SendMessageTask extends AsyncTask<Void, Void, String> {
        String msg;
        String ip;
        SendMessageTask(String msg, String ip) {
            this.msg = msg;
            this.ip = ip;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Log.i("MatchTiming", "Got Msg... " + msg);
                ZMQ.Context context = ZMQ.context(1);
                ZMQ.Socket socket = context.socket(ZMQ.REQ);
                socket.connect("tcp://" + ip + ":5599");
                socket.send(msg, 0);
                String result = new String(socket.recv(0));
                if(result.equals(""))
                    result = "FAIL";
                Log.i("MatchTiming", "Result : " + result);
                socket.close();

                context.term();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("MatchTiming", s);
        }
    }
}
