package Algorithm;

import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class Holistic {
    private final long[] td_time;
    private final double[] td_dirty;
    private double[] td_repair;
    private final long cost_time;

    // Holistic
    private final int varmul;
    private HashSet<Integer> preSet; // the pre conflict list
    private HashSet<Integer> curSet; // the cur conflict list after iteration
    private HashSet<Integer> tempSet;

    // parameter
    private final double EPSILON = 1.0e-5;
    private double S1;
    private double S2;
    private final long T = 4;


    public Holistic(long[] td_time, double[] td_dirty) throws Exception {
        this.td_time = td_time;
        this.td_dirty = td_dirty;
        this.td_repair = new double[td_dirty.length];
        // holistic
        this.varmul = 2;
        this.preSet = new HashSet<>();
        this.curSet = new HashSet<>();
        this.tempSet = new HashSet<>();
        setParameters();

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
        System.arraycopy(td_dirty, 0, td_repair, 0, td_repair.length);

        int len = td_dirty.length;

        double[] xvals = new double[len * varmul];
        int conflictPair = -1;

        while (shouldContinue(conflictPair)) {
            for (int i = 0; i < len * varmul; ++i)
                xvals[i] = 0;
            preSet.clear();
            for (int i : tempSet)
                preSet.add(i);

            if (preSet.isEmpty())
                conflictPair = calcViolations();

            buildProblem(xvals, conflictPair);
            buildVal(xvals);

            tempSet.clear();
            for (int i : curSet)
                tempSet.add(i);
            curSet.clear();

            conflictPair = calcViolations();
            for (int i : curSet)
                tempSet.add(i);

        }

//        conflictPair = calcViolations();
//        System.out.println("Holistic iter = " + iterNum + ", Conflict = "
//                + curSet.size());
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

    private void setParameters() {
        // set the default speed threshold
        double[] speed = speed(td_dirty, td_time);
        double mid = median(speed);
        double sigma = mad(speed);
        S1 = mid + 3 * sigma;
        S2 = mid - 3 * sigma;
    }

    /**
     * calc the violation
     *
     * @return
     */
    private int calcViolations() {
        curSet.clear();
        int len = td_dirty.length;
        int pairnum = 0;

        long timestampi, timestampj;
        double vali, valj;
        double delta1, delta2;

        for (int i = 0; i < len; ++i) {
            timestampi = td_time[i];
            vali = td_dirty[i];

            for (int j = i + 1; j < len; ++j) {
                timestampj = td_time[j];

                if (timestampj - timestampi <= T) {
                    valj = td_dirty[j];
                    delta1 = S1 * (timestampj - timestampi);
                    delta2 = S2 * (timestampj - timestampi);

                    if (valj - vali > delta1 + EPSILON ||
                            valj - vali < delta2 - EPSILON) {
                        pairnum++;
                        curSet.add(i);
                        curSet.add(j);
                    }
                } else
                    break;
            }
        }

        return pairnum;
    }

    /**
     * if curList is the sub set of preList, then return false, over. else
     * return true
     *
     * @return
     */
    private boolean shouldContinue(int conflictPair) {
        if (conflictPair == 0)
            return false;

        if (preSet.isEmpty())
            return true;

//		if(tempSet.isEmpty())
//			return false;		// all are handled

        return !preSet.containsAll(tempSet);
    }

    /**
     * Solves the original Holistic linear program.
     *
     * @param xvals optimized positive and negative adjustments
     */
    private void buildProblem(double[] xvals, int pairnum) {
        int len = td_dirty.length;
        // int varnum = len * varmul; // var num
        pairnum *= 2; // constraint num
        HashSet<Integer> conflictSet = new HashSet<Integer>();

        long timestampi, timestampj;
        int ncols = len * varmul;
        int nrows = pairnum;

        double[] obj = new double[ncols];
        Collection<LinearConstraint> constraints = new ArrayList<>(nrows);

        double vali, valj;
        double delta1, delta2;

        for (int i = 0; i < len; ++i) {
            timestampi = td_time[i];
            vali = td_dirty[i];

            for (int j = i + 1; j < len; ++j) {
                timestampj = td_time[j];

                if (timestampj - timestampi <= T) {
                    valj = td_dirty[j];

                    delta1 = S1 * (timestampj - timestampi);
                    delta2 = S2 * (timestampj - timestampi);

                    if (valj - vali > delta1 + EPSILON ||
                            valj - vali < delta2 - EPSILON) {
                        conflictSet.add(i);
                        conflictSet.add(j);

                        double[] coefficients = new double[ncols];
                        coefficients[i * varmul] = -1;
                        coefficients[i * varmul + 1] = 1;
                        coefficients[j * varmul] = 1;
                        coefficients[j * varmul + 1] = -1;
                        // uj-vj-(ui-vi) <= delta1+xi-xj
                        constraints.add(new LinearConstraint(
                                coefficients, Relationship.LEQ, delta1 + vali - valj));
                        // uj-vj-(ui-vi) >= delta2+xi-xj
                        constraints.add(new LinearConstraint(
                                coefficients, Relationship.GEQ, delta2 + vali - valj));
                    } // end of ||>delta
                } else {
                    break;
                }
            } // end of for j
        } // end of for i

        for (int i = 0; i < len; ++i) {
            if (conflictSet.contains(i)) {
                obj[i * varmul] = 1;
                obj[i * varmul + 1] = 1;
            } else {
                obj[i * varmul] = 0;
                obj[i * varmul] = 0;
            }
        }

        if (constraints.isEmpty()) {
            return;
        }

        try {
            PointValuePair solution = new SimplexSolver().optimize(
                    new MaxIter(10000),
                    new LinearObjectiveFunction(obj, 0),
                    new LinearConstraintSet(constraints),
                    GoalType.MINIMIZE,
                    new NonNegativeConstraint(true));
            System.arraycopy(solution.getPoint(), 0, xvals, 0, ncols);
        } catch (NoFeasibleSolutionException e) {
            throw new IllegalStateException("Holistic linear program is infeasible", e);
        }
    }

    /**
     * xi'=ui-vi+xi
     */
    private void buildVal(double[] xvals) {
        int len = td_dirty.length;
        BigDecimal bd = null;

        for (int i = 0; i < len; ++i) {
            if (curSet.contains(i)) {
                bd = BigDecimal.valueOf(xvals[i * varmul] - xvals[i * varmul + 1]);
                td_repair[i] = bd.setScale(3, RoundingMode.HALF_UP).doubleValue() + td_dirty[i];
            }
        }
    }

    public static void main(String[] args) throws Exception {
        double[] ts_dirty = new double[]{1, 2, 10, 1, 2, 4, 1, 2, 4};
        long[] ts_time = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        Holistic holistic = new Holistic(ts_time, ts_dirty);
        double[] ts_repair = holistic.getTd_repair();
        for (double val : ts_repair) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}
