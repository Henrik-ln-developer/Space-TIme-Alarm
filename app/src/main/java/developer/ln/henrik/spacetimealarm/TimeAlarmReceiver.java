package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by Henrik on 07/11/2017.
 */

public class TimeAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SPACETIMEALARM", "Received TimeAlarm");
        SpaceTimeAlarm alarm = SpaceTimeAlarmManager.getAlarm(intent.getByteArrayExtra(MainActivity.EXTRA_ALARM));
        if(alarm != null)
        {
            Log.d("SPACETIMEALARM", "Is for alarm: " + alarm.getId());
            if(alarm.getStartTime() != null)
            {
                Calendar currentTime = Calendar.getInstance();
                if(alarm.getEndTime() == null)
                {
                    if(currentTime.getTimeInMillis() > alarm.getStartTime())
                    {
                        Log.d("SPACETIMEALARM", "StartTime happened");
                        NotificationSender.getInstance(context).sendNotification("Time Alarm" , alarm.getCaption(), alarm);
                    }
                    else
                    {
                        Log.d("SPACETIMEALARM", "StartTime hasn't happened");
                        Calendar newTime = Calendar.getInstance();
                        newTime.setTimeInMillis(newTime.getTimeInMillis() + 1000*60*15);
                        String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(newTime.getTime()));

                        alarm.setStartTime(newTime.getTimeInMillis());
                        Log.d("SPACETIMEALARM", "Postponing alarm: " + alarm.getId());
                        Log.d("SPACESETALARM", "Alarm set to: " + timeString);
                        DatabaseManager.getInstance(context).updateAlarm(alarm);
                    }
                }
                else
                {
                    if(currentTime.getTimeInMillis() > alarm.getStartTime() && currentTime.getTimeInMillis() < alarm.getEndTime())
                    {
                        Log.d("SPACETIMEALARM", "Within time interval");
                        NotificationSender.getInstance(context).sendNotification("Time Alarm" , alarm.getCaption(), alarm);
                    }
                    else
                    {
                        Log.d("SPACETIMEALARM", "Not Within time interval");
                        Calendar newTime = Calendar.getInstance();
                        newTime.setTimeInMillis(newTime.getTimeInMillis() + 1000*60*15);
                        String timeString = (new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" ).format(newTime.getTime()));

                        alarm.setStartTime(newTime.getTimeInMillis());
                        Log.d("SPACETIMEALARM", "Postponing alarm: " + alarm.getId());
                        Log.d("SPACESETALARM", "Alarm set to: " + timeString);
                        DatabaseManager.getInstance(context).updateAlarm(alarm);
                    }
                }
            }
            else
            {
                Log.d("SPACETIMEALARM", "StartTime is null");
            }
        }
        else
        {
            Log.d("SPACETIMEALARM", "Alarm is null");
        }
    }

}
