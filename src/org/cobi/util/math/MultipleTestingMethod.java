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

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Probability;
import java.text.SimpleDateFormat;
import java.util.Date;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.ContinuousDistribution;

import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;
import umontreal.iro.lecuyer.probdist.HypergeometricDist;

import org.apache.log4j.Logger;
import org.cobi.kgg.ui.GlobalManager;

/**
 *
 * @author mxli
 */
public class MultipleTestingMethod {

    public static void zScores(final DoubleArrayList pValues) {
        double q = 1;

        int size = pValues.size();
        // assume they are two-tailed I2-values
        for (int i = 0; i < size; i++) {
            if (Double.isNaN(pValues.getQuick(i))) {
                continue;
            }
            //dangerous to use 1-pvalue
            q = pValues.getQuick(i);
            //the Probability.normalInverse could handle 1E-323 but cannot handle 1-(1E-323)
            if (q > 0.5) {
                q = 1 - q;
                if (q < 1E-323) {
                    q = 1E-323;
                }
                pValues.setQuick(i, Probability.normalInverse(q));
            } else {
                if (q < 1E-323) {
                    q = 1E-323;
                }
                pValues.setQuick(i, -Probability.normalInverse(q));
            }
        }
    }

    public static double zScore(double pValue) {
        // assume they are two-tailed I2-values 
        if (Double.isNaN(pValue)) {
            return 0;
        }
        //the Probability.normalInverse could handle 1E-323 but cannot handle 1-(1E-323)
        if (pValue > 0.5) {
            pValue = 1 - pValue;
            if (pValue < 1E-323) {
                pValue = 1E-323;
            }
            return Probability.normalInverse(pValue);

        } else {
            if (pValue < 1E-323) {
                pValue = 1E-323;
            }
            return -Probability.normalInverse(pValue);
        }
    }

    //would be very slow
    public static double iterativeChisquareInverse(double df, double p) {
        double chil = 0;
        //this is the maximal value for  df 2 under the computer precise 
        double chih = 1432;
        if (p == 0) {
            return chih;
        }
        double precise = p / 100000000;
        double p1 = 1;
        double chi = 0;
        do {
            chi = (chil + chih) / 2;
            p1 = Probability.chiSquareComplemented(df, chi);
            if (p1 < p) {
                chih = chi;
            } else if (p1 > p) {
                chil = chi;
            }
        } while (Math.abs(p1 - p) > precise);
        return chi;
    }

    public static double inverseChisquareCumulativeProbability(String puporse, int df, double p) throws Exception {
        ContinuousDistribution chq = new ChiSquareDist(df);
        if (p >= 1E-15) {
            double b = chq.inverseF(1 - p);
            //  logInfo(puporse, "Inverse Chisquare Cumulative Probability", b);
            return b;
        } else {
            double b1 = chq.inverseF(1 - (1E-15));
            double b2 = 1440; //assume the maximal handable chi-square value is 1440
            double c1 = (b1 + b2) / 2;
            double p1 = Probability.chiSquareComplemented(df, c1);
            double p2 = 0;
            //too slow
            // while (Math.abs(p - p1) > 1E-310) 
            while (Math.abs(p - p1) > 1E-100) {
                if (p < p1) {
                    b1 = c1;
                } else {
                    b2 = c1;
                }
                c1 = (b1 + b2) / 2;
                p2 = Probability.chiSquareComplemented(df, c1);
                if (p1 == p2) {
                    //logInfo(puporse, "Inverse Chisquare Cumulative Probability", c1);
                    return c1;
                } else {
                    p1 = p2;
                }
                //System.out.println(p1);
            }
            // logInfo(puporse, "Inverse Chisquare Cumulative Probability", c1);
            return c1;
        }

    }

    //make sure the pValues have been sorted by ascending
    public static double originalBenjaminiFDR1(String puporse, double fdrThreshold, DoubleArrayList pValues) throws Exception {
        int i;
        int snpSize = pValues.size();
        if (snpSize == 0) {
            if (puporse != null) {
                logInfo(puporse, "Original Benjamini FDR", fdrThreshold);
            }
            return fdrThreshold;
        }
        fdrThreshold = fdrThreshold / (snpSize);
        i = snpSize - 1;
        while (i >= 0) {
            if (pValues.getQuick(i) <= (i + 1) * fdrThreshold) {
                if (puporse != null) {
                    logInfo(puporse, "Original Benjamini FDR", fdrThreshold * (i + 1));
                }
                return fdrThreshold * (i + 1);
            }
            i--;
        }

        //it must be less than or equal to this value
        if (puporse != null) {
            logInfo(puporse, "Original Benjamini FDR", fdrThreshold);
        }
        return fdrThreshold;

    }
//This is derived from PLINK
    //Benjamini & Hochberg (1995)

