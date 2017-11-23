package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static developer.ln.henrik.spacetimealarm.R.id.listView_Alarms;

/**
 * Created by Henrik on 20/11/2017.
 */

public class DatabaseManager implements ChildEventListener
{
    private static DatabaseManager instance;

    private DatabaseReference database;
    private String application_id;


    private AlarmUpdater alarmUpdater;

    private DatabaseManager(Context context)
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        application_id = sharedPref.getString(context.getString(R.string.APPLICATION_ID), null);
        if(application_id == null)
        {
            SharedPreferences.Editor editor = sharedPref.edit();
            DatabaseReference root = firebaseDatabase.getReference();
            application_id = root.push().getKey();
            editor.putString("APPLICATION_ID", application_id);
            editor.commit();
        }
        database = firebaseDatabase.getReference(application_id + "/alarms");
        database.addChildEventListener(this);
    }

    public static DatabaseManager getInstance(Context context)
    {
        if (instance == null)
        {
            Log.d("SPACEWOOP", "NEW BITCHES");
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    public void initialize(Activity activity, ListView listView_AlarmsReference)
    {
        alarmUpdater = new AlarmUpdater(activity, listView_AlarmsReference);
    }

    public void destroy()
    {
        database.removeEventListener(this);
    }

    public String getNewAlarmID()
    {
        return database.push().getKey();
    }

    public void deleteAlarm(SpaceTimeAlarm alarm)
    {
        database.child(alarm.getId()).removeValue();
    }

    public void updateAlarm(SpaceTimeAlarm alarm)
    {
        Log.d("SPACESTOREALARM", "Saving/updating alarm: " + alarm.getId());
        Map<String, Object> postValues = alarm.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + alarm.getId(), postValues);
        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("SPACESTOREALARM", "Alarm saved/updated");
                } else {
                    Log.d("SPACECHECKSTUFF", "Alarm not saved/updated - " + task.getException().getMessage().toString());
                }
            }
        });
    }

    public int getNextAlarmRequestCode()
    {
        return alarmUpdater.getNextAlarmRequestCode();
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        SpaceTimeAlarm alarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
        if(alarmUpdater != null)
        {
            alarmUpdater.addAlarm(alarm);
        }
        else
        {
            Log.d("SPACECHANGEDALARM", "Couldn't add alarm to list. AlarmUpdater not set");
        }
        SpaceTimeAlarmManager.getInstance().setAlarm(alarm);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        SpaceTimeAlarm changedAlarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
        if(alarmUpdater != null)
        {
            alarmUpdater.updateAlarm(changedAlarm);
        }
        else
        {
            Log.d("SPACECHANGEDALARM", "Couldn't update alarm on list. AlarmUpdater not set");
        }
        SpaceTimeAlarmManager.getInstance().setAlarm(changedAlarm);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        SpaceTimeAlarm deletedAlarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
        if(alarmUpdater != null)
        {
            alarmUpdater.removeAlarm(deletedAlarm);
        }
        else
        {
            Log.d("SPACECHANGEDALARM", "Couldn't remove alarm. AlarmUpdater not set");
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
