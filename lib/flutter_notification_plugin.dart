import 'dart:async';

import 'package:flutter/services.dart';

//class FlutterNotificationPlugin {
//  static const MethodChannel _channel =
//      const MethodChannel('flutter_notification_plugin');
//
//  static const MethodChannel _native_channel = const MethodChannel('android');
//
//  static Future<String> get notify async {
//    final bool enabled = await _channel.invokeMethod('areNotificationsEnabled');
//    final Map resourceMap = await _native_channel.invokeMethod('getResource');
//    var iconSmallResId = resourceMap['iconSmallResId'];
//    var iconLargeResId = resourceMap['iconLargeResId'];
//    var soundResId = resourceMap['soundResId'];
//    var notifyId = Random().nextInt(1000);
//
//    var title = resourceMap['title'];
//    var message = resourceMap['message'];
//
//    final String version = await _channel.invokeMethod('notify', {
//      'channel': 'flutter_channel', //通道ID
//      'channelName': 'flutter_channel_name', //通道名称
//      'notifyId': notifyId, //通知ID
//      'iconSmallResId': iconSmallResId == null ? 0 : iconSmallResId, //小图标
//      'iconLargeResId': iconLargeResId == null ? 0 : iconLargeResId, //大图标
//      'soundResId': soundResId == null ? 0 : soundResId, //通知铃声
//      'title': title,
//      'message': message
//    });
//    return version;
//  }
//}

class FlutterNotificationPlugin {
  final String channel;
  final String channelName;
  final int iconSmallResId;
  final int iconLargeResId;
  final int soundResId;
  static Function(int notifyId, String message) onMessageCallBack;
  static const MethodChannel _channelMethod =
      const MethodChannel('flutter_notification_plugin');
  static const _channelEvent =
      const EventChannel('flutter_notification_plugin_event');

  FlutterNotificationPlugin(
    this.channel, //通道
    this.channelName, //通道描述
    this.iconSmallResId, //通知小图标
    {
    this.iconLargeResId, //通知大图标
    this.soundResId, //通知的铃声
  });

  ///判断是否开启了通知
  Future<bool> areNotificationsEnabled() async {
    bool enabled = await _channelMethod.invokeMethod('areNotificationsEnabled');
    return enabled;
  }

  ///显示通知
  void notify(int notifyId, String title, String message,
      {Function(int notifyId, String message) onMessageCallBack}) {
    assert(this.channel != null);
    assert(this.channelName != null);
//    assert(this.iconSmallResId != null);
    FlutterNotificationPlugin.onMessageCallBack = onMessageCallBack;
    _channelMethod.invokeMethod('notify', {
      'channel': 'flutter_channel',
      'channelName': 'flutter_channel_name',
      'notifyId': notifyId == null ? 0 : notifyId,
      'iconSmallResId': iconSmallResId == null ? 0 : iconSmallResId,
      'iconLargeResId': iconLargeResId == null ? 0 : iconLargeResId,
      'soundResId': soundResId == null ? 0 : soundResId,
      'title': title,
      'message': message
    });

    _channelEvent.receiveBroadcastStream().listen(_onData, onError: _onError);
  }

  void _onData(Object object) {
    print('event - > $object');
  }

  void _onError(Object error) {
    print('error - > $error');
  }

  static void onMessageClick(int notifyId, String message) {
    if (onMessageCallBack != null) {
      onMessageCallBack(notifyId, message);
    }
  }
}
