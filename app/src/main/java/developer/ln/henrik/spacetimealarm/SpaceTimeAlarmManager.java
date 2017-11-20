package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.content;
import static android.R.attr.data;
import static android.content.Context.ALARM_SERVICE;
import static developer.ln.henrik.spacetimealarm.MainActivity.GEOFENCE_EXPIRATION_TIME;
import static developer.ln.henrik.spacetimealarm.MainActivity.REQUEST_CODE_FINE_LOCATION;
import static developer.ln.henrik.spacetimealarm.R.id.listView_Alarms;

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

    private SpaceTimeAlarmManager()
    {

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

    public void destroyinitializeSpaceTimeAlarmManager()
    {
        unregisterAlarmBroadcast();
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
        intent_SetAlarm.putExtra(MainActivity.EXTRA_ALARM, getAlarmByteArray(alarm));
        PendingIntent pendingIntent_Geofence = PendingIntent.getService(activity, alarm.getRequestCode(), intent_SetAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(alarm.getId())
                .setCircularRegion(alarm.getLocation_Lat(), alarm.getLocation_Lng(), alarm.getRadius())
                .setExpirationDuration(GEOFENCE_EXPIRATION_TIME)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setNotificationResponsiveness(1000)
                .setLoiteringDelay(1000*60*60)
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
        intent_SetAlarm.putExtra(MainActivity.EXTRA_ALARM, getAlarmByteArray(alarm));
        PendingIntent pendingIntent_Alarm = PendingIntent.getBroadcast(activity, alarm.getRequestCode(), intent_SetAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent_Alarm);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getStartTime(), pendingIntent_Alarm);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getStartTime());
        // For Debugging
        String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(calendar.getTime()));
        Log.d("SPACESETALARM", "AlarmManager set to: " + timeString);
    }

    public void posponeAlarm(Intent intent, SpaceTimeAlarm alarm)
    {
        Log.d("SPACETIMEALARM", "Posponing alarm");
    }

    private void registerAlarmBroadcast() {
        timeAlarmReceiver = new TimeAlarmReceiver();
        activity.registerReceiver(timeAlarmReceiver, new IntentFilter("developer.ln-henrik.spacetimealarm.alarmfilter"));
    }

    private void unregisterAlarmBroadcast() {
        activity.getBaseContext().unregisterReceiver(timeAlarmReceiver);
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
