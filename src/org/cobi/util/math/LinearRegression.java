/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.math;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * modified from http://www.codeproject.com/KB/recipes/LinReg.aspx by MX Li
 */
public class LinearRegression {

    double[][] covar;            // Least squares and var/covar matrix
    public double[] coef;      // Coefficients
    public double[] coefSD;    // Std Error of coefficients
    double RYSQ;            // Multiple correlation coefficient
    double SDV;             // Standard deviation of errors
    double FReg;            // Fisher F statistic for regression
    double[] fitY;         // Calculated values of Y
    double[] residual;            // Residual values of Y
    double[] weight;  //

    public double[] getResidual() {
        return residual;
    }

    public double[] getWeight() {
        return weight;
    }

    public boolean robustLinearRegression(double[] Y, double[][] X, int iterN, int methodID) {
        weight = new double[Y.length];
        Arrays.fill(weight, 1);
        return weightedRobustLinearRegression(Y, X, weight, iterN, methodID);
    }

    //https://onlinecourses.science.psu.edu/stat501/node/353
    public boolean weightedRobustLinearRegression(double[] Y, double[][] X, double[] iniWeights, int iterN, int methodID) {
        int iterI = 0;
        int size = iniWeights.length;
        double MINDIFF = 1e-6;
        double maxDiff = 0, tempDiff;
        double sd = StdStats.stddev(Y);
        double c = 1.345 * sd;
        //Bisquare
        if (methodID == 2) {
            c = 4.685 * sd;
        }
        boolean success = false;
        weightedLeastSquaresArray(Y, X, iniWeights);
        double[] tmpCoef = new double[X[0].length];
        weight = new double[iniWeights.length];
        System.arraycopy(coef, 0, tmpCoef, 0, tmpCoef.length);
        System.arraycopy(iniWeights, 0, weight, 0, weight.length);
        double[] tmpResi = new double[size];
        while (iterI < iterN) {
            for (int i = 0; i < size; i++) {
                tmpResi[i] = Math.abs(residual[i]);
            }
            //update the SD by a median absolute residual which is emperical
            // sd = StdStats.medianNS(tmpResi) / 0.6745;
            //sd = StdStats.stddev(residual);
            sd = StdStats.stddevWithUnscaledWeights(residual, weight);
            // sd = StdStats.stddevWithUnscaledWeights(Y, weight);
            // System.out.println(sd);
            // sd = StdStats.stddev(Y);
            if (sd == 0) {
                break;
            }
            switch (methodID) {
                case 2:
                    c = 4.685 * sd;
                    break;
                case 1:
                    c = 1.345 * sd;
                    break;
            }

            for (int i = 0; i < size; i++) {
                switch (methodID) {
                    case 2:
                        if (Math.abs(residual[i]) <= c) {
                            weight[i] = (residual[i] / c);
                            weight[i] = weight[i] * weight[i];
                            weight[i] = 1 - weight[i];
                            weight[i] = weight[i] * weight[i];
                        } else {
                            weight[i] = 0;
                        }
                        break;
                    case 1:
                        if (Math.abs(residual[i]) <= c) {
                            weight[i] = 1;
                        } else {
                            weight[i] = c / Math.abs(residual[i]);
                        }
                        break;
                    default:
                        weight[i] = 1;
                        break;
                }
                weight[i] = weight[i] * iniWeights[i];
            }
            weightedLeastSquaresArray(Y, X, weight);

            maxDiff = Math.abs(coef[0] - tmpCoef[0]);
            for (int i = 1; i < tmpCoef.length; i++) {
                tempDiff = Math.abs(coef[i] - tmpCoef[i]);
                if (tempDiff > maxDiff) {
                    maxDiff = tempDiff;
                }
                //System.out.println(coef[i]);
            }
            //System.out.println();
            if (maxDiff <= MINDIFF) {
                success = true;
                break;
            }
            System.arraycopy(coef, 0, tmpCoef, 0, tmpCoef.length);
            iterI++;
        }
         
        return success;
    }

