package developer.ln.henrik.spacetimealarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static developer.ln.henrik.spacetimealarm.SpaceTimeAlarmManager.getAlarmByteArray;

/**
 * Created by Henrik on 20/11/2017.
 */

public class NotificationSender {
    public static void sendNotification(Context context, String title, String text, SpaceTimeAlarm alarm) {
        Log.d("SPACETIMEALARM", "Sending notification");

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra(MainActivity.EXTRA_ALARM, SpaceTimeAlarmManager.getAlarmByteArray(alarm));

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);
        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);
        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setColor(Color.GREEN)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setLights(0xFFb71c1c, 1000, 500)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentIntent(notificationPendingIntent);
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Issue the notification
        mNotificationManager.notify((int)(Math.random()*1000), builder.build());
        Log.d("SPACETIMEALARM", "Notification sent");
    }
}
