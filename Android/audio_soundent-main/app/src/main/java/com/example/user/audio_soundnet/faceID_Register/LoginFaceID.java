package com.example.user.audio_soundnet.faceID_Register;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.user.audio_soundnet.R;

public class LoginFaceID extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;

    private WebView appWebView;
    //private String recognitionapp ="https://pwa-feature-extraction.netlify.app/ ";
    private String recognitionapp ="https://webview-feature-extraction.netlify.app/ ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_face_i_d);
        checkForAndAskForPermission();

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    createWebView();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void createWebView() {

        appWebView = (WebView) findViewById(R.id.SignUp);
        setUpWebView(appWebView);
        appWebView.loadUrl(recognitionapp);
        appWebView.setWebChromeClient(new WebChromeClient() {

            public boolean onConsoleMessage(ConsoleMessage m) {
                Log.d("getUserMedia, WebView", m.message() + " -- From line "
                        + m.lineNumber() + " of "
                        + m.sourceId());

                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {

                // getActivity().
                LoginFaceID.this.runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        // Below isn't necessary, however you might want to:
                        // 1) Check what the site is and perhaps have a blacklist
                        // 2) Have a pop up for the user to explicitly give permission
                        if(request.getOrigin().toString().equals("https://webview-face-recognition.netlify.app/")
                                ||request.getOrigin().toString().equals("https://webview-feature-extraction.netlify.app/" ) ) {
                            request.grant(request.getResources());//單純確認網址
                        } else {
                            request.deny();
                        }
                    }
                });
            }
        });
    }

    private void checkForAndAskForPermission(){
        //check permission

        if(ContextCompat.checkSelfPermission(LoginFaceID.this, Manifest.permission.CAMERA)
                !=PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    LoginFaceID.this,Manifest.permission.CAMERA)){



            }else ActivityCompat.requestPermissions(
                    LoginFaceID.this,new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST);

        }else {
            createWebView();
        }

    }

    private void setUpWebView (WebView webView){

        WebSettings settings =webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSaveFormData(true);
        settings.setEnableSmoothTransition(true);
        settings.setSupportMultipleWindows(true);

        webView.clearCache(true);
        webView.clearHistory();
        webView.setWebViewClient(new WebViewClient());

    }
}