package ca.google.TagTunes;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Switch swShowTags, swShuffle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Views
        swShowTags = findViewById(R.id.swShowTags);
        swShuffle = findViewById(R.id.swShuffle);

        // Load data from SharedPreferences
        sharedPreferences = getSharedPreferences("sharedPreferencesData", 0);
        swShowTags.setChecked(sharedPreferences.getBoolean("showSongTags", true));
        swShuffle.setChecked(sharedPreferences.getBoolean("shuffle", false));

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save data to SharedPreference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showSongTags", swShowTags.isChecked());
        editor.putBoolean("shuffle", swShuffle.isChecked());
        boolean success = editor.commit();

        // Displays a toast on the success of saving to SharedPreferences
        Toast.makeText(SettingsActivity.this,
                                success ? "Settings saved!" : "Error saving settings...",
                                Toast.LENGTH_SHORT).show();
    }
}
