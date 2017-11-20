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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
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
        Log.d("SPACEGEOFENCEALARM", "Received LocationAlarm");
        if(intent != null)
        {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError())
            {
                String errorMessage = getErrorString(this, geofencingEvent.getErrorCode());
                Log.d("SPACEGEOFENCEALARM", errorMessage);
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
                Log.d("SPACEGEOFENCEALARM", geofenceTransitionDetails);
                ByteArrayInputStream bis = new ByteArrayInputStream(intent.getByteArrayExtra(MainActivity.EXTRA_ALARM));
                ObjectInput in = null;
                SpaceTimeAlarm alarm = null;
                try
                {
                    in = new ObjectInputStream(bis);
                    alarm = (SpaceTimeAlarm)in.readObject();
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (in != null)
                        {
                            in.close();
                        }
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if (alarm != null)
                {
                    if(alarm.getStartTime() != null)
                    {
                        Calendar currentTime = Calendar.getInstance();
                        if(alarm.getEndTime() == null)
                        {
                            if(currentTime.getTimeInMillis() > alarm.getStartTime())
                            {
                                NotificationSender.sendNotification(this, "Location Alarm", alarm.getCaption(), alarm);
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
                                NotificationSender.sendNotification(this, "Location Alarm", alarm.getCaption(), alarm);
                            }
                            else
                            {
                                posponeAlarm(intent, alarm);
                            }
                        }
                    }
                    else
                    {
                        NotificationSender.sendNotification(this, "Location Alarm", alarm.getCaption(), alarm);
                    }
                }
                else
                {
                    Log.d("SPACEGEOFENCEALARM", "Alarm is null");
                }
            }
            else
            {
                Log.d("SPACEGEOFENCEALARM", "Invalid Geofence Transition Type - " +  geofenceTransition);
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

    private void posponeAlarm(Intent intent, SpaceTimeAlarm alarm)
    {
        Log.d("SPACEGEOFENCEALARM", "Posponing alarm");
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
