package developer.ln.henrik.spacetimealarm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
            {
                SpaceTimeAlarm alarm = SpaceTimeAlarmManager.getAlarm(intent.getByteArrayExtra(getString(R.string.EXTRA_ALARM)));
                if (alarm != null)
                {
                    if(alarm.getStartTime() != null)
                    {
                        Calendar currentTime = Calendar.getInstance();
                        if(alarm.getEndTime() == null)
                        {
                            if(currentTime.getTimeInMillis() > alarm.getStartTime())
                            {
                                NotificationSender.getInstance(this).sendNotification("Location Alarm", alarm.getCaption(), alarm);
                            }
                            else
                            {
                                Calendar newTime = Calendar.getInstance();
                                newTime.setTimeInMillis(newTime.getTimeInMillis() + 1000*60*15);
                                String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(newTime.getTime()));
                                alarm.setStartTime(newTime.getTimeInMillis());
                                Log.d("SPACETIMEALARM", "Postponing alarm: " + alarm.getId());
                                Log.d("SPACESETALARM", "Alarm set to: " + timeString);
                                DatabaseManager.getInstance(getApplicationContext()).updateAlarm(alarm);
                            }
                        }
                        else
                        {
                            if(currentTime.getTimeInMillis() > alarm.getStartTime() && currentTime.getTimeInMillis() < alarm.getEndTime())
                            {
                                NotificationSender.getInstance(this).sendNotification("Location Alarm", alarm.getCaption(), alarm);
                            }
                            else
                            {
                                Calendar newTime = Calendar.getInstance();
                                newTime.setTimeInMillis(newTime.getTimeInMillis() + 1000*60*15);
                                String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(newTime.getTime()));

                                alarm.setStartTime(newTime.getTimeInMillis());
                                Log.d("SPACETIMEALARM", "Postponing alarm: " + alarm.getId());
                                Log.d("SPACESETALARM", "Alarm set to: " + timeString);
                                DatabaseManager.getInstance(getApplicationContext()).updateAlarm(alarm);
                            }
                        }
                    }
                    else
                    {
                        NotificationSender.getInstance(this).sendNotification("Location Alarm", alarm.getCaption(), alarm);
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
