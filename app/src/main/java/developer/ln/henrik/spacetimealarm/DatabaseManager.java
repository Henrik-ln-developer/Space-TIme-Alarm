package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

/**
 * Created by Henrik on 20/11/2017.
 */

public class DatabaseManager implements ChildEventListener
{
    private Activity activity;
    private ListView listView_Alarms;
    private SpaceTimeAlarmManager alarmManager;

    private ArrayList<SpaceTimeAlarm> alarmArray;
    private SpaceTimeAlarmAdapter alarmAdapter;
    private DatabaseReference database;

    private static DatabaseManager instance;

    private DatabaseManager()
    {

    }

    public static DatabaseManager getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabaseManager(Activity activity, ListView listView_Alarms, SpaceTimeAlarmManager alarmManager)
    {
        this.activity = activity;
        this.listView_Alarms = listView_Alarms;
        this.alarmManager = alarmManager;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        SharedPreferences sharedPref = activity.getSharedPreferences("developer.ln.henrik.spacetimealarm.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        String application_id = sharedPref.getString("APPLICATION_ID", null);
        if(application_id == null)
        {
            SharedPreferences.Editor editor = sharedPref.edit();
            DatabaseReference root = firebaseDatabase.getReference();
            application_id = root.push().getKey();
            editor.putString("APPLICATION_ID", application_id);
            editor.commit();
        }
        alarmArray = new ArrayList<>();
        alarmAdapter = new SpaceTimeAlarmAdapter(alarmArray, activity, application_id);
        database = firebaseDatabase.getReference(application_id + "/alarms");
        database.addChildEventListener(this);
        listView_Alarms.setAdapter(alarmAdapter);
    }

    public String getNewAlarmID()
    {
        return database.push().getKey();
    }

    public void updateAlarm(SpaceTimeAlarm alarm)
    {
        Map<String, Object> postValues = alarm.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + alarm.getId(), postValues);
        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("SPACESTOREALARM", "Alarm Saved to database");
                } else {
                    Log.d("SPACECHECKSTUFF", task.getException().getMessage().toString());
                }
            }
        });
    }

    public void updateAlarm(SpaceTimeAlarm alarm, Activity activity)
    {
        final Activity currentActivity = activity;
        Map<String, Object> postValues = alarm.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + alarm.getId(), postValues);
        database.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("SPACESTOREALARM", "Alarm Saved to database");
                } else {
                    Log.d("SPACECHECKSTUFF", task.getException().getMessage().toString());
                }
                Intent i = new Intent(currentActivity, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                currentActivity.startActivity(i);
            }
        });
    }

    public int getNextAlarmRequestCode()
    {
        int highestRequestCode = 0;
        for(SpaceTimeAlarm alarm : alarmArray)
        {
            if (alarm.getRequestCode() != null)
            {
                int currentRequestCode = alarm.getRequestCode();
                if(currentRequestCode > highestRequestCode)
                {
                    highestRequestCode = currentRequestCode;
                }
            }
        }
        return highestRequestCode+1;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        SpaceTimeAlarm alarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
        if(alarm != null)
        {
            alarmArray.add(alarm);
            alarmAdapter.notifyDataSetChanged();
            alarmManager.setAlarm(alarm);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        SpaceTimeAlarm changedAlarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
        if(changedAlarm != null)
        {
            Log.d("SPACECHANGEDALARM", "Leder efter alarm med ID: " + changedAlarm.getId());
            for(SpaceTimeAlarm alarm : alarmArray)
            {
                if(alarm.getId() != null)
                {
                    Log.d("SPACECHANGEDALARM", "Checker alarm med ID: " + alarm.getId());
                    if(alarm.getId().equals(changedAlarm.getId()))
                    {
                        alarm.setCaption(changedAlarm.getCaption());
                        alarm.setLocation_Id(changedAlarm.getLocation_Id());
                        alarm.setLocation_Name(changedAlarm.getLocation_Name());
                        alarm.setLocation_Lat(changedAlarm.getLocation_Lat());
                        alarm.setLocation_Lng(changedAlarm.getLocation_Lng());
                        alarm.setRadius(changedAlarm.getRadius());
                        alarm.setStartTime(changedAlarm.getStartTime());
                        alarm.setEndTime(changedAlarm.getEndTime());
                        alarm.setRequestCode(changedAlarm.getRequestCode());
                        alarm.setDone(changedAlarm.isDone());
                        alarmAdapter.notifyDataSetChanged();
                        alarmManager.setAlarm(alarm);
                        return;
                    }
                }
                else
                {
                    Log.d("SPACECHANGEDALARM", "Ingen ID på alarm");
                }
            }
            Log.d("SPACECHANGEDALARM", "Alarm changed in database, but hasn't been changed in list");
            Toast.makeText(activity.getApplicationContext(), "Alarm changed in database, but hasn't been changed in list", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        SpaceTimeAlarm deletedAlarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
        if(deletedAlarm != null)
        {
            Log.d("SPACEREMOVEDALARM", "Leder efter alarm med ID: " + deletedAlarm.getId());
            for(SpaceTimeAlarm alarm : alarmArray)
            {
                if(alarm.getId() != null)
                {
                    Log.d("SPACEREMOVEDALARM", "Checker alarm med ID: " + alarm.getId());
                    if(alarm.getId().equals(deletedAlarm.getId()))
                    {
                        Log.d("SPACEREMOVEDALARM", "Remover alarm med ID: " + alarm.getId());
                        alarmArray.remove(alarm);
                        alarmAdapter.notifyDataSetChanged();
                        return;
                    }
                }
                else
                {
                    Log.d("SPACEREMOVEDALARM", "Ingen ID på alarm");
                }
            }
            Log.d("SPACEREMOVEDALARM", "Alarm removed from database, but hasn't been removed from list");
            Toast.makeText(activity.getApplicationContext(), "Alarm removed from database, but hasn't been removed from list", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
