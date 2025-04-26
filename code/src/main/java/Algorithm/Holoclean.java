package Algorithm;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


public class Holoclean {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;
    private final int period;
    public final String FILE_DIR = "/Users/chenzijie/Documents/GitHub/repair-seasonal-exp/src/main/java/Algorithm/HolocleanPy/";

    private final long cost_time;

    public Holoclean(long[] td_time, double[] td_dirty, int period) throws Exception {
        this.period = period;
        this.td_time = td_time;
        this.td_dirty = td_dirty;
        this.td_repair = new double[td_dirty.length];

        long startTime = System.currentTimeMillis();
        this.repair();
        long endTime = System.currentTimeMillis();
        this.cost_time = endTime - startTime;
//        System.out.println("Screen time cost:" + cost_time + "ms");
    }

    public double[] getTd_repair() {
        return td_repair;
    }

    public long getCost_time() {
        return cost_time;
    }

    private void repair() throws Exception {
        // write
        StringBuilder string = new StringBuilder();
        for (double value : td_dirty) {
            string.append(String.format("%.2f", value));  // reformat for rule mining
            string.append("\n");
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(FILE_DIR + "input.txt", false));
        bufferedWriter.write(string.toString());
        bufferedWriter.close();

        // python shell
        String[] cmd = {
                "/Users/chenzijie/anaconda3/envs/holoclean/bin/python",
                FILE_DIR + "hc.py",
                String.valueOf(period),
        };
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        String line;
        // read
        BufferedReader bufferedReader;
        if (process.exitValue() == 0)
            bufferedReader = new BufferedReader(new FileReader(FILE_DIR + "output.txt"));
        else {
            System.out.println("Holoclean Runtime Error");
            bufferedReader = new BufferedReader(new FileReader(FILE_DIR + "input.txt"));
        }
        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            td_repair[i++] = Double.parseDouble(line);
        }
        bufferedReader.close();
    }

    public static void main(String[] args) throws Exception {
        double[] ts_dirty = new double[]{1, 2, 0, 1, 2, 4, 1, 200, 4, 1, 2, 4, 1, 2, 4};
        long[] ts_time = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        Holoclean holoclean = new Holoclean(ts_time, ts_dirty, 3);
        double[] ts_repair = holoclean.getTd_repair();
        for (double val : ts_repair) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}
