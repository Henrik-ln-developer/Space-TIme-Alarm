package developer.ln.henrik.spacetimealarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
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

    private static NotificationSender instance;
    private  NotificationManager notificationManager;
    private static Context context;

    private NotificationSender(Context contextReference)
    {

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            // The id of the channel.
            String id = MainActivity.CHANNEL_ID;
            // The user-visible name of the channel.
            CharSequence name = "Channel Name";
            // The user-visible description of the channel.
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    public static NotificationSender getInstance(Context contextReference)
    {
        context = contextReference;
        if (instance == null)
        {
            instance = new NotificationSender(context);
        }
        return instance;
    }

    public void sendNotification(String title, String text, SpaceTimeAlarm alarm) {
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID);
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
        // Issue the notification
        notificationManager.notify((int)(Math.random()*1000), builder.build());
        Log.d("SPACETIMEALARM", "Notification sent");
    }
}
