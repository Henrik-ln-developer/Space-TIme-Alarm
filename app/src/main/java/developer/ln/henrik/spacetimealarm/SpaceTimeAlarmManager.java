package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Henrik on 20/11/2017.
 */

public class SpaceTimeAlarmManager
{
    private static SpaceTimeAlarmManager instance;
    private static Activity activity;
    private AlarmManager alarmManager;
    private GeofencingClient geofencingManager;
    private TimeAlarmReceiver timeAlarmReceiver;

    public SharedPreferences sharedPref;
    public int GEOFENCE_EXPIRATION_TIME;

    private SpaceTimeAlarmManager()
    {
        MainActivity.havePermission = false;
    }

    public static SpaceTimeAlarmManager getInstance()
    {
        if(instance == null)
        {
            instance = new SpaceTimeAlarmManager();
        }
        return instance;
    }

    public void initializeSpaceTimeAlarmManager(Activity activityReference)
    {
        activity = activityReference;
        registerAlarmBroadcast();
        alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        geofencingManager = LocationServices.getGeofencingClient(activity);
    }

    public void destroySpaceTimeAlarmManager()
    {
        unregisterAlarmBroadcast();
    }

    public void setAlarm(SpaceTimeAlarm alarm)
    {
        Log.d("SPACESETALARM", "Setting alarm" + alarm.getId());
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
                Log.d("SPACESETALARM", "Alarm already done");
            }
        }
        else
        {
            Log.d("SPACESETALARM", "Couldn't set alarm. No done information");
        }
    }

    public void setPostponedAlarm(SpaceTimeAlarm alarm)
    {
        Log.d("SPACESETALARM", "Setting postponed location alarm" + alarm.getId());
        if(alarm.isDone() != null)
        {
            if(!alarm.isDone())
            {
                if(alarm.getRequestCode() != null)
                {
                    if(alarm.getLocation_Lat() != null && alarm.getLocation_Lng() != null && alarm.getRadius() != null)
                    {
                        if(alarm.getStartTime() != null)
                        {
                            setTimeAlarm(alarm);
                        }
                        else
                        {
                            Log.d("SPACESETALARM", "Couldn't set postponed location alarm. Insufficient Time information");
                        }
                    }
                    else
                    {
                        Log.d("SPACESETALARM", "Couldn't set postponed location alarm. Insufficient location information");
                    }
                }
                else
                {
                    Log.d("SPACESETALARM", "Couldn't set postponed location alarm. No requst code");
                }
            }
            else
            {
                Log.d("SPACESETALARM", "Alarm already done");
            }
        }
        else
        {
            Log.d("SPACESETALARM", "Couldn't set postponed location alarm. No done information");
        }
    }

    public void removeAlarm(SpaceTimeAlarm alarm)
    {
        Intent intent_RemoveAlarm = new Intent(activity, GeofenceAlarmReceiver.class);
        intent_RemoveAlarm.putExtra(activity.getString(R.string.EXTRA_ALARM), getAlarmByteArray(alarm));
        PendingIntent pendingIntent_Alarm = PendingIntent.getService(activity, alarm.getRequestCode(), intent_RemoveAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        geofencingManager.removeGeofences(pendingIntent_Alarm);
        Log.d("SPACESETALARM", "Geofence Alarm removed: " + alarm.getId());
        intent_RemoveAlarm.setAction("developer.ln-henrik.spacetimealarm.alarmfilter");
        pendingIntent_Alarm = PendingIntent.getBroadcast(activity, alarm.getRequestCode(), intent_RemoveAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent_Alarm);
        Log.d("SPACESETALARM", "Time Alarm removed" + alarm.getId());
    }

    private void setLocationAlarm(SpaceTimeAlarm alarm)
    {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        GEOFENCE_EXPIRATION_TIME = sharedPref.getInt(activity.getString(R.string.GEOFENCE_EXPIRATION_TIME), Integer.parseInt(activity.getString(R.string.expireDefaultDuration)));

        Intent intent_SetAlarm = new Intent(activity, GeofenceAlarmReceiver.class);
        intent_SetAlarm.putExtra(activity.getString(R.string.EXTRA_ALARM), getAlarmByteArray(alarm));
        PendingIntent pendingIntent_Geofence = PendingIntent.getService(activity, alarm.getRequestCode(), intent_SetAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(alarm.getId())
                .setCircularRegion(alarm.getLocation_Lat(), alarm.getLocation_Lng(), alarm.getRadius())
                .setExpirationDuration(TimeUnit.DAYS.toMillis(GEOFENCE_EXPIRATION_TIME))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setNotificationResponsiveness(1000)
                .setLoiteringDelay(1000*60*60)
                .build();
        ArrayList<Geofence> geofences = new ArrayList<>();
        geofences.add(geofence);
        builder.addGeofences(geofences);
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("SPACESETALARM", "Need permission");
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, activity.getResources().getInteger(R.integer.REQUEST_CODE_FINE_LOCATION));
        }
        else
        {
            Log.d("SPACESETALARM", "Already have permission");
            MainActivity.havePermission = true;
        }
        while(!MainActivity.havePermission)
        {
            Log.d("SPACESETALARM", "Waiting for permission");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        geofencingManager.removeGeofences(pendingIntent_Geofence);
        Log.d("SPACESETALARM", "Adding Location alarm for alarm: " + alarm.getId());
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
        intent_SetAlarm.putExtra(activity.getString(R.string.EXTRA_ALARM), getAlarmByteArray(alarm));
        PendingIntent pendingIntent_Alarm = PendingIntent.getBroadcast(activity, alarm.getRequestCode(), intent_SetAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent_Alarm);
        Log.d("SPACESETALARM", "Adding Time alarm for alarm: " + alarm.getId());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getStartTime(), pendingIntent_Alarm);
        Log.d("SPACESETALARM", "Time alarm set: " + alarm.getId());
    }

    private void registerAlarmBroadcast() {
        timeAlarmReceiver = new TimeAlarmReceiver();
        activity.registerReceiver(timeAlarmReceiver, new IntentFilter("developer.ln-henrik.spacetimealarm.alarmfilter"));
    }

    private void unregisterAlarmBroadcast() {
        activity.unregisterReceiver(timeAlarmReceiver);
    }

    public static byte[] getAlarmByteArray(SpaceTimeAlarm alarm)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] data = null;
        try
        {
            out = new ObjectOutputStream(bos);
            out.writeObject(alarm);
            out.flush();
            data = bos.toByteArray();
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
            return data;
        }
    }

    public static SpaceTimeAlarm getAlarm(byte[] alarmBytes)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(alarmBytes);
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
            return alarm;
        }
    }
}
