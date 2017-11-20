package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static developer.ln.henrik.spacetimealarm.MainActivity.GEOFENCE_EXPIRATION_TIME;
import static developer.ln.henrik.spacetimealarm.MainActivity.REQUEST_CODE_FINE_LOCATION;

/**
 * Created by Henrik on 20/11/2017.
 */

public class SpaceTimeAlarmManager
{
    private Activity activity;
    private AlarmManager alarmManager;
    private GeofencingClient geofencingManager;

    public SpaceTimeAlarmManager(Activity activity)
    {
        this.activity = activity;
        alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        geofencingManager = LocationServices.getGeofencingClient(activity);
    }

    public static void sendNotification(Context context, String caption) {
        Log.d("SPACETIMEALARM", "Sending notification");
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

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
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(caption)
                .setContentText(caption)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
        Log.d("SPACETIMEALARM", "Notification sent");
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        ringtone.play();
    }

    public void setAlarm(SpaceTimeAlarm alarm)
    {
        Log.d("SPACESETALARM", "Setting " + alarm.toString());
        if(alarm.isDone() != null)
        {
            if(!alarm.isDone())
            {
                if(alarm.getRequestCode() != null)
                {
                    if(alarm.getLocation_Lat() != null && alarm.getLocation_Lng() != null && alarm.getRadius() != null)
                    {
                        setLocationAlarm(alarm);
                    }
                    else if(alarm.getStartTime() != null)
                    {
                        setTimeAlarm(alarm);
                    }
                    else
                    {
                        Log.d("SPACESETALARM", "Couldn't set alarm. Insufficient information");
                    }
                }
                else
                {
                    Log.d("SPACESETALARM", "Couldn't set alarm. No requst code");
                }
            }
            else
            {
                Log.d("SPACESETALARM", "Couldn't set alarm. Alarm already done");
            }
        }
        else
        {
            Log.d("SPACESETALARM", "Couldn't set alarm. No done information");
        }
    }

    private void setLocationAlarm(SpaceTimeAlarm alarm)
    {
        Intent intent_SetAlarm = new Intent(activity, GeofenceAlarmReceiver.class);
        if(alarm != null)
        {
            Log.d("SPACESETALARM", "Location alarm getting extra: " + alarm.toString());
        }
        else
        {
            Log.d("SPACESETALARM", "Location alarm getting extra: null");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try
        {
            out = new ObjectOutputStream(bos);
            out.writeObject(alarm);
            out.flush();
            byte[] data = bos.toByteArray();
            intent_SetAlarm.putExtra(MainActivity.EXTRA_ALARM, data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                bos.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        PendingIntent pendingIntent_Geofence = PendingIntent.getService(activity, alarm.getRequestCode(), intent_SetAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(alarm.getId())
                .setCircularRegion(alarm.getLocation_Lat(), alarm.getLocation_Lng(), alarm.getRadius())
                .setExpirationDuration(GEOFENCE_EXPIRATION_TIME)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
        ArrayList<Geofence> geofences = new ArrayList<>();
        geofences.add(geofence);
        builder.addGeofences(geofences);
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
        }
        geofencingManager.removeGeofences(pendingIntent_Geofence);
        geofencingManager.addGeofences(builder.build(), pendingIntent_Geofence)
                .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("SPACESETALARM", "Location alarm set");
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("SPACESETALARM", "Failed to set Location alarm - " + e.toString());
                    }
                });
    }

    private  void setTimeAlarm(SpaceTimeAlarm alarm)
    {
        Intent intent_SetAlarm = new Intent(activity, TimeAlarmReceiver.class);
        intent_SetAlarm.setAction("developer.ln-henrik.spacetimealarm.alarmfilter");
        if(alarm != null)
        {
            Log.d("SPACESETALARM", "Time alarm getting extra: " + alarm.toString());
        }
        else
        {
            Log.d("SPACESETALARM", "Time alarm getting extra: null");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try
        {
            out = new ObjectOutputStream(bos);
            out.writeObject(alarm);
            out.flush();
            byte[] data = bos.toByteArray();
            intent_SetAlarm.putExtra(MainActivity.EXTRA_ALARM, data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                bos.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        PendingIntent pendingIntent_Alarm = PendingIntent.getBroadcast(activity, alarm.getRequestCode(), intent_SetAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent_Alarm);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getStartTime(), pendingIntent_Alarm);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getStartTime());
        // For Debugging
        String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(calendar.getTime()));
        Log.d("SPACESETALARM", "AlarmManager set to: " + timeString);
    }
}
