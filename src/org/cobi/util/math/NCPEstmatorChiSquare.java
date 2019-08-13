// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011

package org.cobi.util.math;

import cern.jet.stat.Gamma;

/**
 *
 * @author mxli
 */
public class NCPEstmatorChiSquare {

    static double zero = 1.0e-300;
    static double inf = 1.0e300;
    static double tolerance = 1.0e-6;

    static double partBesselFunc(double x, int n, double ga) {
        double s = 1.0 / ga;
        for (int i = 1; i <= n; i++) {
            s = s * x / i;
        }
        return s;
    }

    static double nonCentralChisquarePDF(double x, int df, double lambda) {
        if (x <= zero) {
            return 0.0;
        }
        double part1 = 0.5 * Math.pow(x / lambda, df * 0.25 - 0.5) * Math.exp(-(lambda + x) / 2);
        double a = df * 0.5;
        part1 = part1 * Math.pow(Math.sqrt(x * lambda) / 2, a - 1);
        int i = 0;

        double part2 = 1.0 / Gamma.gamma(a);

        double sum = 0;

        sum += part2;
        while (part2 >= tolerance) {
            i++;
            part2 = partBesselFunc(0.25 * x * lambda, i, Gamma.gamma(a + i));
            sum += part2;
        }
        return sum * part1;
    }

    static double nonCentralChisquarePDFArea(double x, int df, double lambda) {
        if (x <= zero) {
            return 0.0;
        } else {
            return (x * nonCentralChisquarePDF(x, df, lambda));
        }
    }

    static double centralChisquarePDF(double x, int df) {
        if (x <= zero) {
            return 0.0;
        }
        double part1 = Math.pow(x, df * 0.5 - 1) * Math.exp(-x / 2) / Math.pow(2, df * 0.5);
        return (part1 / Gamma.gamma(df * 0.5));
    }

