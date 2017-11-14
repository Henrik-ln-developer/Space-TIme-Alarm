package developer.ln.henrik.spacetimealarm;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.Calendar;


public class SpaceTimeAlarmActivity extends AppCompatActivity implements View.OnClickListener {

    private Place location;
    private Calendar startTime;
    private Calendar endTime;
    private String alarm_Id;
    private Integer requestCode;

    private boolean isEdit;

    private EditText editText_Caption;
    private TextView textView_LocationChoose;
    private EditText editText_Radius;
    private TextView textView_StartTimeChoose;
    private TextView textView_EndTimeChoose;
    private Button button_Finish;

    private GoogleMap mMap;

    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_time_alarm);

        editText_Caption = (EditText) findViewById(R.id.editText_Caption);
        textView_LocationChoose = (TextView) findViewById(R.id.textView_LocationChoose);
        editText_Radius = (EditText) findViewById(R.id.editText_Radius);
        textView_StartTimeChoose = (TextView) findViewById(R.id.textView_StartTimeChoose);
        textView_EndTimeChoose = (TextView) findViewById(R.id.textView_EndTimeChoose);
        button_Finish = (Button) findViewById(R.id.button_Finish);
        isEdit = getIntent().getBooleanExtra(MainActivity.EXTRA_EDIT, false);
        if (isEdit) {
            SpaceTimeAlarm alarm = (SpaceTimeAlarm) getIntent().getSerializableExtra(MainActivity.EXTRA_ALARM);
            if(alarm != null)
            {
                if(alarm.getId() != null)
                {
                    alarm_Id = alarm.getId();
                }

                if(alarm.getCaption() != null)
                {
                    editText_Caption.setText(alarm.getCaption());
                }

                if (alarm.getLocation_Id() != null) {
                    GeoDataClient geoDataClient = Places.getGeoDataClient(this, null);
                    Task<PlaceBufferResponse> response = geoDataClient.getPlaceById(alarm.getLocation_Id());
                    response.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            PlaceBufferResponse places = task.getResult();
                            if (places.getCount() > 0) {
                                location = places.get(0).freeze();
                                if (location != null) {
                                    textView_LocationChoose.setText(location.toString());
                                }
                            } else {
                                Toast.makeText(SpaceTimeAlarmActivity.this, "Couldn't find alarm's location", Toast.LENGTH_SHORT).show();
                            }
                            places.release();
                        }
                    });
                }

                if(alarm.getRadius() != null)
                {
                    editText_Radius.setText(alarm.getRadius().toString());
                }

                if(alarm.getStartTime() != null)
                {
                    startTime = Calendar.getInstance();
                    startTime.setTimeInMillis(alarm.getStartTime());
                    textView_StartTimeChoose.setText(startTime.getTime().toString());
                }

                if (alarm.getEndTime() != null) {
                    endTime = Calendar.getInstance();
                    endTime.setTimeInMillis(alarm.getEndTime());
                    textView_EndTimeChoose.setText(endTime.getTime().toString());
                }

                if(alarm.getRequestCode() != null)
                {
                    requestCode = alarm.getRequestCode();
                }
            }
            else
            {
                Toast.makeText(SpaceTimeAlarmActivity.this, "Couldn't find alarm", Toast.LENGTH_SHORT).show();
            }
            button_Finish.setText("Update");
        }
        textView_LocationChoose.setOnClickListener(this);
        textView_StartTimeChoose.setOnClickListener(this);
        textView_EndTimeChoose.setOnClickListener(this);
        button_Finish.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQUEST_CODE_LOCATION) {
            if (resultCode == RESULT_OK) {
                location = PlacePicker.getPlace(data, this);
                if (location != null) {
                    textView_LocationChoose.setText(location.toString());
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.button_Finish:
                if (editText_Caption.getText().toString().equals("")) {
                    Toast.makeText(SpaceTimeAlarmActivity.this, "An alarm needs a caption", Toast.LENGTH_SHORT).show();
                } else if (textView_LocationChoose.getText().toString().equals("Choose Location") && textView_StartTimeChoose.getText().toString().equals("Choose Start time")) {
                    Toast.makeText(SpaceTimeAlarmActivity.this, "An alarm needs a Location or a Start time", Toast.LENGTH_SHORT).show();
                } else {
                    Intent result_intent = new Intent();
                    result_intent.putExtra(MainActivity.EXTRA_EDIT, isEdit);
                    result_intent.putExtra(MainActivity.EXTRA_ALARM_ID, alarm_Id);
                    result_intent.putExtra(MainActivity.EXTRA_CAPTION, editText_Caption.getText().toString());
                    if (location != null) {
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_ID, location.getId());
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_LAT, location.getLatLng().latitude);
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_LNG, location.getLatLng().longitude);
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_NAME, location.getAddress().toString());
                        if(editText_Radius.getText().toString() != "")
                        {
                            result_intent.putExtra(MainActivity.EXTRA_RADIUS, Integer.parseInt(editText_Radius.getText().toString()));
                        }
                    }
                    if(startTime != null)
                    {
                        result_intent.putExtra(MainActivity.EXTRA_START_TIME, startTime.getTimeInMillis());
                    }
                    if (endTime != null)
                    {
                        result_intent.putExtra(MainActivity.EXTRA_END_TIME, endTime.getTimeInMillis());
                    }
                    if (requestCode != null)
                    {
                        result_intent.putExtra(MainActivity.EXTRA_REQUESTCODE, requestCode);
                    }
                    setResult(RESULT_OK, result_intent);
                    finish();
                }
                break;

            case R.id.textView_EndTimeChoose:
                Bundle bundle_EndTime = new Bundle();
                bundle_EndTime.putInt(MainActivity.EXTRA_TYPE, MainActivity.REQUEST_CODE_END_TIME);
                bundle_EndTime.putSerializable(MainActivity.EXTRA_HANDLER, new TimePickerHandler());
                if (endTime != null) {
                    bundle_EndTime.putInt(MainActivity.EXTRA_HOUR, endTime.get(Calendar.HOUR_OF_DAY));
                    bundle_EndTime.putInt(MainActivity.EXTRA_MIN, endTime.get(Calendar.MINUTE));
                }
                TimePickerDialogFragment fragment_EndTime = new TimePickerDialogFragment();
                fragment_EndTime.setArguments(bundle_EndTime);
                FragmentManager manager_EndTime = getSupportFragmentManager();
                FragmentTransaction transaction_EndTime = manager_EndTime.beginTransaction();
                transaction_EndTime.add(fragment_EndTime, MainActivity.EXTRA_TIME_PICKER);
                transaction_EndTime.commit();
                break;
            case R.id.textView_StartTimeChoose:
                Bundle bundle_StartTime = new Bundle();
                bundle_StartTime.putInt(MainActivity.EXTRA_TYPE, MainActivity.REQUEST_CODE_START_TIME);
                bundle_StartTime.putSerializable(MainActivity.EXTRA_HANDLER, new TimePickerHandler());
                if (startTime != null) {
                    bundle_StartTime.putInt(MainActivity.EXTRA_HOUR, startTime.get(Calendar.HOUR_OF_DAY));
                    bundle_StartTime.putInt(MainActivity.EXTRA_MIN, startTime.get(Calendar.MINUTE));
                }
                TimePickerDialogFragment fragment_StartTime = new TimePickerDialogFragment();
                fragment_StartTime.setArguments(bundle_StartTime);
                FragmentManager manager_StartTime = getSupportFragmentManager();
                FragmentTransaction transaction_StartTime = manager_StartTime.beginTransaction();
                transaction_StartTime.add(fragment_StartTime, MainActivity.EXTRA_TIME_PICKER);
                transaction_StartTime.commit();
                break;
            case R.id.textView_LocationChoose:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                if (location != null) {
                    LatLng northeast = new LatLng(location.getLatLng().latitude - MainActivity.ZOOM_VARIABLE, location.getLatLng().longitude - MainActivity.ZOOM_VARIABLE);
                    LatLng southwest = new LatLng(location.getLatLng().latitude + MainActivity.ZOOM_VARIABLE, location.getLatLng().longitude + MainActivity.ZOOM_VARIABLE);
                    LatLngBounds latLngBounds = new LatLngBounds(northeast, southwest);
                    builder.setLatLngBounds(latLngBounds);
                }
                try {
                    startActivityForResult(builder.build(SpaceTimeAlarmActivity.this), MainActivity.REQUEST_CODE_LOCATION);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Toast.makeText(SpaceTimeAlarmActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(SpaceTimeAlarmActivity.this, "Google Play services are not available", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    class TimePickerHandler extends Handler implements Serializable {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int type = bundle.getInt(MainActivity.EXTRA_TYPE);
            int timeHour = bundle.getInt(MainActivity.EXTRA_HOUR);
            int timeMinute = bundle.getInt(MainActivity.EXTRA_MIN);
            if(type == MainActivity.REQUEST_CODE_START_TIME)
            {
                startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, timeHour);
                startTime.set(Calendar.MINUTE, timeMinute);
                textView_StartTimeChoose.setText(startTime.getTime().toString());
            }
            else if(type == MainActivity.REQUEST_CODE_END_TIME)
            {
                endTime = Calendar.getInstance();
                endTime.set(Calendar.HOUR_OF_DAY, timeHour);
                endTime.set(Calendar.MINUTE, timeMinute);
                textView_EndTimeChoose.setText(endTime.getTime().toString());
            }
            else
            {
                Toast.makeText(SpaceTimeAlarmActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
