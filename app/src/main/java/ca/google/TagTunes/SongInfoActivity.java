package ca.google.TagTunes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class SongInfoActivity extends AppCompatActivity {

    private TextView tvSongInfo;
    private EditText etTags;
    private Button btnOnlineSearch;

    // Used to check if an internet connection is established
    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;

    private String songPath, songTitle, songArtist;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        // Initializing the dbHelper used for retrieving song information
        dbHelper = new DatabaseHelper(this);

        // Initializing views
        tvSongInfo = findViewById(R.id.tvSongInfo);
        etTags = findViewById(R.id.etTags);
        btnOnlineSearch = findViewById(R.id.btnOnlineSearch);


        // Getting the song path passed through the intent
        songPath = getIntent().getStringExtra("songPath");

        // Getting the song data using the filePath, via dbHelper
        songTitle = dbHelper.getSong(songPath).get("Title");
        songArtist = dbHelper.getSong(songPath).get("Artist");

        //Getting all the tags, separating them with a space
        String tagList = "";
        ArrayList<String> tagArray = dbHelper.getTags(songPath);
        for (String tag : tagArray) {
            tagList += tag.replace(" ", "_") + " ";
        }

        // Sets the text to the song information
        tvSongInfo.setText(songArtist + " - " + songTitle);

        // Lists all the tags in the tags editText (tags separated with spaces)
        etTags.setText(tagList);

        // Sets up an event listener for the Button
        btnOnlineSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(SongInfoActivity.this, songArtist + " - " + songTitle, Toast.LENGTH_SHORT).show();

                if(internetConnected()) {
                    Toast.makeText(SongInfoActivity.this, songArtist + " - " + songTitle, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Initializing the dbHelper used to update the song's comment in the database
        dbHelper = new DatabaseHelper(this);

        // Gets the list of tags currently in the EditText, splits them by space,
        //   and puts them all into an array
        String[] tagArray = etTags.getText().toString().split(" ");

        for (String tag : tagArray) {
            dbHelper.insertTag(tag, songPath);
        }

        // Removes any unused tags in the SongTags table
        dbHelper.removeOldTags(tagArray, songPath);

        // Action performed when pressing the back button
        finish();
    }

    // Checks to see if an internet connection is established.
    public boolean internetConnected() {
        boolean connection = false;

        //For checking to see if a connection is established
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        // Checks to see if an internet connection is established before connecting online
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            connection = true;
        } else {
            Toast.makeText(SongInfoActivity.this, "No internet connection.\nPlease check your connection settings.", Toast.LENGTH_SHORT).show();
        }

        return connection;
    }

}
