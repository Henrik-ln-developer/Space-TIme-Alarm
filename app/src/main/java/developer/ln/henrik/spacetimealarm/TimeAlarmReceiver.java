package developer.ln.henrik.spacetimealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
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
            if(alarm.getStartTime() != null)
            {
                Calendar currentTime = Calendar.getInstance();
                if(alarm.getEndTime() == null)
                {
                    if(currentTime.getTimeInMillis() > alarm.getStartTime())
                    {
                        Log.d("SPACETIMEALARM", "StartTime happened");
                        NotificationSender.sendNotification(context, "Time Alarm" , alarm.getCaption(), alarm);
                    }
                    else
                    {
                        Log.d("SPACETIMEALARM", "StartTime hasn't happened");
                        posponeAlarm(intent, alarm);
                    }
                }
                else
                {
                    if(currentTime.getTimeInMillis() > alarm.getStartTime() && currentTime.getTimeInMillis() < alarm.getEndTime())
                    {
                        Log.d("SPACETIMEALARM", "Within time interval");
                        NotificationSender.sendNotification(context, "Time Alarm", alarm.getCaption(), alarm);
                    }
                    else
                    {
                        Log.d("TIMEALARM", "Not within time interval");
                        posponeAlarm(intent, alarm);
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

    private void posponeAlarm(Intent intent, SpaceTimeAlarm alarm)
    {
        Log.d("SPACETIMEALARM", "Posponing alarm");
    }
}
