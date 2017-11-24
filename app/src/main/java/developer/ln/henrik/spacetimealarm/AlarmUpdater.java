package developer.ln.henrik.spacetimealarm;

import android.app.Activity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Henrik on 22/11/2017.
 */

public class AlarmUpdater {
    private Activity activity;
    private ListView listView_Alarms;
    private SpaceTimeAlarmAdapter alarmAdapter;
    private ArrayList<SpaceTimeAlarm> alarmArray;

    public AlarmUpdater(Activity activity, ListView listView_Alarms) {
        this.activity = activity;
        this.listView_Alarms = listView_Alarms;
        alarmArray = new ArrayList<>();
        alarmAdapter = new SpaceTimeAlarmAdapter(alarmArray, activity);
        listView_Alarms.setAdapter(alarmAdapter);
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

    public void addAlarm(SpaceTimeAlarm alarm)
    {
        if(alarm != null)
        {
            Log.d("SPACECHANGEDALARM", "Alarm Added to database: " + alarm.getId());
            alarmArray.add(alarm);
            alarmAdapter.notifyDataSetChanged();
        }
    }

    public void updateAlarm(SpaceTimeAlarm changedAlarm)
    {
        if(changedAlarm != null)
        {
            Log.d("SPACECHANGEDALARM", "Alarm Changed: Leder efter alarm med ID: " + changedAlarm.getId());
            for(SpaceTimeAlarm alarm : alarmArray)
            {
                if(alarm.getId() != null)
                {
                    Log.d("SPACECHANGEDALARM", "Alarm Changed: Checker alarm med ID: " + alarm.getId());
                    if(alarm.getId().equals(changedAlarm.getId()))
                    {
                        Log.d("SPACECHANGEDALARM", "Alarm Changed: Found alarm: " + alarm.getId());
                        alarm.setCaption(changedAlarm.getCaption());
                        alarm.setLocation_Id(changedAlarm.getLocation_Id());
                        alarm.setLocation_Name(changedAlarm.getLocation_Name());
                        alarm.setLocation_Lat(changedAlarm.getLocation_Lat());
                        alarm.setLocation_Lng(changedAlarm.getLocation_Lng());
                        alarm.setRadius(changedAlarm.getRadius());
                        alarm.setEndTime(changedAlarm.getEndTime());
                        alarm.setRequestCode(changedAlarm.getRequestCode());
                        alarm.setDone(changedAlarm.isDone());
                        if(alarm.getLocation_Lat() != null && alarm.getLocation_Lng() != null && alarm.getRadius() != null)
                        {
                            if(changedAlarm.getStartTime() > alarm.getStartTime())
                            {
                                alarm.setStartTime(changedAlarm.getStartTime());
                                SpaceTimeAlarmManager.getInstance().setPostponedAlarm(changedAlarm);
                            }
                            else
                            {
                                alarm.setStartTime(changedAlarm.getStartTime());
                                SpaceTimeAlarmManager.getInstance().setAlarm(changedAlarm);
                            }
                        }
                        else
                        {
                            alarm.setStartTime(changedAlarm.getStartTime());
                            SpaceTimeAlarmManager.getInstance().setAlarm(changedAlarm);
                        }
                        alarmAdapter.notifyDataSetChanged();
                        return;
                    }
                }
                else
                {
                    Log.d("SPACECHANGEDALARM", "Alarm Changed: Ingen ID på alarm");
                }
            }
            Log.d("SPACECHANGEDALARM", "Alarm changed in database, but hasn't been changed in list");
            Toast.makeText(activity.getApplicationContext(), "Alarm changed in database, but hasn't been changed in list", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeAlarm(SpaceTimeAlarm deletedAlarm)
    {
        if(deletedAlarm != null)
        {
            Log.d("SPACEREMOVEDALARM", "Child Removed: Leder efter alarm med ID: " + deletedAlarm.getId());
            for(SpaceTimeAlarm alarm : alarmArray)
            {
                if(alarm.getId() != null)
                {
                    Log.d("SPACEREMOVEDALARM", "Child Removed: Checker alarm med ID: " + alarm.getId());
                    if(alarm.getId().equals(deletedAlarm.getId()))
                    {
                        Log.d("SPACEREMOVEDALARM", "Child Removed: Remover alarm med ID: " + alarm.getId());
                        alarmArray.remove(alarm);
                        SpaceTimeAlarmManager.getInstance().removeAlarm(alarm);
                        alarmAdapter.notifyDataSetChanged();
                        return;
                    }
                }
                else
                {
                    Log.d("SPACEREMOVEDALARM", "Child Removed: Ingen ID på alarm");
                }
            }
            Log.d("SPACEREMOVEDALARM", "Alarm removed from database, but hasn't been removed from list");
            Toast.makeText(activity.getApplicationContext(), "Alarm removed from database, but hasn't been removed from list", Toast.LENGTH_SHORT).show();
        }

    }

    public void clearAlarms()
    {
        while(alarmArray.size() > 0)
        {
            removeAlarm(alarmArray.get(alarmArray.size() - 1));
        }
    }

    public void notifyForUpdate()
    {
        alarmAdapter.notifyDataSetChanged();
    }
}
