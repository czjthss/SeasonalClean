import Algorithm.*;

import java.io.File;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Objects;

public class Experiment {
    private static final String INPUT_DIR = "/Users/chenzijie/Documents/GitHub/data/input/repair/synthetic/";
    private static final String OUTPUT_DIR = "/Users/chenzijie/Documents/GitHub/data/output/repair/";
    // dataset
    private static final String[] datasetFileList = {
            "power_5241600.csv",
            "voltage_22825440.csv",
    };
    private static final int[] periodList = {
            144,
            1440,
    };
    private static final int[] dataLenList = {
            52416 * 5,
            228254 * 5,
    };

    // task
    private static final String[] taskList = {
            "rate",
//            "range",
//            "length",
//            "scala",
    };
    private static final double[][] scaleTaskList = {
            {2.5, 2.5},
            {1.0, 1.0},
            {10, 10},
            {1000000, 4000000},
    };

    private static final String[][] ticksTaskList = {
            {"2,4,6,8,10", "2,4,6,8,10"},
            {"1,2,3,4,5", "1,2,3,4,5"},
            {"2,4,6,8,10", "2,4,6,8,10"},
            {"1M,2M,3M,4M,5M", "4M,8M,12M,16M,20M"},
    };

    // parameter
    private static String datasetFile, datasetName, task, x_ticks;
    private static int dataLen, period, max_iter, error_length;
    private static double k, error_range, label_rate, error_rate, scale;
    private static int seed = 666;

    private static void reset(int datasetIdx, int taskIdx) {
        datasetFile = datasetFileList[datasetIdx];
        datasetName = datasetFile.split("_")[0];

        task = taskList[taskIdx];
        x_ticks = ticksTaskList[taskIdx][datasetIdx];
        scale = scaleTaskList[taskIdx][datasetIdx];
        // parameter
        dataLen = dataLenList[datasetIdx];
        // error
        error_rate = 5.0;
        error_range = 2.0;
        error_length = 25;
        // 4seasonal
        period = periodList[datasetIdx];
        max_iter = 5;
        k = 6.;
        // 4imr
        label_rate = 0.5;
    }

