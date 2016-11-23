package com.celdev.migstat.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.R;

public class NotificationHelper {

    private static int NOTIFICATION_ID = 0x123456;
    public static String NOTIFICATION_INTENT_KEY = MainActivity.APPLICATION_KEY + ".NOTIFICATION";

    public static void doFinishedApplicationStatusNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setContentText(context.getString(R.string.notification_text_made_decision))
                .setContentTitle(context.getString(R.string.notification_title_made_decision))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_heart).build();
        notification.ledARGB = 0x1e4e79;
        notification.ledOnMS = 750;
        notification.ledOffMS = 250;
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        notification.vibrate = new long[]{450, 250, 300, 700, 120, 240, 500, 500};
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
