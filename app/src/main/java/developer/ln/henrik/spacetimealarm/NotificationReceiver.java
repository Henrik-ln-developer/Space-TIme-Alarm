package developer.ln.henrik.spacetimealarm;

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
        ByteArrayInputStream bis = new ByteArrayInputStream(getIntent().getByteArrayExtra(MainActivity.EXTRA_ALARM));
        ObjectInput in = null;
        SpaceTimeAlarm alarm = null;
        try
        {
            in = new ObjectInputStream(bis);
            alarm = (SpaceTimeAlarm)in.readObject();
            alarm.setDone(true);
            Log.d("SPACETIMEALARM", "Notification's alarm updating to done");
            DatabaseManager.getInstance().updateAlarm(alarm, this);
        }
        catch (ClassNotFoundException e)
        {
            Log.d("SPACETIMEALARM", "InputStream threw an ClassNotFoundException");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Log.d("SPACETIMEALARM", "InputStream threw an IOException");
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                Log.d("SPACETIMEALARM", "Failed to shutdown InputStream");
                ex.printStackTrace();
            }
        }
    }
}
