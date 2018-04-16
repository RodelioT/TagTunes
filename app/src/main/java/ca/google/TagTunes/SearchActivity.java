package ca.google.TagTunes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    private Spinner spSearchStyle;
    private EditText etSearchTags;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Preparing the search style spinner
        spSearchStyle = findViewById(R.id.spSearchStyle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.SearchStyles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSearchStyle.setAdapter(adapter);

        // Initializing the other views
        etSearchTags = findViewById(R.id.etSearchTags);
        btnSearch = findViewById(R.id.btnSearch);
    }
}
