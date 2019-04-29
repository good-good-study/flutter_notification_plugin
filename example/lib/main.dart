import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_notification_plugin/flutter_notification_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String title = '假如生活欺骗了你';
  String message = '不要悲伤，不要心急！' +
      '忧郁的日子里须要镇静：' +
      '相信吧，快乐的日子将会来临！' +
      '心儿永远向往着未来；' +
      '现在却常是忧郁。' +
      '一切都是瞬息，一切都将会过去；' +
      '而那过去了的，就会成为亲切的怀恋。';
  String url =
      "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556532216241&di=8822c32f4899b3e706765d47fd037233&imgtype=0&src=http%3A%2F%2Fpic31.nipic.com%2F20130804%2F7487939_090818211000_2.jpg";

  ///发送通知
  Future<void> notify(MessageType messageType) async {
    var notificationPlugin = FlutterNotificationPlugin('ic_launcher_round',
        iconType: IconType.mipmap, soundResName: 'notify_message');
    var enabled = await notificationPlugin.areNotificationsEnabled();
    print('通知权限是否已开启：$enabled');
    await notificationPlugin.notify(
        notifyId: Random().nextInt(100000),
        title: title,
        message: message,
        iconLargeResName: 'ic_launcher_round',
        largeIconUri: url,
        messageType: messageType,
        onMessageCallBack: (int notifyId, String message) {
          print('点击了通知栏 notifyId -> $notifyId , message -> $message');
        });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('通知栏插件->适配Android Pie'),
        ),
        body: Container(
          width: double.infinity,
          alignment: AlignmentDirectional.center,
          padding: EdgeInsets.all(30),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              RaisedButton(
                onPressed: () {
                  notify(MessageType.text);
                },
                child: Text('文本消息'),
              ),
              SizedBox(
                width: 20,
              ),
              RaisedButton(
                onPressed: () {
                  notify(MessageType.image);
                },
                child: Text('图片消息'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
