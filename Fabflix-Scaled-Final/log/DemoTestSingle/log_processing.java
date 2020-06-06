import java.io.*;
import java.util.*;

public class log_processing {
    private String filename;
    private double TS;
    private double TJ;
    private List<String> filenames = null;

    public log_processing(String filename) {
        this.filename = filename;
        this.TS = 0.0;
        this.TJ = 0.0;
    }

    public log_processing(List<String> filenames) {
        this.filenames = filenames;
        this.TS = 0.0;
        this.TJ = 0.0;
    }

    public log_processing() {
        this.filename = "/log_processing.txt";
        this.TS = 0.0;
        this.TJ = 0.0;
    }

    public void process_log() {
        int size = 0;
        if (filenames != null) {
            for (String name : filenames) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
                    String curLine;
                    while ((curLine = reader.readLine()) != null) {
                        if (curLine.length() > 0) {
                            String[] items = curLine.split(",");
                            TS += Long.parseLong(items[0]);
                            TJ += Long.parseLong(items[1]);
                            size++;
                        }
                    }

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    System.out.println("Error reading from log");
                }
            }
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
                String curLine;
                while ((curLine = reader.readLine()) != null) {
                    if (curLine.length() > 0) {
                        String[] items = curLine.split(",");
                        TS += Long.parseLong(items[0]);
                        TJ += Long.parseLong(items[1]);
                        size++;
                    }
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("Error reading from log");
            }
        }

        TS /= size;
        TS /= 1000000000;
        TJ /= size;
        TJ /= 1000000000;
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

    public static void main(String[] args) {
        log_processing log_proc;
        if (args.length > 0) {
            List<String> names = new ArrayList<>();
            for (String arg : args) names.add(arg);
            log_proc = new log_processing(names);
        } else {
            log_proc = new log_processing();
        }
        log_proc.process_log();
        log_proc.output_result("log_processing_result_local.txt");
    }
}
