package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class StarPageMovieAdapter extends ArrayAdapter<Movie> {
    private ArrayList<Movie> movies;

    public StarPageMovieAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.movie_row, movies);
        this.movies = movies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.movie_row, parent, false);
        Movie movie = movies.get(position);

        TextView titleView = view.findViewById(R.id.movie_list_title);
        titleView.setText(movie.getTitle());
        return view;
    }
}
