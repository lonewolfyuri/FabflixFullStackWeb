package edu.uci.ics.fabflixmobile;

import java.util.ArrayList;
import java.util.List;

public class Star {
    private String name, id;
    private short birth_year;
    private List<Movie> movies;

    public Star(String name, String id, short year) {
        this.name = name;
        this.id = id;
        this.birth_year = year;
        this.movies = new ArrayList<>();
    }

    public Star(String name, String id, short year, List<Movie> movies) {
        this.name = name;
        this.id = id;
        this.birth_year = year;
        this.movies = movies;
    }

    public void addMovie(Movie newMov) {
        this.movies.add(newMov);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public short getYear() {
        return birth_year;
    }

    public List<Movie> getMovies() {
        return movies;
    }
}
