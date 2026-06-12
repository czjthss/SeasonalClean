package Algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ASAP {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;
    private final long cost_time;
    private final int windowSize;
    private final Path fileDir = RuntimePaths.algorithmDirectory("ASAPPy");

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
        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(fileDir.resolve("input.txt").toFile(), false));
        bufferedWriter.write(string.toString());
        bufferedWriter.close();

        // python shell
        Process process = new ProcessBuilder(
                RuntimePaths.pythonExecutable(),
                fileDir.resolve("ASAP.py").toString(),
                String.valueOf(windowSize))
                .inheritIO()
                .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("ASAP Python process exited with code " + exitCode);
        }
        String line;

        // read
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(fileDir.resolve("output.txt").toFile()));
        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            td_repair[i++] = Double.parseDouble(line);
        }
        bufferedReader.close();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(RuntimePaths.algorithmDirectory("ASAPPy").resolve("output.txt"));
    }
}
