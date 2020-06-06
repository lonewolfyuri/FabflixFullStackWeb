package edu.uci.ics.fabflixmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MoviePageStarAdapter extends ArrayAdapter<Star> {
    private ArrayList<Star> stars;

    public MoviePageStarAdapter(ArrayList<Star> stars, Context context) {
        super(context, R.layout.star_row, stars);
        this.stars = stars;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.star_row, parent, false);
        Star star = stars.get(position);

        TextView nameView = view.findViewById(R.id.star_name);
        nameView.setText(star.getName());
        return view;
    }
}
