package developer.ln.henrik.spacetimealarm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Henrik on 20/11/2017.
 */

public class NotificationReceiver extends IntentService
{

    public NotificationReceiver() {
        super("NotificationReceiver");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ByteArrayInputStream bis = new ByteArrayInputStream(intent.getByteArrayExtra(MainActivity.EXTRA_ALARM));
        ObjectInput in = null;
        SpaceTimeAlarm alarm = null;
        try
        {
            in = new ObjectInputStream(bis);
            alarm = (SpaceTimeAlarm)in.readObject();
            alarm.setDone(true);
            DatabaseManager.getInstance().updateAlarm(alarm);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
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
                ex.printStackTrace();
            }
        }
    }
}
