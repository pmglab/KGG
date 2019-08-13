/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.math;

import cern.jet.stat.Probability;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
 

/**
 *
 * @author limx54
 */
public class OutlierDetector {

    LinearRegression linReg = new LinearRegression();
    List<double[]> indexY = new ArrayList<double[]>();
    DoubleArrayListComparator cp = new DoubleArrayListComparator(0);

    public double[] iterativeRobustZScore(double[] data, int maxIterN, boolean needP) {
        int tissueSizeOrg = data.length;
        int tissueSize;
        int ignoredGene = 0;
        double[][] x;
        double[] y;

        double wMean, wSD;
        boolean success = false;
        indexY.clear();
        double[] coef = new double[2];
        double diff, maxDiff, MINDIFF = 1e-8;

        //data[0] = 6;
        double[] standardizedData = new double[tissueSizeOrg];
        double[] finalWeights = new double[tissueSizeOrg];
        double[] residues = new double[tissueSizeOrg];
        Arrays.fill(coef, 0);
        for (int j = 0; j < tissueSizeOrg; j++) {
            if (!Double.isNaN(data[j])) {
                indexY.add(new double[]{data[j], j});
            }
        }

        //because of missing values the tissue number may be diffrent
        tissueSize = indexY.size();
        if (tissueSize < 10) {
            ignoredGene++;
            return null;
        }
        Collections.sort(indexY, cp);
        x = new double[tissueSize][2];
        y = new double[tissueSize];
        double[] weights0 = new double[tissueSize];
        double[] weights1 = new double[tissueSize];
        Arrays.fill(weights0, 1);

        for (int j = 0; j < tissueSize; j++) {
            y[j] = indexY.get(j)[0];
            x[j][0] = 1;
            x[j][1] = j + 1;
        }
        int iter = 0;
        do {
            for (int j = 0; j < tissueSize; j++) {
                y[j] = indexY.get(j)[0];
            }
            //iteratively standize the expression values untile the coefficients are converged
            wMean = StdStats.mean(y, weights0, true);
            //sd = StdStats.stddevWithScaledWeights(residual);
            wSD = StdStats.stddevWithScaledWeights(y, weights0);
            for (int j = 0; j < tissueSize; j++) {
                y[j] = (y[j] - wMean) / wSD;
                //convert to uniform distribution
                if (y[j] <= 0) {
                    // y[j] = Probability.normal(y[j]);
                } else {
                    // y[j] = 1 - Probability.normal(-y[j]);
                }
            }

            success = linReg.robustLinearRegression(y, x, 100, 1);
            if (!success) {
                System.out.println(success);
            }
            //System.arraycopy(linReg.coef, 0, coef, 0, coef.length);
            System.arraycopy(linReg.getWeight(), 0, weights1, 0, weights1.length);
            StdStats.standWeight(weights1);
            maxDiff = Math.abs(weights0[0] - weights1[0]);
            for (int i = 1; i < weights0.length; i++) {
                diff = Math.abs(weights0[i] - weights1[i]);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }
            if (maxDiff < MINDIFF) {
                break;
            }
            //revserve weights
            System.arraycopy(weights1, 0, weights0, 0, weights0.length);
            iter++;
        } while (iter < maxIterN);
        if (iter >= maxIterN) {
            System.out.println("Over");
        }
        System.arraycopy(linReg.getWeight(), 0, weights0, 0, weights0.length);
        double[] residual = linReg.getResidual();
        for (int j = 0; j < tissueSize; j++) {
            residues[(int) (indexY.get(j)[1])] = -residual[j];
            finalWeights[(int) (indexY.get(j)[1])] = weights0[j];
            //data[(int) (indexY.get(j)[1])] = y[j];
        }
        double sampleMean = StdStats.mean(data, finalWeights, true);
        double sampleSD = StdStats.stddevWithScaledWeights(data, finalWeights);
        double p;
        for (int j = 0; j < tissueSizeOrg; j++) {
            standardizedData[j] = data[j] - sampleMean;
            standardizedData[j] /= sampleSD;
            p = standardizedData[j];
            if (p > 0) {
                p = -p;
            }

            p = Probability.normal(0, 1, p);
            p *= 2;
            standardizedData[j] = p;
        }

        return standardizedData;
    }

