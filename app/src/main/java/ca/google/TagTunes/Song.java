package ca.google.TagTunes;

// Represents a Song object
public class Song {
    // Parameters for a song
    private long id;
    private String title;
    private String artist;
    private String path;
    // You can add more song metadata here if you wish

    // Constructor method
    public Song(long songID, String songTitle, String songArtist, String songPath) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        path = songPath;
    }

    // GET methods for the parameters
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getPath(){return path;}
}
