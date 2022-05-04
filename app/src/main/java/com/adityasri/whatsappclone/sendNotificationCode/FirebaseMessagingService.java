package com.adityasri.whatsappclone.sendNotificationCode;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.adityasri.whatsappclone.R;
import com.google.firebase.messaging.RemoteMessage;

import static java.lang.System.currentTimeMillis;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    String title,message,receiver;


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String NOTIFICATION_CHANNEL_ID = String.valueOf((int) currentTimeMillis());

        String notification_title = remoteMessage.getData().get("Title");
        String notification_message = remoteMessage.getData().get("Message");
        String click_action = remoteMessage.getData().get("click_action");
        String from_user_id = remoteMessage.getData().get("userId");
        String UID = remoteMessage.getData().get("UID");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_message);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("userId", from_user_id);
        resultIntent.putExtra("UID",UID);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);

            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            manager.createNotificationChannel(notificationChannel);
        }

        manager.notify(Integer.parseInt(NOTIFICATION_CHANNEL_ID),builder.build());

    }
}
