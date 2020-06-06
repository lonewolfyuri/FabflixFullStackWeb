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

public class MoviePageActivity extends Activity {
    private String id, url;
    private TextView title, dir_yr_rat;
    private ListView genreList, starList;
    private ArrayList<Genre> genres = null;
    private ArrayList<Star> stars = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.movie);
        genreList = findViewById(R.id.genre_list);
        starList = findViewById(R.id.star_list);
        title = findViewById(R.id.movie_title);
        dir_yr_rat = findViewById(R.id.director_year_rating);

        url = Parameters.url;

        //this should be retrieved from the database and the backend server
        id = getIntent().getStringExtra("movie_id");
        getMovie();
    }

    private void fillText(JSONObject parsed) throws JSONException {
        title.setText(parsed.getString("movieTitle"));
        dir_yr_rat.setText(parsed.getString("director") + " - " + parsed.getInt("year") + " - " + parsed.getDouble("rating") + " / 10");
    }

    private ArrayList<Genre> makeGenres(JSONObject parsed) throws JSONException {
        ArrayList<Genre> gens = new ArrayList<>();
        JSONArray json_gens = parsed.getJSONArray("genres");
        for (int ndx = 0; ndx < json_gens.length(); ndx++) {
            JSONObject curGen = json_gens.getJSONObject(ndx);
            gens.add(new Genre(curGen.getString("genre"), Integer.parseInt(curGen.getString("id"))));
        }
        return gens;
    }

    private ArrayList<Star> makeStars(JSONObject parsed) throws JSONException {
        ArrayList<Star> strs = new ArrayList<>();
        JSONArray json_strs = parsed.getJSONArray("stars");
        for (int ndx = 0; ndx < json_strs.length(); ndx++) {
            JSONObject curStr = json_strs.getJSONObject(ndx);
            strs.add(new Star(curStr.getString("star"), curStr.getString("id"), (short) -1));
        }
        return strs;
    }

    private void fillLists() {
        MoviePageStarAdapter starAdapt = new MoviePageStarAdapter(stars, this);
        starList.setAdapter(starAdapt);
        MoviePageGenreAdapter genAdapt = new MoviePageGenreAdapter(genres, this);
        genreList.setAdapter(genAdapt);

        starList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Star star = stars.get(position);
                Intent starPage = new Intent(MoviePageActivity.this, StarPageActivity.class);
                starPage.putExtra("star_id", star.getId());
                startActivity(starPage);
            }
        });

        genreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Genre genre = genres.get(position);
                Intent movieList = new Intent(MoviePageActivity.this, ListViewActivity.class);
                movieList.putExtra("list_type", "genre");
                movieList.putExtra("genre", genre.getName());
                movieList.putExtra("page", "0");
                startActivity(movieList);
            }
        });
    }

    private void getMovie() {
        MoviePageActivity ctx = this;
        String path = "singleMovie?id=" + id;

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest movieRequest = new StringRequest(Request.Method.GET, url + path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //TODO should parse the json response to redirect to appropriate functions.
                        try {
                            JSONObject parsed = new JSONObject(response);
                            fillText(parsed);
                            genres = makeGenres(parsed);
                            stars = makeStars(parsed);
                            Log.d("movie.success", response);
                        } catch (Exception e) {
                            genres = new ArrayList<>();
                            stars = new ArrayList<>();
                        }

                        ctx.fillLists();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("movie.error", error.toString());
                    }
                });

        queue.add(movieRequest);
    }
}