    public static Analysis srdRepair(long[] td_time, double[] td_clean, double[] td_dirty, int period, double k, int max_iter, boolean[] td_bool) throws Exception {
        System.out.println("SRD");
        SRD srrd = new SRD(td_time, td_dirty, period, k, max_iter);
        double[] td_repair = srrd.getTd_repair();
        long cost_time = srrd.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis screenRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("SCREEN");
        SCREEN screen = new SCREEN(td_time, td_dirty);
        double[] td_repair = screen.getTd_repair();
        long cost_time = screen.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis lsgreedyRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("Lsgreedy");
        Lsgreedy lsgreedy = new Lsgreedy(td_time, td_dirty);
        double[] td_repair = lsgreedy.getTd_repair();
        long cost_time = lsgreedy.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis imrRepair(long[] td_time, double[] td_clean, double[] td_dirty, double[] td_label, boolean[] td_bool) throws Exception {
        System.out.println("IMR");
        IMR imr = new IMR(td_time, td_dirty, td_label, td_bool);
        double[] td_repair = imr.getTd_repair();
        long cost_time = imr.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis sequentialRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("Sequential");
        Sequential sequential = new Sequential(td_time, td_dirty);
        double[] td_repair = sequential.getTd_repair();
        long cost_time = sequential.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis holisticRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("Holistic");
        Holistic holistic = new Holistic(td_time, td_dirty);
        double[] td_repair = holistic.getTd_repair();
        long cost_time = holistic.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis garfRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("Garf");
        Garf garf = new Garf(td_time, td_dirty);
        double[] td_repair = garf.getTd_repair();
        long cost_time = garf.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis holocleanRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool, int period) throws Exception {
        System.out.println("Holoclean");
        Holoclean holoclean = new Holoclean(td_time, td_dirty, period);
        double[] td_repair = holoclean.getTd_repair();
        long cost_time = holoclean.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis asapRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("ASAP");
        ASAP asap = new ASAP(td_time, td_dirty, period);
        double[] td_repair = asap.getTd_repair();
        long cost_time = asap.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static Analysis ewmaRepair(long[] td_time, double[] td_clean, double[] td_dirty, boolean[] td_bool) throws Exception {
        System.out.println("EWMA");
        EWMA ewma = new EWMA(td_time, td_dirty);
        double[] td_repair = ewma.getTd_repair();
        long cost_time = ewma.getCost_time();
        return new Analysis(td_time, td_clean, td_repair, td_bool, cost_time);
    }

    public static void recordRMSE(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(OUTPUT_DIR + "expRMSE.txt", true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void recordTime(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(OUTPUT_DIR + "expTime.txt", true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void recordIterRMSE(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(OUTPUT_DIR + "iterRMSE.txt", true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void recordIterTime(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(OUTPUT_DIR + "iterTime.txt", true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void write2File(double[] clean, double[] dirty, String fileName) throws Exception {
        int len = clean.length;
        FileWriter fileWritter = new FileWriter(OUTPUT_DIR + "akaneData/" + fileName + ".data", true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        for (int i = 0; i < len; ++i) {
            bw.write((i + 1) + "," + dirty[i] + "," + clean[i] + "\n");
        }
        bw.close();
    }

    public static void main_synthetic() throws Exception { //synthetic
        for (int datasetIdx = 0; datasetIdx < datasetFileList.length; ++datasetIdx) {
            for (int taskIdx = 0; taskIdx < taskList.length; ++taskIdx) {
                // reset
                reset(datasetIdx, taskIdx);

                System.out.print(datasetName + " " + task + " " + x_ticks + "\n");
                recordRMSE(datasetName + "_rmse " + task + " " + x_ticks + "\n");
                recordTime(datasetName + "_time " + task + " " + x_ticks + "\n");

                for (int base = 1; base <= 5; base++) {
                    switch (task) {
                        case "scala" -> dataLen = base * (int) scale;
                        case "rate" -> error_rate = base * scale;
                        case "range" -> error_range = base * scale;
                        case "length" -> error_length = base * (int) scale;
                    }

                    // start
                    LoadData loadData = new LoadData(INPUT_DIR + datasetFile, dataLen);
                    long[] td_time = loadData.getTd_time();
                    double[] td_clean = loadData.getTd_clean();

                    // add noise
                    AddNoise addNoise = new AddNoise(td_clean, error_rate, error_range, error_length, seed);
                    double[] td_dirty = addNoise.getTd_dirty();

                    // label4imr
                    LabelData labelData = new LabelData(td_clean, td_dirty, label_rate, seed);
                    double[] td_label = labelData.getTd_label();
                    boolean[] td_bool = labelData.getTd_bool();

                    boolean[] default_bool = new boolean[td_bool.length];
                    Arrays.fill(default_bool, false);

                    write2File(td_clean, td_dirty, datasetName + "_" + task + "_" + base);

                    Analysis analysis;
                    for (int j = 1; j < 1; j++) {
                        switch (j) {
                            case 1 ->
                                    analysis = srdRepair(td_time, td_clean, td_dirty, period, k, max_iter, default_bool);
                            case 2 -> analysis = screenRepair(td_time, td_clean, td_dirty, default_bool);
                            case 3 -> analysis = lsgreedyRepair(td_time, td_clean, td_dirty, default_bool);
                            case 4 -> analysis = imrRepair(td_time, td_clean, td_dirty, td_label, td_bool);
                            case 5 -> analysis = ewmaRepair(td_time, td_clean, td_dirty, default_bool);
                            case 6 -> analysis = asapRepair(td_time, td_clean, td_dirty, default_bool);
                            case 7 -> analysis = sequentialRepair(td_time, td_clean, td_dirty, default_bool);
                            case 8 -> analysis = holisticRepair(td_time, td_clean, td_dirty, default_bool);
                            case 9 -> analysis = garfRepair(td_time, td_clean, td_dirty, default_bool);
                            default -> analysis = holocleanRepair(td_time, td_clean, td_dirty, default_bool, period);
                        }
                        recordRMSE(analysis.getRMSE() + ",");
                        recordTime(analysis.getCost_time() + ",");
                        System.gc();
                        Runtime.getRuntime().gc();
                    }
                    recordRMSE("\n");
                    recordTime("\n");
                }
            }
        }
    }

    public static void main_real() throws Exception { //synthetic
        // parameter
        max_iter = 5;
        k = 9.;
        // 4imr
        label_rate = 0.5;

        // reset
        String INPUT_DIR_REAL = "/Users/chenzijie/Documents/GitHub/data/input/repair/";
        String OUTPUT_DIR_REAL = "/Users/chenzijie/Documents/GitHub/data/output/repair/task/";

        File dir = new File(INPUT_DIR_REAL + "real_clean/");
        for (String fileName : Objects.requireNonNull(dir.list())) {
            String methodName = fileName.split("_")[0];
            if (Objects.equals(methodName, "grid")) period = 1440;
            else period = 288;

            // td_clean
            LoadData loadData = new LoadData(INPUT_DIR_REAL + "real_clean/" + fileName, Integer.MAX_VALUE);
            long[] td_time = loadData.getTd_time();
            double[] td_clean = loadData.getTd_clean();

            // td_dirty
            loadData = new LoadData(INPUT_DIR_REAL + "real_dirty/" + fileName, Integer.MAX_VALUE);
            double[] td_dirty = loadData.getTd_clean();

            // label4imr
            LabelData labelData = new LabelData(td_clean, td_dirty, label_rate, seed);
            double[] td_label = labelData.getTd_label();
            boolean[] td_bool = labelData.getTd_bool();

            boolean[] default_bool = new boolean[td_bool.length];
            Arrays.fill(default_bool, false);

            Analysis analysis;
            for (int j = 6; j < 11; j++) {
                switch (j) {
                    case 1 -> analysis = srdRepair(td_time, td_clean, td_dirty, period, k, max_iter, default_bool);
                    case 2 -> analysis = screenRepair(td_time, td_clean, td_dirty, default_bool);
                    case 3 -> analysis = lsgreedyRepair(td_time, td_clean, td_dirty, default_bool);
                    case 4 -> analysis = imrRepair(td_time, td_clean, td_dirty, td_label, td_bool);
                    case 5 -> analysis = ewmaRepair(td_time, td_clean, td_dirty, default_bool);
                    case 6 -> analysis = asapRepair(td_time, td_clean, td_dirty, default_bool);
                    case 7 -> analysis = sequentialRepair(td_time, td_clean, td_dirty, default_bool);
                    case 8 -> analysis = holisticRepair(td_time, td_clean, td_dirty, default_bool);
                    case 9 -> analysis = garfRepair(td_time, td_clean, td_dirty, default_bool);
                    default -> analysis = holocleanRepair(td_time, td_clean, td_dirty, default_bool, period);
                }
                String[] methodRealList = {"", "srd", "screen", "lsgreedy", "imr", "ewma", "asap", "sequential", "holistic", "garf", "holoclean"};
                analysis.writeRepairResultToFile(OUTPUT_DIR_REAL + methodRealList[j] + "/" + fileName);

                FileWriter fileWritter = new FileWriter(OUTPUT_DIR_REAL + "performance.txt", true);
                BufferedWriter bw = new BufferedWriter(fileWritter);
                bw.write(fileName.split("\\.")[0] + "," + j + "," + analysis.getRMSE() + "\n");
                bw.close();

                System.gc();
                Runtime.getRuntime().gc();
            }
        }
    }

    public static void main_ablation() throws Exception { //synthetic
        for (int datasetIdx = 0; datasetIdx < datasetFileList.length; ++datasetIdx) {
            System.out.println(datasetFileList[datasetIdx]);
            datasetFile = datasetFileList[datasetIdx];
            datasetName = datasetFile.split("_")[0];
            recordIterRMSE(datasetName + "\n");
            recordIterTime(datasetName + "\n");
            // parameter
            dataLen = dataLenList[datasetIdx];
            // error
            error_rate = 5.0;
            error_range = 2.0;
            error_length = 25;
            // 4seasonal
            period = periodList[datasetIdx];
            max_iter = 5;
            k = 6.;
            // 4imr
            label_rate = 0.5;

            double[] choice = new double[]{0.5, 5, 15};

            for (int base = 0; base < 3; base++) {
                error_rate = choice[base];
                System.out.println("Error Rate: " + error_rate);
                String[] rmseList = new String[10];
                long[] timeList = new long[10];
                for (max_iter = 1; max_iter <= 10; max_iter++) {
                    // start
                    LoadData loadData = new LoadData(INPUT_DIR + datasetFile, dataLen);
                    long[] td_time = loadData.getTd_time();
                    double[] td_clean = loadData.getTd_clean();

                    // add noise
                    AddNoise addNoise = new AddNoise(td_clean, error_rate, error_range, error_length, seed);
                    double[] td_dirty = addNoise.getTd_dirty();

                    // label4imr
                    LabelData labelData = new LabelData(td_clean, td_dirty, label_rate, seed);
                    double[] td_label = labelData.getTd_label();
                    boolean[] td_bool = labelData.getTd_bool();

                    boolean[] default_bool = new boolean[td_bool.length];
                    Arrays.fill(default_bool, false);

                    Analysis analysis = srdRepair(td_time, td_clean, td_dirty, period, k, max_iter, default_bool);
                    recordIterRMSE(analysis.getRMSE() + ",");
                    recordIterTime(analysis.getCost_time() + ",");
                }
                recordIterRMSE("\n");
                recordIterTime("\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        main_synthetic();
//        main_real();
//        main_ablation();
//        System.out.println("hei");
    }
}
