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
    DatabaseHelper dbHelper;  // Will be needed later for writing to the database (also needs to be edited in dbHelper class)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        dbHelper = new DatabaseHelper(this);

        tvSongInfo = findViewById(R.id.tvSongInfo);
        etComment = findViewById(R.id.etComment);

        String songPath = getIntent().getStringExtra("songPath");
        String songTitle = getIntent().getStringExtra("songTitle");
        String songArtist = getIntent().getStringExtra("songArtist");

        tvSongInfo.setText(songArtist + " - " + songTitle);

        etComment.setText(dbHelper.getSongComment(songPath));
    }
}
