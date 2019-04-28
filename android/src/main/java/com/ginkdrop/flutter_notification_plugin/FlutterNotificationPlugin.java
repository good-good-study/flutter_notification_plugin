package com.ginkdrop.flutter_notification_plugin;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterNotificationPlugin
 */
public class FlutterNotificationPlugin extends FlutterChannel implements MethodCallHandler {

    private static final String DEFAULT_CHANNEL = "DEFAULT_CHANNEL";
    private static final String DEFAULT_CHANNEL_NAME = "DEFAULT_CHANNEL_NAME";
    private static Registrar registrar;
    private static EventChannel.EventSink eventSink;
    private static final String ACTION_NOTIFY_INTENT = "ACTION_NOTIFY_INTENT";
    private static final String ACTION_NOTIFY_ID = "ACTION_NOTIFY_ID";
    private static final String TAG = "FlutterNotification";

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        if (registrar.activity() == null) {
            // When a background flutter view tries to register the plugin, the registrar has no activity.
            // We stop the registration process as this plugin is foreground only.
            return;
        }
        FlutterNotificationPlugin.registrar = registrar;
        registerChannel(registrar.view(), "flutter_notification_plugin_event", "flutter_notification_plugin");
        registrar.addNewIntentListener(new PluginRegistry.NewIntentListener() {
            @Override
            public boolean onNewIntent(Intent intent) {
                Log.e(TAG, "onNewIntent , " + intent.toString());
                String stringExtra = intent.getStringExtra(ACTION_NOTIFY_INTENT);
                int intExtra = intent.getIntExtra(ACTION_NOTIFY_ID, 0);
                if (ACTION_NOTIFY_INTENT.equals(stringExtra)) {
                    Log.e(TAG, String.format("stringExtra -> %s ; intExtra - > %s", stringExtra, intExtra));
                    if (eventSink != null) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("notifyId", intExtra);
                        map.put("message", stringExtra);
                        eventSink.success(map);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        super.onListen(o, eventSink);
        FlutterNotificationPlugin.eventSink = eventSink;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        super.onMethodCall(call, result);
        if (call.method.equals("areNotificationsEnabled")) {
            boolean enabled = NotificationManagerCompat.from(registrar.context()).areNotificationsEnabled();
            result.success(enabled);
            Log.e(TAG, "enabled -> " + enabled);
        } else if (call.method.equals("notify")) {
            if (call.arguments instanceof Map) {
                Map map = (Map) call.arguments;
                String channel = (String) map.get("channel");
                String channelName = (String) map.get("channelName");
                int iconSmallResId = (int) map.get("iconSmallResId");
                if ((TextUtils.isEmpty(channel))) return;
                if ((TextUtils.isEmpty(channelName))) return;
                if (iconSmallResId == 0) return;

                int iconLargeResId = (int) map.get("iconLargeResId");
                int soundResId = (int) map.get("soundResId");
                int notifyId = (int) map.get("notifyId");
                String title = (String) map.get("title");
                String message = (String) map.get("message");

                Log.e(TAG, String.format("channel -> %s ; channelName -> %s ; iconResId -> %s ; soundResId -> %s", channel, channelName, iconSmallResId, soundResId));
                Bitmap largeIcon = iconLargeResId == 0 ? null : BitmapFactory.decodeResource(registrar.context().getResources(), iconLargeResId);
                Intent intent = new Intent(registrar.context(), registrar.activity().getClass());
                intent.putExtra(ACTION_NOTIFY_INTENT, ACTION_NOTIFY_INTENT);
                intent.putExtra(ACTION_NOTIFY_ID, notifyId);
                PendingIntent pendingIntent = PendingIntent.getActivity(registrar.context(), notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationHelper helper = new NotificationHelper(
                        registrar.context(),
                        !TextUtils.isEmpty(channel) ? channel : DEFAULT_CHANNEL,
                        !TextUtils.isEmpty(channelName) ? channelName : DEFAULT_CHANNEL_NAME,
                        soundResId
                );
                helper.notify(notifyId, helper.buildNotificationText(
                        title,
                        message,
                        pendingIntent,
                        largeIcon,
                        iconSmallResId)
                );
            }
            result.success("success");
        } else {
            result.notImplemented();
        }
    }
}