    static double centralChisquarePDFArea(double x, int df) {
        if (x <= zero) {
            return 0.0;
        }
        return (x * centralChisquarePDF(x, df));
    }

//Simpson aglorithm to get upper integral  
    static double nonCentralChisquareCDF(double a, double b, int df, double lambda) {
        int n, j;
        double s = 0.0;
        double h, d, s1, s2, t1, x, t2, g, s0, ep;
        n = 1;
        h = 0.5 * (b - a);
        d = Math.abs((b - a) * 1.0e-06);
        s1 = nonCentralChisquarePDF(a, df, lambda);
        s2 = nonCentralChisquarePDF(b, df, lambda);
        t1 = h * (s1 + s2);
        s0 = 1.0e+35;
        ep = 1.0 + tolerance;
        while (((ep >= tolerance) && (Math.abs(h) > d)) || (n < 100)) {
            x = a - h;
            t2 = 0.5 * t1;
            for (j = 1; j <= n; j++) {
                x = x + 2.0 * h;
                g = nonCentralChisquarePDF(x, df, lambda);
                t2 = t2 + h * g;
            }
            s = (4.0 * t2 - t1) / 3.0;
            ep = Math.abs(s - s0) / (1.0 + Math.abs(s));
            n = n + n;
            s0 = s;
            t1 = t2;
            h = h * 0.5;
        }
        return (s);
    }

//Simpson aglorithm to get upper integral  
    static double centralChisquareCDF(double a, double b, int df) {
        int n, j;
        double s = 0.0;
        double h, d, s1, s2, t1, x, t2, g, s0, ep;
        n = 1;
        h = 0.5 * (b - a);
        d = Math.abs((b - a) * 1.0e-06);
        s1 = centralChisquarePDF(a, df);
        s2 = centralChisquarePDF(b, df);
        t1 = h * (s1 + s2);
        s0 = 1.0e+35;
        ep = 1.0 + tolerance;
        while (((ep >= tolerance) && (Math.abs(h) > d)) || (n < 100)) {
            x = a - h;
            t2 = 0.5 * t1;
            for (j = 1; j <= n; j++) {
                x = x + 2.0 * h;
                g = centralChisquarePDF(x, df);
                t2 = t2 + h * g;
            }
            s = (4.0 * t2 - t1) / 3.0;
            ep = Math.abs(s - s0) / (1.0 + Math.abs(s));
            n = n + n;
            s0 = s;
            t1 = t2;
            h = h * 0.5;
        }
        return (s);
    }

//Simpson aglorithm to get upper integral   
    static double nonCentralChisquareMean(double a, double b, int df, double lambda) {
        int n, j;
        double s = 0.0;
        double h, d, s1, s2, t1, x, t2, g, s0, ep;
        n = 1;
        h = 0.5 * (b - a);
        d = Math.abs((b - a) * 1.0e-06);
        s1 = nonCentralChisquarePDFArea(a, df, lambda);
        s2 = nonCentralChisquarePDFArea(b, df, lambda);
        t1 = h * (s1 + s2);
        s0 = 1.0e+35;
        ep = 1.0 + tolerance;
        while (((ep >= tolerance) && (Math.abs(h) > d)) || (n < 100)) {
            x = a - h;
            t2 = 0.5 * t1;
            for (j = 1; j <= n; j++) {
                x = x + 2.0 * h;
                g = nonCentralChisquarePDFArea(x, df, lambda);
                t2 = t2 + h * g;
            }
            s = (4.0 * t2 - t1) / 3.0;
            ep = Math.abs(s - s0) / (1.0 + Math.abs(s));
            n = n + n;
            s0 = s;
            t1 = t2;
            h = h * 0.5;
        }
        return (s);
    }

//Simpson aglorithm to get upper integral   
    static double centralChisquareMean(double a, double b, int df) {
        int n, j;
        double s = 0.0;
        double h, d, s1, s2, t1, x, t2, g, s0, ep;
        n = 1;
        h = 0.5 * (b - a);
        d = Math.abs((b - a) * 1.0e-06);
        s1 = centralChisquarePDFArea(a, df);
        s2 = centralChisquarePDFArea(b, df);
        t1 = h * (s1 + s2);
        s0 = 1.0e+35;
        ep = 1.0 + tolerance;
        while (((ep >= tolerance) && (Math.abs(h) > d)) || (n < 100)) {
            x = a - h;
            t2 = 0.5 * t1;
            for (j = 1; j <= n; j++) {
                x = x + 2.0 * h;
                g = centralChisquarePDFArea(x, df);
                t2 = t2 + h * g;
            }
            s = (4.0 * t2 - t1) / 3.0;
            ep = Math.abs(s - s0) / (1.0 + Math.abs(s));
            n = n + n;
            s0 = s;
            t1 = t2;
            h = h * 0.5;
        }
        return (s);
    }

    static double nonCentralChisquareUpperProb(double a, int df, double lambda) {
        double t = a;

        double inc = 5.0;
        while (nonCentralChisquarePDF(t, df, lambda) > tolerance) {
            t += inc;
        }
        return nonCentralChisquareCDF(a, t, df, lambda);

    }

    static double nonCentralChisquareUpperMean(double a, int df, double lambda) {
        double t = a;

        double inc = 5.0;
        while (nonCentralChisquarePDF(t, df, lambda) > tolerance) {
            t += inc;
        }
        return nonCentralChisquareMean(a, t, df, lambda);

    }

    static double centralChisquareUpperProb(double a, int df) {
        double t = a;

        double inc = 5.0;
        while (centralChisquarePDF(t, df) > tolerance) {
            t += inc;
        }
        return centralChisquareCDF(a, t, df);

    }

    static double centralChisquareUpperMean(double a, int df) {
        double t = a;

        double inc = 5.0;
        while (centralChisquarePDF(t, df) > tolerance) {
            t += inc;
        }
        return centralChisquareMean(a, t, df);
    }