    public double[] robustZScore(double[] data, boolean needP) {
        int tissueSizeOrg = data.length;
        int tissueSize;
        int ignoredGene = 0;
        double[][] x;
        double[] y;

        double wMean, wSD;
        boolean success = false;
        indexY.clear();
        double[] coef = new double[2];
        double diff, maxDiff, MINDIFF = 1e-8;

        double[] standardizedData = new double[tissueSizeOrg];
        double[] finalWeights = new double[tissueSizeOrg];
        double[] residues = new double[tissueSizeOrg];
        Arrays.fill(coef, 0);
        Arrays.fill(finalWeights, Double.NaN);
        Arrays.fill(residues, Double.NaN);
        for (int j = 0; j < tissueSizeOrg; j++) {
            if (!Double.isNaN(data[j]) && Double.isFinite(data[j])) {
                indexY.add(new double[]{data[j], j});
            }
        }

        //because of missing values the tissue number may be diffrent
        tissueSize = indexY.size();
        if (tissueSize < 10) {
            ignoredGene++;
            return null;
        }

        Collections.sort(indexY, cp);
        x = new double[tissueSize][2];
        y = new double[tissueSize];
        double[] weights = new double[tissueSize];
        Arrays.fill(weights, 1);

        for (int j = 0; j < tissueSize; j++) {
            y[j] = indexY.get(j)[0];
            x[j][0] = 1;
            x[j][1] = j + 1;
        }

        success = linReg.robustLinearRegression(y, x, 1000, 1);
        if (!success) {
            System.out.println(success);
        }

        weights = linReg.getWeight();
        double[] residual = linReg.getResidual();
        for (int j = 0; j < tissueSize; j++) {
            residues[(int) (indexY.get(j)[1])] = -residual[j];
            finalWeights[(int) (indexY.get(j)[1])] = weights[j];
            //data[(int) (indexY.get(j)[1])] = y[j];
        }

        double sampleMean = StdStats.mean(data, finalWeights, true);
        double sampleSD = StdStats.stddevWithScaledWeights(data, finalWeights);
        //   sampleMean = StdStats.mean(data);
        //  sampleSD = StdStats.stddevWithScaledWeights(data);  
        // System.out.println(sampleMean + "\t" + sampleSD);
        double p;
        if (sampleSD == 0) {
            for (int j = 0; j < tissueSizeOrg; j++) {
                if (needP) {
                    standardizedData[j] = 0.5;
                } else {
                    standardizedData[j] = 0;
                }
            }

        } else {
            for (int j = 0; j < tissueSizeOrg; j++) {
                standardizedData[j] = data[j] - sampleMean;
                standardizedData[j] /= sampleSD;
                if (needP) {
                    p = standardizedData[j];
                    if (p > 0) {
                        p = -p;
                    }

                    p = Probability.normal(0, 1, p);
                    p *= 2;
                    standardizedData[j] = p;
                }
            }
        }

        return standardizedData;
    }

