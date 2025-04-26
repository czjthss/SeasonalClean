package Algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ASAP {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;
    private final long cost_time;
    private final int windowSize;
    public final String FILE_DIR = "/Users/chenzijie/Documents/GitHub/repair-seasonal-exp/src/main/java/Algorithm/ASAPPy/";

    public ASAP(long[] td_time, double[] td_dirty, int windowSize) throws Exception {
        this.td_time = td_time;
        this.td_dirty = td_dirty;
        this.td_repair = new double[td_dirty.length];
        this.windowSize = windowSize;

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
            string.append(value);
            string.append("\n");
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(FILE_DIR + "input.txt", false));
        bufferedWriter.write(string.toString());
        bufferedWriter.close();

        // python shell
        String[] cmd = {
                "/Users/chenzijie/anaconda3/envs/holoclean/bin/python",
                FILE_DIR + "ASAP.py",
                String.valueOf(windowSize),
        };
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        String line;

        // read
        BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_DIR + "output.txt"));
        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            td_repair[i++] = Double.parseDouble(line);
        }
        bufferedReader.close();
    }

    public static void main(String[] args) throws Exception {
        // read
        String FILE_DIR = "/Users/chenzijie/Documents/GitHub/repair-seasonal-exp/src/main/java/Algorithm/ASAP/";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_DIR + "output.txt"));
        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            i++;
        }
        System.out.println(i);
        bufferedReader.close();
    }
}
