package developer.ln.henrik.spacetimealarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Henrik on 14/11/2017.
 */

public class GeofenceAlarmReceiver extends IntentService {

    public GeofenceAlarmReceiver() {
        super("GeofenceAlarmReceiver");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null)
        {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError())
            {
                String errorMessage = getErrorString(this, geofencingEvent.getErrorCode());
                Log.d("GEOFENCEALARM", errorMessage);
                return;
            }
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            {
                // Get the geofences that were triggered.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences);
                Log.d("GEOFENCEALARM", geofenceTransitionDetails);
                SpaceTimeAlarm alarm = (SpaceTimeAlarm) intent.getSerializableExtra(MainActivity.EXTRA_ALARM);
                if (alarm != null)
                {
                    if(alarm.getStartTime() != null)
                    {
                        Calendar currentTime = Calendar.getInstance();
                        if(alarm.getEndTime() == null)
                        {
                            if(currentTime.getTimeInMillis() > alarm.getStartTime())
                            {
                                sendNotification(geofenceTransitionDetails);
                            }
                            else
                            {
                                posponeAlarm(intent, alarm);
                            }
                        }
                        else
                        {
                            if(currentTime.getTimeInMillis() > alarm.getStartTime() && currentTime.getTimeInMillis() < alarm.getEndTime())
                            {
                                sendNotification(geofenceTransitionDetails);
                            }
                            else
                            {
                                posponeAlarm(intent, alarm);
                            }
                        }
                    }
                    else
                    {
                        sendNotification(geofenceTransitionDetails);
                    }
                }
                else
                {
                    Log.d("GEOFENCEALARM", "Alarm is null");
                }
            }
            else
            {
                Log.d("GEOFENCEALARM", "Invalid Geofence Transition Type");
            }
        }
    }

    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences)
    {
        String geofenceTransitionString = getTransitionString(geofenceTransition);
        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences)
        {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);
        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);
        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);
        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Transition notification")
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Issue the notification
        mNotificationManager.notify(0, builder.build());

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        ringtone.play();
    }

    private void posponeAlarm(Intent intent, SpaceTimeAlarm alarm)
    {
        Log.d("GEOFENCEALARM", "Posponing alarm");
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Transition Enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Transition Exit";
            default:
                return "Transition UNKNOWN";
        }
    }

    /**
     * Returns the error string for a geofencing exception.
     */
    public static String getErrorString(Context context, Exception e) {
        if (e instanceof ApiException)
        {
            return getErrorString(context, ((ApiException) e).getStatusCode());
        }
        else
        {
            return "Unknown Geofene Error";
        }
    }

    /**
     * Returns the error string for a geofencing error code.
     */
    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many Geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown Geofence Error";
        }
    }
}
