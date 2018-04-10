package ca.google.TagTunes;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList; // ArrayList to hold all discovered music on the device
    private ListView songView; // ListView to display all the songs

    private MusicService musicSrv; // Represents the custom class we created
    private Intent playIntent; // The intent to play music within the MusicService class
    private boolean musicBound = false; // Flag to check if MainActivity is bound to MusicService

    private MusicController controller;

    private boolean paused, playbackPaused = false;

    private DatabaseHelper dbHelper; // Handles database operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Checks for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }
        }

        // Initialize the dbHelper
        dbHelper = new DatabaseHelper(this);

        songView = findViewById(R.id.song_list);

        // Instantiate the song ArrayList
        songList = new ArrayList<>();

        // Get all songs on the device
        getSongList();

        // Sorts the songList alphabetically
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        // Creates a new adapter (using our custom class)
        // and sets it on the ListView
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        // Sets up the music controls
        setController();

        // Sets an on-click event listener for each item in the ListView
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
                musicSrv.playSong();

                if(playbackPaused){
                    setController();
                    playbackPaused=false;
                }

                controller.show(0);
            }
        });

        // Sets an on-long-click event listener for each item in the ListView
        songView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Gets the selected song
                Song selectedSong= songList.get(position);

                //Starts a new intent to view the selected song details
                Intent songInfoIntent = new Intent(getBaseContext(), SongInfoActivity.class);
                songInfoIntent.putExtra("songPath", selectedSong.getPath());
                songInfoIntent.putExtra("songTitle", selectedSong.getTitle());
                songInfoIntent.putExtra("songArtist", selectedSong.getArtist());
                startActivity(songInfoIntent);

                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If the intent doesn't exist, create one, bind to it, and start it
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            // Gets the reference to the service so we can interact with it
            musicSrv = binder.getService();
            // Passes the songList
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                // Call the class that was created in MusicService.java
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to retrieve song metadata
    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        // If there's music stored on the device
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //Gets column information for song information
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int pathColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

            // Adds songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisPath));

                dbHelper.insertSong(thisPath, thisTitle, thisArtist, "no comment");
            } while (musicCursor.moveToNext());
        }
    }

    // Sets the controller up
    private void setController(){
        controller = new MusicController(this);

        // Setting the EventListeners for the 'previous song' and 'next song' buttons
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    // Plays the next song
    private void playNext(){
        // Calls the method that were made in MusicService.java
        musicSrv.playNext();

        if(playbackPaused){
            setController();
            playbackPaused = false;
        }

        controller.show(0);
    }

    // Plays the previous song
    private void playPrev(){
        // Calls the method that were made in MusicService.java
        musicSrv.playPrev();

        if(playbackPaused){
            setController();
            playbackPaused = false;
        }

        controller.show(0);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        // Return the current song length if music is playing, else return 0
        if((musicSrv != null) && musicBound && musicSrv.isPng()) {
            return musicSrv.getDur();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        // Return the current position if music is playing, else return 0
        if((musicSrv != null) && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        // Checks for certain parameters before checking if a song is playing
        if(musicSrv != null && musicBound) {
            return musicSrv.isPng();
        } else {
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}

// In-depth tutorial at => https://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764 (loading/displaying all audio files on device)
//          second part => https://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778 (music playback)
//           third part => https://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787 (refined music actions, music controls, and notification controls)