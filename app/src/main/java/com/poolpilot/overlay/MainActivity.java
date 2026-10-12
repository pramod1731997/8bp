package com.poolpilot.overlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private int dp(float px) { return (int)(getResources().getDisplayMetrics().density*px+0.5f); }
    private GradientDrawable bg(int color) { GradientDrawable b=new GradientDrawable(); b.setColor(color); b.setCornerRadius(dp(12)); return b; }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(25),dp(34),dp(25),dp(25));
        root.setBackgroundColor(Color.rgb(8,23,31));

        TextView title=new TextView(this);
        title.setText("PoolPilot Shot Lab"); title.setTextSize(27);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setTextColor(Color.rgb(112,243,228)); root.addView(title);

        TextView desc=new TextView(this);
        desc.setText("Android-only, manually calibrated trajectory simulator. It does not inspect or control the game automatically.\n\nGrant Display over other apps, then start the overlay and switch to your game. Tap the floating ✎ bubble to edit; tap ✓ to lock and touch the game normally.\n\nUse the overlay only where permitted. Accuracy is approximate.");
        desc.setTextColor(Color.rgb(193,218,222)); desc.setTextSize(14);
        desc.setPadding(0,dp(13),0,dp(18)); root.addView(desc);

        status=new TextView(this); status.setTextSize(13);
        status.setTextColor(Color.rgb(255,209,133)); root.addView(status);

        button(root,"1 · Allow overlay permission", ()->{
            Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()));
            startActivity(i);
        });
        button(root,"2 · Start floating assistant", ()->{
            if(!Settings.canDrawOverlays(this)){askPermission();return;}
            Intent i=new Intent(this,OverlayService.class); i.setAction("START");
            if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
            status.setText("Overlay enabled. Open the game and tap the floating bubble to edit.");
        });
        button(root,"Stop assistant", ()->{
            stopService(new Intent(this,OverlayService.class));status.setText("Stopped.");
        });
        button(root,"Open full screen shot planner", ()->{
            startActivity(new Intent(this,PlannerActivity.class));
        });
        TextView note=new TextView(this);
        note.setText("No root · No VPN · No network permission · No automatic screenshots");
        note.setGravity(Gravity.CENTER); note.setTextColor(Color.rgb(139,173,180));
        note.setPadding(0,dp(28),0,0); root.addView(note);
        setContentView(root); refresh();
    }
    private void button(LinearLayout root,String title,Runnable click) {
        Button b=new Button(this); b.setText(title); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        b.setBackground(bg(Color.rgb(28,77,83)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));
        lp.topMargin=dp(12); root.addView(b,lp); b.setOnClickListener(v->click.run());
    }
    private void askPermission(){status.setText("Grant 'Display over other apps' permission first.");}
    private void refresh(){status.setText(Settings.canDrawOverlays(this)?"Overlay permission granted":"Overlay permission required");}
    @Override protected void onResume(){super.onResume();if(status!=null)refresh();}
}
