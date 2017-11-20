package developer.ln.henrik.spacetimealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.defaultValue;
import static android.R.attr.id;


public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT = "EXTRA EDIT";
    public static final String EXTRA_CAPTION = "EXTRA CAPTION";
    public static final String EXTRA_LOCATION_ID = "EXTRA LOCATION ID";
    public static final String EXTRA_LOCATION_LAT = "EXTRA LOCATION LAT";
    public static final String EXTRA_LOCATION_LNG = "EXTRA LOCATION LNG";
    public static final String EXTRA_LOCATION_NAME = "EXTRA LOCATION NAME";
    public static final String EXTRA_START_TIME = "EXTRA START TIME";
    public static final String EXTRA_END_TIME = "EXTRA END TIME";
    public static final String EXTRA_TYPE = "EXTRA TYPE";
    public static final String EXTRA_HOUR = "EXTRA HOUR";
    public static final String EXTRA_MIN = "EXTRA MIN";
    public static final String EXTRA_HANDLER = "EXTRA HANDLER";
    public static final String EXTRA_TIME_PICKER = "EXTRA TIME PICKER";
    public static final String EXTRA_ALARM = "EXTRA ALARM";
    public static final String EXTRA_ALARM_ID = "EXTRA ALARM ID";
    public static final String EXTRA_REQUESTCODE = "EXTRA REQUESTCODE";
    public static final String EXTRA_RADIUS = "EXTRA RADIUS";
    public static final String EXTRA_DONE = "EXTRA DONE";


    public static final int REQUEST_CODE_ALARM = 1;
    public static final int REQUEST_CODE_LOCATION = 2;
    public static final int REQUEST_CODE_START_TIME = 3;
    public static final int REQUEST_CODE_END_TIME = 4;
    public static final int REQUEST_CODE_FINE_LOCATION = 5;


    public static final double ZOOM_VARIABLE = 0.01;
    public static final long GEOFENCE_EXPIRATION_TIME = 999999999;

    private ListView listView_Alarms;
    private ArrayList<SpaceTimeAlarm> alarmArray;
    private SpaceTimeAlarmAdapter alarmAdapter;
    private FloatingActionButton button_NewAlarm;

    private DatabaseReference database;
    private SpaceTimeAlarmManager manager;

    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerAlarmBroadcast();
        manager = new SpaceTimeAlarmManager(this);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        SharedPreferences sharedPref = getSharedPreferences("developer.ln.henrik.spacetimealarm.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
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
        alarmAdapter = new SpaceTimeAlarmAdapter(alarmArray, this, application_id);
        database = firebaseDatabase.getReference(application_id + "/alarms");
        database.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                SpaceTimeAlarm alarm = dataSnapshot.getValue(SpaceTimeAlarm.class);
                if(alarm != null)
                {
                    alarmArray.add(alarm);
                    alarmAdapter.notifyDataSetChanged();
                    manager.setAlarm(alarm);
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
                                alarm.setStartTime(changedAlarm.getStartTime());
                                alarm.setEndTime(changedAlarm.getEndTime());
                                alarmAdapter.notifyDataSetChanged();
                                manager.setAlarm(alarm);
                                return;
                            }
                        }
                        else
                        {
                            Log.d("SPACECHANGEDALARM", "Ingen ID på alarm");
                        }
                    }
                    Log.d("SPACECHANGEDALARM", "Alarm changed in database, but hasn't been changed in list");
                    Toast.makeText(getApplicationContext(), "Alarm changed in database, but hasn't been changed in list", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Alarm removed from database, but hasn't been removed from list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listView_Alarms = (ListView) findViewById(R.id.listView_Alarms) ;
        listView_Alarms.setAdapter(alarmAdapter);
        listView_Alarms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SpaceTimeAlarm alarm = (SpaceTimeAlarm) listView_Alarms.getItemAtPosition(position);
                createOrEditAlarm(alarm);
            }
        });
        button_NewAlarm = (FloatingActionButton) findViewById(R.id.button_NewAlarm);
        button_NewAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOrEditAlarm(null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_ALARM) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                boolean isEdit = data.getBooleanExtra(EXTRA_EDIT, false);
                String alarm_Id = data.getStringExtra(EXTRA_ALARM_ID);
                String caption = data.getStringExtra(EXTRA_CAPTION);
                String location_Id = data.getStringExtra(EXTRA_LOCATION_ID);
                String location_Name = data.getStringExtra(EXTRA_LOCATION_NAME);
                Double location_lat = data.getDoubleExtra(EXTRA_LOCATION_LAT, 0);
                location_lat = location_lat == 0 ? null : location_lat;
                Double location_lng = data.getDoubleExtra(EXTRA_LOCATION_LNG, 0);
                location_lng = location_lng == 0 ? null : location_lng;
                Integer radius = data.getIntExtra(EXTRA_RADIUS, 0);
                Long startTime = data.getLongExtra(EXTRA_START_TIME, 0);
                startTime = startTime == 0 ? null : startTime;
                Long endTime = data.getLongExtra(EXTRA_END_TIME, 0);
                endTime = endTime == 0 ? null : endTime;
                int alarm_RequestCode = data.getIntExtra(EXTRA_REQUESTCODE, 0);
                alarm_RequestCode = alarm_RequestCode == 0 ? getNextAlarmRequestCode() : alarm_RequestCode;
                Boolean done = data.getBooleanExtra(EXTRA_DONE, false);

                if(caption != null && ((location_lat != null && location_lng != null) || startTime != null))
                {
                    final SpaceTimeAlarm alarm;
                    if(alarm_Id != null)
                    {
                        alarm = new SpaceTimeAlarm(alarm_Id, caption, location_Id, location_Name, location_lat, location_lng, radius, startTime, endTime, alarm_RequestCode, done);
                        Log.d("SPACESTOREALARM", "Updating alarm with id: " + alarm_Id);
                    }
                    else
                    {
                        String newId = database.push().getKey();
                        alarm = new SpaceTimeAlarm(newId, caption, location_Id, location_Name, location_lat, location_lng, radius, startTime, endTime, alarm_RequestCode, done);
                        Log.d("SPACESTOREALARM", "Creating alarm with id: " + newId);
                    }

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
                else
                {
                    Log.d("SPACESTOREALARM", "An error occured");
                }
            }
            else
            {
                Log.d("SPACESTOREALARM", "An error occured - Result Not OK");
            }
        }
    }

    private void createOrEditAlarm(SpaceTimeAlarm alarm)
    {
        Intent intent_CreateOrEditAlarm = new Intent(MainActivity.this, SpaceTimeAlarmActivity.class);

        if(alarm != null)
        {
            intent_CreateOrEditAlarm.putExtra(EXTRA_EDIT, true);
            intent_CreateOrEditAlarm.putExtra(EXTRA_ALARM, alarm);
        }
        else
        {
            intent_CreateOrEditAlarm.putExtra(EXTRA_EDIT, false);
        }
        startActivityForResult(intent_CreateOrEditAlarm, REQUEST_CODE_ALARM);
    }



    private int getNextAlarmRequestCode()
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

    private void registerAlarmBroadcast() {
        alarmReceiver = new AlarmReceiver();
        registerReceiver(alarmReceiver, new IntentFilter("developer.ln-henrik.spacetimealarm.alarmfilter"));
    }

    private void unregisterAlarmBroadcast() {
        getBaseContext().unregisterReceiver(alarmReceiver);
    }
}
