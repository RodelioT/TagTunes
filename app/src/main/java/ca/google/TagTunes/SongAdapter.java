package ca.google.TagTunes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

// Adapter to serve as a bridge between the audio files and the View
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    private Context context;
    private DatabaseHelper dbHelper;

    // Constructor
    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(c);

        this.context = c;
    }

    @Override
    public int getCount() {
        // Returns the size of the list
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Instantiates the dbHelper and assigns it a context
        dbHelper = new DatabaseHelper(context);

        //map to song layout
        LinearLayout songLayout = (LinearLayout)songInf.inflate(R.layout.song, parent, false);

        // Get title and artist views
        TextView songView = songLayout.findViewById(R.id.song_title);
        TextView artistView = songLayout.findViewById(R.id.song_artist);
        TextView pathView = songLayout.findViewById(R.id.song_path);

        // Get song using position
        Song currentSong = songs.get(position);

        // Sets the text for the views
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        pathView.setText(dbHelper.getSongComment(currentSong.getPath()));

        // Set index position as the tag
        songLayout.setTag(position);

        return songLayout;
    }
}
