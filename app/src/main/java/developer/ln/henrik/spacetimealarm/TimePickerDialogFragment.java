package developer.ln.henrik.spacetimealarm;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TimePicker;

/**
 * Created by Henrik on 07/11/2017.
 */

public class TimePickerDialogFragment extends android.support.v4.app.DialogFragment {
    private int timeHour;
    private int timeMinute;
    private int type;
    private Handler handler;

    public TimePickerDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        handler = (Handler) bundle.getSerializable(getString(R.string.EXTRA_HANDLER));
        type = bundle.getInt(getString(R.string.EXTRA_TYPE));
        timeHour = bundle.getInt(getString(R.string.EXTRA_HOUR));
        timeMinute = bundle.getInt(getString(R.string.EXTRA_MIN));

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timeHour = hourOfDay;
                timeMinute = minute;
                Bundle b = new Bundle();
                b.putInt(getString(R.string.EXTRA_TYPE), type);
                b.putInt(getString(R.string.EXTRA_HOUR), timeHour);
                b.putInt(getString(R.string.EXTRA_MIN), timeMinute);
                Message msg = new Message();
                msg.setData(b);
                handler.sendMessage(msg);
            }
        };
        return new TimePickerDialog(getActivity(), listener, timeHour, timeMinute, false);
    }
}
