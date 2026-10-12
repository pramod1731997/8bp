package com.poolpilot.overlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PlannerActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> pendingFile;
    private static final int FILE_REQUEST=48;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web=new WebView(this);
        web.setBackgroundColor(Color.rgb(8,23,31));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> callback,FileChooserParams params){
                if(pendingFile!=null)pendingFile.onReceiveValue(null);
                pendingFile=callback;
                try {startActivityForResult(params.createIntent(),FILE_REQUEST);return true;}
                catch(Exception e){pendingFile=null;callback.onReceiveValue(null);return false;}
            }
        });
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==FILE_REQUEST&&pendingFile!=null) {
            pendingFile.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));pendingFile=null;
        }
    }
    @Override protected void onDestroy() {
        if(pendingFile!=null){pendingFile.onReceiveValue(null);pendingFile=null;}
        if(web!=null){web.destroy();web=null;}
        super.onDestroy();
    }
}
