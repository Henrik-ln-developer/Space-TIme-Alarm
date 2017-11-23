package developer.ln.henrik.spacetimealarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    public SharedPreferences sharedPref;
    public int GEOFENCE_EXPIRATION_TIME;
    public String APPLICATION_ID;

    private Toolbar toolbar;
    private EditText expireTime;
    private Button saveButton;
    private EditText appID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        expireTime = (EditText) findViewById(R.id.expireDateField);
        saveButton = (Button) findViewById(R.id.save_button);
        appID = (EditText) findViewById(R.id.appIDField);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        GEOFENCE_EXPIRATION_TIME = sharedPref.getInt(getString(R.string.GEOFENCE_EXPIRATION_TIME),Integer.parseInt(getString(R.string.expireDefaultDuration)));
        APPLICATION_ID = sharedPref.getString(getString(R.string.APPLICATION_ID),null);

        expireTime.setText(GEOFENCE_EXPIRATION_TIME+"");
        appID.setText(APPLICATION_ID);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                int parseExpireTime = Integer.parseInt(expireTime.getText().toString());
                String newAppID = ""+ appID.getText().toString();
                editor.putString(getString(R.string.APPLICATION_ID), newAppID);
                editor.putInt("GEOFENCE_EXPIRATION_TIME", parseExpireTime);
                editor.commit();
                Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

}