    public static double BenjaminiHochbergFDR(String puporse, double fdrThreshold, DoubleArrayList sp) {

        int ti = sp.size();
        if (ti == 0) {
            if (puporse != null) {
                logInfo(puporse, "Benjamini & Hochberg (1995) FDR", fdrThreshold);
            }
            return fdrThreshold;
        }
        // BH 

        double[] pv_BH = new double[ti];
        double t = (double) ti;

        pv_BH[ti - 1] = sp.getQuick(ti - 1);
        double x = 0;
        for (int i = ti - 2; i >= 0; i--) {
            x = (t / (double) (i + 1)) * sp.getQuick(i) < 1 ? (t / (double) (i + 1)) * sp.getQuick(i) : 1;
            pv_BH[i] = pv_BH[i + 1] < x ? pv_BH[i + 1] : x;
        }
        if (pv_BH[0] <= fdrThreshold) {
            for (int i = 1; i < ti; i++) {
                if (pv_BH[i] >= fdrThreshold) {
                    if (puporse != null) {
                        logInfo(puporse, "Benjamini & Hochberg (1995) FDR", sp.getQuick(i - 1));
                    }
                    return sp.getQuick(i - 1);
                }
            }
        }
        return fdrThreshold / ti;
    }

    //This is derived from PLINK
    //Benjamini & Hochberg (1995)
    public static double BenjaminiHochbergFDR(String puporse, double fdrThreshold, DoubleArrayList sp, DoubleArrayList adjustedP) {

        int ti = sp.size();
        if (ti == 0) {
            if (puporse != null) {
                logInfo(puporse, "Benjamini & Hochberg (1995) FDR", fdrThreshold);
            }
            return fdrThreshold;
        }
        // BH 
        adjustedP.setSize(ti);
        double[] pv_BH = new double[ti];
        double t = (double) ti;

        pv_BH[ti - 1] = sp.getQuick(ti - 1);
        adjustedP.setQuick(ti - 1, pv_BH[ti - 1]);
        double x = 0;
        for (int i = ti - 2; i >= 0; i--) {
            x = (t / (double) (i + 1)) * sp.getQuick(i) < 1 ? (t / (double) (i + 1)) * sp.getQuick(i) : 1;
            pv_BH[i] = pv_BH[i + 1] < x ? pv_BH[i + 1] : x;
            adjustedP.setQuick(i, pv_BH[i]);
        }
        if (pv_BH[0] <= fdrThreshold) {
            for (int i = 1; i < ti; i++) {
                if (pv_BH[i] >= fdrThreshold) {
                    if (puporse != null) {
                        logInfo(puporse, "Benjamini & Hochberg (1995) FDR", sp.getQuick(i - 1));
                    }
                    return sp.getQuick(i - 1);
                }
            }
        }
        return fdrThreshold / ti;
    }

//This is derived from PLINK
    //Benjamini & Yekutieli (2001) ("BY")
    public static double BenjaminiYekutieliFDR(String puporse, double fdrThreshold, DoubleArrayList sp) {
        int ti = sp.size();
        if (ti == 0) {
            if (puporse != null) {
                logInfo(puporse, "Benjamini & Yekutieli (2001) FDR", fdrThreshold);
            }
            return fdrThreshold;
        }
        // BY 

        double[] pv_BY = new double[ti];
        double a = 0;
        double t = (double) ti;
        for (double i = 1; i <= t; i++) {
            a += 1 / i;
        }
        pv_BY[ti - 1] = a * sp.getQuick(ti - 1) < 1 ? a * sp.getQuick(ti - 1) : 1;

        for (int i = ti - 2; i >= 0; i--) {
            double x = ((t * a) / (double) (i + 1)) * sp.getQuick(i) < 1 ? ((t * a) / (double) (i + 1)) * sp.getQuick(i) : 1;
            pv_BY[i] = pv_BY[i + 1] < x ? pv_BY[i + 1] : x;
        }
        if (pv_BY[0] <= fdrThreshold) {
            for (int i = 1; i < ti; i++) {
                if (pv_BY[i] >= fdrThreshold) {
                    if (puporse != null) {
                        logInfo(puporse, "Benjamini & Yekutieli (2001) FDR", sp.getQuick(i - 1));
                    }
                    return sp.getQuick(i - 1);
                }
            }
        }
        return fdrThreshold / ti;
    }

