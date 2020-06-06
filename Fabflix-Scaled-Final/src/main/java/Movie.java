package main.java;

import java.util.List;
import java.util.Map;

public class Movie {
    public String id;
    public String title;
    public int year;
    public String director;
    public double price;
    public Map<String, Integer> genres;

    public Movie(String i, String ttl, int yr, String dir, double prc, Map<String, Integer> gnrs) {
        id = i;
        title = ttl;
        year = yr;
        director = dir;
        price = prc;
        genres = gnrs;
    }

    public Movie(String i, String ttl, int yr, String dir, Map<String, Integer> gnrs) {
        id = i;
        title = ttl;
        year = yr;
        director = dir;
        price = 9.99;
        genres = gnrs;
    }
}
