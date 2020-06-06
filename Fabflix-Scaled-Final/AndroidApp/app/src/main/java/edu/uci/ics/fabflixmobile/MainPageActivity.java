package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MainPageActivity extends Activity {
    private EditText search_input;
    private Button search;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        search_input = findViewById(R.id.search_input);
        search = findViewById(R.id.search);

        url = Parameters.url;

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
    }

    public void search() {
        Intent movieList = new Intent(MainPageActivity.this, ListViewActivity.class);
        movieList.putExtra("list_type", "search");
        movieList.putExtra("search_input", search_input.getText().toString());
        movieList.putExtra("page", "0");
        startActivity(movieList);
    }
}