    public double[] robustMeanSE(double[] data) {
        int tissueSizeOrg = data.length;
        int tissueSize;
        int ignoredGene = 0;
        double[][] x;
        double[] y;

        double wMean, wSD;
        boolean success = false;
        indexY.clear();
        double[] coef = new double[2];
        double diff, maxDiff, MINDIFF = 1e-8;

        double[] finalWeights = new double[tissueSizeOrg];
        double[] residues = new double[tissueSizeOrg];
        Arrays.fill(coef, 0);
        Arrays.fill(finalWeights, Double.NaN);
        Arrays.fill(residues, Double.NaN);
        for (int j = 0; j < tissueSizeOrg; j++) {
            if (!Double.isNaN(data[j]) && Double.isFinite(data[j])) {
                indexY.add(new double[]{data[j], j});
            }
        }

        //because of missing values the tissue number may be diffrent
        tissueSize = indexY.size();
        if (tissueSize < 5) {
            ignoredGene++;
            return null;
        }

        Collections.sort(indexY, cp);
        x = new double[tissueSize][2];
        y = new double[tissueSize];
        double[] weights = new double[tissueSize];
        Arrays.fill(weights, 1);

        for (int j = 0; j < tissueSize; j++) {
            y[j] = indexY.get(j)[0];
            x[j][0] = 1;
            x[j][1] = j + 1;
        }

        success = linReg.robustLinearRegression(y, x, 100, 1);
        if (!success) {
            System.out.println(success);
        }

        weights = linReg.getWeight();
        double[] residual = linReg.getResidual();

        for (int j = 0; j < tissueSize; j++) {
            residues[(int) (indexY.get(j)[1])] = -residual[j];
            finalWeights[(int) (indexY.get(j)[1])] = weights[j];
            //data[(int) (indexY.get(j)[1])] = y[j];
        }

        double sampleMean = StdStats.mean(data, finalWeights, true);
        double sampleSD = StdStats.stddevWithScaledWeights(data, finalWeights);
        //   sampleMean = StdStats.mean(data);
        //  sampleSD = StdStats.stddevWithScaledWeights(data);  
        // System.out.println(sampleMean + "\t" + sampleSD);
        double effectiveSize = 0;
        double effectiveSize1 = 0;
        for (int j = 0; j < tissueSize; j++) {
            effectiveSize1 += finalWeights[j];
            effectiveSize += (finalWeights[j] * finalWeights[j]);
        }
        effectiveSize = (effectiveSize1 * effectiveSize1) / effectiveSize;

        double[] results = new double[2];
        results[0] = sampleMean;
        results[1] = sampleSD / Math.sqrt(effectiveSize);

        return results;
    }

    public double[] robustZScore(double[] data, double[] seWeights, boolean needP) {
        int tissueSizeOrg = data.length;
        int tissueSize;

        double[][] x;
        double[] y;
        double[] w;

        boolean success = false;
        indexY.clear();
        double[] coef = new double[2];
        double diff, maxDiff, MINDIFF = 1e-8;

        double[] standardizedData = new double[tissueSizeOrg];
        double[] finalWeights = new double[tissueSizeOrg];
        double[] residues = new double[tissueSizeOrg];
        Arrays.fill(coef, 0);
        Arrays.fill(finalWeights, Double.NaN);
        Arrays.fill(residues, Double.NaN);
        for (int j = 0; j < tissueSizeOrg; j++) {
            if (!Double.isNaN(data[j]) && Double.isFinite(data[j])) {
                indexY.add(new double[]{data[j], j, seWeights[j]});
            }
        }

        //because of missing values the tissue number may be diffrent
        tissueSize = indexY.size();
        if (tissueSize < 10) {
            return null;
        }

        double infa = Math.sqrt(1.5);
        Collections.sort(indexY, cp);
        x = new double[tissueSize][2];
        y = new double[tissueSize];
        w = new double[tissueSize];
        double[] weights = new double[tissueSize];
        Arrays.fill(weights, 1);

        for (int j = 0; j < tissueSize; j++) {
            y[j] = indexY.get(j)[0];
            w[j] = indexY.get(j)[2];
            x[j][0] = 1;
            x[j][1] = j + 1;

            // w[j] = 1;
        }

        success = linReg.weightedRobustLinearRegression(y, x, w, 10000, 1);
        if (!success) {
            System.out.println(success);
        }

        weights = linReg.getWeight();
        double[] residual = linReg.getResidual();
        for (int j = 0; j < tissueSize; j++) {
            residues[(int) (indexY.get(j)[1])] = -residual[j];
            finalWeights[(int) (indexY.get(j)[1])] = weights[j];
            //data[(int) (indexY.get(j)[1])] = y[j];
        }

        double sampleMean = StdStats.mean(data, finalWeights, true);
        double sampleSD = StdStats.stddevWithScaledWeights(data, finalWeights);
        //   sampleMean = StdStats.mean(data);
        //  sampleSD = StdStats.stddevWithScaledWeights(data);  
        // System.out.println(sampleMean + "\t" + sampleSD);
        double p;
        if (sampleSD == 0) {
            for (int j = 0; j < tissueSizeOrg; j++) {
                if (needP) {
                    standardizedData[j] = 0.5;
                } else {
                    standardizedData[j] = 0;
                }
            }

        } else { 
            for (int j = 0; j < tissueSizeOrg; j++) {
                standardizedData[j] = (data[j] - sampleMean);
                standardizedData[j] /= (sampleSD * infa);
                if (needP) {
                    p = standardizedData[j];
                    if (p > 0) {
                        p = -p;
                    }

                    p = Probability.normal(0, 1, p);
                    p *= 2;
                    standardizedData[j] = p;
                }
            }
        }

        return standardizedData;
    }

