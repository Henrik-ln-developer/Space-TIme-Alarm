package developer.ln.henrik.spacetimealarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Henrik on 20/11/2017.
 */

public class NotificationReceiver extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d("SPACETIMEALARM", "Notification received");
        boolean done = getIntent().getBooleanExtra(getString(R.string.EXTRA_ALARM_DONE), false);
        SpaceTimeAlarm alarm = SpaceTimeAlarmManager.getAlarm(getIntent().getByteArrayExtra(getString(R.string.EXTRA_ALARM)));
        if(done)
        {
            alarm.setDone(true);
            Log.d("SPACETIMEALARM", "Updating alarm to done: " + alarm.getId());
            //DatabaseManager.getInstance(this).updateAlarm(alarm, this);
            DatabaseManager.getInstance(this).updateAlarm(alarm);
            finish();
        }
        else
        {
            Calendar newTime = Calendar.getInstance();
            newTime.setTimeInMillis(newTime.getTimeInMillis() + 1000*60*15);
            String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(newTime.getTime()));

            alarm.setStartTime(newTime.getTimeInMillis());
            Log.d("SPACETIMEALARM", "Postponing alarm: " + alarm.getId());
            Log.d("SPACESETALARM", "Alarm set to: " + timeString);
            //DatabaseManager.getInstance(this).updateAlarm(alarm, this);
            DatabaseManager.getInstance(this).updateAlarm(alarm);
            finish();
        }
    }
}
