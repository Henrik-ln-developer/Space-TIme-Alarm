package developer.ln.henrik.spacetimealarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Henrik on 14/11/2017.
 */

public class SpaceTimeAlarmAdapter extends ArrayAdapter<SpaceTimeAlarm> {

    private ArrayList<SpaceTimeAlarm> dataSet;
    private Context context;
    private int lastPosition = -1;

    public SpaceTimeAlarmAdapter(ArrayList<SpaceTimeAlarm> data, Context context) {
        super(context, R.layout.space_time_alarm_row, data);
        this.dataSet = data;
        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SpaceTimeAlarm alarm = getItem(position);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.space_time_alarm_row, parent, false);

        TextView caption = (TextView) rowView.findViewById(R.id.textView_Caption);
        TextView locationName = (TextView) rowView.findViewById(R.id.textView_LocationName);
        TextView time = (TextView) rowView.findViewById(R.id.textView_Time);
        final CheckBox done = (CheckBox) rowView.findViewById(R.id.checkBox_Done);
        final ImageButton button_Delete = (ImageButton) rowView.findViewById(R.id.button_Delete);

        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        rowView.startAnimation(animation);
        lastPosition = position;

        if(alarm != null)
        {
            if(alarm.getCaption() != null)
            {
                caption.setText(alarm.getCaption());
            }
            if(alarm.getLocation_Name() != null)
            {
                locationName.setText(alarm.getLocation_Name());
            }
            if(alarm.getStartTime() != null)
            {
                Calendar startTime = Calendar.getInstance();
                startTime.setTimeInMillis(alarm.getStartTime());
                String timeString = (new SimpleDateFormat( "HH:mm" ).format(startTime.getTime()));
                if(alarm.getEndTime() != null)
                {
                    Calendar endTime = Calendar.getInstance();
                    endTime.setTimeInMillis(alarm.getEndTime());
                    timeString += " - " + (new SimpleDateFormat( "HH:mm" ).format(endTime.getTime()));
                }
                time.setText(timeString);
            }
            if(alarm.isDone() != null)
            {
                done.setChecked(alarm.isDone());
            }
            else
            {
                done.setChecked(false);
            }
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(done.isChecked())
                {
                    alarm.setDone(true);
                }
                else
                {
                    alarm.setDone(false);
                }
                DatabaseManager.getInstance(context).updateAlarm(alarm);
            }
        });
        done.setFocusable(false);
        button_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseManager.getInstance(context).deleteAlarm(alarm);
            }
        });
        button_Delete.setFocusable(false);

        return rowView;
    }


}
