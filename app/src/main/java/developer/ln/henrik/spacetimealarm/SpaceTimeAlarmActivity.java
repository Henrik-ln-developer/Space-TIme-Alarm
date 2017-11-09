package developer.ln.henrik.spacetimealarm;

import android.content.Intent;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.Calendar;


public class SpaceTimeAlarmActivity extends AppCompatActivity {

    private Place location;
    private Calendar startTime;
    private Calendar endTime;
    private String alarm_Id;

    private EditText editText_Caption;
    private TextView textView_LocationChoose;
    private TextView textView_StartTimeChoose;
    private TextView textView_EndTimeChoose;
    private Button button_Finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_time_alarm);

        editText_Caption = (EditText) findViewById(R.id.editText_Caption);
        textView_LocationChoose = (TextView) findViewById(R.id.textView_LocationChoose);
        textView_StartTimeChoose = (TextView) findViewById(R.id.textView_StartTimeChoose);
        textView_EndTimeChoose = (TextView) findViewById(R.id.textView_EndTimeChoose);
        button_Finish = (Button) findViewById(R.id.button_Finish);

        final boolean isEdit = getIntent().getBooleanExtra(MainActivity.EXTRA_EDIT, false);
        if(isEdit)
        {
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

                if(alarm.getLocation_Id() != null)
                {
                    GeoDataClient geoDataClient = Places.getGeoDataClient(this, null);
                    Task<PlaceBufferResponse> response = geoDataClient.getPlaceById(alarm.getLocation_Id());
                    response.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            PlaceBufferResponse places = task.getResult();
                            if(places.getCount() > 0)
                            {
                                location = places.get(0).freeze();
                                if(location != null)
                                {
                                    textView_LocationChoose.setText(location.toString());
                                }
                            }
                            else
                            {
                                Toast.makeText(SpaceTimeAlarmActivity.this, "Couldn't find alarm's location", Toast.LENGTH_SHORT).show();
                            }
                            places.release();
                        }
                    });
                }

                if(alarm.getStartTime() != null)
                {
                    startTime = Calendar.getInstance();
                    startTime.setTimeInMillis(alarm.getStartTime());
                    textView_StartTimeChoose.setText(startTime.getTime().toString());
                }

                if(alarm.getEndTime() != null)
                {
                    endTime = Calendar.getInstance();
                    endTime.setTimeInMillis(alarm.getEndTime());
                    textView_EndTimeChoose.setText(endTime.getTime().toString());
                }
            }
            else
            {
                Toast.makeText(SpaceTimeAlarmActivity.this, "Couldn't find alarm", Toast.LENGTH_SHORT).show();
            }

            button_Finish.setText("Update");
        }

        textView_LocationChoose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                if(location != null) {
                    LatLng northeast = new LatLng(location.getLatLng().latitude-MainActivity.ZOOM_VARIABLE, location.getLatLng().longitude-MainActivity.ZOOM_VARIABLE);
                    LatLng southwest = new LatLng(location.getLatLng().latitude+MainActivity.ZOOM_VARIABLE, location.getLatLng().longitude+MainActivity.ZOOM_VARIABLE);
                    LatLngBounds latLngBounds = new LatLngBounds(northeast, southwest);
                    builder.setLatLngBounds(latLngBounds);
                } else {

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
            }
        });
        textView_StartTimeChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.EXTRA_TYPE, MainActivity.REQUEST_CODE_START_TIME);
                bundle.putSerializable(MainActivity.EXTRA_HANDLER, new TimePickerHandler());
                if(startTime != null)
                {
                    bundle.putInt(MainActivity.EXTRA_HOUR, startTime.get(Calendar.HOUR_OF_DAY));
                    bundle.putInt(MainActivity.EXTRA_MIN, startTime.get(Calendar.MINUTE));
                }
                TimePickerDialogFragment fragment = new TimePickerDialogFragment();
                fragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(fragment, MainActivity.EXTRA_TIME_PICKER);
                transaction.commit();
            }
        });
        textView_EndTimeChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.EXTRA_TYPE, MainActivity.REQUEST_CODE_END_TIME);
                bundle.putSerializable(MainActivity.EXTRA_HANDLER, new TimePickerHandler());
                if(endTime != null)
                {
                    bundle.putInt(MainActivity.EXTRA_HOUR, endTime.get(Calendar.HOUR_OF_DAY));
                    bundle.putInt(MainActivity.EXTRA_MIN, endTime.get(Calendar.MINUTE));
                }
                TimePickerDialogFragment fragment = new TimePickerDialogFragment();
                fragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(fragment, MainActivity.EXTRA_TIME_PICKER);
                transaction.commit();
            }
        });
        button_Finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText_Caption.getText().toString().equals(""))
                {
                    Toast.makeText(SpaceTimeAlarmActivity.this, "An alarm needs a caption", Toast.LENGTH_SHORT).show();
                }
                else if(textView_LocationChoose.getText().toString().equals("Choose Location") && textView_StartTimeChoose.getText().toString().equals("Choose Start time"))
                {
                    Toast.makeText(SpaceTimeAlarmActivity.this, "An alarm needs a Location or a Start time", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent result_intent = new Intent();
                    result_intent.putExtra(MainActivity.EXTRA_EDIT, isEdit);
                    result_intent.putExtra(MainActivity.EXTRA_ALARM_ID, alarm_Id);
                    result_intent.putExtra(MainActivity.EXTRA_CAPTION, editText_Caption.getText().toString());
                    if(location != null)
                    {
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_ID, location.getId());
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_LAT, location.getLatLng().latitude);
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_LNG, location.getLatLng().latitude);
                        result_intent.putExtra(MainActivity.EXTRA_LOCATION_NAME, location.getAddress().toString());
                    }
                    long startTimeMillis = startTime == null ? 0 : startTime.getTimeInMillis();
                    result_intent.putExtra(MainActivity.EXTRA_START_TIME, startTimeMillis);
                    long endTimeMillis = endTime == null ? 0 : endTime.getTimeInMillis();
                    result_intent.putExtra(MainActivity.EXTRA_END_TIME, endTimeMillis);
                    setResult(RESULT_OK, result_intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MainActivity.REQUEST_CODE_LOCATION) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                location = place;
                if(location != null)
                {
                    textView_LocationChoose.setText(location.toString());
                }
            }
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