    public double[] robustZScore(double[] data, boolean needP, double[][] dis) {
        int tissueSizeOrg = data.length;
        int tissueSize;
        int ignoredGene = 0;
        double[][] x;
        double[] y;

        double wMean, wSD;
        boolean success = false;
        indexY.clear();
        double[] coef = new double[2];
        double diff, maxDiff, MINDIFF = 1e-8;

        double[] standardizedData = new double[tissueSizeOrg];
        double[] finalWeights = new double[tissueSizeOrg];
        double[] residues = new double[tissueSizeOrg];
        Arrays.fill(coef, 0);
        for (int j = 0; j < tissueSizeOrg; j++) {
            if (!Double.isNaN(data[j])) {
                indexY.add(new double[]{data[j], j});
            }
        }

        //because of missing values the tissue number may be diffrent
        tissueSize = indexY.size();
        if (tissueSize < 10) {
            ignoredGene++;
            return null;
        }

        Collections.sort(indexY, cp);
        x = new double[tissueSize][2];
        y = new double[tissueSize];
        double[] weights = new double[tissueSize];
        Arrays.fill(weights, 1);
        double diss;
        for (int j = 0; j < tissueSize; j++) {
            y[j] = indexY.get(j)[0];
            x[j][0] = 1;
        }
        x[0][1] = 0;
        for (int j = 1; j < tissueSize; j++) {
            //x[j][1] = j + 1;
            diss = dis[(int) (indexY.get(j - 1)[1])][(int) (indexY.get(j)[1])];
            diss = Math.pow(diss, 2) * 10;
            //System.out.println(diss);
            //diss=1;
            x[j][1] = (x[j - 1][1] + diss);
            // System.out.println( x[j][1] );
        }
        

        success = linReg.robustLinearRegression(y, x, 100, 1);
        if (!success) {
            System.out.println(success);
        }

        weights = linReg.getWeight();
        double[] residual = linReg.getResidual();
        for (int j = 0; j < tissueSize; j++) {
            residues[(int) (indexY.get(j)[1])] = -residual[j];
            finalWeights[(int) (indexY.get(j)[1])] = weights[j];
            //data[(int) (indexY.get(j)[1])] = y[j];
            //System.out.println(y[j] + " " + x[j][1] + " " + weights[j] + " " + " " + (-residual[j]));
        }

        double sampleMean = StdStats.mean(data, finalWeights, true);
        double sampleSD = StdStats.stddevWithScaledWeights(data, finalWeights);
        //   sampleMean = StdStats.mean(data);
        //  sampleSD = StdStats.stddevWithScaledWeights(data);           
        double p;
        for (int j = 0; j < tissueSizeOrg; j++) {
            standardizedData[j] = data[j] - sampleMean;
            standardizedData[j] /= sampleSD;
            if (needP) {
                p = standardizedData[j];
                if (p > 0) {
                    p = -p;
                }

                p = Probability.normal(0, 1, p);
                p *= 2;
                //System.out.println(data[j] + "\t" + standardizedData[j] + "\t" + p);
                standardizedData[j] = p;
            }

        }

        return standardizedData;
    }

    public double[] zScore(double[] data, boolean needP) {
        double sampleMean = StdStats.mean(data);
        double sampleSD = StdStats.stddev(data);
        int tissueSizeOrg = data.length;
        double[] standardizedData = new double[tissueSizeOrg];
        double p;
        for (int j = 0; j < tissueSizeOrg; j++) {
            standardizedData[j] = data[j] - sampleMean;
            standardizedData[j] /= sampleSD;
            if (needP) {
                p = standardizedData[j];
                if (p > 0) {
                    p = -p;
                }

                p = Probability.normal(0, 1, p);
                p *= 2;
                standardizedData[j] = p;
            }

        }

        return standardizedData;
    }
}
