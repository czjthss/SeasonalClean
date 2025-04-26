package Algorithm;

public class Garf {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;

    private final long cost_time;

    public Garf(long[] td_time, double[] td_dirty) throws Exception {
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
        System.arraycopy(td_dirty, 0, td_repair, 0, td_dirty.length);
    }
}
