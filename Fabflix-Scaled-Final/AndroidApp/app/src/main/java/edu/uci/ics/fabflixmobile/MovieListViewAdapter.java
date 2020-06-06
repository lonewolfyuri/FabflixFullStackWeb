package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie> {
    private ArrayList<Movie> movies;

    public MovieListViewAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.result_row, movies);
        this.movies = movies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.result_row, parent, false);

        Movie movie = movies.get(position);

        TextView titleView = view.findViewById(R.id.title);
        TextView dirYrRatView = view.findViewById(R.id.director_year_rating);
        TextView genreView = view.findViewById(R.id.genres);
        TextView starView = view.findViewById(R.id.stars);

        titleView.setText(movie.getTitle());
        dirYrRatView.setText(movie.getDirector() + "   " + movie.getYear() + "   " + movie.getRating() + " / 10");// need to cast the year to a string to set the label
        genreView.setText(movie.gensToString());
        starView.setText(movie.starsToString());

        return view;
    }
}