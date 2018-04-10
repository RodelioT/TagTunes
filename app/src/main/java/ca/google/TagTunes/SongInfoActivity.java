package ca.google.TagTunes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SongInfoActivity extends AppCompatActivity {

    TextView tvSongInfo;
    EditText etComment;

    String songPath, songTitle, songArtist, songComment;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        // Initializing the dbHelper used for retrieving song information
        dbHelper = new DatabaseHelper(this);

        // Initializing views
        tvSongInfo = findViewById(R.id.tvSongInfo);
        etComment = findViewById(R.id.etComment);

        // Getting the song path passed through the intent
        songPath = getIntent().getStringExtra("songPath");

        // Getting the song data using the filePath, via dbHelper
        songTitle = dbHelper.getSong(songPath).get("Title");
        songArtist = dbHelper.getSong(songPath).get("Artist");
        songComment = dbHelper.getSong(songPath).get("Comment");

        // Sets the text to the song information
        tvSongInfo.setText(songArtist + " - " + songTitle);

        // Sets the comment text to the comment that was retrieved from the database
        etComment.setText(songComment);
    }

    @Override
    public void onBackPressed() {
        // Initializing the dbHelper used to update the song's comment in the database
        dbHelper = new DatabaseHelper(this);

        // Gets the current comment text in the View
        String updatedComment = etComment.getText().toString();

        //Updates the comment in the database with what was entered in the View
        dbHelper.updateComment(updatedComment, songPath);

        // Action performed when pressing the back button
        finish();
    }

    //TODO: Make two buttons, one to save, one to cancel, and then make the back button ask the user to save or not
}
