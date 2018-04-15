package ca.google.TagTunes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

public class SongInfoActivity extends AppCompatActivity {

    TextView tvSongInfo;
    EditText etTags;

    String songPath, songTitle, songArtist;
    String songTags = "";
    String[] tagList;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        // Initializing the dbHelper used for retrieving song information
        dbHelper = new DatabaseHelper(this);

        // Initializing views
        tvSongInfo = findViewById(R.id.tvSongInfo);
        etTags = findViewById(R.id.etTags);

        // Getting the song path passed through the intent
        songPath = getIntent().getStringExtra("songPath");

        // Getting the song data using the filePath, via dbHelper
        songTitle = dbHelper.getSong(songPath).get("Title");
        songArtist = dbHelper.getSong(songPath).get("Artist");

        //Getting all the tags, separating them with a space
        Object[] tagArray = dbHelper.getTags(songPath).get(0).values().toArray();
        tagList = Arrays.copyOf(tagArray, tagArray.length, String[].class);
        for (String tag : tagList) {
            // Tags should use underscores with spaces. Does a replace if they have spaces
            tag = tag.replace(" ", "_");
            songTags += tag + " ";
        }

        // Sets the text to the song information
        tvSongInfo.setText(songArtist + " - " + songTitle);

        // Lists all the tags in the tags editText (tags separated with spaces)
        etTags.setText(songTags);
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

        // Action performed when pressing the back button
        finish();
    }

    //TODO: Make two buttons, one to save, one to cancel, and then make the back button ask the user to save or not
}
