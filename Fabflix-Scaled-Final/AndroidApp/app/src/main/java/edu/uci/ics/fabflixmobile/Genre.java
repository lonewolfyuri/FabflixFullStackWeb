package edu.uci.ics.fabflixmobile;

public class Genre {
    private String name;
    private int id;

    public Genre(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