    public static double BenjaminiYekutieliFDR(String puporse, double fdrThreshold, DoubleArrayList sp, DoubleArrayList adjustedP) {
        int ti = sp.size();
        if (ti == 0) {
            if (puporse != null) {
                logInfo(puporse, "Benjamini & Yekutieli (2001) FDR", fdrThreshold);
            }
            return fdrThreshold;
        }
        // BY 
        adjustedP.setSize(ti);
        double[] pv_BY = new double[ti];
        double a = 0;
        double t = (double) ti;
        for (double i = 1; i <= t; i++) {
            a += 1 / i;
        }
        pv_BY[ti - 1] = a * sp.getQuick(ti - 1) < 1 ? a * sp.getQuick(ti - 1) : 1;
        adjustedP.setQuick(ti - 1, pv_BY[ti - 1]);
        for (int i = ti - 2; i >= 0; i--) {
            double x = ((t * a) / (double) (i + 1)) * sp.getQuick(i) < 1 ? ((t * a) / (double) (i + 1)) * sp.getQuick(i) : 1;
            pv_BY[i] = pv_BY[i + 1] < x ? pv_BY[i + 1] : x;
            adjustedP.setQuick(i, pv_BY[i]);
        }
        if (pv_BY[0] <= fdrThreshold) {
            for (int i = 1; i < ti; i++) {
                if (pv_BY[i] >= fdrThreshold) {
                    if (puporse != null) {
                        logInfo(puporse, "Benjamini & Yekutieli (2001) FDR", sp.getQuick(i - 1));
                    }
                    return sp.getQuick(i - 1);
                }
            }
        }
        return fdrThreshold / ti;
    }

    //make sure the pValues have been sorted by ascending
    public static double adaptiveBenjaminiFDR(String puporse, double fdrThreshold, DoubleArrayList pValues) {
        int pvSize = pValues.size();
        int i;
        // stage I
        fdrThreshold = fdrThreshold / (1 + fdrThreshold);
        fdrThreshold = fdrThreshold / pvSize;

        i = pvSize - 1;
        while ((i >= 0) && (pValues.getQuick(i) > (i + 1) * fdrThreshold)) {
            i--;
        }

        if (i == 0) {
            logInfo(puporse, "Adaptive Benjamini FDR", fdrThreshold);
            return fdrThreshold;
        } else if (i == pvSize) {
            logInfo(puporse, "Adaptive Benjamini FDR", fdrThreshold * (pvSize));
            return fdrThreshold * (pvSize);
        }

        // stage II
        int m0 = pvSize - i;
        fdrThreshold = fdrThreshold * pvSize / m0;

        i = pvSize - 1;
        while ((i >= 0) && (pValues.getQuick(i) > (i + 1) * fdrThreshold)) {
            i--;
        }
        //it must be less than or equal to this value
        logInfo(puporse, "Adaptive Benjamini FDR", fdrThreshold * (i + 1));
        return fdrThreshold * (i + 1);
    }

    //make sure the pValues have been sorted by ascending
    public static double storeyFDRTest(String puporse, double alpha, DoubleArrayList pValues) {
        int snpSize = pValues.size();
        double a, tmpDouble;
        int i;
        tmpDouble = 0;
        a = 0;
        i = 0;
        while (pValues.getQuick(i) <= 0.5) {
            i++;
            if (i == snpSize) {
                break;
            }
        }
        a = i;
        a /= snpSize;
        a = (a - 0.5) / (1 - 0.5);
        //adjustedAlpha = alpha / (1 - a);
        tmpDouble = alpha / ((1 - a) * snpSize);

        i = 0;
        while ((i < snpSize) && (pValues.getQuick(i) <= (i + 1) * tmpDouble)) {
            i++;
        }
        //it must be less than this value
        logInfo(puporse, "Storey FDR Test", tmpDouble * (i + 1));
        return tmpDouble * (i + 1);
    }