    private boolean weightedLeastSquaresArray(double[] Y, double[][] X, double[] W) {
        // Y[j]   = j-th observed data point
        // X[i,j] = j-th value of the i-th independent varialble
        // W[j]   = j-th weight value

        int M = Y.length;             // M = Number of data points
        int N = X[0].length;         // N = Number of linear terms
        int NDF = M - N;              // Degrees of freedom
        fitY = new double[M];
        residual = new double[M];
        // If not enough data, don't attempt regression
        if (NDF < 1) {
            return false;
        }
        covar = new double[N][N];
        coef = new double[N];
        coefSD = new double[N];
        double[] B = new double[N];   // Vector for LSQ

        // Clear the matrices to start out
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                covar[i][j] = 0;
            }
        }

        // Form Least Squares Matrix
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                covar[i][j] = 0;
                for (int k = 0; k < M; k++) {
                    covar[i][j] = covar[i][j] + W[k] * X[k][i] * X[k][j];
                }
            }
            B[i] = 0;
            for (int k = 0; k < M; k++) {
                B[i] = B[i] + W[k] * X[k][i] * Y[k];
            }
        }
        // V now contains the raw least squares matrix
        if (!symmetricMatrixInvert(covar)) {
            return false;
        }
        // V now contains the inverted least square matrix
        // Matrix multpily to get coefficients C = VB
        for (int i = 0; i < N; i++) {
            coef[i] = 0;
            for (int j = 0; j < N; j++) {
                coef[i] = coef[i] + covar[i][j] * B[j];
            }
        }

        // Calculate statistics
        double TSS = 0;
        double RSS = 0;
        double YBAR = 0;
        double WSUM = 0;
        for (int k = 0; k < M; k++) {
            YBAR = YBAR + W[k] * Y[k];
            WSUM = WSUM + W[k];
        }
        YBAR = YBAR / WSUM;
        for (int k = 0; k < M; k++) {
            fitY[k] = 0;
            for (int i = 0; i < N; i++) {
                fitY[k] = fitY[k] + coef[i] * X[k][i];
            }
            residual[k] = fitY[k] - Y[k];
            TSS = TSS + W[k] * (Y[k] - YBAR) * (Y[k] - YBAR);
            RSS = RSS + W[k] * residual[k] * residual[k];
        }
        double SSQ = RSS / NDF;
        RYSQ = 1 - RSS / TSS;
        FReg = 9999999;
        if (RYSQ < 0.9999999) {
            FReg = RYSQ / (1 - RYSQ) * NDF / (N - 1);
        }
        SDV = Math.sqrt(SSQ);

        // Calculate var-covar matrix and std error of coefficients
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                covar[i][j] = covar[i][j] * SSQ;
            }
            coefSD[i] = Math.sqrt(covar[i][i]);
        }

        return true;
    }

    public boolean symmetricMatrixInvert(double[][] V) {
        int N = V.length;
        double[] t = new double[N];
        double[] Q = new double[N];
        double[] R = new double[N];
        double AB;
        int K, L, M;

        // Invert a symetric matrix in V
        for (M = 0; M < N; M++) {
            R[M] = 1;
        }
        K = 0;
        for (M = 0; M < N; M++) {
            double Big = 0;
            for (L = 0; L < N; L++) {
                AB = Math.abs(V[L][L]);
                if ((AB > Big) && (R[L] != 0)) {
                    Big = AB;
                    K = L;
                }
            }
            if (Big == 0) {
                return false;
            }
            R[K] = 0;
            Q[K] = 1 / V[K][K];
            t[K] = 1;
            V[K][K] = 0;
            if (K != 0) {
                for (L = 0; L < K; L++) {
                    t[L] = V[L][K];
                    if (R[L] == 0) {
                        Q[L] = V[L][K] * Q[K];
                    } else {
                        Q[L] = -V[L][K] * Q[K];
                    }
                    V[L][K] = 0;
                }
            }
            if ((K + 1) < N) {
                for (L = K + 1; L < N; L++) {
                    if (R[L] != 0) {
                        t[L] = V[K][L];
                    } else {
                        t[L] = -V[K][L];
                    }
                    Q[L] = -V[K][L] * Q[K];
                    V[K][L] = 0;
                }
            }
            for (L = 0; L < N; L++) {
                for (K = L; K < N; K++) {
                    V[L][K] = V[L][K] + t[L] * Q[K];
                }
            }
        }
        M = N;
        L = N - 1;
        for (K = 1; K < N; K++) {
            M = M - 1;
            L = L - 1;
            for (int J = 0; J <= L; J++) {
                V[M][J] = V[J][M];
            }
        }
        return true;
    }

    /**
     * An example.
     *
     * testing R codes
     */
    //library(MASS) 
    //path<-"/mnt/A/limx/java/kggseq/lencount.txt"
    //dat<-read.table(path,sep = "\t", head=TRUE);
    //result1 <- lm(dat[,1] ~ dat[,2]+ dat[,3]+ dat[,4] + dat[,5],weights=rep(1,length(dat[,1] )))
    //summary(result1)
    //result1 <- rlm(dat[,1] ~ dat[,2]+ dat[,3]+ dat[,4] + dat[,5], maxit = 100)
    //summary(result1)
    public static void main(String[] args) {
        LinearRegression linReg = new LinearRegression();
        try {
            //,2,3,4
            List<String> tissueName = new ArrayList<String>();
            List<String> geneGenes = new ArrayList<String>();

            //double[][] data = linReg.loadDataTissueRow("rank-average-exprdata.expr", tissueName, geneGenes);
            String fileName = "MeanExpr_200.txt";
            //fileName = "MeanExpr_immune.txt";


            /*
            int runNum = 1000;
            long startTime = System.nanoTime();
            for (int i = 0; i < runNum; i++) {
                linReg.weightedLeastSquaresArray(y, x, w);
            }
            long endTime = System.nanoTime();
            System.out.println("程序运行时间： " + (endTime - startTime) + "ns");

            DoubleMatrix2D xx = new DenseDoubleMatrix2D(sN, pN);
            DoubleMatrix2D yy = new DenseDoubleMatrix2D(sN, 1);
            DoubleMatrix2D ww = new DenseDoubleMatrix2D(sN, sN);
            ww.assign(0);
            for (int k = 0; k < sN; k++) {
                for (int j = 0; j < pN; j++) {
                    xx.setQuick(k, j, x[k][j]);
                }
                yy.setQuick(k, 0, y[k]);
                ww.setQuick(k, k, 1);
            }

            startTime = System.nanoTime();
            for (int i = 0; i < runNum; i++) {
                linReg.weightedLeastSquareMatrix(xx, yy, ww);
            }
            endTime = System.nanoTime();
            System.out.println("程序运行时间： " + (endTime - startTime) + "ns");

            DoubleMatrix2D beta = linReg.weightedLeastSquareMatrix(xx, yy, ww);

            for (int i = 0; i < pN; i++) {
                System.out.println(beta.getQuick(i, 0));
            }
             */
        } catch (Exception nex) {
            nex.printStackTrace();
            //throw new Exception(info);
        }
    }

    private Algebra alg = new Algebra();

    //https://onlinecourses.science.psu.edu/stat501/node/352
    //Note this function is much slower than weightedLeastSquaresArray
    public DoubleMatrix2D weightedLeastSquareMatrix(DoubleMatrix2D xx, DoubleMatrix2D yy, DoubleMatrix2D ww) throws Exception {
        DoubleMatrix2D wx = alg.mult(alg.transpose(xx), ww);
        DoubleMatrix2D wy = alg.mult(wx, yy);
        wx = alg.mult(wx, xx);
        DoubleMatrix2D beta = alg.solve(wx, wy);
        //DoubleMatrix2D beta = mySudoSVDSolverColt(wx, wy);

        return beta;
    }

}
