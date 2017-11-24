package developer.ln.henrik.spacetimealarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


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
    public static final String EXTRA_ALARM_DONE = "EXTRA ALARM DONE";


    public static final int REQUEST_CODE_ALARM = 1;
    public static final int REQUEST_CODE_LOCATION = 2;
    public static final int REQUEST_CODE_START_TIME = 3;
    public static final int REQUEST_CODE_END_TIME = 4;
    public static final int REQUEST_CODE_FINE_LOCATION = 5;


    public static final double ZOOM_VARIABLE = 0.01;
    public static final String CHANNEL_ID = "my_channel_id";

    private ListView listView_Alarms;
    private FloatingActionButton button_NewAlarm;
    private Toolbar toolbar;

    private DatabaseManager databaseManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up the toolbar and support
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        listView_Alarms = (ListView) findViewById(R.id.listView_Alarms);
        SpaceTimeAlarmManager.getInstance().initializeSpaceTimeAlarmManager(this);
        databaseManager = DatabaseManager.getInstance(this);
        databaseManager.initialize(this, listView_Alarms);
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
    protected void onStart() {
        super.onStart();
        databaseManager.notifyForUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.destroy();
        // Kan ikke receive alarms hvis der unregisters
        //SpaceTimeAlarmManager.getInstance().destroySpaceTimeAlarmManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_ALARM) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
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
                alarm_RequestCode = alarm_RequestCode == 0 ? databaseManager.getNextAlarmRequestCode() : alarm_RequestCode;
                Boolean done = data.getBooleanExtra(EXTRA_DONE, false);

                if(caption != null && ((location_lat != null && location_lng != null) || startTime != null))
                {
                    final SpaceTimeAlarm alarm;
                    if(alarm_Id != null)
                    {
                        alarm = new SpaceTimeAlarm(alarm_Id, caption, location_Id, location_Name, location_lat, location_lng, radius, startTime, endTime, alarm_RequestCode, done);
                        Log.d("SPACESTOREALARM", "Updating alarm: " + alarm_Id);
                    }
                    else
                    {
                        String newId = databaseManager.getNewAlarmID();
                        alarm = new SpaceTimeAlarm(newId, caption, location_Id, location_Name, location_lat, location_lng, radius, startTime, endTime, alarm_RequestCode, done);
                        Log.d("SPACESTOREALARM", "Creating alarm: " + newId);
                    }
                    databaseManager.updateAlarm(alarm);
                }
                else
                {
                    Log.d("SPACESTOREALARM", "An error occured - Insuficient Information");
                }
            }
            else
            {
                Log.d("SPACESTOREALARM", "An error occured - Result Not OK");
            }
        }
        else
        {
            Log.d("SPACESTOREALARM", "An error occured - Unknown RequestCode");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //go to this fancy settings activity!
                //thought: perhaps have these setting variables somewhere fancy
                //like Shared Preferences? Send help
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
