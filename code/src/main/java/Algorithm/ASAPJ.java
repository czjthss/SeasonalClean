package Algorithm;

import java.util.Arrays;

public class ASAPJ {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;
    private final long cost_time;
    private final int windowSize;

    public ASAPJ(long[] td_time, double[] td_dirty, int windowSize) throws Exception {
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

    private void repair() throws Exception {
        int slideSize = 1;
        double[] smaData = sma(td_dirty, slideSize);
        double[] smoothed = sma(smaData, windowSize);

        int smoothedL = windowSize / 2;
        int smoothedR = smoothedL + smoothed.length - 1;

        // Populate rtn array
        for (int ii = 0; ii < td_repair.length; ii++) {
            if (ii < smoothedL || ii >= smoothedR) {
                td_repair[ii] = smaData[ii];
            } else {
                td_repair[ii] = smoothed[ii - smoothedL];
            }
        }
    }

    public double[] getTd_repair() {
        return td_repair;
    }

    public long getCost_time() {
        return cost_time;
    }

    // Moving average method in Java
    private double[] movingAverage(double[] data, int range) {
        double[] ret = new double[data.length];
        double sum = 0.0;

        for (int i = 0; i < range; i++) {
            sum += data[i];
        }

        for (int i = range; i < data.length; i++) {
            sum += data[i] - data[i - range];
            if (i - range + 1 < ret.length)
                ret[i - range + 1] = sum / range;
        }

        return Arrays.copyOfRange(ret, Math.max(range - 1, 0), ret.length);
    }

    // SMA method in Java with stride parameter 'slide'
    private double[] sma(double[] data, int range) {
        return movingAverage(data, range);
    }

    public static void main(String[] args) throws Exception {
        double[] ts_dirty = new double[]{1, 2, 10, 1, 2, 4, 1, 2, 4};
        long[] ts_time = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        ASAPJ asapj = new ASAPJ(ts_time, ts_dirty, 2);
        double[] ts_repair = asapj.getTd_repair();
        for (double val : ts_repair) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}