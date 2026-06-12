import Algorithm.*;

import java.io.File;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Experiment {
    private static final Path REPOSITORY_ROOT = findRepositoryRoot();
    private static final Path SYNTHETIC_DATA_DIR = REPOSITORY_ROOT.resolve("data/synthetic");
    private static final Path REAL_CLEAN_DIR = REPOSITORY_ROOT.resolve("data/real_clean");
    private static final Path REAL_DIRTY_DIR = REPOSITORY_ROOT.resolve("data/real_dirty");
    private static final Path OUTPUT_DIR = REPOSITORY_ROOT.resolve("results");
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

    private static Path findRepositoryRoot() {
        Path current = Path.of("").normalize();
        if (Files.isDirectory(current.resolve("data"))
                && Files.isDirectory(current.resolve("code"))) {
            return current;
        }
        if (Files.isDirectory(current.resolve("../data"))
                && Files.isRegularFile(current.resolve("pom.xml"))) {
            return current.resolve("..").normalize();
        }
        throw new IllegalStateException(
                "Run SeasonalClean from the repository root or the code directory.");
    }

    private static Path outputFile(String fileName) throws Exception {
        Files.createDirectories(OUTPUT_DIR);
        return OUTPUT_DIR.resolve(fileName);
    }

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
        FileWriter fileWritter = new FileWriter(outputFile("expRMSE.txt").toFile(), true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void recordTime(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(outputFile("expTime.txt").toFile(), true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void recordIterRMSE(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(outputFile("iterRMSE.txt").toFile(), true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void recordIterTime(String string) throws Exception {
        FileWriter fileWritter = new FileWriter(outputFile("iterTime.txt").toFile(), true);
        BufferedWriter bw = new BufferedWriter(fileWritter);
        bw.write(string);
        bw.close();
    }

    public static void write2File(double[] clean, double[] dirty, String fileName) throws Exception {
        int len = clean.length;
        Path akaneDir = OUTPUT_DIR.resolve("akaneData");
        Files.createDirectories(akaneDir);
        FileWriter fileWritter = new FileWriter(
                akaneDir.resolve(fileName + ".data").toFile(), true);
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
                    LoadData loadData = new LoadData(
                            SYNTHETIC_DATA_DIR.resolve(datasetFile).toString(), dataLen);
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
                    for (int j = 1; j <= 5; j++) {
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

        File dir = REAL_CLEAN_DIR.toFile();
        for (String fileName : Objects.requireNonNull(dir.list())) {
            String methodName = fileName.split("_")[0];
            if (Objects.equals(methodName, "grid")) period = 1440;
            else period = 288;

            // td_clean
            LoadData loadData = new LoadData(
                    REAL_CLEAN_DIR.resolve(fileName).toString(), Integer.MAX_VALUE);
            long[] td_time = loadData.getTd_time();
            double[] td_clean = loadData.getTd_clean();

            // td_dirty
            loadData = new LoadData(
                    REAL_DIRTY_DIR.resolve(fileName).toString(), Integer.MAX_VALUE);
            double[] td_dirty = loadData.getTd_clean();

            // label4imr
            LabelData labelData = new LabelData(td_clean, td_dirty, label_rate, seed);
            double[] td_label = labelData.getTd_label();
            boolean[] td_bool = labelData.getTd_bool();

            boolean[] default_bool = new boolean[td_bool.length];
            Arrays.fill(default_bool, false);

            Analysis analysis;
            for (int j = 1; j <= 5; j++) {
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
                analysis.writeRepairResultToFile(
                        OUTPUT_DIR.resolve("real").resolve(methodRealList[j])
                                .resolve(fileName).toString());

                FileWriter fileWritter = new FileWriter(
                        outputFile("performance.txt").toFile(), true);
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
                    LoadData loadData = new LoadData(
                            SYNTHETIC_DATA_DIR.resolve(datasetFile).toString(), dataLen);
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

    public static void main_smoke() throws Exception {
        int smokeLength = 2000;
        int smokePeriod = 288;
        LoadData loadData = new LoadData(
                REAL_CLEAN_DIR.resolve("bank_1_7983.csv").toString(), smokeLength);
        long[] tdTime = loadData.getTd_time();
        double[] tdClean = loadData.getTd_clean();

        AddNoise addNoise = new AddNoise(tdClean, 5.0, 2.0, 25, seed);
        double[] tdDirty = addNoise.getTd_dirty();
        LabelData labelData = new LabelData(tdClean, tdDirty, 0.5, seed);
        double[] tdLabel = labelData.getTd_label();
        boolean[] tdBool = labelData.getTd_bool();
        boolean[] noLabels = new boolean[tdClean.length];

        Analysis srd = srdRepair(tdTime, tdClean, tdDirty, smokePeriod, 6.0, 5, noLabels);
        Analysis screen = screenRepair(tdTime, tdClean, tdDirty, noLabels);
        Analysis lsgreedy = lsgreedyRepair(tdTime, tdClean, tdDirty, noLabels);
        Analysis imr = imrRepair(tdTime, tdClean, tdDirty, tdLabel, tdBool);
        Analysis ewma = ewmaRepair(tdTime, tdClean, tdDirty, noLabels);
        Path result = OUTPUT_DIR.resolve("smoke/srd_repaired.csv");
        srd.writeRepairResultToFile(result.toString());

        System.out.println("Smoke test completed.");
        System.out.println("SRD RMSE: " + srd.getRMSE() + ", " + srd.getCost_time() + " ms");
        System.out.println(
                "SCREEN RMSE: " + screen.getRMSE() + ", " + screen.getCost_time() + " ms");
        System.out.println(
                "LsGreedy RMSE: " + lsgreedy.getRMSE() + ", "
                        + lsgreedy.getCost_time() + " ms");
        System.out.println("IMR RMSE: " + imr.getRMSE() + ", " + imr.getCost_time() + " ms");
        System.out.println(
                "EWMA RMSE: " + ewma.getRMSE() + ", " + ewma.getCost_time() + " ms");
        System.out.println("Output: " + REPOSITORY_ROOT.relativize(result));
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "smoke" : args[0].toLowerCase();
        switch (mode) {
            case "smoke" -> main_smoke();
            case "synthetic" -> main_synthetic();
            case "real" -> main_real();
            case "ablation" -> main_ablation();
            default -> throw new IllegalArgumentException(
                    "Unknown mode: " + mode
                            + ". Expected smoke, synthetic, real, or ablation.");
        }
    }
}
