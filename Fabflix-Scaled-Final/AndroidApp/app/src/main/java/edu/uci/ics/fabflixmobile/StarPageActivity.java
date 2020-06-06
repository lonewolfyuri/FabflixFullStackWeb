package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarPageActivity extends Activity {
    private String id, url;
    private TextView name, year;
    private ListView movieList;
    private ArrayList<Movie> movies = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.star);
        movieList = findViewById(R.id.movie_list);
        name = findViewById(R.id.star_name);
        year = findViewById(R.id.birth_year);

        url = Parameters.url;

        //this should be retrieved from the database and the backend server
        id = getIntent().getStringExtra("star_id");
        getStar();
    }

    private void fillText(JSONObject parsed) throws JSONException {
        name.setText(parsed.getString("name"));
        year.setText(parsed.getString("birth"));
    }

    private ArrayList<Movie> makeMovies(JSONObject parsed) throws JSONException {
        ArrayList<Movie> movs = new ArrayList<>();
        JSONArray json_movs = parsed.getJSONArray("movies");
        for (int ndx = 0; ndx < json_movs.length(); ndx++) {
            JSONObject curMov = json_movs.getJSONObject(ndx);
            movs.add(new Movie(curMov.getString("title"), curMov.getString("id"), "",(short) -1, 0.0));
        }
        return movs;
    }

    private void fillList() {
        StarPageMovieAdapter movAdapt = new StarPageMovieAdapter(movies, this);
        movieList.setAdapter(movAdapt);

        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                Intent moviePage = new Intent(StarPageActivity.this, MoviePageActivity.class);
                moviePage.putExtra("movie_id", movie.getId());
                startActivity(moviePage);
            }
        });
    }

    private void getStar() {
        StarPageActivity ctx = this;
        String path = "singleStar?id=" + id;

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest starRequest = new StringRequest(Request.Method.GET, url + path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //TODO should parse the json response to redirect to appropriate functions.
                        try {
                            JSONObject parsed = new JSONObject(response);
                            fillText(parsed);
                            movies = makeMovies(parsed);
                            Log.d("movie.success", response);
                        } catch (Exception e) {
                            movies = new ArrayList<>();
                        }

                        ctx.fillList();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("movie.error", error.toString());
                    }
                });

        queue.add(starRequest);
    }
}
