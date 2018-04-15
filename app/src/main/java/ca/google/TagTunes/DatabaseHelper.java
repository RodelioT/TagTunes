package ca.google.TagTunes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DB_VERSION = 10;

    // Database Name
    private static final String DB_NAME = "SongDatabase";

    // Table Names
    private static final String TABLE_NAME_SONGS = "Songs";
    private static final String TABLE_NAME_SONGTAGS = "SongTags";
    private static final String TABLE_NAME_TAGS = "Tags";

    // Constants for Column Names
    private static final String COL_SONGS_FILEPATH = "FilePath";
    private static final String COL_SONGS_TITLE = "Title";
    private static final String COL_SONGS_ARTIST = "Artist";
    private static final String COL_SONGTAGS_FILEPATH = "FilePath";
    private static final String COL_SONGTAGS_NAME = "Name";
    private static final String COL_TAGS_NAME = "Name";



    // Defining the create statement for Songs
    private static final String TABLE_SONGS_CREATE = "CREATE TABLE " + TABLE_NAME_SONGS + " (" +
            COL_SONGS_FILEPATH + " TEXT UNIQUE NOT NULL, " +
            COL_SONGS_TITLE + " TEXT NOT NULL, " +
            COL_SONGS_ARTIST + " TEXT);";

    // Defining the create statement for SongTags
    private static final String TABLE_SONGTAGS_CREATE = "CREATE TABLE " + TABLE_NAME_SONGTAGS + " (" +
            COL_SONGTAGS_FILEPATH + " TEXT NOT NULL, " +
            COL_SONGTAGS_NAME + " TEXT NOT NULL, " +
            "PRIMARY KEY(" + COL_SONGTAGS_FILEPATH + ", " + COL_SONGTAGS_NAME + "));";

    // Defining the create statement for Tags
    private static final String TABLE_TAGS_CREATE = "CREATE TABLE " + TABLE_NAME_TAGS + " (" +
            COL_TAGS_NAME + " TEXT UNIQUE NOT NULL);";



    // Defining the destroy statement for Songs
    private static final String DROP_SONGS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_SONGS;

    // Defining the destroy statement for SongTags
    private static final String DROP_SONGTAGS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_SONGTAGS;

    // Defining the destroy statement for Tags
    private static final String DROP_TAGS_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_TAGS;




    // Constructor for the Database helper Class
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(TABLE_SONGS_CREATE);
        db.execSQL(TABLE_TAGS_CREATE);
        db.execSQL(TABLE_SONGTAGS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop all tables, and cre-create them
        db.execSQL(DROP_SONGS_TABLE);
        db.execSQL(DROP_SONGTAGS_TABLE);
        db.execSQL(DROP_TAGS_TABLE);
        onCreate(db);
    }

    // Inserts a song into the Songs table using ContentValues
    public void insertSong(String filePath,String title, String artist) {
        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // ContentValues class is used to store sets of values that are easier to process
        // Two is created for each Song and SongTags tables
        ContentValues songRowValues = new ContentValues();
        ContentValues songtagRowValues = new ContentValues();

        // Add values to the ContentValues
        songRowValues.put(COL_SONGS_FILEPATH, filePath);
        songRowValues.put(COL_SONGS_TITLE, title);
        songRowValues.put(COL_SONGS_ARTIST, artist);

        // If there's no errors inserting the song (i.e. song doesn't already exist)
        if(db.insert(TABLE_NAME_SONGS, null, songRowValues) != -1) {
            // Insert an initial tag_me tag for the song
            songtagRowValues.put(COL_SONGTAGS_FILEPATH, filePath);
            songtagRowValues.put(COL_SONGTAGS_NAME, "tag_me");
            db.insert(TABLE_NAME_SONGTAGS, null, songtagRowValues);
        }

        // Close the database
        db.close();
    }

    // Inserts a tag into the Tags table (if it doesn't exist) and
    //   associates a song to the tag through the SongTags join table
    public void insertTag(String tag, String filePath) {
        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();


        // Using two ContentValues; each for inserting into Tags and SongTags tables
        ContentValues tagsRowValues = new ContentValues();
        ContentValues songtagsRowValues = new ContentValues();

        // Add values to the ContentValues
        tagsRowValues.put(COL_TAGS_NAME, tag);
        songtagsRowValues.put(COL_SONGTAGS_FILEPATH, filePath);
        songtagsRowValues.put(COL_SONGTAGS_NAME, tag);


        // Inserts the values into the Tags and SongTags table
        db.insert(TABLE_NAME_TAGS, null, tagsRowValues);
        db.insert(TABLE_NAME_SONGTAGS, null, songtagsRowValues);

        // Close the database
        db.close();
    }

    // Removes a song's tags that are currently not being used
    public void removeOldTags(String[] tags, String filePath) {

        // Start creating the WHERE clause
        String whereClause = "FilePath = '" + filePath + "' AND Name NOT IN(";
        // Surrounds all the tags with quotes, and add commas and spaces between them,
        //   and adds them to the NOT IN portion of the WHERE clause
        for(String tag : tags) {
            whereClause += "'" + tag + "', ";
        }
        // Removes the last set of ", " and places the closing bracket for the NOT IN clause
        whereClause = whereClause.substring(0, whereClause.length() - 2) + ")";

        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();

//        // Selects all the tag names where the song (filepath) matches the song given
//        db.execSQL("DELETE FROM SongTags " +
//                        "WHERE FilePath = '" + filePath + "' " +
//                        "AND Name NOT IN(" + formattedTags + ");");

        db.delete(TABLE_NAME_SONGTAGS, whereClause, null);

        // Close the database
        db.close();
    }

    // Removes a tag from a song TODO: and delete the tag if it is no longer being used
    public void removeTag(String name, String filePath) {
        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_NAME_SONGTAGS +
                    " WHERE " + COL_SONGTAGS_FILEPATH + " = '" + filePath +
                    "' AND " + COL_SONGTAGS_NAME + " = '" + name + "';");

        // Close the database
        db.close();
    }



    // This method is used to load the data from the table into a hash map
    //   This enables the use of multiple TextViews in the ListView
    public List<Map<String,String>> loadData()
    {
        List<Map<String,String>> lm = new ArrayList<>();

        // Open the readable database
        SQLiteDatabase db = this.getReadableDatabase();
        // Create an array of the column names
        String[] selection = {COL_SONGS_FILEPATH, COL_SONGS_TITLE, COL_SONGS_ARTIST};
        // Create a cursor item for querying the database
        Cursor c = db.query(TABLE_NAME_SONGS,	//The name of the table to query
                selection,				//The columns to return
                null,			//The columns for the where clause
                null,		//The values for the where clause
                null,			//Group the rows
                null,			//Filter the row groups
                null);			//The sort order

        // Move to the first row
        c.moveToFirst();

        // For each row that was retrieved
        for(int i=0; i < c.getCount(); i++)
        {
            Map<String,String> map = new HashMap<>();

            // Assign the value to the corresponding array
            map.put("FilePath", c.getString(0));
            map.put("Title", c.getString(1));
            map.put("Artist", c.getString(2));
            // map.put("Age", String.valueOf(c.getInt(#)));  //For integer values

            lm.add(map);
            c.moveToNext();
        }

        // Close the cursor
        c.close();

        // Close the database
        db.close();

        return lm;
    }



    // This method is used to fetch a song using its unique filePath
    public Map<String,String> getSong(String filePath)
    {
        List<Map<String,String>> lm = new ArrayList<>();

        // Open the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        String[] selectClause = {COL_SONGS_FILEPATH, COL_SONGS_TITLE, COL_SONGS_ARTIST}; // Create an array of the column names
        String[] whereClause = {filePath};                                          // Create a String array for the where clause


        // Create a cursor item for querying the database
        Cursor c = db.query(TABLE_NAME_SONGS,	//The name of the table to query
                selectClause,			//The columns to return
                "FilePath=?",	//The columns for the where clause
                whereClause,		    //The values for the where clause
                null,			//Group the rows
                null,			//Filter the row groups
                null);			//The sort order


        // Make sure it returned at least 1 row before doing operations on the result
        if(c.getCount() > 0){
            // Move to the first row
            c.moveToFirst();


            Map<String,String> map = new HashMap<>();

            // Assign the value to the corresponding array
                map.put("FilePath", c.getString(0));
                map.put("Title", c.getString(1));
                map.put("Artist", c.getString(2));
                // map.put("Age", String.valueOf(c.getInt(#)));  //For integer values

            lm.add(map);
            c.moveToNext();
        }

        // Close the cursor
        c.close();

        // Close the database
        db.close();

        return lm.get(0);
    }

    // This method is used to return all tags associated with a song(its filepath)
    public ArrayList<String> getTags(String filepath)
    {
        ArrayList<String> tagList = new ArrayList<>();

        // Open the readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Selects all the tag names where the song (filepath) matches the song given
        Cursor c = db.rawQuery("SELECT Name " +
                                    "FROM SongTags " +
                                    "WHERE FilePath = '" + filepath + "';", null);


        // Make sure it returned at least 1 row before doing operations on the result
        if(c.getCount() > 0) {
            // Move to the first row
            c.moveToFirst();

            // For each row that was retrieved
            for(int i=0; i < c.getCount(); i++)
            {
                String tag = c.getString(0);
                tagList.add(tag);

                c.moveToNext();
            }
        }

        // Close the cursor
        c.close();

        // Close the database
        db.close();

        return tagList;
    }
}
