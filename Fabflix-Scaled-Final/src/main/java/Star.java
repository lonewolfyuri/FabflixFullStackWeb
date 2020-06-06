package main.java;

public class Star {
    public String name;
    public int year;
    public String id = null;

    public Star(String nm, int yr, String i) {
        name = nm;
        year = yr;
        id = i;
    }

    public Star(String nm, int yr) {
        name = nm;
        year = yr;
    }

    public Star(String nm) {
        name = nm;
        year = -1;
    }
}
