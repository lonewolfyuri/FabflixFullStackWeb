package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MoviePageGenreAdapter extends ArrayAdapter<Genre> {
    private ArrayList<Genre> genres;

    public MoviePageGenreAdapter(ArrayList<Genre> genres, Context context) {
        super(context, R.layout.genre_row, genres);
        this.genres = genres;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.genre_row, parent, false);
        Genre genre = genres.get(position);

        TextView nameView = view.findViewById(R.id.genre_name);
        nameView.setText(genre.getName());
        return view;
    }
}
