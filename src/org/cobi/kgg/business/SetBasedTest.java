/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.stat.Gamma;
import cern.jet.stat.Probability;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cobi.kgg.business.entity.PValueWeight;
import org.cobi.kgg.business.entity.PValueWeightComparator;
import org.cobi.util.thread.Task;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;

/**
 *
 * @author mxli
 */
public class SetBasedTest {

    static public double combineGATESPValuebyWeightedSimeCombinationTestMyMe(PValueWeight[] pValueArray, DoubleMatrix2D ldCorr, int[] keyBlockIndex) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return Double.NaN;
        } else if (snpSize == 1) {
            return pValueArray[0].pValue;
        }
        Arrays.sort(pValueArray, new PValueWeightComparator());
        pValueArray[0].effectiveIndex = 1;
        Set<Integer> selectedIndex = new HashSet<Integer>();

        double ess = 0;
        double accumulatedIndex = 1;
        double totalRatio = pValueArray[0].effectiveIndex * pValueArray[0].weight;

        for (int i = 0; i < snpSize; i++) {
            selectedIndex.add(pValueArray[i].physicalPos);
            for (int j = i + 1; j < snpSize; j++) {
                if (i == j) {
                    continue;
                }

                //poweredcorrMat.setQuick(j, j, Math.pow(corrMat.getQuick(j, j), power));
                double x = ldCorr.getQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos);
                // x = x * x;
                //when r2                 
                //this is the key difference compared to above combinePValuebyWeightedSimeCombinationTestMyMe
                // x = 0.945 * Math.sqrt(x);
                //x = 0.945 * x;
                //when r2                 
                //I do not know why it seems if I use x=x*x  it woks better in terms of type 1 error
                x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                ldCorr.setQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos, x);
                ldCorr.setQuick(pValueArray[j].physicalPos, pValueArray[i].physicalPos, x);
            }
        }

        double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
        selectedIndex.clear();
        selectedIndex.add(pValueArray[0].physicalPos);
        snpSize--;
        for (int i = 1; i < snpSize; i++) {
            selectedIndex.add(pValueArray[i].physicalPos);
            ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
            pValueArray[i].effectiveIndex = ess - accumulatedIndex;
            accumulatedIndex = ess;
            totalRatio += pValueArray[i].effectiveIndex * pValueArray[i].weight;
        }
        //to save time
        pValueArray[snpSize].effectiveIndex = totalEffetiveSize - accumulatedIndex;
        totalRatio += pValueArray[snpSize].effectiveIndex * pValueArray[snpSize].weight;
        snpSize++;

        double factor = totalEffetiveSize / totalRatio;
        pValueArray[0].effectiveIndex = pValueArray[0].effectiveIndex * pValueArray[0].weight * factor;
        for (int i = 1; i < snpSize; i++) {
            pValueArray[i].effectiveIndex = pValueArray[i].effectiveIndex * pValueArray[i].weight * factor + pValueArray[i - 1].effectiveIndex;
        }
        double minP = totalEffetiveSize * pValueArray[0].pValue / pValueArray[0].effectiveIndex;
        double p;
        for (int i = 1; i < snpSize; i++) {
            p = totalEffetiveSize * pValueArray[i].pValue / pValueArray[i].effectiveIndex;
            if (p < minP) {
                minP = p;
                keyBlockIndex[0] = i;
            }
        }
        return minP;
    }

    static public double combineGATESPValuebyNoWeightSimeCombinationTestMyMe(PValueWeight[] pValueArray,
            DoubleMatrix2D ldCorr, int[] keySNPPosition) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return Double.NaN;
        } else if (snpSize == 1) {
            keySNPPosition[0] = pValueArray[0].physicalPos;
            return pValueArray[0].pValue;
        }
        Arrays.sort(pValueArray, new PValueWeightComparator());
        pValueArray[0].effectiveIndex = 1;
        Set<Integer> selectedIndex = new HashSet<Integer>();

        double ess = 0;
        double accumulatedIndex = 1;
        double totalRatio = pValueArray[0].effectiveIndex * pValueArray[0].weight;

        for (int i = 0; i < snpSize; i++) {
            selectedIndex.add(pValueArray[i].physicalPos);
            for (int j = i + 1; j < snpSize; j++) {
                if (i == j) {
                    continue;
                }

                //poweredcorrMat.setQuick(j, j, Math.pow(corrMat.getQuick(j, j), power));
                double x = ldCorr.getQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos);
                // x = x * x;
                //when r2                 
                //this is the key difference compared to above combinePValuebyWeightedSimeCombinationTestMyMe
                // x = 0.945 * Math.sqrt(x);
                //x = 0.945 * x;
                //when r2                 
                //I do not know why it seems if I use x=x*x  it woks better in terms of type 1 error
                x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                ldCorr.setQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos, x);
                ldCorr.setQuick(pValueArray[j].physicalPos, pValueArray[i].physicalPos, x);
            }
        }
        //   System.out.println(ldCorr.toString());
        double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
        keySNPPosition[0] = pValueArray[0].physicalPos;
        double minP = totalEffetiveSize * pValueArray[0].pValue;
        double p;
        selectedIndex.clear();
        selectedIndex.add(pValueArray[0].physicalPos);
        ess = 0;

        double maxCurrMe = 1;
        for (int i = 1; i < snpSize; i++) {
            //Note: a smart decise to avoid unneccessary expoloration
            if (minP <= pValueArray[i].pValue) {
                return minP;
            }
            selectedIndex.add(pValueArray[i].physicalPos);

            //System.out.println(j);
            maxCurrMe += 1;
            if (minP > totalEffetiveSize * pValueArray[i].pValue / (maxCurrMe)) {
                ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
                maxCurrMe = ess;
                p = totalEffetiveSize * pValueArray[i].pValue / (ess);
                if (p < minP) {
                    keySNPPosition[0] = pValueArray[i].physicalPos;
                    minP = p;
                }
            }
        }

        return minP;

    }

    static public double combinePValuebyWeightedSimeCombinationTestMyMe(PValueWeight[] pValueArray,
            DoubleMatrix2D ldCorr, int[] bestSNPIndex) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return 1;
        } else if (snpSize == 1) {
            return pValueArray[0].pValue;
        }
        Arrays.sort(pValueArray, new PValueWeightComparator());
        pValueArray[0].effectiveIndex = 1;
        Set<Integer> selectedIndex = new HashSet<Integer>();

        double ess = 0;
        double accumulatedIndex = 1;
        double totalRatio = pValueArray[0].effectiveIndex * pValueArray[0].weight;

        for (int i = 0; i < snpSize; i++) {
            selectedIndex.add(pValueArray[i].physicalPos);
            for (int j = i + 1; j < snpSize; j++) {
                if (i == j) {
                    continue;
                }

                //poweredcorrMat.setQuick(j, j, Math.pow(corrMat.getQuick(j, j), power));
                double x = ldCorr.getQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos);
                x = x * x;
                //when r2
                //I do not know why it seems if I use x=x*x  it woks better in terms of type 1 error
                x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                //x = x * x;
                ldCorr.setQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos, x);
                ldCorr.setQuick(pValueArray[j].physicalPos, pValueArray[i].physicalPos, x);
            }
        }

        double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
        selectedIndex.clear();
        selectedIndex.add(pValueArray[0].physicalPos);
        snpSize--;
        for (int i = 1; i < snpSize; i++) {
            selectedIndex.add(pValueArray[i].physicalPos);
            ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
            pValueArray[i].effectiveIndex = ess - accumulatedIndex;
            accumulatedIndex = ess;
            totalRatio += pValueArray[i].effectiveIndex * pValueArray[i].weight;
        }
        //to save time
        pValueArray[snpSize].effectiveIndex = totalEffetiveSize - accumulatedIndex;
        totalRatio += pValueArray[snpSize].effectiveIndex * pValueArray[snpSize].weight;
        snpSize++;

        double factor = totalEffetiveSize / totalRatio;
        pValueArray[0].effectiveIndex = pValueArray[0].effectiveIndex * pValueArray[0].weight * factor;
        for (int i = 1; i < snpSize; i++) {
            pValueArray[i].effectiveIndex = pValueArray[i].effectiveIndex * pValueArray[i].weight * factor + pValueArray[i - 1].effectiveIndex;
        }
        bestSNPIndex[0] = pValueArray[0].physicalPos;
        double minP = totalEffetiveSize * pValueArray[0].pValue / pValueArray[0].effectiveIndex;
        double p;
        for (int i = 1; i < snpSize; i++) {
            p = totalEffetiveSize * pValueArray[i].pValue / pValueArray[i].effectiveIndex;
            if (p < minP) {
                minP = p;
                bestSNPIndex[0] = pValueArray[i].physicalPos;
            }
        }
        return minP;
    }

    public static void myGeneBasedTest() throws Exception {
        DoubleMatrix2D corrMat = new DenseDoubleMatrix2D(5, 5);
        corrMat.setQuick(0, 1, 0.8);
        corrMat.setQuick(0, 2, 0.78);
        corrMat.setQuick(0, 3, 0.43);
        corrMat.setQuick(0, 4, 0.72);

        corrMat.setQuick(1, 2, 0.86);
        corrMat.setQuick(1, 3, 0.35);
        corrMat.setQuick(1, 4, 0.93);

        corrMat.setQuick(2, 3, 0.4);
        corrMat.setQuick(2, 4, 0.93);

        corrMat.setQuick(3, 4, 0.37);

        /*
         * 0.8	0.78	0.43	0.72
         0.86	0.35	0.93
         0.4	0.93
         0.37       
         */
        int snpNum = corrMat.rows();
        System.out.println(corrMat.toString());
        double[][] pValues = new double[][]{{0.018, 0.05, 0.378, 0.023, 0.037, 0.244}, {0.047, 0.105, 0.248, 0.036, 0.065, 0.247},
        {0.01, 0.01, 0.075, 0.031, 0.028, 0.257},
        {0.091, 0.107, 0.397, 0.057, 0.105, 0.241}};

        for (int i = 0; i < snpNum; i++) {
            corrMat.set(i, i, 1);
            for (int j = i + 1; j < snpNum; j++) {
                double x = corrMat.getQuick(i, j);
                //when r2
                //y = 0.7723x6 - 1.5659x5 + 1.201x4 - 0.2355x3 + 0.2184x2 + 0.6086x
                x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                corrMat.set(i, j, x);
                corrMat.set(j, i, x);
            }
        }

        System.out.println(corrMat.toString());

        int rowN = pValues.length;
        int colN = pValues[0].length;

        //because it is very time consumming, when SNP number is over 200. I try to spl
        int maxBlockLen = 200;
        int maxCheckingNum = 20;
        int maxCheckingDistance = 100000;
        double weakCorrelationThreshold = 0.45;

        double totalEffetiveSize = 0;
        totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(corrMat, null);
        for (int j = 0; j < colN; j++) {
            System.out.println("Test " + j);
            List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();
            for (int i = 0; i < rowN; i++) {
                PValueWeight pw = new PValueWeight();
                pw.physicalPos = i;
                pw.pValue = pValues[i][j];
                pw.effectiveIndex = i;
                pw.weight = 1;
                pvalueWeightList.add(pw);
                System.out.println(pw.pValue);
            }

            Collections.sort(pvalueWeightList, new PValueWeightComparator());
            double minP = totalEffetiveSize * pvalueWeightList.get(0).pValue;
            double p;
            Set<Integer> selectedIndex = new HashSet<Integer>();
            selectedIndex.add(pvalueWeightList.get(0).physicalPos);
            double ess = 0;
            snpNum--;
            double maxCurrMe = 1;
            for (int i = 1; i <= snpNum; i++) {
                //Note: a smart decision to avoid unneccessary expoloration
                if (minP <= pvalueWeightList.get(i).pValue) {
                    break;
                }
                selectedIndex.add(pvalueWeightList.get(i).physicalPos);
                //System.out.println(j);
                maxCurrMe += 1;
                if (minP > totalEffetiveSize * pvalueWeightList.get(i).pValue / (maxCurrMe)) {
                    ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(corrMat, selectedIndex);
                    maxCurrMe = ess;
                    p = totalEffetiveSize * pvalueWeightList.get(i).pValue / (ess);
                    if (p < minP) {
                        minP = p;
                    }
                }
            }
            System.out.println(minP);
        }
    }

    static Algebra alg = new Algebra();

    public static double mySudoSVDSolverColt(DoubleMatrix2D A, double[] chisquares) throws Exception {
        cern.colt.matrix.linalg.SingularValueDecomposition svd = new cern.colt.matrix.linalg.SingularValueDecomposition(A);
        int size = A.columns();
        DoubleMatrix2D x1 = null;
        DoubleMatrix2D x2 = null;
        DoubleMatrix2D b1 = new DenseDoubleMatrix2D(size, 1);
        DoubleMatrix2D b2 = new DenseDoubleMatrix2D(size, 1);

        for (int i = 0; i < size; i++) {
            b1.set(i, 0, chisquares[i]);
            b2.set(i, 0, 1);
        }

        DoubleMatrix2D U = alg.transpose(svd.getU());
        DoubleMatrix2D W = svd.getS();
        // System.out.println(W.toString());
        DoubleMatrix2D V = svd.getV();

        double threshlod = 1e-6;
        double maxX1 = 0, maxB1 = 0;
        double maxX2 = 0, maxB2 = 0;
        int trancatedID = size;

        DoubleMatrix2D W1 = null;
        DoubleMatrix2D tb1 = null;
        DoubleMatrix2D tb2 = null;
        for (int t = 0; t < size; t++) {
            if (maxB1 < Math.abs(b1.get(t, 0))) {
                maxB1 = Math.abs(b1.get(t, 0));
            }
            if (maxB2 < Math.abs(b2.get(t, 0))) {
                maxB2 = Math.abs(b2.get(t, 0));
            }
            if (W.get(t, t) < threshlod) {
                W.set(t, t, 0);
            } else {
                W.set(t, t, 1 / W.get(t, t));
            }
        }
        double minDiff = 0.01 * size;
        maxB1 = maxB1 * 10;
        maxB2 = maxB2 * 10;
        trancatedID--;
        boolean needContinue = true;
        double diff = 0;
        do {
            W1 = alg.mult(alg.mult(V, W), U);
            x1 = alg.mult(W1, b1);
            maxX1 = 0;
            maxX2 = 0;
            for (int t = 0; t < size; t++) {
                if (maxX1 < Math.abs(x1.get(t, 0))) {
                    maxX1 = Math.abs(x1.get(t, 0));
                }
            }
            //  System.out.println("Max1: " + maxX1 + " : " + trancatedID);
            if (maxX1 <= maxB1) {
                x2 = alg.mult(W1, b2);
                for (int t = 0; t < size; t++) {
                    if (maxX2 < Math.abs(x2.get(t, 0))) {
                        maxX2 = Math.abs(x2.get(t, 0));
                    }
                }
                if (maxX2 <= maxB2) {
                    tb1 = alg.mult(A, x1);
                    diff = 0;
                    for (int t = 0; t < size; t++) {
                        diff += Math.abs(tb1.getQuick(t, 0) - b1.getQuick(t, 0));
                    }
                    // System.out.println("Diff1: " + diff + " : " + trancatedID);
                    if (diff < minDiff) {
                        tb2 = alg.mult(A, x2);
                        diff = 0;
                        for (int t = 0; t < size; t++) {
                            diff += Math.abs(tb2.getQuick(t, 0) - b2.getQuick(t, 0));
                        }
                        //  System.out.println("Diff2: " + diff + " : " + trancatedID);
                        if (diff < minDiff) {
                            needContinue = false;
                            break;
                        }
                    }
                }
            }
            W.set(trancatedID, trancatedID, W.get(trancatedID, trancatedID) / 1E+6);
            if (W.get(trancatedID, trancatedID) <= threshlod) {
                W.set(trancatedID, trancatedID, 0);
                trancatedID--;
                if (trancatedID < 0) {
                    break;
                }
            }
        } while (needContinue);

        double df = 0;
        double Y = 0;
        for (int i = 0; i < size; i++) {
            Y += (x1.get(i, 0));
            df += (x2.get(i, 0));
        }
        if (Y <= 0 || df <= 0) {
            return 1;
        }

        double p1 = Gamma.incompleteGammaComplement(df / 2, Y / 2);

        return p1;
    }

    /*
     public static void myInverseUJMP(DenseMatrix64F A, DenseMatrix64F b1, DenseMatrix64F b2, DenseMatrix64F finalX1, DenseMatrix64F finalX2) throws Exception {
     int n = A.numCols;
     SparseMatrix ujmpA = SparseMatrix.Factory.zeros(n, n);
     Matrix b11 = Matrix.Factory.zeros(n, 1);
     Matrix b21 = Matrix.Factory.zeros(n, 1);
     double v;
     double c = 0.02;
     for (int i = 0; i < n; i++) {
     ujmpA.setAsDouble(1, i, i);
     b11.setAsDouble(b1.get(i, 0), i, 0);
     b21.setAsDouble(b2.get(i, 0), i, 0);
     for (int j = i + 1; j < n; j++) {
     v = A.get(i, j);
     if (v > c) {
     ujmpA.setAsDouble(v, i, j);
     ujmpA.setAsDouble(v, j, i);
     }
     }
     }
     Matrix invA = ujmpA.inv();
     Matrix ib11 = invA.mtimes(b11);
     Matrix ib21 = invA.mtimes(b21);
     for (int i = 0; i < n; i++) {
     finalX1.set(i, 0, ib11.getAsDouble(i, 0));
     finalX2.set(i, 0, ib21.getAsDouble(i, 0));
     }
        
     }
     */
    public static int[] partitionEvenBlock(int startIndex, int endIndex, int intervalLen, double overlappedRatio) {
        int totalSnpSize = endIndex - startIndex;
        int blockNum = totalSnpSize / intervalLen;
        if (blockNum == 0) {
            int[] bigBlockIndexes = new int[2];
            bigBlockIndexes[0] = startIndex;
            bigBlockIndexes[1] = endIndex;
            return bigBlockIndexes;
        }
        int overlappedLen = (int) (intervalLen * overlappedRatio);

        int l = 0;
        IntArrayList indexes = new IntArrayList();
        indexes.add(startIndex);
        indexes.add(indexes.getQuick(l) + intervalLen - overlappedLen);
        l++;
        while (indexes.getQuick(l) + intervalLen < endIndex) {
            indexes.add(indexes.getQuick(l) + overlappedLen);
            indexes.add(indexes.getQuick(l) + intervalLen - overlappedLen);
            l += 2;
        }
        indexes.remove(l);
        indexes.add(endIndex);
        l = indexes.size();
        int[] bigBlockIndexes = new int[l];
        for (int i = 0; i < l; i++) {
            bigBlockIndexes[i] = indexes.getQuick(i);
        }
        return bigBlockIndexes;

    }

    public static int[] partitionEvenBlock1(int startIndex, int endIndex, int intervalLen) {
        int totalSnpSize = endIndex - startIndex;
        int blockNum = totalSnpSize / intervalLen;
        if (blockNum <= 1) {
            int[] bigBlockIndexes = new int[2];
            bigBlockIndexes[0] = startIndex;
            bigBlockIndexes[1] = endIndex;
            return bigBlockIndexes;
        }

        int[] bigBlockIndexes = new int[blockNum + blockNum + 1];
        bigBlockIndexes[0] = startIndex;
        blockNum++;
        for (int i = 1; i < blockNum; i++) {
            bigBlockIndexes[i * 2] = startIndex + i * intervalLen;
            bigBlockIndexes[i * 2 - 1] = (bigBlockIndexes[i * 2] + bigBlockIndexes[i * 2 - 2]) / 2;
        }
        blockNum--;
        if (bigBlockIndexes[blockNum * 2] < endIndex) {
            bigBlockIndexes[blockNum * 2] = endIndex;
            bigBlockIndexes[blockNum * 2 - 1] = (bigBlockIndexes[blockNum * 2] + bigBlockIndexes[blockNum * 2 - 2]) / 2;
        }
        return bigBlockIndexes;
    }

    public void mySudoSVDSolverOverlappedWindow(DenseMatrix64F A, DenseMatrix64F b1, DenseMatrix64F b2, double[] results) throws Exception {
        int superBlockLen = 150;

        int n = A.numCols;
        int[] bigBlockIndexes = partitionEvenBlock(0, n, superBlockLen, 0.4);

        int blockNum = bigBlockIndexes.length - 2;
        // System.out.println(A.toString());
        double df = 0, dft = 0, Y = 0, Yt = 0;
        int blockLen = 0;
        if (blockNum == 0) {
            DenseMatrix64F x1 = new DenseMatrix64F(n, 1);
            DenseMatrix64F x2 = new DenseMatrix64F(n, 1);
            mySudoSVDSolverEJML(A, b1, b2, x1, x2);

            Y = CommonOps.elementSum(x1);
            df = CommonOps.elementSum(x2);

            if (Y < 0) {
                Y = 0;
            }
            if (df < 0) {
                df = 1;
            }
            results[0] = df;
            results[1] = Y;
            return;
        }

        blockNum = bigBlockIndexes.length;
        IntArrayList bounds = new IntArrayList();
        bounds.add(bigBlockIndexes[0]);
        bounds.add(bigBlockIndexes[2]);
        for (int i = 1; i < blockNum; i += 2) {
            bounds.add(bigBlockIndexes[i]);
            if (i + 3 < blockNum) {
                bounds.add(bigBlockIndexes[i + 3]);
            } else {
                bounds.add(bigBlockIndexes[blockNum - 1]);
                break;
            }
        }
        blockNum = bounds.size();

        int maxThreadNum = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(maxThreadNum);
        final CompletionService<String> serv = new ExecutorCompletionService<String>(exec);
        int runningThread = 0;
        List<SudoSVDSolverEJMLTask> solverList = new ArrayList<SudoSVDSolverEJMLTask>();
        boolean needThread = true;

        for (int i = 0; i < blockNum; i += 2) {
            if (needThread) {
                SudoSVDSolverEJMLTask task = new SudoSVDSolverEJMLTask(A, b1, b2, bounds.getQuick(i), bounds.getQuick(i + 1));
                serv.submit(task);
                runningThread++;
                solverList.add(task);
            } else {
                blockLen = bounds.getQuick(i + 1) - bounds.getQuick(i);
                DenseMatrix64F sA = CommonOps.extract(A, bounds.getQuick(i), bounds.getQuick(i + 1), bounds.getQuick(i), bounds.getQuick(i + 1));
                DenseMatrix64F sb1 = CommonOps.extract(b1, bounds.getQuick(i), bounds.getQuick(i + 1), 0, 1);
                DenseMatrix64F sb2 = CommonOps.extract(b2, bounds.getQuick(i), bounds.getQuick(i + 1), 0, 1);
                DenseMatrix64F x1 = new DenseMatrix64F(blockLen, 1);
                DenseMatrix64F x2 = new DenseMatrix64F(blockLen, 1);
                mySudoSVDSolverEJML(sA, sb1, sb2, x1, x2);
                Y = CommonOps.elementSum(x1);
                df = CommonOps.elementSum(x2);
                if (Y < 0) {
                    Y = 0;
                }
                if (df < 0) {
                    df = 0.001;
                }
                dft += df;
                Yt += Y;
                // System.out.println( bounds.getQuick(i) + "-" + bounds.getQuick(i + 1) + ":" + df + ":" + Y);
            }

        }
        if (needThread) {
            for (int s = 0; s < runningThread; s++) {
                Future<String> task = serv.take();
                String infor = task.get();
                //System.out.println(infor);
            }
            for (SudoSVDSolverEJMLTask task : solverList) {
                df = task.getDf();
                Y = task.getY();
                if (Y < 0) {
                    Y = 0;
                }
                if (df < 0) {
                    df = 0.001;
                }
                dft += df;
                Yt += Y;
            }
            solverList.clear();
        }

        bounds.clear();
        blockNum = bigBlockIndexes.length - 2;
        //calculate overlapped regions
        runningThread = 0;
        for (int i = 1; i < blockNum; i += 2) {
            if (needThread) {
                SudoSVDSolverEJMLTask task = new SudoSVDSolverEJMLTask(A, b1, b2, bigBlockIndexes[i], bigBlockIndexes[i + 1]);
                serv.submit(task);
                runningThread++;
                solverList.add(task);
            } else {
                blockLen = bigBlockIndexes[i + 1] - bigBlockIndexes[i];
                DenseMatrix64F sA = CommonOps.extract(A, bigBlockIndexes[i], bigBlockIndexes[i + 1], bigBlockIndexes[i], bigBlockIndexes[i + 1]);
                DenseMatrix64F sb1 = CommonOps.extract(b1, bigBlockIndexes[i], bigBlockIndexes[i + 1], 0, 1);
                DenseMatrix64F sb2 = CommonOps.extract(b2, bigBlockIndexes[i], bigBlockIndexes[i + 1], 0, 1);
                DenseMatrix64F x1 = new DenseMatrix64F(blockLen, 1);
                DenseMatrix64F x2 = new DenseMatrix64F(blockLen, 1);
                mySudoSVDSolverEJML(sA, sb1, sb2, x1, x2);
                Y = CommonOps.elementSum(x1);
                df = CommonOps.elementSum(x2);
                if (Y < 0) {
                    Y = 0;
                }
                if (df < 0) {
                    df = 0.001;
                }
                dft -= df;
                Yt -= Y;
                //  System.out.println(bigBlockIndexes[i] + "-" + bigBlockIndexes[i + 1] + ":" + df + ":" + Y);
            }
        }
        if (needThread) {
            for (int s = 0; s < runningThread; s++) {
                Future<String> task = serv.take();
                String infor = task.get();
                //System.out.println(infor);
            }
            for (SudoSVDSolverEJMLTask task : solverList) {
                df = task.getDf();
                Y = task.getY();
                if (Y < 0) {
                    Y = 0;
                }
                if (df < 0) {
                    df = 0.001;
                }
                dft -= df;
                Yt -= Y;
            }
            solverList.clear();
        }

        exec.shutdown();
        if (dft < 0) {
            dft = 0;
        }
        if (Yt < 0) {
            Yt = 0.001;
        }
        results[0] = dft;
        results[1] = Yt;

        /* //codes for testing
         int[] bigBlockIndexes = partitionEvenBlock(0, 41, 20, 0.5);
         int blockNum = bigBlockIndexes.length;
         IntArrayList bounds = new IntArrayList();
         bounds.add(bigBlockIndexes[0]);
         if (blockNum > 2) {
         bounds.add(bigBlockIndexes[2]);
         for (int i = 1; i < blockNum; i += 2) {
         bounds.add(bigBlockIndexes[i]);
         if (i + 3 < blockNum) {
         bounds.add(bigBlockIndexes[i + 3]);
         } else {
         bounds.add(bigBlockIndexes[blockNum - 1]);
         break;
         }
         }
         } else {
         bounds.add(bigBlockIndexes[1]);
         }

         bounds.clear();
         blockNum -= 2;
         for (int i = 1; i < blockNum; i += 2) {
         bounds.add(bigBlockIndexes[i]);
         bounds.add(bigBlockIndexes[i + 1]);
         }        
         */
    }

    // a lot of strange values
    public static void mySudoInverseSolverEJML(DenseMatrix64F A, DenseMatrix64F b1, DenseMatrix64F b2, DenseMatrix64F finalX1, DenseMatrix64F finalX2) throws Exception {
        DenseMatrix64F invA = new DenseMatrix64F(A.numRows, A.numRows);
        boolean completed = CommonOps.invert(A, invA);
        CommonOps.pinv(A, invA);
        if (!completed) {
            CommonOps.pinv(A, invA);
        }
        CommonOps.mult(invA, b1, finalX1);
        CommonOps.mult(invA, b2, finalX2);
    }

    //It has some inflation
    public static double combinePValuebyCorrectedChiFisherCombinationTestMXLiS(DenseMatrix64F ldCorr, DenseMatrix64F b1, double[] re) throws Exception {
        int snpSize = ldCorr.numCols;
        if (snpSize == 0) {
            re[0] = 0;
            re[1] = 0;
            return -1;
        } else if (snpSize == 1) {
            re[0] = 1;
            re[1] = b1.get(0, 0);
            return Probability.chiSquareComplemented(re[0], re[1]);
        }

        double[] chisquares = new double[snpSize];
        double[] dfs = new double[snpSize];
        for (int i = 0; i < snpSize; i++) {
            chisquares[i] = b1.get(i, 0);
            dfs[i] = 1;
        }
        double r;

        double df1 = 0;
        double Y1 = 0;
        for (int t = 0; t < snpSize; t++) {
            for (int k = t + 1; k < snpSize; k++) {
                r = ldCorr.get(t, k);
                chisquares[k] -= (chisquares[t] * r);
                dfs[k] -= (dfs[t] * r);
            }
        }

        for (int i = 0; i < snpSize; i++) {
            Y1 += chisquares[i];
            df1 += dfs[i];
        }
        re[0] = df1;
        re[1] = Y1;

        if (Y1 <= 0 || df1 <= 0) {
            return 1;
        }

        //calcualte the scalled chi 
        // p1 = Probability.chiSquareComplemented((df1 + df2) / 2, (Y1 + Y2) / 2);
        double p1 = Probability.chiSquareComplemented(df1, Y1);
        if (p1 < 0.0) {
            p1 = 0.0;
        }

        return p1;
    }

    public class SudoSVDSolverEJMLTask extends Task implements Callable<String> {

        DenseMatrix64F aA;
        DenseMatrix64F ab1;
        DenseMatrix64F ab2;
        int startIndex;
        int endIndex;
        double df;
        double Y;

        private DenseMatrix64F extract(DenseMatrix64F src,
                int srcY0, int srcY1,
                int srcX0, int srcX1) {
            int w = srcX1 - srcX0;
            int h = srcY1 - srcY0;

            DenseMatrix64F dst = new DenseMatrix64F(h, w);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    double v = src.get(y + srcY0, x + srcX0);
                    dst.set(y, x, v);
                }
            }

            return dst;
        }

        public SudoSVDSolverEJMLTask(DenseMatrix64F aA, DenseMatrix64F ab1, DenseMatrix64F ab2, int startIndex, int endIndex) {
            this.aA = aA;
            this.ab1 = ab1;
            this.ab2 = ab2;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public double getDf() {
            return df;
        }

        public double getY() {
            return Y;
        }

        @Override
        public String call() throws Exception {
            int blockLen = endIndex - startIndex;
            DenseMatrix64F A = extract(aA, startIndex, endIndex, startIndex, endIndex);
            DenseMatrix64F b1 = extract(ab1, startIndex, endIndex, 0, 1);
            DenseMatrix64F b2 = extract(ab2, startIndex, endIndex, 0, 1);
            DenseMatrix64F x1 = new DenseMatrix64F(blockLen, 1);
            DenseMatrix64F x2 = new DenseMatrix64F(blockLen, 1);

            SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, true, true, true);
            if (!DecompositionFactory.decomposeSafe(svd, A)) {
                System.err.println("Decomposition failed");
                //throw new DetectedException("Decomposition failed");
            }

            DenseMatrix64F U = svd.getU(null, true);
            DenseMatrix64F W = svd.getW(null);
            // System.out.println(W.toString());
            DenseMatrix64F V = svd.getV(null, false);
            DenseMatrix64F W1 = CommonOps.identity(A.numRows);
            DenseMatrix64F T1 = CommonOps.identity(A.numRows);
            double threshlod = 1e-6;
            double maxX1 = 0, maxB1 = 0;
            double maxX2 = 0, maxB2 = 0;
            int trancatedID = A.numRows;
            DenseMatrix64F tb1 = new DenseMatrix64F(A.numRows, 1);
            DenseMatrix64F tb2 = new DenseMatrix64F(A.numRows, 1);
            for (int t = 0; t < A.numRows; t++) {
                if (maxB1 < Math.abs(b1.get(t, 0))) {
                    maxB1 = Math.abs(b1.get(t, 0));
                }
                if (maxB2 < Math.abs(b2.get(t, 0))) {
                    maxB2 = Math.abs(b2.get(t, 0));
                }
                if (W.get(t, t) < threshlod) {
                    W.set(t, t, 0);
                } else {
                    W.set(t, t, 1 / W.get(t, t));
                }
            }
            double diffCutoff = (1E-5) * A.numRows;
            double diff1, diff2;
            double minDiff1 = Integer.MAX_VALUE, minDiff2 = Integer.MAX_VALUE;
            DenseMatrix64F finalX1 = new DenseMatrix64F(A.numRows, 1);
            DenseMatrix64F finalX2 = new DenseMatrix64F(A.numRows, 1);
            maxB1 = maxB1 * 10;
            maxB2 = maxB2 * 10;
            trancatedID--;
            boolean needContinue = true;
            do {
                CommonOps.mult(V, W, T1);
                CommonOps.mult(T1, U, W1);
                CommonOps.mult(W1, b1, x1);

                maxX1 = 0;
                maxX2 = 0;
                for (int t = 0; t < A.numRows; t++) {
                    if (maxX1 < Math.abs(x1.get(t, 0))) {
                        maxX1 = Math.abs(x1.get(t, 0));
                    }
                }
                if (maxX1 <= maxB1) {
                    CommonOps.mult(W1, b2, x2);
                    for (int t = 0; t < A.numRows; t++) {
                        if (maxX2 < Math.abs(x2.get(t, 0))) {
                            maxX2 = Math.abs(x2.get(t, 0));
                        }
                    }
                    if (maxX2 <= maxB2) {
                        CommonOps.mult(A, x1, tb1);
                        CommonOps.changeSign(tb1);
                        CommonOps.add(b1, tb1, tb1);
                        diff1 = CommonOps.elementSumAbs(tb1);

                        CommonOps.mult(A, x2, tb2);
                        CommonOps.changeSign(tb2);
                        CommonOps.add(b2, tb2, tb2);
                        diff2 = CommonOps.elementSumAbs(tb2);

                        /*
                     if (diff1 < minDiff1) {
                     minDiff1 = diff1;
                     for (int t = 0; t < A.numRows; t++) {
                     finalX1.set(t, 0, x1.get(t, 0));
                     finalX2.set(t, 0, x2.get(t, 0));
                     }
                     }
                         */
                        if (diff2 < minDiff2) {
                            minDiff2 = diff2;
                            for (int t = 0; t < A.numRows; t++) {
                                finalX1.set(t, 0, x1.get(t, 0));
                                finalX2.set(t, 0, x2.get(t, 0));
                            }
                        }

                        if (diff1 <= diffCutoff && diff2 <= diffCutoff) {
                            needContinue = false;
                            break;
                        }
                    }
                }

                // W.set(trancatedID, trancatedID, W.get(trancatedID, trancatedID) / 1E6);
                // if (W.get(trancatedID, trancatedID) <= threshlod) 
                {
                    W.set(trancatedID, trancatedID, 0);
                    trancatedID--;
                    if (trancatedID < 0) {
                        break;
                    }
                }
            } while (needContinue);
            Y = CommonOps.elementSum(finalX1);
            df = CommonOps.elementSum(finalX2);

            if (Y < 0) {
                Y = 0;
            }
            if (df < 0) {
                df = 0.001;
            }
            return startIndex + "-" + endIndex + ":" + df + ":" + Y;
        }
    }

    public static void mySudoSVDSolverEJML(DenseMatrix64F A, DenseMatrix64F b1, DenseMatrix64F b2, DenseMatrix64F finalX1, DenseMatrix64F finalX2) throws Exception {
        org.ejml.interfaces.decomposition.SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows, A.numCols, true, true, true);
        if (!DecompositionFactory.decomposeSafe(svd, A)) {
            System.err.println("Decomposition failed");
            //throw new DetectedException("Decomposition failed");
        }

        DenseMatrix64F U = svd.getU(null, true);
        DenseMatrix64F W = svd.getW(null);
        // System.out.println(W.toString());
        DenseMatrix64F V = svd.getV(null, false);
        DenseMatrix64F W1 = CommonOps.identity(A.numRows);
        DenseMatrix64F T1 = CommonOps.identity(A.numRows);
        double threshlod = 1e-6;
        double maxX1 = 0, maxB1 = 0;
        double maxX2 = 0, maxB2 = 0;
        int trancatedID = A.numRows;
        DenseMatrix64F tb1 = new DenseMatrix64F(A.numRows, 1);
        DenseMatrix64F tb2 = new DenseMatrix64F(A.numRows, 1);
        for (int t = 0; t < A.numRows; t++) {
            if (maxB1 < Math.abs(b1.get(t, 0))) {
                maxB1 = Math.abs(b1.get(t, 0));
            }
            if (maxB2 < Math.abs(b2.get(t, 0))) {
                maxB2 = Math.abs(b2.get(t, 0));
            }
            if (W.get(t, t) < threshlod) {
                W.set(t, t, 0);
            } else {
                W.set(t, t, 1 / W.get(t, t));
            }
        }
        double diffCutoff = (1E-5) * A.numRows;
        double diff1, diff2 = Double.MAX_VALUE;
        double minDiff1 = Integer.MAX_VALUE, minDiff2 = Integer.MAX_VALUE;
        int minDiffNum = trancatedID;
        DenseMatrix64F x1 = new DenseMatrix64F(A.numRows, 1);
        DenseMatrix64F x2 = new DenseMatrix64F(A.numRows, 1);
        maxB1 = maxB1 * 10;
        maxB2 = maxB2 * 10;
        trancatedID--;
        double sum = 0;
        boolean needContinue = true;
        do {
            CommonOps.mult(V, W, T1);
            CommonOps.mult(T1, U, W1);
            CommonOps.mult(W1, b1, x1);

            maxX1 = 0;
            maxX2 = 0;
            for (int t = 0; t < A.numRows; t++) {
                if (maxX1 < Math.abs(x1.get(t, 0))) {
                    maxX1 = Math.abs(x1.get(t, 0));
                }
            }
            if (maxX1 <= maxB1) {
                CommonOps.mult(W1, b2, x2);
                for (int t = 0; t < A.numRows; t++) {
                    if (maxX2 < Math.abs(x2.get(t, 0))) {
                        maxX2 = Math.abs(x2.get(t, 0));
                    }
                }
                if (maxX2 <= maxB2) {
                    CommonOps.mult(A, x1, tb1);
                    CommonOps.changeSign(tb1);
                    CommonOps.add(b1, tb1, tb1);
                    diff1 = CommonOps.elementSumAbs(tb1);

                    CommonOps.mult(A, x2, tb2);
                    CommonOps.changeSign(tb2);
                    CommonOps.add(b2, tb2, tb2);
                    sum = CommonOps.elementSum(x2);
                    if (sum >= 1) {
                        diff2 = CommonOps.elementSumAbs(tb2);

                        /*
                     if (diff1 < minDiff1) {
                     minDiff1 = diff1;
                     for (int t = 0; t < A.numRows; t++) {
                     finalX1.set(t, 0, x1.get(t, 0));
                     finalX2.set(t, 0, x2.get(t, 0));
                     }
                     }
                         */
                        if (diff2 < minDiff2) {
                            minDiff2 = diff2;
                            for (int t = 0; t < A.numRows; t++) {
                                finalX1.set(t, 0, x1.get(t, 0));
                                finalX2.set(t, 0, x2.get(t, 0));
                            }
                            minDiffNum = trancatedID;
                        }
                    }
                    if (diff1 <= diffCutoff && diff2 <= diffCutoff) {
                        needContinue = false;
                        break;
                    }
                }
            }

            // W.set(trancatedID, trancatedID, W.get(trancatedID, trancatedID) / 1E6);
            // if (W.get(trancatedID, trancatedID) <= threshlod) 
            {
                W.set(trancatedID, trancatedID, 0);
                trancatedID--;
                if (trancatedID < 0) {
                    break;
                }
            }
        } while (needContinue);

    }

    static public double combinePValuebyWeightedSimeCombinationTestMyMe(PValueWeight[] pValueArray,
            DoubleMatrix2D ldCorr, boolean needConvert) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return 1;
        } else if (snpSize == 1) {
            return pValueArray[0].pValue;
        }
        Arrays.sort(pValueArray, new PValueWeightComparator());
        pValueArray[0].effectiveIndex = 1;
        Set<Integer> selectedIndex = new HashSet<Integer>();

        double ess = 0;
        double accumulatedIndex = 1;
        double totalRatio = pValueArray[0].effectiveIndex * pValueArray[0].weight;
        if (needConvert) {
            for (int i = 0; i < snpSize; i++) {
                selectedIndex.add(pValueArray[i].physicalPos);
                for (int j = i + 1; j < snpSize; j++) {
                    if (i == j) {
                        continue;
                    }

                    //poweredcorrMat.setQuick(j, j, Math.pow(corrMat.getQuick(j, j), power));
                    double x = ldCorr.getQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos);
                    // x = x * x;
                    //when r2                 
                    //I do not know why it seems if I use x=x*x  it woks better in terms of type 1 error
                    x = (((((0.7723 * x - 1.5659) * x + 1.201) * x - 0.2355) * x + 0.2184) * x + 0.6086) * x;
                    //x = x * x;
                    ldCorr.setQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos, x);
                    ldCorr.setQuick(pValueArray[j].physicalPos, pValueArray[i].physicalPos, x);
                }
            }
        } else {
            for (int i = 0; i < snpSize; i++) {
                selectedIndex.add(pValueArray[i].physicalPos);
            }
        }

        double totalEffetiveSize = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
        selectedIndex.clear();
        selectedIndex.add(pValueArray[0].physicalPos);
        snpSize--;
        for (int i = 1; i < snpSize; i++) {
            selectedIndex.add(pValueArray[i].physicalPos);
            ess = EffectiveNumberEstimator.calculateEffectSampleSizeColtMatrixMyMethodByPCov(ldCorr, selectedIndex);
            pValueArray[i].effectiveIndex = ess - accumulatedIndex;
            accumulatedIndex = ess;
            totalRatio += pValueArray[i].effectiveIndex * pValueArray[i].weight;
        }
        //to save time
        pValueArray[snpSize].effectiveIndex = totalEffetiveSize - accumulatedIndex;
        totalRatio += pValueArray[snpSize].effectiveIndex * pValueArray[snpSize].weight;
        snpSize++;

        double factor = totalEffetiveSize / totalRatio;
        pValueArray[0].effectiveIndex = pValueArray[0].effectiveIndex * pValueArray[0].weight * factor;
        for (int i = 1; i < snpSize; i++) {
            pValueArray[i].effectiveIndex = pValueArray[i].effectiveIndex * pValueArray[i].weight * factor + pValueArray[i - 1].effectiveIndex;
        }
        double minP = totalEffetiveSize * pValueArray[0].pValue / pValueArray[0].effectiveIndex;
        double p;
        for (int i = 1; i < snpSize; i++) {
            p = totalEffetiveSize * pValueArray[i].pValue / pValueArray[i].effectiveIndex;
            if (p < minP) {
                minP = p;
            }
        }
        return minP;
    }

    public static double setPValuebyHystBlock(PValueWeight[] pValueArray, DoubleMatrix2D ldCorr) throws Exception {
        int totaSNPSize = pValueArray.length;
        //because it is very time consumming, when SNP number is over 200. I try to spl
        int minBlockLen = 1;

        int maxBlockLen = 50;
        int maxCheckingNum = 5;
        IntArrayList blockKeySNPPositions = new IntArrayList();
        DoubleArrayList blockPValues = new DoubleArrayList();

        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.25;
        double p;
        int[] keySNPIndex = new int[1];
        keySNPIndex[0] = -1;

        //  System.out.println(ldCorr.toString());
        if (totaSNPSize <= minBlockLen) {
            p = SetBasedTest.combineGATESPValuebyNoWeightSimeCombinationTestMyMe(pValueArray, ldCorr, keySNPIndex);
            return p;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;

        int movedRow;
        List<PValueWeight> tmpSNPList = new ArrayList<PValueWeight>();
        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > totaSNPSize) {
            outBlockFirst = totaSNPSize;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;

        while (inBlockLast <= totaSNPSize) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < totaSNPSize)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getQuick(inBlockFirst, outBlockFirst) >= minWthinBlockR2)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= totaSNPSize) {

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < totaSNPSize; i++) {
                    tmpSNPList.add(pValueArray[i]);
                }
                keySNPIndex[0] = -1;
                p = SetBasedTest.combineGATESPValuebyNoWeightSimeCombinationTestMyMe(tmpSNPList.toArray(new PValueWeight[0]), ldCorr.copy(), keySNPIndex);
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(pValueArray[i]);
                }
                keySNPIndex[0] = -1;
                p = SetBasedTest.combineGATESPValuebyNoWeightSimeCombinationTestMyMe(tmpSNPList.toArray(new PValueWeight[0]), ldCorr.copy(), keySNPIndex);
                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }

                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < totaSNPSize)
                        && (ldCorr.getQuick(movedRow, checkingIndex) < minWthinBlockR2)) {

                    if (checkingLen >= maxCheckingNum) {
                        allAreLess = true;
                        break;
                    }
                    checkingLen++;
                    checkingIndex++;
                }

                if (!allAreLess) {
                    break;
                }
                movedRow--;
            }

            if (allAreLess) {

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(pValueArray[i]);
                }
                keySNPIndex[0] = -1;
                p = SetBasedTest.combineGATESPValuebyNoWeightSimeCombinationTestMyMe(tmpSNPList.toArray(new PValueWeight[0]), ldCorr.copy(), keySNPIndex);

                if (keySNPIndex[0] != -1) {
                    blockKeySNPPositions.add(keySNPIndex[0]);
                    blockPValues.add(p);
                }
                inBlockFirst = outBlockFirst;
                outBlockFirst = inBlockFirst + minBlockLen;
                if (outBlockFirst > totaSNPSize) {
                    outBlockFirst = totaSNPSize;
                }
                inBlockLast = outBlockFirst - 1;

            } else {
                // System.out.println(ldRsMatrix.getLDAt(snpPositionArray[startIndex], snpPositionArray[checkingIndex]) + "\t" + snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[stopIndex - 1] + "\t" + (stopIndex - inBlockFirst));
                //go to the minExtendLen and re-check
                inBlockLast = checkingIndex;
                outBlockFirst = inBlockLast + 1;
                //force to cut into a block with maxBlockLen
                if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                    outBlockFirst = inBlockFirst + maxBlockLen;
                }
            }
        }

        //combine the block wise p-values by scaled chisqure test
        int blockSize = blockKeySNPPositions.size();
        if (blockSize == 0) {
            return Double.NaN;
        }
        PValueWeight[] blockKeySNPs = new PValueWeight[blockSize];

        for (int i = 0; i < blockSize; i++) {
            blockKeySNPs[i] = new PValueWeight();
            blockKeySNPs[i].pValue = blockPValues.get(i);
            blockKeySNPs[i].weight = 1;
            blockKeySNPs[i].physicalPos = i;
        }

        DoubleMatrix2D rLDMatrix = new DenseDoubleMatrix2D(blockSize, blockSize);
        for (int i = 0; i < blockSize; i++) {
            rLDMatrix.setQuick(i, i, 1);
            for (int j = i + 1; j < blockSize; j++) {

                double x = ldCorr.getQuick(blockKeySNPPositions.getQuick(i), blockKeySNPPositions.getQuick(j));

                rLDMatrix.setQuick(i, j, x);
                rLDMatrix.setQuick(j, i, x);

            }
        }

        p = SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(blockKeySNPs, rLDMatrix);
        // System.out.println(rLDMatrix.toString());
        return p;

    }

    public static double combinePValuebyScaleedFisherCombinationTestCovLogP(PValueWeight[] pValueArray, DoubleMatrix2D ldCorr) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return Double.NaN;
        } else if (snpSize == 1) {
            return pValueArray[0].pValue;
        }

        DoubleMatrix2D covLogP = new DenseDoubleMatrix2D(snpSize, snpSize);
        for (int i = 0; i < snpSize; i++) {
            for (int j = i + 1; j < snpSize; j++) {
                if (i == j) {
                    continue;
                }
                double x = ldCorr.getQuick(pValueArray[i].physicalPos, pValueArray[j].physicalPos);
                //x = Math.sqrt(x);
                //for r
                //y = 0.0079x3 + 3.9459x2 - 0.0024x ; y = 0.0331x3 + 3.9551x2 - 0.0156x
                //x = 0.0331 * (Math.pow(x, 3)) + 3.9551 * (Math.pow(x, 2)) - 0.0156 * x;
                //x = ((0.0331 * x + 3.9551) * x - 0.0156) * x;
                // x = (0.75 * x + 3.25) * x ;

                //for r2
                //x = 0.75 * x + 3.25 * Math.sqrt(x); //a little bit liberal
                //later we found this approximation will be more accurate
                //x = 8.6 * x;
                //but later we found the above approximation was a little bit conservative in real data especially in imputed dataset
                //so I just took the weighted values
                if (x > 0.5) {
                    x = 0.75 * x + 3.25 * Math.sqrt(x);
                } else {
                    x = 8.6 * x;
                }
                covLogP.setQuick(i, j, x);
            }
        }
        // System.out.println(ldCorr.toString());
        // System.out.println(covLogP.toString());
        /*
         double weightSum = 0;
         for (int j = 0; j < snpSize; j++) {
         weightSum += pValueArray[j].weight;
         }
         for (int j = 0; j < snpSize; j++) {
         pValueArray[j].weight = pValueArray[j].weight / weightSum;
         }
         System.out.println(pValueArray[0].weight/pValueArray[1].weight);
         * 
         */
        double Y = 0;
        for (int i = 0; i < snpSize; i++) {
            if (!Double.isNaN(pValueArray[i].pValue)) {
                Y += (-2 * Math.log(pValueArray[i].pValue) * pValueArray[i].weight);// weight is all zero? What to do?
            }
        }
        /*
        
         //calcualte the scalled chi
         double varTD = 4 * snpSize;
         for (int j = 0; j < snpSize; j++) {
         for (int j = j + 1; j < snpSize; j++) {
         varTD += 2 * covLogP.get(j, j);
         }
         }
         double c = varTD / (4 * snpSize);
         double f = 8 * snpSize * snpSize / varTD;
         double p = Probability.chiSquareComplemented(f, Y / c);
         */
        double sumWeight = 0;
        double sumWeight2 = 0;
        double sumCov = 0;
        for (int i = 0; i < snpSize; i++) {
            if (!Double.isNaN(pValueArray[i].pValue)) {
                sumWeight += pValueArray[i].weight;
            }
        }
        //http://www.sciencedirect.com/science/article/pii/S016771520500009X A simple approximation for the distribution of the weighted combination of non-independent or independent probabilities Chia-Ding Hou

        for (int i = 0; i < snpSize; i++) {
            if (!Double.isNaN(pValueArray[i].pValue)) {
                sumWeight2 += pValueArray[i].weight * pValueArray[i].weight;
            }
        }

        for (int i = 0; i < snpSize; i++) {
            if (!Double.isNaN(pValueArray[i].pValue)) {
                for (int j = i + 1; j < snpSize; j++) {
                    if (!Double.isNaN(pValueArray[j].pValue)) {
                        sumCov += covLogP.get(i, j) * pValueArray[i].weight * pValueArray[j].weight;
                    }
                }
            }
        }

        double c = (sumWeight2 + sumCov / 2) / sumWeight; //when sumWeight is 0, c,f,p are NaN. How to handle that? 
        double f = 4 * sumWeight * sumWeight / (2 * sumWeight2 + sumCov);
        double p = Probability.chiSquareComplemented(f, Y / c);
        if (p < 0.0) {
            p = 0.0;
        }
        return p;
    }

    public static double combinePValuebyFisherCombinationTest(double[] pValueArray) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return -1;
        } else if (snpSize == 1) {
            return pValueArray[0];
        }

        double Y = 0;
        for (int i = 0; i < snpSize; i++) {
            double p = pValueArray[i];
            Y += (-2 * Math.log(p));
        }
        double p = Probability.chiSquareComplemented(snpSize * 2, Y);
        if (p < 0.0) {
            p = 0.0;
        }
        return p;

    }

    public static double combinePValuebySimeCombinationTest(double[] pValueArray) throws Exception {
        int snpSize = pValueArray.length;
        if (snpSize == 0) {
            return 1;
        } else if (snpSize == 1) {
            return pValueArray[0];
        }
        Arrays.sort(pValueArray);

        double minP = snpSize * pValueArray[0];
        double p;

        for (int i = 1; i < snpSize; i++) {
            p = snpSize * pValueArray[i] / (i + 1);
            if (p < minP) {
                minP = p;
            }
        }
        return minP;
    }
}
