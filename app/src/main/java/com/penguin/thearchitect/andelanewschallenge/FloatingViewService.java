package com.penguin.thearchitect.andelanewschallenge;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


import java.util.List;
import java.util.Timer;


import javax.xml.transform.Result;

public class FloatingViewService extends Service implements View.OnClickListener {


    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private static String packageName = "com.android.chrome";
    private static Timer timer = new Timer();
    private Context ctx;
    NotificationCompat.Builder notification;
    PendingIntent pIntent;
    NotificationManager manager;
    TaskStackBuilder stackBuilder;
    Intent resultIntent;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ctx = this;
        return super.onStartCommand(intent, flags, startId);
    }

    protected void startNotification() {
        // TODO Auto-generated method stub
        //Creating Notification Builder
        notification = new NotificationCompat.Builder(FloatingViewService.this);
        //Title for Notification
        notification.setContentTitle("Hunter");
        //Message in the Notification
        notification.setContentText("The request has been received.");
        //Alert shown when Notification is received
        notification.setTicker("Oh Hunter!");
        //Icon to be set on Notification
        notification.setSmallIcon(android.R.drawable.ic_dialog_info);
        //Creating new Stack Builder
        stackBuilder = TaskStackBuilder.create(FloatingViewService.this);
        stackBuilder.addParentStack(FloatingViewService.class);
        //Intent which is opened when notification is clicked
        resultIntent = new Intent(FloatingViewService.this, Result.class);
        stackBuilder.addNextIntent(resultIntent);
        pIntent =  stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pIntent);
        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }

    private void initMenu()
    {

        if (!isAppIsInBackground(FloatingViewService.this))
        {

        }else {

            mFloatingView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);


            //getting windows services and adding the floating view to it
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mFloatingView, params);


            //getting the collapsed and expanded view from the floating view
            collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
            expandedView = mFloatingView.findViewById(R.id.layoutExpanded);

            //adding click listener to close button and expanded view
            mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(this);
            expandedView.setOnClickListener(this);

            //adding an touchlistener to make drag movement of the floating widget
            mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_UP:
                            //when the drag is ended switching the state of the widget
                            collapsedView.setVisibility(View.GONE);
                            expandedView.setVisibility(View.VISIBLE);
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            //this code is helping the widget to move around the screen with fingers
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mFloatingView, params);
                            return true;
                    }
                    return false;
                }
            });

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initMenu();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable,15000);


    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(packageName)) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(packageName)) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutExpanded:
                //switching views
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;

            case R.id.buttonClose:
                //closing the widget
                stopSelf();
                startNotification();
                break;
        }
    }
}