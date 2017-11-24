package developer.ln.henrik.spacetimealarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    public SharedPreferences sharedPref;
    public int GEOFENCE_EXPIRATION_TIME;
    public String APPLICATION_ID;

    private Toolbar toolbar;
    private EditText editText_expirationTime;
    private Button saveButton;
    private EditText editText_ApplicationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText_expirationTime = (EditText) findViewById(R.id.editText_ExpirationTime);
        saveButton = (Button) findViewById(R.id.save_button);
        editText_ApplicationID = (EditText) findViewById(R.id.editText_ApplicationID);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        GEOFENCE_EXPIRATION_TIME = sharedPref.getInt(getString(R.string.GEOFENCE_EXPIRATION_TIME),Integer.parseInt(getString(R.string.expireDefaultDuration)));
        APPLICATION_ID = sharedPref.getString(getString(R.string.APPLICATION_ID),null);

        editText_expirationTime.setText(GEOFENCE_EXPIRATION_TIME+"");
        editText_ApplicationID.setText(APPLICATION_ID);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                int parseExpireTime = Integer.parseInt(editText_expirationTime.getText().toString());
                String newAppID = ""+ editText_ApplicationID.getText().toString();
                editor.putString(getString(R.string.APPLICATION_ID), newAppID);
                editor.putInt("GEOFENCE_EXPIRATION_TIME", parseExpireTime);
                editor.commit();
                Intent result_intent = new Intent();
                result_intent.putExtra(getString(R.string.EXTRA_APPLICATION_ID), APPLICATION_ID);
                setResult(RESULT_OK, result_intent);
                finish();
            }
        });
    }

    public Intent getSupportParentActivityIntent() {
        Intent result_intent = new Intent();
        result_intent.putExtra(getString(R.string.EXTRA_APPLICATION_ID), APPLICATION_ID);
        setResult(RESULT_CANCELED, result_intent);
        finish();
        return null;
    }

}
