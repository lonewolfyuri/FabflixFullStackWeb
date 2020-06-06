package edu.uci.ics.fabflixmobile;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String title, id, director;
    private Double rating;
    private List<Genre> genres;
    private List<Star> stars;
    private short year;

    public Movie(String name, String id,String director, short year, Double rating) {
        this.title = name;
        this.id = id;
        this.director = director;
        this.year = year;
        this.rating = rating;
        this.genres = new ArrayList<>();
        this.stars = new ArrayList<>();
    }

    public Movie(String name, String id, String director, short year, Double rating, List<Genre> genres, List<Star> stars) {
        this.title = name;
        this.id = id;
        this.director = director;
        this.year = year;
        this.rating = rating;
        this.genres = genres;
        this.stars = stars;
    }

    public void addGenre(Genre newGen) {
        genres.add(newGen);
    }

    public void addStar(Star newStar) {
        stars.add(newStar);
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getDirector() {
        return director;
    }

    public short getYear() {
        return year;
    }

    public Double getRating() {
        return rating;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public List<Star> getStars() {
        return stars;
    }

    public String gensToString() {
        if (genres.size() == 0) return "";
        String result = "Genres: ";
        for (Genre gen : genres) result += gen.getName() + ", ";
        return result.substring(0, result.length() - 2);
    }

    public String starsToString() {
        if (stars.size() == 0) return "";
        String result = "Stars: ";
        for (Star str : stars) result += str.getName() + ", ";
        return result.substring(0, result.length() - 2);
    }
}