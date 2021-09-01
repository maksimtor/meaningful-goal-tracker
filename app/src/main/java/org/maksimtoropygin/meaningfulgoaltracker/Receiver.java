package org.maksimtoropygin.meaningfulgoaltracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Receiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        showNotification(context, id, title, desc);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(Context context, int id, String title, String desc) {
        NotificationChannel channel = new NotificationChannel(Integer.toString(id), title, NotificationManager.IMPORTANCE_HIGH);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.createNotificationChannel(channel);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, id, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Integer.toString(id))
                .setSmallIcon(R.drawable.icon_done)
                .setContentTitle("Your scheduled task " + title + " starts right now.")
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi);
        notificationManager.notify(id, builder.build());
    }
}