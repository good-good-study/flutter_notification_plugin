package com.ginkdrop.flutter_notification_plugin;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.FlutterView;

/**
 * Created by sxt on 2019/3/29.
 */
public class FlutterChannel implements EventChannel.StreamHandler, MethodChannel.MethodCallHandler {

    /**
     * 注册Android与Flutter的交互通道
     *
     * @param flutterView       flutterView
     * @param channelNameEvent  Android向Flutter发送信息的通道标识
     * @param channelNameMethod Flutter向Android发送信息的通道标识
     */
    public static void registerChannel(FlutterView flutterView, String channelNameEvent, String channelNameMethod) {
        FlutterChannel flutterChannel = new FlutterChannel();
        new EventChannel(flutterView, channelNameEvent).setStreamHandler(flutterChannel);
        new MethodChannel(flutterView, channelNameMethod).setMethodCallHandler(flutterChannel);
    }

    /**
     * 通道建立完成,在此向Flutter发送消息
     *
     * @param o
     * @param eventSink
     */
    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {

    }

    @Override
    public void onCancel(Object o) {

    }

    /**
     * Flutter向Android 发送消息的监听
     *
     * @param methodCall
     * @param result
     */
    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {

    }
}