    public static double lambdaFinder(double x0, int df, double t) {
        double J0 = centralChisquareUpperMean(t, df) / centralChisquareUpperProb(t, df);

        double la1 = 1e-8;
        double la2 = la1;
        //if E<E0, there is no solution
        if (x0 < J0) {
            return -9;
        }
        double diff = nonCentralChisquareUpperMean(t, df, la2) / nonCentralChisquareUpperProb(t, df, la2) - x0;

        if (Math.abs(diff) <= tolerance) {
            return la2;
        }
        while (diff <= 0) {
            la1 = la2;
            la2 += 1.0;
            diff = nonCentralChisquareUpperMean(t, df, la2) / nonCentralChisquareUpperProb(t, df, la2) - x0;
            if (Math.abs(diff) <= tolerance) {
                return la2;
            }
        }

        // cout << la1 << ' ' << la2 << endl;

        int i;
        int gridNum = 5;
        int pointNum = gridNum + 1;
        double startBound = la1;
        double endBound = la2;
        double increament = (endBound - startBound) / gridNum;
        double wd = 0;

        int minNegIndex = 0, minPosIndex = gridNum;
        double minNegValue = -1, minPosValue = -1;
        double lastMinNegValue = -1, lastMinPosValue = -1;
        int lastMinNegIndex = -1, lastMinPosIndex = -1;

        boolean notFound = true;
        // //note: the assumption of the following code is that we  know the diff must have a point equal to 0 within [startBound, endBound]
        do {
            startBound = startBound + increament * Math.min(minNegIndex, minPosIndex);
            endBound = startBound + increament * Math.max(minNegIndex, minPosIndex);
            increament = (endBound - startBound) / gridNum;

            lastMinNegValue = minNegValue;
            lastMinPosValue = minPosValue;
            lastMinNegIndex = minNegIndex;
            lastMinPosIndex = minPosIndex;

            minNegIndex = -1;
            minPosIndex = -1;
            minNegValue = -1;
            minPosValue = -1;
            for (i = 0; i < pointNum; i++) {
                wd = startBound + increament * i;
                diff = x0 - nonCentralChisquareUpperMean(t, df, wd) / nonCentralChisquareUpperProb(t, df, wd);

                if (diff < 0) {
                    if (-diff <= tolerance) {
                        notFound = false;
                        break;
                    }
                    if (minNegIndex < 0 || minNegValue < diff) {
                        minNegIndex = i;
                        minNegValue = diff;
                    }
                } else {
                    if (diff <= tolerance) {
                        notFound = false;
                        break;
                    }
                    if (minPosIndex < 0 || minPosValue > diff) {
                        minPosIndex = i;
                        minPosValue = diff;
                    }
                }
            }

            if (lastMinNegValue == minNegValue && lastMinPosValue == minPosValue && lastMinNegIndex == minNegIndex && lastMinPosIndex == minPosIndex) {
                if (minPosValue < (-minNegValue)) {
                    wd = startBound + increament * minPosIndex;
                } else {
                    wd = startBound + increament * minNegIndex;
                }
                break;
            }
        } while (notFound);

        return (wd);
    }

