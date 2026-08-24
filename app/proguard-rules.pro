# JavaScript interface methods must remain callable from the WebView.
-keepclassmembers class com.arad.settingschat.MainActivity$AndroidBridge {
    public *;
}
