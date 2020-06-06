package main.java;

import java.util.List;
import java.util.Map;

public class Cast {
    public String movie_id;
    public String title;
    public Map<String, String> stars;

    public Cast(String id, Map<String, String> strs) {
        movie_id = id;
        stars = strs;
    }
}
