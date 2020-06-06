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

import static java.lang.Math.min;

public class ListViewActivity extends Activity {
    private String search_input = null, url = null, list_type = null, genre = null;
    private int page;
    private ListView results;
    private ArrayList<Movie> movies = null;
    private Button prev, next;
    private TextView pageView;
    private MovieListViewAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listview);
        results = findViewById(R.id.results);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        pageView = findViewById(R.id.page);

        url = Parameters.url;

        //this should be retrieved from the database and the backend server
        list_type = getIntent().getStringExtra("list_type");
        page = Integer.parseInt(getIntent().getStringExtra("page"));
        if (list_type.equals("search")) search_input = getIntent().getStringExtra("search_input");
        else if (list_type.equals("genre")) genre = getIntent().getStringExtra("genre");

        getMovies(false);
    }

    private void goPrev() {
        page -= 1;
        getMovies(true);
    }

    private void goNext() {
        page += 1;
        getMovies(true);
    }

    private ArrayList<Movie> makeMovies(JSONObject response) throws JSONException {
        movies = new ArrayList<>();
        JSONArray json_movies = response.getJSONArray("movies");
        for (int ndx = 0; ndx < json_movies.length(); ndx++) {
            JSONObject curMov = json_movies.getJSONObject(ndx);

            List<Genre> genres = new ArrayList<>();
            String[] names = curMov.getString("genres").split(",");
            String[] ids = curMov.getString("genreIds").split(",");
            for (int ndxGenre = 0; ndxGenre < min(names.length, 3); ndxGenre++) genres.add(new Genre(names[ndxGenre], Integer.parseInt(ids[ndxGenre])));

            List<Star> stars = new ArrayList<>();
            names = curMov.getString("starNames").split(",");
            ids = curMov.getString("starIds").split(",");
            for (int ndxStar = 0; ndxStar < min(names.length, 3); ndxStar++) stars.add(new Star(names[ndxStar], ids[ndxStar], (short) -1));

            movies.add(new Movie(curMov.getString("movieTitle"), curMov.getString("movieId"), curMov.getString("director"), (short) curMov.getInt("year"), curMov.getDouble("rating"), genres, stars));
        }
        return movies;
    }

    private void fillMovies() {
        adapter = new MovieListViewAdapter(movies, this);

        results.setAdapter(adapter);

        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                Intent moviePage = new Intent(ListViewActivity.this, MoviePageActivity.class);
                moviePage.putExtra("movie_id", movie.getId());
                startActivity(moviePage);
            }
        });

        pageView.setText("          Page: " + (page + 1) + "         ");

        if (page > 0) {
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goPrev();
                }
            });
        }

        if (movies.size() >= 20) {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goNext();
                }
            });
        }
    }

    private void updateMovies() {
        adapter = new MovieListViewAdapter(movies, this);

        results.setAdapter(adapter);

        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                Intent moviePage = new Intent(ListViewActivity.this, MoviePageActivity.class);
                moviePage.putExtra("movie_id", movie.getId());
                startActivity(moviePage);
            }
        });

        pageView.setText("          Page: " + (page + 1) + "         ");

        if (page > 0) {
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goPrev();
                }
            });
        } else prev.setOnClickListener(null);

        if (movies.size() >= 20) {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goNext();
                }
            });
        } else next.setOnClickListener(null);
    }

    private void getMovies(boolean refill) {
        ListViewActivity ctx = this;
        String path = "movies?limit=20&page=" + page;
        if (list_type.equals("search")) path += "&search_input=" + search_input;
        else if (list_type.equals("genre")) path += "&genre=" + genre;

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest searchRequest = new StringRequest(Request.Method.GET, url + path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //TODO should parse the json response to redirect to appropriate functions.
                        try {
                            movies = makeMovies(new JSONObject(response));
                            Log.d("search.success", response);
                        } catch (Exception e) {
                            movies = new ArrayList<>();
                        }

                        if (refill) ctx.updateMovies();
                        else ctx.fillMovies();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        movies = new ArrayList<>();
                        Log.d("search.error", error.toString());
                    }
        });

        queue.add(searchRequest);
    }
}