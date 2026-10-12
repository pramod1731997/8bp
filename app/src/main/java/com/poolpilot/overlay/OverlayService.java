package com.poolpilot.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class OverlayService extends Service {
    private static final String CHANNEL="poolpilot_overlay";
    private WindowManager wm;
    private WindowManager.LayoutParams sheetParams,bubbleParams;
    private WebView web;
    private TextView bubble;
    private boolean locked=true;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null && "STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
        foreground(); if(web==null)show();return START_STICKY;
    }
    private void foreground(){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26)
            nm.createNotificationChannel(new NotificationChannel(CHANNEL,"PoolPilot overlay",NotificationManager.IMPORTANCE_LOW));
        Intent stop=new Intent(this,OverlayService.class);stop.setAction("STOP");
        PendingIntent pi=PendingIntent.getService(this,2,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent launch=new Intent(this,MainActivity.class);
        PendingIntent content=PendingIntent.getActivity(this,1,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(this,CHANNEL)
            .setContentTitle("PoolPilot overlay is running")
            .setContentText("Tap floating bubble to edit or lock shot guides")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(content)
            .addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"Stop",pi).build())
            .setOngoing(true).build();
        if(Build.VERSION.SDK_INT>=34)
            startForeground(88,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(88,n);
    }
    private void show(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        web=new WebView(this);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView v,String url){setJSLocked();}
        });
        sheetParams=new WindowManager.LayoutParams(-1,-1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        sheetParams.gravity=Gravity.TOP|Gravity.START;
        sheetParams.alpha=.65f;
        wm.addView(web,sheetParams);
        web.loadUrl("file:///android_asset/index.html?overlay=1");

        bubble=new TextView(this);
        bubble.setText("✎");bubble.setTextColor(Color.rgb(4,32,37));
        bubble.setTextSize(25);bubble.setGravity(Gravity.CENTER);
        GradientDrawable b=new GradientDrawable();
        b.setColor(Color.rgb(92,239,214));
        b.setCornerRadius(dp(40));b.setStroke(dp(2),Color.rgb(10,72,72));
        bubble.setBackground(b);bubble.setElevation(dp(7));
        bubbleParams=new WindowManager.LayoutParams(dp(52),dp(52),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        bubbleParams.gravity=Gravity.TOP|Gravity.START;
        bubbleParams.x=dp(16);bubbleParams.y=dp(95);
        wm.addView(bubble,bubbleParams);
        bubble.setOnTouchListener(new View.OnTouchListener(){
            float sx,sy;int ox,oy;boolean moved;
            @Override public boolean onTouch(View view,MotionEvent event) {
                switch(event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        sx=event.getRawX();sy=event.getRawY();
                        ox=bubbleParams.x;oy=bubbleParams.y;moved=false;return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx=event.getRawX()-sx,dy=event.getRawY()-sy;
                        if(Math.hypot(dx,dy)>dp(8))moved=true;
                        if(moved){
                            bubbleParams.x=ox+Math.round(dx);
                            bubbleParams.y=oy+Math.round(dy);
                            wm.updateViewLayout(bubble,bubbleParams);
                        }return true;
                    case MotionEvent.ACTION_UP:if(!moved)toggle();return true;
                    case MotionEvent.ACTION_CANCEL:return true;
                }return false;
            }
        });
    }
    private void toggle(){
        locked=!locked;
        if(locked){
            sheetParams.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            sheetParams.alpha=.65f;bubble.setText("✎");
        }else{
            sheetParams.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            sheetParams.alpha=1f;bubble.setText("✓");
        }
        try{wm.updateViewLayout(web,sheetParams);}catch(Exception ignored){}
        setJSLocked();
    }
    private void setJSLocked(){
        if(web!=null)web.evaluateJavascript("window.poolPilot && window.poolPilot.setLocked("+locked+");",null);
    }
    @Override public void onDestroy(){
        if(wm!=null){
            if(bubble!=null){try{wm.removeView(bubble);}catch(Exception ignored){}}
            if(web!=null){try{wm.removeView(web);}catch(Exception ignored){}web.destroy();}
        }
        web=null;bubble=null;super.onDestroy();
    }
}
