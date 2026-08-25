package com.arad.settingschat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int MEDIA_PERMISSION_REQUEST = 41;
    private static final int FILE_PICKER_REQUEST = 42;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 43;
    private static final String NOTIFICATION_CHANNEL = "nuvia_messages";
    private static final String SILENT_NOTIFICATION_CHANNEL = "nuvia_silent_messages";
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private int notificationId = 100;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF070B19);
        getWindow().setNavigationBarColor(0xFF070B19);
        webView = new WebView(this);
        setContentView(webView);
        createNotificationChannel();
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
        settings.setUserAgentString(settings.getUserAgentString() + " Nuvia/2.9");
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
            Toast.makeText(this, "Could not open Nuvia", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasMediaPermissions() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMediaPermissions() {
        ArrayList<String> missing = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.CAMERA);
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!missing.isEmpty()) requestPermissions(missing.toArray(new String[0]), MEDIA_PERMISSION_REQUEST);
    }

    private void createNotificationChannel() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL,
                "Messages and calls",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("New Nuvia messages, stories, and incoming calls");
        channel.enableVibration(true);
        manager.createNotificationChannel(channel);

        NotificationChannel silentChannel = new NotificationChannel(
                SILENT_NOTIFICATION_CHANNEL,
                "Silent messages",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        silentChannel.setDescription("Nuvia messages intentionally sent without sound");
        silentChannel.setSound(null, null);
        silentChannel.enableVibration(false);
        manager.createNotificationChannel(silentChannel);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void showNotification(String title, String body, String tag, boolean silent) {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        Intent openIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        boolean call = tag != null && tag.contains("call");
        String channelId = silent ? SILENT_NOTIFICATION_CHANNEL : NOTIFICATION_CHANNEL;
        Notification.Builder builder = new Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(call ? Notification.CATEGORY_CALL : Notification.CATEGORY_MESSAGE)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setGroup(call ? "nuvia_calls" : "nuvia_conversations");

        if (call) {
            builder.setStyle(new Notification.BigTextStyle().bigText(body));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Notification.Person sender = new Notification.Person.Builder().setName(title).build();
            builder.setStyle(new Notification.MessagingStyle(sender)
                    .setConversationTitle(title)
                    .addMessage(body, System.currentTimeMillis(), sender));
        } else {
            builder.setStyle(new Notification.MessagingStyle("Nuvia")
                    .setConversationTitle(title)
                    .addMessage(body, System.currentTimeMillis(), title));
        }

        if (silent) builder.setSilent(true);
        else builder.setDefaults(Notification.DEFAULT_ALL);
        Notification notification = builder.build();
        NotificationManager manager = getSystemService(NotificationManager.class);
        int id = tag == null ? notificationId++ : 1000 + Math.abs(tag.hashCode() % 100000);
        if (manager != null) manager.notify(tag == null ? "nuvia" : tag, id, notification);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == MEDIA_PERMISSION_REQUEST || requestCode == NOTIFICATION_PERMISSION_REQUEST)
                && webView != null) {
            webView.evaluateJavascript("window.SettingsConnect && window.SettingsConnect.refresh()", null);
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
            return "2.9.0";
        }

        @JavascriptInterface
        public void requestNotifications() {
            runOnUiThread(MainActivity.this::requestNotificationPermission);
        }

        @JavascriptInterface
        public boolean notificationsAllowed() {
            return Build.VERSION.SDK_INT < 33
                    || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        @JavascriptInterface
        public void notify(String title, String body, String tag, boolean silent) {
            runOnUiThread(() -> showNotification(title, body, tag, silent));
        }

        @JavascriptInterface
        public void haptic(int milliseconds) {
            runOnUiThread(() -> {
                Vibrator vibrator = getSystemService(Vibrator.class);
                if (vibrator == null || !vibrator.hasVibrator()) return;
                int duration = Math.max(1, Math.min(milliseconds, 200));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(duration);
                }
            });
        }
    }
}