    public static double lambdaFinderModified(double x0, int df, double t) {
        double J0 = centralChisquareUpperMean(t, df) / centralChisquareUpperProb(t, df);
        //if E<E0, there is no solution
        if (x0 < J0) {
            return -9;
        }

        double J1 = J0;
        double left = 0.0;
        double beta = (t + 2) / (df + t + 2);
        double right = 0.0;
        do {
            J0 = J1;
            J1 = J1 + 1.0;
            left = lambdaFinder(J1, df, t);
            right = beta * (J1 - t);
        } while (left <= right);


        int i;
        double diff = 0;
        int gridNum = 5;
        int pointNum = gridNum + 1;
        double startBound = J0;
        double endBound = J1;
        double increament = (endBound - startBound) / gridNum;
        double wd = 0;

        int minNegIndex = 0, minPosIndex = gridNum;
        double minNegValue = -1, minPosValue = -1;
        double lastMinNegValue = -1, lastMinPosValue = -1;
        int lastMinNegIndex = -1, lastMinPosIndex = -1;
        //note: the assumption of the following code is that we  know the left must be equal to the right within [startBound, endBound]
        boolean notFound = true;
        do {
            startBound = startBound + increament * Math.min(minNegIndex, minPosIndex);
            endBound = startBound + increament * Math.max(minNegIndex, minPosIndex);
            increament = (endBound - startBound) / gridNum;

            lastMinNegValue = minNegValue;
            lastMinPosValue = minPosValue;
            lastMinNegIndex = minNegIndex;
            lastMinPosIndex = minPosIndex;
            minNegIndex = -1;
            minPosIndex = -1;
            minNegValue = -1;
            minPosValue = -1;
            for (i = 0; i < pointNum; i++) {
                wd = startBound + increament * i;
                left = lambdaFinder(wd, df, t);
                right = beta * (wd - t);

                diff = left - right;
                if (diff < 0) {
                    if (-diff <= tolerance) {
                        notFound = false;
                        break;
                    }
                    if (minNegIndex < 0 || minNegValue < diff) {
                        minNegIndex = i;
                        minNegValue = diff;
                    }
                } else {
                    if (diff <= tolerance) {
                        notFound = false;
                        break;
                    }
                    if (minPosIndex < 0 || minPosValue > diff) {
                        minPosIndex = i;
                        minPosValue = diff;
                    }
                }
            }

            if (lastMinNegValue == minNegValue && lastMinPosValue == minPosValue && lastMinNegIndex == minNegIndex && lastMinPosIndex == minPosIndex) {
                if (minPosValue < (-minNegValue)) {
                    wd = startBound + increament * minPosIndex;
                } else {
                    wd = startBound + increament * minNegIndex;
                }
                break;
            }
            //System.out.println(minNegValue + "@" + minNegIndex + " " + minPosValue + "@" + minPosIndex);
        } while (notFound);

        double chibeta = wd;

        if (x0 <= chibeta) {
            return beta * (x0 - t);
        } else {
            return lambdaFinder(x0, df, t);
        }
    }

    public static void main(String[] args) {
        double lamda = 1.0e-8;
        int df = 1;
        double t = 5.036401311353763;
        double x0 = 6.952091328815194;

        double pi0 = 0.02;
        double pi1 = 1 - pi0;
        double inc = 0.01;
        double len = 100;
        double E1 = centralChisquareUpperMean(t, df) / centralChisquareUpperProb(t, df);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 0; i++) {
            lamda += i * inc;
            double p0 = (nonCentralChisquareUpperProb(t, df, lamda)) / (centralChisquareUpperProb(t, df) + nonCentralChisquareUpperProb(t, df, lamda));
            double p1 = 1 - p0;
            double pr0 = pi0 * p0 / (pi0 * p0 + pi1 * p1);
            double pr1 = 1 - pr0;
            System.out.println("PPPPP " + lamda);
            System.out.print(nonCentralChisquareUpperMean(t, df, lamda) / nonCentralChisquareUpperProb(t, df, lamda));
            System.out.print(" ");
            System.out.println((x0 - pr1 * E1) / (pr0));
        }
        System.out.println(lambdaFinderMix(x0, 1 - pi0, df, t));

        System.out.println(lambdaFinder(x0, df, t));
        System.out.println(lambdaFinderModified(x0, df, t));



