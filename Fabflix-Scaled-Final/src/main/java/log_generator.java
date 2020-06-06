package main.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class log_generator {
    private String filename;
    private FileWriter writer;

    public log_generator(String filename) {
        this.filename = filename;
        make_file();
    }

    public log_generator() {
        this.filename = "/log_processing.txt";
        make_file();
    }

    private void make_file() {
        try {
            writer = new FileWriter(new File(filename), true);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error opening file");
        }
    }

    public void output_time(long elapsedTimeTS, long elapsedTimeTJ) {
        try {
            writer.write(String.format("%d,%d\n", elapsedTimeTS, elapsedTimeTJ));
            writer.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error writing to file");
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (Exception e) {
            System.out.println("Trouble closing writer");
        }
    }
}
