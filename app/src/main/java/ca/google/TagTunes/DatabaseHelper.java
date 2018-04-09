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
    private static final int DB_VERSION = 3;

    // Database Name
    private static final String DB_NAME = "SongDatabase";

    // Table Name
    private static final String TABLE_NAME = "Songs";

    // Constants for Column Names
    private static final String COL_FILEPATH = "FilePath";
    private static final String COL_TITLE = "Title";
    private static final String COL_ARTIST = "Artist";
    private static final String COL_COMMENT = "Comment";

    // Defining the create statement
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                                                COL_FILEPATH + " TEXT UNIQUE NOT NULL, " +
                                                COL_TITLE + " TEXT NOT NULL, " +
                                                COL_ARTIST + " TEXT, " +
                                                COL_COMMENT + " TEXT);";

    // Defining the destroy statement
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the Create statement
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop the database and re-create it
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    // Inserts a song into the database using ContentValues
    public void insertSong(String filePath,String title, String artist, String comment) {
        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create an instance of a writable Databse
        //   ContentValues class is used to store sets of values that are easier to process
        ContentValues rowValues = new ContentValues();

        // Add values to the ContentValues
        rowValues.put(COL_FILEPATH, filePath);
        rowValues.put(COL_TITLE, title);
        rowValues.put(COL_ARTIST, artist);
        rowValues.put(COL_COMMENT, comment);


        // Insert the values into the table
        db.insert(TABLE_NAME, null, rowValues);

        // Close the database
        db.close();
    }

    // This method is used to load the data from the table into a hash map
    //   This enables the use of multiple textviews in the listview
    public List<Map<String,String>> loadData()
    {
        List<Map<String,String>> lm = new ArrayList<>();

        // Open the readable database
        SQLiteDatabase db = this.getReadableDatabase();
        // Create an array of the column names
        String[] selection = {COL_FILEPATH, COL_TITLE, COL_ARTIST, COL_COMMENT};
        // Create a cursor item for querying the database
        Cursor c = db.query(TABLE_NAME,	//The name of the table to query
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
            map.put("Comment", c.getString(3));
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

}
