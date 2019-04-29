/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ginkdrop.flutter_notification_plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;


/**
 * created by sxt at 2018/11/20
 * <p>
 * 从Android 8.1（API级别27）开始，如果同一时间发布了多个通知的话 ,只有第一个通知会发出声音
 */
public class NotificationHelper {
    private Uri soundUri;
    private NotificationManager manager;
    private Context context;
    public static final String DEFAULT_CHANNEL = "DEFAULT_CHANNEL";
    public static final String DEFAULT_CHANNEL_NAME = "DEFAULT_CHANNEL_NAME";
    public String currentChannle;

    /**
     * 获取 notification manager.
     */
    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    /**
     * 注册通道
     * <p>
     * note : 必须在notify() 之前创建 重复创建已有的通道,不会有任何影响 so,尽情的 Create吧
     */
    public NotificationHelper(Context context, String channelId, String channelName, int... soundResId) {
        this.context = context;
        this.currentChannle = channelId;
        soundUri = soundResId == null || soundResId.length == 0 || soundResId[0] == 0 ? null : Uri.parse("android.resource://" + context.getPackageName() + "/" + soundResId[0]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            NotificationChannel channel = new NotificationChannel(
                    TextUtils.isEmpty(channelId) ? DEFAULT_CHANNEL : channelId,
                    TextUtils.isEmpty(channelName) ? DEFAULT_CHANNEL_NAME : channelName,
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setSound(soundUri, att);
            channel.setLightColor(Color.YELLOW);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(channel);//创建通道
        }
    }

    /**
     * 获取默认通道的 NotificationCompat.Builder
     */
    private NotificationCompat.Builder getNotificationBuilderByChannel(String... channel) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(context, channel == null || channel.length == 0 ? DEFAULT_CHANNEL : channel[0]);
        } else {
            builder = new NotificationCompat.Builder(context).setSound(soundUri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//8.0以下 && 7.0及以上 设置优先级
                builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            } else {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }
        }
        return builder;
    }

    /**
     * 创建普通的文字通知1
     * <p>
     * note: 默认通知只显示一行(系统自动截取)
     * 可以通过NotificationCompat.BigTextStyle()显示多行文本
     */
    private NotificationCompat.Builder buildNotificationText(String title, String body, PendingIntent pendingIntent, Bitmap largeIcon, int smallIcon) {
        NotificationCompat.Builder builder = getNotificationBuilderByChannel(currentChannle)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body).setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setContentIntent(pendingIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                //.setTimeoutAfter(3000)//时间过后自动取消该通知
                //.setNumber(1127)//超过999 系统就直接显示999
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)//长按应用图标,通知显示的图标类型, 默认显示大图标
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(body));
        return builder;
    }

    /**
     * 创建普通的文字通知2 添加Action
     * <p>
     *
     * @param largeIcon
     * @param smallIcon
     * @param actions   在通知消息中添加按钮 最多添加3个
     */
    private NotificationCompat.Builder buildNotificationTextAction(String title, String body, PendingIntent pendingIntent, Bitmap largeIcon, int smallIcon, NotificationCompat.Action... actions) {
        NotificationCompat.Builder builder = getNotificationBuilderByChannel(currentChannle)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(body));

        if (null != actions && actions.length > 0) {
            for (NotificationCompat.Action action : actions) {
                builder.addAction(action);
            }
        }
        return builder;
    }

    /**
     * 创建带图片的通知
     * <p>
     * 消息折叠时显示小图, 展开后显示大图
     */
    private NotificationCompat.Builder buildNotificationImage(String title, String body, Bitmap imgBitmap, PendingIntent pendingIntent, Bitmap largeIcon, int smallIcon) {
        NotificationCompat.Builder builder = getNotificationBuilderByChannel(currentChannle)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(title)
                        .bigLargeIcon(imgBitmap)
                        .bigPicture(imgBitmap));
        return builder;
    }

    /**
     * 创建带图片的通知2 添加Action
     * <p>
     * 消息折叠时显示小图, 展开后显示大图
     */
    private NotificationCompat.Builder buildNotificationImageAction(String title, String body, Bitmap imgBitmap, PendingIntent pendingIntent, Bitmap largeIcon, int smallIcon, NotificationCompat.Action... actions) {
        NotificationCompat.Builder builder = getNotificationBuilderByChannel(currentChannle)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(title)
                        .bigLargeIcon(imgBitmap)
                        .bigPicture(imgBitmap));

        if (null != actions && actions.length > 0) {
            for (NotificationCompat.Action action : actions) {
                builder.addAction(action);
            }
        }
        return builder;
    }

    public NotificationCompat.Builder buildNotificationText(String title, String body, PendingIntent pendingIntent, Bitmap largeIcon, int smallIcon, NotificationCompat.Action... actions) {
        return null != actions && actions.length > 0 ? buildNotificationTextAction(title, body, pendingIntent, largeIcon, smallIcon, actions) : buildNotificationText(title, body, pendingIntent, largeIcon, smallIcon);
    }

    public NotificationCompat.Builder buildNotificationImage(String title, String body, Bitmap imgBitmap, PendingIntent pendingIntent, Bitmap largeIcon, int smallIcon, NotificationCompat.Action... actions) {
        return null != actions && actions.length > 0 ? buildNotificationImageAction(title, body, imgBitmap, pendingIntent, largeIcon, smallIcon, actions) : buildNotificationImage(title, body, imgBitmap, pendingIntent, largeIcon, smallIcon);
    }

    /**
     * 发送通知
     */
    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }
}