    public static void main(String[] args) {
        try {
            //MultipleTestingMethod.simulationSimesTest();
            //MultipleTestingMethod.simulationModifiedSidakTest();
            //MultipleTestingMethod.simulationTestEstimateNullHypothesisProportion();
            // System.out.println(MultipleTestingMethod.hypergeometricEnrichmentTest(62787, 35310, 40914, 26543));
            // DecimalFormat df = new DecimalFormat("0.00E0", new DecimalFormatSymbols(Locale.US));
            // MultipleTestingMethod.simulationTestWeightedCombinationProbabilities();
            /*
             if (true) {
             MultipleTestingMethod.simulationFDR();
             return;
             }
            
             ChiSquaredDistribution chiDis = new ChiSquaredDistributionImpl(1);
            
            
             double I2 = 1E-311;
             I2 = Probability.normalInverse(0.5 * I2);// two tails to be one tail
             I2 = I2 * I2;
             System.out.println(I2);
             I2 = Probability.chiSquareComplemented(1, I2);
            
             String formattedNumber = df.format(I2, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
             System.out.println(formattedNumber);
             double Q = -2;
             I2 = 1 - Probability.normal(Q);
             formattedNumber = df.format(I2, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
             System.out.println(formattedNumber);
            
             I2 = Probability.chiSquareComplemented(1, Q * Q) / 2;
             if (Q < 0) {
             I2 = 1 - I2;
             }
            
             double[] weights = {0.9, 0.1};
             double[] pValues = new double[2];
             pValues[0] = 1E-30;
             pValues[1] = 1E-3;
             double x = -2E-12;
             double I2 = (weightedCombinationProbabilities(weights, pValues));
            
            
             int n = 2;
             double p1 = 0;
            
             double s = 1;
             for (int k = n; k >= 1; k--) {
             s *= ((n - k + 1) * 1.0 / k);
             }
            
             for (int i = n; i >= 1; i--) {
             p1 += (s * Math.pow(x, i));
             String formattedNumber = df.format(p1, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
             System.out.println(formattedNumber);
             s *= (i / ((n - i + 1) * 1.0));
             }
             String formattedNumber = df.format(-p1, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
             System.out.println(formattedNumber);
            
             I2 = 1 - Math.pow(1 + x, n);
             formattedNumber = df.format(I2, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
             System.out.println(formattedNumber);
            
            
             List<double[]> pVs = new ArrayList<double[]>();
             pVs.add(new double[]{2.46E-06, 6.58E-05});
             pVs.add(new double[]{7.97E-06, 1.38E-04});
             pVs.add(new double[]{2.11E-06, 1.21E-03});
             pVs.add(new double[]{3.37E-06, 1});
             pVs.add(new double[]{1.85E-05, 1.35E-02});
             pVs.add(new double[]{1.12E-05, 1});
             pVs.add(new double[]{1.80E-04, 1});
             pVs.add(new double[]{3.96E-06, 4.30E-04});
             pVs.add(new double[]{2.60E-05, 9.15E-08});
             pVs.add(new double[]{4.27E-04, 1});
             pVs.add(new double[]{3.23E-04, 1});
             pVs.add(new double[]{1.00E-04, 1});
             double[] weights = {1, 1};
             for (int i = 0; i < pVs.size(); i++) {
             double p = (weightedCombinationProbabilities(weights, pVs.get(i)));
             String formattedNumber = df.format(p, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
             System.out.println(formattedNumber);
             }
             * 
             */

            // public static double hypergeometricEnrichmentTest(int populationSize, int subPopulationSize, int sampleSize,            int subSampleSize)
            System.out.println(MultipleTestingMethod.hypergeometricEnrichmentTest(19000, 190, 20, 2));
            double[] ps = new double[]{5.89E-08, 0.00000336, 0.00000412, 0.0000106, 0.000011, 0.0000139, 0.0000198, 0.0000204, 0.0000219, 0.0000242, 0.0000492, 0.0000495, 0.0000659, 0.000071, 0.0000732, 0.0000892, 0.0000974, 0.000106, 0.000115, 0.000116, 0.000143, 0.000224, 0.00024, 0.000298, 0.000332, 0.000344, 0.000353, 0.000367, 0.000377, 0.000422, 0.000467, 0.000474, 0.000493, 0.000496, 0.000518, 0.000543, 0.00055, 0.000574, 0.000578, 0.00058, 0.000613, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.361, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.362, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.363, 0.364, 0.364, 0.364, 0.364, 0.364, 0.364};
            DoubleArrayList plist = new DoubleArrayList(ps);

            double s = MultipleTestingMethod.BenjaminiHochbergFDR(null, 0.05, plist);
            System.out.println(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Algorithm proposed by  Statistics & Probability Letters 73 (2005) 179–187; A simple approximation for the distribution of the weighted combination ofnon-independent or independent probabilities
    // and Liptak-Stouffer method  http://en.wikipedia.org/wiki/Fisher%27s_method
    public static double weightedCombinationProbabilities(String puporse, double[] weights, double[] pValues) {
        double p = 1;
        double w2 = 0;
        double w = 0;
        int size = weights.length;
        double X = 0;
        double Z = 0;
        double q = 0;

        // weights[0] = 1;
        //  weights[1] = 1;
        for (int i = 0; i < size; i++) {
            if (pValues[i] > 0.5) {
                if (pValues[i] >= 1) {
                    //the Probability.normalInverse could handle 1E-323 but cannot handle 1-(1E-323)
                    q = Probability.normalInverse(1E-323);
                } else {
                    q = Probability.normalInverse(1 - pValues[i]);
                }
            } else {
                if (pValues[i] < 1E-320) {
                    pValues[i] = 1E-320;
                }
                q = -Probability.normalInverse(pValues[i]);
            }

            // q =  Probability.normalInverse(1 - pValues[i]);
            w2 += weights[i] * weights[i];
            Z += weights[i] * q;
            //w += weights[i];
            //X += weights[i] * Math.log(pValues[i]);
        }
        Z = Z / Math.sqrt(w2);

        /*
         //Statistics & Probability Letters 73 (2005) 179–187
         double c = w2 / w;
         double f = 2 * w * w / w2;
         I2 = Probability.chiSquareComplemented(f, -2 * X / c);
         */
        //Liptak-Stouffer method  http://en.wikipedia.org/wiki/Fisher%27s_method
        //I2 = 1 - Probability.normal(Q);
        p = Probability.chiSquareComplemented(1, Z * Z) / 2;
        if (Z < 0) {
            p = 1 - p;
        }
        // logInfo(puporse, "Weighted Combination Probabilities", p);
        return p;
    }

    public static double combinePValuebyFisherCombinationTest(String puporse, double[] pValueArray) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            // logInfo(puporse, "Combine PValue by Fisher Combination Test", (double) 1);
            return 1;
        } else if (snpSize == 1) {
            // logInfo(puporse, "Combine PValue by Fisher Combination Test", pValueArray[0]);
            return pValueArray[0];
        }

        double Y = 0;
        for (int i = 0; i < snpSize; i++) {
            Y += (-2 * Math.log(pValueArray[i]));
        }

        double p = Probability.chiSquareComplemented(2 * snpSize, Y);
        if (p < 0.0) {
            p = 0.0;
        }
        // logInfo(puporse, "Combine PValue by Fisher Combination Test", p);
        return p;
    }

    public static double combinationHeterogeneityCochranQTest(String puporse, final double[] pValues) {
        double p = 1;
        double Q = 0;
        double meanQ = 0;
        int size = pValues.length;
        double[] qValues = new double[size];
        // assume they are two-tailed I2-values
        for (int i = 0; i < size; i++) {
            qValues[i] = pValues[i];
            //the Probability.normalInverse could handle 1E-323 but cannot handle 1-(1E-323)
            if (qValues[i] > 0.5) {
                qValues[i] = 1 - qValues[i];
                if (qValues[i] < 1E-323) {
                    qValues[i] = 1E-323;
                }
                qValues[i] = -Probability.normalInverse(qValues[i]);

            } else {
                if (qValues[i] < 1E-323) {
                    qValues[i] = 1E-323;
                }
                qValues[i] = Probability.normalInverse(qValues[i]);
            }

            //Cochran's Q statistic  http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0000841
            // meanQ += qValues[i];
        }

        Q = qValues[0] - qValues[1];
        Q = Q * Q / 2;
        p = Probability.chiSquareComplemented(1, Q);
        //Quantifying heterogeneity in a meta-analysis Julian P. T. Higgins∗; † and Simon G. Thompson  I 2 = 100%×(Q − df)/Q http://onlinelibrary.wiley.com/doi/10.1002/sim.1186/pdf       
        if (p < 0) {
            p = 0;
        }
        //  logInfo(puporse, "Combination Heterogeneity CochranQ Test", p);
        return p;
    }

    public static double combinationHeterogeneityI2(String puporse, final double[] pValues) {
        double I2 = 1;
        double Q = 0;

        double meanQ = 0;
        int size = pValues.length;
        double[] qValues = new double[size];
        // assume they are two-tailed I2-values
        for (int i = 0; i < size; i++) {
            qValues[i] = pValues[i];
            //the Probability.normalInverse could handle 1E-323 but cannot handle 1-(1E-323)
            if (qValues[i] > 0.5) {
                qValues[i] = 1 - qValues[i];
                if (qValues[i] < 1E-323) {
                    qValues[i] = 1E-323;
                }
                qValues[i] = -Probability.normalInverse(qValues[i]);

            } else {
                if (qValues[i] < 1E-323) {
                    qValues[i] = 1E-323;
                }
                qValues[i] = Probability.normalInverse(qValues[i]);
            }
        }

        Q = qValues[0] - qValues[1];
        Q = Q * Q / 2;

        //Quantifying heterogeneity in a meta-analysis Julian P. T. Higgins∗; † and Simon G. Thompson  I 2 = 100%×(Q − df)/Q http://onlinelibrary.wiley.com/doi/10.1002/sim.1186/pdf
        I2 = 1 - 1 / Q;
        if (I2 < 0) {
            I2 = 0;
        }
        //  logInfo(puporse, "Combination Heterogeneity I2", I2);
        return I2;
    }

    /*
     BufferedWriter bw = new BufferedWriter(new FileWriter("test.txt"));
     for (int i = 0; i < pValues.size(); i++) {
     bw.write(String.valueOf(pValues.getQuick(i)));
     bw.newLine();
     }
     bw.close();
    
     pp <- scan("D:/home/mxli/MyJava/KGG2Eclipse/test.txt", quiet= TRUE);
     #hist(pp, breaks=1000);
    
     observed <- sort(pp)
     lobs <- -(log10(observed))
    
     expected <- c(1:length(observed))
     lexp <- -(log10(expected / (length(expected)+1)))
    
    
    
     #pdf("qqplot.pdf", width=6, height=6)
     plot(c(0,7), c(0,7), col="red", lwd=3, type="l", xlab="Expected (-logP)", ylab="Observed (-logP)", xlim=c(0,7), ylim=c(0,7), las=1, xaxs="i", yaxs="i", bty="l")
     points(lexp, lobs, pch=23, cex=.4, bg="black")
    
     *
     */
    //note: try to make sampleSize smaller than subPopulationSize
    //for example (19061, 89, 307, 8) will not work, but (19061, 307, 89, 8) wiil although they have the same p value
    public static double hypergeometricEnrichmentTest(int populationSize, int subPopulationSize, int sampleSize,
            int subSampleSize) throws Exception {
        if (sampleSize == 0) {
            // logInfo("","Hypergeometric Enrichment Test", (double) 1);
            return 1;
        }
        double p = -9;
        if (subPopulationSize < sampleSize) {
            int tmp = sampleSize;
            sampleSize = subPopulationSize;
            subPopulationSize = tmp;
        }

        /*
         //calcluate pvalues       
         HyperGeometric hp = new HyperGeometric(populationSize, subPopulationSize, sampleSize, null);
         double I2 = 0;
         for (int k = 0; k < subSampleSize; k++) {
         I2 += hp.pdf(k);
         }
         p = 1 - I2;
              
        
         //I found the above colt function is more robust 
         //For example, this function does not work for  (19061, 307, 89, 8))
         HypergeometricDistribution hpd = new HypergeometricDistribution(populationSize, subPopulationSize, sampleSize);
        
         p = 1 - hpd.cumulativeProbability(0, subSampleSize - 1);
         if (p < 0) {
         p = 1.0E-40;
         }
         */
        //Later, I found this function is the most robust function among the three and the results are identical to R 1-phyper(7, 289,19061-289, 407)
        DiscreteDistributionInt hpd1 = new HypergeometricDist(subPopulationSize, populationSize, sampleSize);
        p = 1 - hpd1.cdf(subSampleSize - 1);

        if (Double.isNaN(p)) {
            p = -9;
        }
        // logInfo("","Hypergeometric Enrichment Test", p);
        return p;
    }

    public static void logInfo(String purpose, String strTestName, Double dblThreshold) {
        Logger logOutput = Logger.getRootLogger();
        SimpleDateFormat sdfData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strOutput = purpose + "-->" + strTestName + "-->The threshold is: " + dblThreshold;
        logOutput.info(strOutput);
        if (GlobalManager.aotcWindow != null) {
            GlobalManager.aotcWindow.insertMessage("\nINFO " + sdfData.format(new Date()) + " - " + strOutput + "\n");
        }
    }
}
