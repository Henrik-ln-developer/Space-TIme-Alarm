package developer.ln.henrik.spacetimealarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

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
        boolean done = getIntent().getBooleanExtra(MainActivity.EXTRA_ALARM_DONE, false);
        SpaceTimeAlarm alarm = SpaceTimeAlarmManager.getAlarm(getIntent().getByteArrayExtra(MainActivity.EXTRA_ALARM));
        if(done)
        {
            alarm.setDone(true);
            Log.d("SPACETIMEALARM", "Updating alarm to done: " + alarm.toString());
            DatabaseManager.getInstance().updateAlarm(alarm, this);
        }
        else
        {
            Log.d("SPACETIMEALARM", "Postponing alarm: " + alarm.toString());
            Intent intent_BackToMain = new Intent(this, MainActivity.class);
            startActivity(intent_BackToMain);
        }

    }
}
