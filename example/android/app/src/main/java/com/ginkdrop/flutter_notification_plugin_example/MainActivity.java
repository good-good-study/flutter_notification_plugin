package com.ginkdrop.flutter_notification_plugin_example;

import android.os.Bundle;

import java.util.LinkedHashMap;
import java.util.Map;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity implements MethodChannel.MethodCallHandler {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);
        final MethodChannel channel = new MethodChannel(getFlutterView(), "android");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {

        if (call.method.equals("getResource")) {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("iconSmallResId", R.mipmap.ic_launcher_round);
            map.put("iconLargeResId", R.mipmap.ic_launcher_round);
            map.put("soundResId", R.raw.notify_message);
            result.success(map);
        } else {
            result.notImplemented();
        }
    }
}