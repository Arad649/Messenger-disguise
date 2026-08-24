package com.arad.settingschat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int MEDIA_PERMISSION_REQUEST = 41;
    private static final int FILE_PICKER_REQUEST = 42;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF070B19);
        getWindow().setNavigationBarColor(0xFF070B19);
        webView = new WebView(this);
        setContentView(webView);
        configureWebView();
        requestMediaPermissions();
        loadApplication();
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " SettingsConnect/2.0");
        webView.setBackgroundColor(0xFF070B19);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidApp");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (hasMediaPermissions()) request.grant(request.getResources());
                    else {
                        request.deny();
                        requestMediaPermissions();
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_PICKER_REQUEST);
                } catch (Exception error) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "No file picker is available", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void loadApplication() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("index.html"), StandardCharsets.UTF_8))) {
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) html.append(line).append('\n');
            webView.loadDataWithBaseURL("https://settings-connect.local/", html.toString(), "text/html", "UTF-8", null);
        } catch (Exception error) {
            Toast.makeText(this, "Could not open Settings Connect", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasMediaPermissions() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMediaPermissions() {
        if (!hasMediaPermissions()) requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MEDIA_PERMISSION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript("window.SettingsConnect && window.SettingsConnect.back()", value -> {
            if ("false".equals(value) || "null".equals(value)) super.onBackPressed();
        });
    }

    @Override
    protected void onDestroy() {
        webView.evaluateJavascript("window.SettingsConnect && window.SettingsConnect.shutdown()", null);
        webView.destroy();
        super.onDestroy();
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void toast(String message) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public String version() {
            return "1.0.0";
        }
    }
}
