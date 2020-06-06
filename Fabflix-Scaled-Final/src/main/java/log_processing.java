package main.java;

import java.io.*;

public class log_processing {
    private String filename;
    private double TS;
    private double TJ;

    public log_processing(String filename) {
        this.filename = filename;
        this.TS = 0.0;
        this.TJ = 0.0;
    }

    public log_processing() {
        this.filename = "/log_processing.txt";
        this.TS = 0.0;
        this.TJ = 0.0;
    }

    public void process_log() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            String curLine;
            int size = 0;
            while((curLine = reader.readLine()) != null) {
                if (curLine.length() > 0) {
                    String[] items = curLine.split(",");
                    TS += Long.parseLong(items[0]);
                    TJ += Long.parseLong(items[1]);
                    size++;
                }
            }
            TS /= size;
            TS /= 1000000000;
            TJ /= size;
            TJ /= 1000000000;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Error reading from log");
        }
    }

    public void output_result(String filename) {
        try {
            FileWriter writer = new FileWriter(new File(filename), false);
            writer.write(toString());
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to write to file");
        }
    }

    public String toString() {
        return String.format("Avg TS: %.5f sec. | Avg TJ: %.5f sec.\n", TS, TJ);
    }
}
