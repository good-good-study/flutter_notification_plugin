package com.ginkdrop.flutter_notification_plugin;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.LinkedHashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterNotificationPlugin
 */
public class FlutterNotificationPlugin implements MethodChannel.MethodCallHandler, EventChannel.StreamHandler {

    private static Registrar registrar;
    private static EventChannel.EventSink eventSink;
    private static final String TAG = "FlutterNotification";
    private static final String DEFAULT_CHANNEL = "DEFAULT_CHANNEL";
    private static final String DEFAULT_CHANNEL_NAME = "DEFAULT_CHANNEL_NAME";
    private static final String ACTION_NOTIFY_ID = "ACTION_NOTIFY_ID";
    private static final String ACTION_NOTIFY_INTENT = "ACTION_NOTIFY_INTENT";
    private static String CHANNEL_EVENT = "flutter_notification_plugin_event";
    private static String CHANNEL_METHOD = "flutter_notification_plugin_method";

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
        //注册通道
        final MethodChannel channelMethod = new MethodChannel(registrar.messenger(), CHANNEL_METHOD);
        final EventChannel channelEvent = new EventChannel(registrar.messenger(), CHANNEL_EVENT);
        FlutterNotificationPlugin plugin = new FlutterNotificationPlugin();
        channelEvent.setStreamHandler(plugin);
        channelMethod.setMethodCallHandler(plugin);
        //当通知栏点击时调用(实则监听的是pendingIntent)
        registrar.addNewIntentListener(new PluginRegistry.NewIntentListener() {
            @Override
            public boolean onNewIntent(Intent intent) {
                String stringExtra = intent.getStringExtra(ACTION_NOTIFY_INTENT);
                int intExtra = intent.getIntExtra(ACTION_NOTIFY_ID, 0);
                if (ACTION_NOTIFY_INTENT.equals(stringExtra)) {
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
        FlutterNotificationPlugin.eventSink = eventSink;
        Log.e(TAG, "onListen");
    }

    @Override
    public void onCancel(Object o) {
        Log.e(TAG, "onCancel");
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("areNotificationsEnabled")) {//通知权限是否开启
            result.success(areNotificationsEnabled());
        } else if (call.method.equals("notify")) {//通知
            parseData(call);
            result.success(null);
        } else {
            result.notImplemented();
        }
    }

    private boolean areNotificationsEnabled() {
        return NotificationManagerCompat.from(registrar.context()).areNotificationsEnabled();
    }

    //解析由Flutter传递的数据集
    private void parseData(MethodCall call) {
        if (call.arguments instanceof Map) {
            Map map = (Map) call.arguments;
            String channel = (String) map.get("channel");
            String channelName = (String) map.get("channelName");
            final String iconSmallResName = (String) map.get("iconSmallResName");
            if ((TextUtils.isEmpty(channel))) {
                throw new NullPointerException("通道不能为空");
            }
            if ((TextUtils.isEmpty(channelName))) {
                throw new NullPointerException("通道描述不能为空");
            }
            if (TextUtils.isEmpty(iconSmallResName)) {
                throw new NullPointerException("通知图标不能为空");
            }
            String iconLargeResName = (String) map.get("iconLargeResName");
            String largeIconUri = (String) map.get("largeIconUri");
            String iconType = (String) map.get("iconType");//mipmap或者drawable
            String messageType = (String) map.get("messageType");//消息类型 text : 文本消息 ,image : 图片消息
            String soundResName = (String) map.get("soundResName");

            final int notifyId = (int) map.get("notifyId");
            final String title = (String) map.get("title");
            final String message = (String) map.get("message");

            //res文件夹下,通过图片名称获取资源id
            Context context = registrar.context();
            String packageName = context.getPackageName();
            try {
                int iconSmallResId = context.getResources().getIdentifier(TextUtils.isEmpty(iconSmallResName) ? "" : iconSmallResName, iconType, packageName);
                int iconLargeResId = context.getResources().getIdentifier(TextUtils.isEmpty(iconLargeResName) ? "" : iconLargeResName, iconType, packageName);
                int soundResId = context.getResources().getIdentifier(TextUtils.isEmpty(soundResName) ? "" : soundResName, "raw", packageName);
                final Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), iconLargeResId);
                Log.e(TAG, String.format("iconSmallResId->%s,iconLargeResId->%s,soundResId->%s", iconSmallResId, iconLargeResId, soundResId));
                notify(channel, channelName, notifyId, iconSmallResId, largeIcon, largeIconUri, soundResId, messageType, title, message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //显示通知
    private void notify(String channel, String channelName, final int notifyId, final int iconSmallResId, final Bitmap largeIcon, String largeIconUri, int soundResId, String messageType, final String title, final String message) {
        Context context = registrar.context();
        Intent intent = new Intent(context, registrar.activity().getClass());
        intent.putExtra(ACTION_NOTIFY_INTENT, ACTION_NOTIFY_INTENT);
        intent.putExtra(ACTION_NOTIFY_ID, notifyId);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationHelper helper = new NotificationHelper(
                context,
                !TextUtils.isEmpty(channel) ? channel : DEFAULT_CHANNEL,
                !TextUtils.isEmpty(channelName) ? channelName : DEFAULT_CHANNEL_NAME,
                soundResId
        );
        if ("text".equals(messageType)) {//文本消息
            helper.notify(notifyId, helper.buildNotificationText(
                    title,
                    message,
                    pendingIntent,
                    largeIcon,
                    iconSmallResId));
        } else {
            //图片消息
            Glide.with(context)
                    .load(largeIconUri)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap source, GlideAnimation<? super Bitmap> glideAnimation) {
                            helper.notify(notifyId, helper.buildNotificationImage(title, message, source, pendingIntent, largeIcon, iconSmallResId));
                        }
                    });
        }
    }
}