        //System.out.println("PDF  " + NCPEstmatorChiSquare.nonCentralChisquareCDF(1, 4, df, lamda));
        //System.out.println("PDF  " + (NCPEstmatorChiSquare.nonCentralChisquareCDF(1e-8, 4, df, lamda) - NCPEstmatorChiSquare.nonCentralChisquareCDF(1e-8, 1, df, lamda)));
        System.out.println("Lapsed Time: " + (System.currentTimeMillis() - start));
    }

    /**
     * Explore signal strength in a mix distribution (central and non-central chisquare distribution) by 
     * moment estimate for a truncated non-central chi-square distribution extended from Li and Yu (2008)
     * 
     * @param observedMean obvserved truncated mean
     * @param pi0 proportion of null hypothesis
     * @param df degree of freedom
     * @param truncate statisitics
     * 
     * @return signal strength of the non-central chi-square distribution      
     */
    public static double lambdaFinderMix(double observedMean, double pi0, int df, double t) {
        double upperProb = centralChisquareUpperProb(t, df);
        if (upperProb == 0) {
            return -9;
        }
        double E0 = centralChisquareUpperMean(t, df) / upperProb;
        //if E<E0, there is no solution
        if (E0 == Double.NaN || observedMean < E0) {
            return -9;
        }

        double lambdaStart = tolerance;
        double lambdaEnd = lambdaStart;
        double left = 0.0;
        double right = 0.0;
        double cp, ncp;
        cp = centralChisquareUpperProb(t, df);
        do {
            lambdaStart = lambdaEnd;
            lambdaEnd = lambdaEnd + 1.0;
            ncp = nonCentralChisquareUpperProb(t, df, lambdaEnd);
            left = nonCentralChisquareUpperMean(t, df, lambdaEnd) / ncp;
            right = observedMean + pi0 * cp * (observedMean - E0) / ((1 - pi0) * ncp);
        } while (left <= right);


        int i;
        double diff = 0;
        int gridNum = 5;
        int pointNum = gridNum + 1;
        double startBound = lambdaStart;
        double endBound = lambdaEnd;
        double increament = (endBound - startBound) / gridNum;
        double wd = 0;

        int minNegIndex = 0, minPosIndex = gridNum;
        double minNegValue = -1, minPosValue = -1;
        double lastMinNegValue = -1, lastMinPosValue = -1;
        int lastMinNegIndex = -1, lastMinPosIndex = -1;
        //note: the assumption of the following code is that we  know the left must be equal to the right within [startBound, endBound]
        boolean notFound = true;
        do {
            startBound = startBound + increament * Math.min(minNegIndex, minPosIndex);
            endBound = startBound + increament * Math.max(minNegIndex, minPosIndex);
            increament = (endBound - startBound) / gridNum;

            lastMinNegValue = minNegValue;
            lastMinPosValue = minPosValue;
            lastMinNegIndex = minNegIndex;
            lastMinPosIndex = minPosIndex;
            minNegIndex = -1;
            minPosIndex = -1;
            minNegValue = -1;
            minPosValue = -1;
            for (i = 0; i < pointNum; i++) {
                wd = startBound + increament * i;
                ncp = nonCentralChisquareUpperProb(t, df, wd);
                left = nonCentralChisquareUpperMean(t, df, wd) / ncp;
                right = observedMean + pi0 * cp * (observedMean - E0) / ((1 - pi0) * ncp);

                diff = left - right;
                if (diff < 0) {
                    if (-diff <= tolerance) {
                        notFound = false;
                        break;
                    }
                    if (minNegIndex < 0 || minNegValue < diff) {
                        minNegIndex = i;
                        minNegValue = diff;
                    }
                } else {
                    if (diff <= tolerance) {
                        notFound = false;
                        break;
                    }
                    if (minPosIndex < 0 || minPosValue > diff) {
                        minPosIndex = i;
                        minPosValue = diff;
                    }
                }
            }

            if (lastMinNegValue == minNegValue && lastMinPosValue == minPosValue && lastMinNegIndex == minNegIndex && lastMinPosIndex == minPosIndex) {
                if (minPosValue < (-minNegValue)) {
                    wd = startBound + increament * minPosIndex;
                } else {
                    wd = startBound + increament * minNegIndex;
                }
                break;
            }
            //System.out.println(minNegValue + "@" + minNegIndex + " " + minPosValue + "@" + minPosIndex);
        } while (notFound);
        //System.out.println("Effect Proportion: "+pr1);
        return wd;
    }
}
