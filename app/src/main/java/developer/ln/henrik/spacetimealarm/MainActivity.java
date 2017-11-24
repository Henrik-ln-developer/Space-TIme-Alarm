package developer.ln.henrik.spacetimealarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Henrik on 07/11/2017.
 */

public class MainActivity extends AppCompatActivity {
    public static boolean havePermission;

    private ListView listView_Alarms;
    private FloatingActionButton button_NewAlarm;
    private Toolbar toolbar;

    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SPACESTOREALARM", "CREATING");
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
    protected void onDestroy() {
        Log.d("SPACESTOREALARM", "DESTROYING");
        super.onDestroy();
        databaseManager.destroy();
        SpaceTimeAlarmManager.getInstance().destroySpaceTimeAlarmManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == getResources().getInteger(R.integer.REQUEST_CODE_ALARM)) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String alarm_Id = data.getStringExtra(getString(R.string.EXTRA_ALARM_ID));
                String caption = data.getStringExtra(getString(R.string.EXTRA_CAPTION));
                String location_Id = data.getStringExtra(getString(R.string.EXTRA_LOCATION_ID));
                String location_Name = data.getStringExtra(getString(R.string.EXTRA_LOCATION_NAME));
                Double location_lat = data.getDoubleExtra(getString(R.string.EXTRA_LOCATION_LAT), 0);
                location_lat = location_lat == 0 ? null : location_lat;
                Double location_lng = data.getDoubleExtra(getString(R.string.EXTRA_LOCATION_LNG), 0);
                location_lng = location_lng == 0 ? null : location_lng;
                Integer radius = data.getIntExtra(getString(R.string.EXTRA_RADIUS), 0);
                radius = radius == 0 ? null : radius;
                Long startTime = data.getLongExtra(getString(R.string.EXTRA_START_TIME), 0);
                startTime = startTime == 0 ? null : startTime;
                Long endTime = data.getLongExtra(getString(R.string.EXTRA_END_TIME), 0);
                endTime = endTime == 0 ? null : endTime;
                int alarm_RequestCode = data.getIntExtra(getString(R.string.EXTRA_REQUESTCODE), 0);
                alarm_RequestCode = alarm_RequestCode == 0 ? databaseManager.getNextAlarmRequestCode() : alarm_RequestCode;
                Boolean done = data.getBooleanExtra(getString(R.string.EXTRA_DONE), false);

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
        else if (requestCode == getResources().getInteger(R.integer.REQUEST_CODE_SETTINGS))
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
                databaseManager.updateApplicationId();
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
            intent_CreateOrEditAlarm.putExtra(getString(R.string.EXTRA_EDIT), true);
            intent_CreateOrEditAlarm.putExtra(getString(R.string.EXTRA_ALARM), alarm);
        }
        else
        {
            intent_CreateOrEditAlarm.putExtra(getString(R.string.EXTRA_EDIT), false);
        }
        startActivityForResult(intent_CreateOrEditAlarm, getResources().getInteger(R.integer.REQUEST_CODE_ALARM));
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
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, getResources().getInteger(R.integer.REQUEST_CODE_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int index = 0;
        Map<String, Integer> PermissionsMap = new HashMap<String, Integer>();
        for (String permission : permissions){
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if((PermissionsMap.get("ACCESS_FINE_LOCATION") != 0)){

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, getResources().getInteger(R.integer.REQUEST_CODE_FINE_LOCATION));
        }
        else
        {
            havePermission = true;
        }
    }
}
