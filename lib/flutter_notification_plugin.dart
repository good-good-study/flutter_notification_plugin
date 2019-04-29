import 'dart:async';

import 'package:flutter/services.dart';

///Android资源图片的类型
enum IconType { drawable, mipmap }

///消息类型 text : 文本消息, image : 图片消息
enum MessageType { text, image }

class FlutterNotificationPlugin {
  String channel; //通道
  String channelName; //通道描述
  String iconSmallResName; //通知小图标res下的文件名称
  String iconLargeResName; //通知大图标res下的文件名称
  String soundResName; //通知的铃声res下的文件名称
  IconType iconType; //资源图片的类型
  MessageType messageType; //消息类型
  String largeIconUri; //大图:可以是网络图片的url或者资源图片的路径
  Function(int notifyId, String message) onMessageCallBack; //通知栏点击回调

  static const String DEFAULT_CHANNEL = "default_channel";
  static const String DEFAULT_CHANNEL_NAME = "default_channel_name";

  static const MethodChannel _channelMethod =
      const MethodChannel('flutter_notification_plugin_method');
  static const _channelEvent =
      const EventChannel('flutter_notification_plugin_event');

  FlutterNotificationPlugin(
    this.iconSmallResName, {
    this.channel = DEFAULT_CHANNEL,
    this.channelName = DEFAULT_CHANNEL_NAME,
    this.soundResName,
    this.iconType = IconType.drawable,
  }) {
    assert(this.iconSmallResName != null);
    _channelEvent.receiveBroadcastStream().listen(_onData, onError: _onError);
  }

  ///判断是否开启了通知
  Future<bool> areNotificationsEnabled() async {
    bool enabled = await _channelMethod.invokeMethod('areNotificationsEnabled');
    return enabled;
  }

  ///显示通知
  Future<void> notify({
    int notifyId = 0,
    String title,
    String message,
    String iconLargeResName,
    String largeIconUri,
    MessageType messageType = MessageType.text,
    Function(int notifyId, String message) onMessageCallBack,
  }) async {
    this.onMessageCallBack = onMessageCallBack;
    this.iconLargeResName = iconLargeResName;
    this.largeIconUri = largeIconUri;
    this.messageType = messageType;
    _channelMethod.invokeMethod(
        'notify', _buildNotifyDataMap(notifyId, title, message));
  }

  ///构建通知栏数据集
  Map<String, Object> _buildNotifyDataMap(
      int notifyId, String title, String message) {
    return {
      'channel': 'flutter_channel',
      'channelName': 'flutter_channel_name',
      'notifyId': notifyId == null ? 0 : notifyId,
      'iconSmallResName': iconSmallResName,
      'iconLargeResName': iconLargeResName,
      'largeIconUri': largeIconUri,
      'iconType': _parseResType(),
      'messageType': _parseMessageType(),
      'soundResName': soundResName,
      'title': title,
      'message': message
    };
  }

  ///监听通知栏点击
  void _onData(Object object) {
    if (object != null) {
      Map map = object;
      int notifyId = map['notifyId'];
      String message = map['message'];
      _onMessageClick(notifyId, message);
    }
  }

  void _onError(Object error) {
    print('error - > $error');
  }

  ///通知栏点击回调
  void _onMessageClick(int notifyId, String message) {
    if (onMessageCallBack != null) {
      onMessageCallBack(notifyId, message);
    }
  }

  ///解析资源图片的类型
  String _parseResType() {
    if (iconType == IconType.drawable) {
      return 'drawable';
    }
    return 'mipmap';
  }

  ///解析消息的类型 true : 文本消息
  String _parseMessageType() {
    if (messageType == MessageType.text) {
      return 'text';
    }
    return 'image';
  }
}
