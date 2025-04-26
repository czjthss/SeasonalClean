package Algorithm;

import java.util.Arrays;

public class Sequential {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;
    private final long cost_time;
    private double smin, smax;


    public Sequential(long[] td_time, double[] td_dirty) throws Exception {
        this.td_time = td_time;
        this.td_dirty = td_dirty;
        this.td_repair = new double[td_dirty.length];
        setParameters();

        long startTime = System.currentTimeMillis();
        this.repair();
        long endTime = System.currentTimeMillis();
        this.cost_time = endTime - startTime;
//        System.out.println("Sequential time cost:" + cost_time + "ms");
    }

    public double[] getTd_repair() {
        return td_repair;
    }

    public long getCost_time() {
        return cost_time;
    }


    public void repair() {
        double upperbound, lowerbound, val, tempVal;
        td_repair[0] = td_dirty[0];

        for (int idx = 1; idx < td_dirty.length; idx++) {
            val = td_repair[idx - 1];
            upperbound = val + smax;
            lowerbound = val + smin;

            tempVal = td_dirty[idx];
            tempVal = Math.min(upperbound, tempVal);
            tempVal = Math.max(lowerbound, tempVal);

            td_repair[idx] = tempVal;
        }
    }

    private void setParameters() {
        // set the default speed threshold
        double[] speed = speed(td_dirty, td_time);
        double mid = median(speed);
        double sigma = mad(speed);
        smax = mid + 3 * sigma;
        smin = mid - 3 * sigma;
    }

    public static double median(double[] list) {
        Arrays.sort(list);
        int size = list.length;
        if (size % 2 != 1) {
            return (list[size / 2 - 1] + list[size / 2]) / 2;
        } else {
            return list[(size - 1) / 2];
        }
    }

    public static double[] speed(double[] origin, long[] time) {
        int n = origin.length;
        double[] speed = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            speed[i] = (origin[i + 1] - origin[i]) / (time[i + 1] - time[i]);
        }
        return speed;
    }

    public static double mad(double[] value) {
        double mid = median(value);
        double[] d = new double[value.length];
        for (int i = 0; i < value.length; i++) {
            d[i] = Math.abs(value[i] - mid);
        }
        return 1.4826 * median(d);
    }

    public static void main(String[] args) throws Exception {
        double[] ts_dirty = new double[]{1, 2, 100, 4, 5, 6, 7, 8, 9};
        long[] ts_time = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        Sequential sequential = new Sequential(ts_time, ts_dirty);
        double[] ts_repair = sequential.getTd_repair();
        for (double val : ts_repair) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}
