/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.math;

import java.util.Arrays;

/**
 * ****************************************************************************
 * Compilation: javac StdStats.java Execution: java StdStats < input.txt
 * Dependencies: StdOut.java
 *
 * Library of statistical functions.
 *
 * The test client reads an array of real numbers from standard input, and
 * computes the minimum, mean, maximum, and standard deviation.
 *
 * The functions all throw a java.lang.IllegalArgumentException if the array
 * passed in as an argument is null.
 *
 * The floating-point functions all return NaN if any input is NaN.
 *
 * Unlike Math.min() and Math.max(), the min() and max() functions do not
 * differentiate between -0.0 and 0.0.
 *
 * % more tiny.txt 5 3.0 1.0 2.0 5.0 4.0
 *
 * % java StdStats < tiny.txt min 1.000 mean 3.000 max 5.000 std dev 1.581
 *
 * Should these funtions use varargs instead of array arguments?
 *
 *****************************************************************************
 */
/**
 * The {@code StdStats} class provides static methods for computing statistics
 * such as min, max, mean, sample standard deviation, and sample variance.
 * <p>
 * For additional documentation, see
 * <a href="http://introcs.cs.princeton.edu/22library">Section 2.2</a> of
 * <i>Computer Science: An Interdisciplinary Approach</i>
 * by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public final class StdStats {

    private StdStats() {
    }

    /**
     * Returns the maximum value in the specified array.
     *
     * @param a the array
     * @return the maximum value in the array {@code a[]};
     *         {@code Double.NEGATIVE_INFINITY} if no such value
     */
    public static double max(double[] a) {
        validateNotNull(a);

        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < a.length; i++) {
            if (Double.isNaN(a[i])) {
                return Double.NaN;
            }
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }

    /**
     * Returns the maximum value in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the maximum value in the subarray {@code a[lo..hi]};
     *         {@code Double.NEGATIVE_INFINITY} if no such value
     */
    public static double max(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        double max = Double.NEGATIVE_INFINITY;
        for (int i = lo; i <= hi; i++) {
            if (Double.isNaN(a[i])) {
                return Double.NaN;
            }
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }

    /**
     * Returns the maximum value in the specified array.
     *
     * @param a the array
     * @return the maximum value in the array {@code a[]};
     *         {@code Integer.MIN_VALUE} if no such value
     */
    public static int max(int[] a) {
        validateNotNull(a);

        int max = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }

    /**
     * Returns the minimum value in the specified array.
     *
     * @param a the array
     * @return the minimum value in the array {@code a[]};
     *         {@code Double.POSITIVE_INFINITY} if no such value
     */
    public static double min(double[] a) {
        validateNotNull(a);

        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < a.length; i++) {
            if (Double.isNaN(a[i])) {
                return Double.NaN;
            }
            if (a[i] < min) {
                min = a[i];
            }
        }
        return min;
    }

    /**
     * Returns the minimum value in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the maximum value in the subarray {@code a[lo..hi]};
     *         {@code Double.POSITIVE_INFINITY} if no such value
     */
    public static double min(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        double min = Double.POSITIVE_INFINITY;
        for (int i = lo; i <= hi; i++) {
            if (Double.isNaN(a[i])) {
                return Double.NaN;
            }
            if (a[i] < min) {
                min = a[i];
            }
        }
        return min;
    }

    /**
     * Returns the minimum value in the specified array.
     *
     * @param a the array
     * @return the minimum value in the array {@code a[]};
     *         {@code Integer.MAX_VALUE} if no such value
     */
    public static int min(int[] a) {
        validateNotNull(a);

        int min = Integer.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < min) {
                min = a[i];
            }
        }
        return min;
    }

    /**
     * Returns the average value in the specified array.
     *
     * @param a the array
     * @return the average value in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double mean(double[] a) {
        validateNotNull(a);

        if (a.length == 0) {
            return Double.NaN;
        }
        double sum = sum(a);
        return sum / a.length;
    }

    public static void standWeight(double[] weight) {
        double sumW = sum(weight);
        for (int i = 0; i < weight.length; i++) {
            weight[i] = weight[i] / sumW;
        }
    }

    public static double mean(double[] a, double[] weight, boolean standizeWeight) {
        validateNotNull(a);
        validateNotNull(weight);

        if (a.length == 0) {
            return Double.NaN;
        }
        if (standizeWeight) {
            double sumW = sum(weight);
            for (int i = 0; i < weight.length; i++) {
                if (!Double.isNaN(weight[i]) && Double.isFinite(weight[i])) {
                    weight[i] = weight[i] / sumW;
                }
            }
        }
        double mean = 0;
        for (int i = 0; i < weight.length; i++) {
            if (!Double.isNaN(a[i])) {
                if (!Double.isNaN(weight[i]) && Double.isFinite(weight[i])) {
                    mean += weight[i] * a[i];
                }
            }
        }
        return mean;
    }

    //caculate unbiased weights from alogrithm in wiki
    public static double stddevWithScaledWeights(double[] a, double[] weight) {
        validateNotNull(a);
        validateNotNull(weight);

        if (a.length == 0) {
            return Double.NaN;
        }

        double avg = mean(a, weight, false);
        double v2 = 0;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
             if (!Double.isNaN(a[i]) && Double.isFinite(a[i]))  {
                v2 += weight[i] * weight[i];
                sum += ((a[i] - avg) * (a[i] - avg) * weight[i]);
            }
        }
        return Math.sqrt(sum / (1 - v2));
    }

    public static double stddevWithUnscaledWeights(double[] a, double[] weight) {
        validateNotNull(a);
        validateNotNull(weight);

        if (a.length == 0) {
            return Double.NaN;
        }
        double[] newW = new double[weight.length];
        System.arraycopy(weight, 0, newW, 0, weight.length);

        double avg = mean(a, newW, true);
        double v2 = 0;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            v2 += newW[i] * newW[i];
            sum += ((a[i] - avg) * (a[i] - avg) * newW[i]);
        }
        return Math.sqrt(sum / (1 - v2));
    }

    /**
     * Returns the average value in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the average value in the subarray {@code a[lo..hi]};
     *         {@code Double.NaN} if no such value
     */
    public static double mean(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        int length = hi - lo + 1;
        if (length == 0) {
            return Double.NaN;
        }

        double sum = sum(a, lo, hi);
        return sum / length;
    }

    /**
     * Returns the average value in the specified array.
     *
     * @param a the array
     * @return the average value in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double mean(int[] a) {
        validateNotNull(a);

        if (a.length == 0) {
            return Double.NaN;
        }
        int sum = sum(a);
        return 1.0 * sum / a.length;
    }

    /**
     * Returns the sample variance in the specified array.
     *
     * @param a the array
     * @return the sample variance in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double var(double[] a) {
        validateNotNull(a);

        if (a.length == 0) {
            return Double.NaN;
        }
        double avg = mean(a);
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += (a[i] - avg) * (a[i] - avg);
        }
        return sum / (a.length - 1);
    }

    /**
     * Returns the sample variance in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the sample variance in the subarray {@code a[lo..hi]};
     *         {@code Double.NaN} if no such value
     */
    public static double var(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        int length = hi - lo + 1;
        if (length == 0) {
            return Double.NaN;
        }

        double avg = mean(a, lo, hi);
        double sum = 0.0;
        for (int i = lo; i <= hi; i++) {
            sum += (a[i] - avg) * (a[i] - avg);
        }
        return sum / (length - 1);
    }

    /**
     * Returns the sample variance in the specified array.
     *
     * @param a the array
     * @return the sample variance in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double var(int[] a) {
        validateNotNull(a);
        if (a.length == 0) {
            return Double.NaN;
        }
        double avg = mean(a);
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += (a[i] - avg) * (a[i] - avg);
        }
        return sum / (a.length - 1);
    }

    /**
     * Returns the population variance in the specified array.
     *
     * @param a the array
     * @return the population variance in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double varp(double[] a) {
        validateNotNull(a);
        if (a.length == 0) {
            return Double.NaN;
        }
        double avg = mean(a);
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += (a[i] - avg) * (a[i] - avg);
        }
        return sum / a.length;
    }

    /**
     * Returns the population variance in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the population variance in the subarray {@code a[lo..hi]};
     *         {@code Double.NaN} if no such value
     */
    public static double varp(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        int length = hi - lo + 1;
        if (length == 0) {
            return Double.NaN;
        }

        double avg = mean(a, lo, hi);
        double sum = 0.0;
        for (int i = lo; i <= hi; i++) {
            sum += (a[i] - avg) * (a[i] - avg);
        }
        return sum / length;
    }

    /**
     * Returns the sample standard deviation in the specified array.
     *
     * @param a the array
     * @return the sample standard deviation in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double stddev(double[] a) {
        validateNotNull(a);
        return Math.sqrt(var(a));
    }

    /**
     * Returns the sample standard deviation in the specified array.
     *
     * @param a the array
     * @return the sample standard deviation in the array {@code a[]};
     *         {@code Double.NaN} if no such value
     */
    public static double stddev(int[] a) {
        validateNotNull(a);
        return Math.sqrt(var(a));
    }

    // the array double[] m MUST BE SORTED
    public static double median(double[] m) {
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }

    // the array double[] m MUST BE SORTED
    public static double medianNS(double[] a) {
        double[] m = new double[a.length];
        System.arraycopy(a, 0, m, 0, a.length);
        Arrays.parallelSort(m);
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            return m[middle];
        } else {
            return (m[middle - 1] + m[middle]) / 2.0;
        }
    }

    /**
     * Returns the sample standard deviation in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the sample standard deviation in the subarray {@code a[lo..hi]};
     *         {@code Double.NaN} if no such value
     */
    public static double stddev(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        return Math.sqrt(var(a, lo, hi));
    }

    /**
     * Returns the population standard deviation in the specified array.
     *
     * @param a the array
     * @return the population standard deviation in the array;
     * {@code Double.NaN} if no such value
     */
    public static double stddevp(double[] a) {
        validateNotNull(a);
        return Math.sqrt(varp(a));
    }

    /**
     * Returns the population standard deviation in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the population standard deviation in the subarray {@code a[lo..hi]};
     *         {@code Double.NaN} if no such value
     */
    public static double stddevp(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        return Math.sqrt(varp(a, lo, hi));
    }

    /**
     * Returns the sum of all values in the specified array.
     *
     * @param a the array
     * @return the sum of all values in the array {@code a[]};
     *         {@code 0.0} if no such value
     */
    private static double sum(double[] a) {
        validateNotNull(a);
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            if (!Double.isNaN(a[i]) && Double.isFinite(a[i])) {
                sum += a[i];
            }
        }
        return sum;
    }

    /**
     * Returns the sum of all values in the specified subarray.
     *
     * @param a the array
     * @param lo the left endpoint of the subarray (inclusive)
     * @param hi the right endpoint of the subarray (inclusive)
     * @return the sum of all values in the subarray {@code a[lo..hi]};
     *         {@code 0.0} if no such value
     */
    private static double sum(double[] a, int lo, int hi) {
        validateNotNull(a);
        validateSubarrayIndices(lo, hi, a.length);

        double sum = 0.0;
        for (int i = lo; i <= hi; i++) {
            sum += a[i];
        }
        return sum;
    }

    /**
     * Returns the sum of all values in the specified array.
     *
     * @param a the array
     * @return the sum of all values in the array {@code a[]};
     *         {@code 0.0} if no such value
     */
    private static int sum(int[] a) {
        validateNotNull(a);
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        return sum;
    }

    // throw an IllegalArgumentException if x is null
    private static void validateNotNull(Object x) {
        if (x == null) {
            throw new IllegalArgumentException("argument is null");
        }
    }

    // throw an exception unless 0 <= lo <= hi < length
    private static void validateSubarrayIndices(int lo, int hi, int length) {
        if (lo < 0 || hi >= length || lo > hi) {
            throw new IndexOutOfBoundsException("subarray indices out of bounds");
        }
    }

}
