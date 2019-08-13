// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.business;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cobi.kgg.business.entity.LDSparseMatrix;
import org.cobi.kgg.business.entity.PValueWeight;

/**
 *
 * @author MX Li
 */
public class EffectiveNumberEstimator {

    /*
     public static double calculateEffectSampleSizeColtMatrixMyMethod(DoubleMatrix2D corrMat, Set<Integer> selectedSampleIndex) throws Exception {
     //http://gump.qimr.edu.au/general/daleN/SNPSpD/KeavneyResultsNew.pdf
     DoubleMatrix2D poweredcorrMat = corrMat.copy();
     int originalMatrixSize = poweredcorrMat.columns();
     // DoubleMatrix2D corrMat = ColtMatrixBasic.readMatrixFromFile("test.txt", originalMatrixSize, originalMatrixSize);
     int newSampleSize = originalMatrixSize;
    
     if (selectedSampleIndex != null && selectedSampleIndex.size() > 0) {
     // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
     newSampleSize = selectedSampleIndex.size();
    
     DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
     int incRow = 0;
     int incCol = 0;
     for (int i = 0; i < originalMatrixSize; i++) {
     if (!selectedSampleIndex.contains(i)) {
     continue;
     }
     incCol = 0;
     for (int j = 0; j < originalMatrixSize; j++) {
     if (!selectedSampleIndex.contains(j)) {
     continue;
     }
     tmpCorMat.setQuick(incRow, incCol, poweredcorrMat.getQuick(i, j));
     incCol++;
     }
     incRow++;
     }
     poweredcorrMat = tmpCorMat;
     // System.out.println(corrMat.toString());
     } else if (selectedSampleIndex != null && selectedSampleIndex.isEmpty()) {
     return 0;
     }
    
    
     originalMatrixSize = poweredcorrMat.columns();
     Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
     double maxCorr = 1;
     for (int i = 0; i < originalMatrixSize; i++) {
     for (int j = i + 1; j < originalMatrixSize; j++) {
     if (Math.abs(poweredcorrMat.getQuick(i, j)) >= maxCorr) {
     if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
     highlyCorrIndexes.add(j);
     // System.out.println(i + " <-> " + j);
     }
     }
     }
     }
     if (highlyCorrIndexes.size() > 0) {
     // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
     newSampleSize = originalMatrixSize - highlyCorrIndexes.size();
    
     DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
     int incRow = 0;
     int incCol = 0;
     for (int i = 0; i < originalMatrixSize; i++) {
     if (highlyCorrIndexes.contains(i)) {
     continue;
     }
     incCol = 0;
     for (int j = 0; j < originalMatrixSize; j++) {
     if (highlyCorrIndexes.contains(j)) {
     continue;
     }
     tmpCorMat.setQuick(incRow, incCol, poweredcorrMat.getQuick(i, j));
     incCol++;
     }
     incRow++;
     }
     poweredcorrMat = tmpCorMat;
     // System.out.println(corrMat.toString());
     }
     if (newSampleSize == 1) {
     return 1;
     }
    
     for (int i = 0; i < newSampleSize; i++) {
     for (int j = 0; j < newSampleSize; j++) {
     if (i == j) {
     continue;
     }
    
     double x = poweredcorrMat.getQuick(i, j);
     //y = 0.2993x6 - 0.0154x5 + 0.0619x4 + 0.014x3 + 0.6259x2 - 0.0013x
     //when r
     // x = (((((0.2993 * x - 0.0154) * x + 0.0619) * x + 0.014) * x + 0.6259) * x - 0.0013) * x;
     //when r2
     //y = 0.9779x6 - 2.1349x5 + 1.7571x4 - 0.4626x3 + 0.2555x2 + 0.606x
     x=x*x;
     x = (((((0.9779 * x - 2.1349) * x + 1.7571) * x - 0.4626) * x + 0.2555) * x + 0.606) * x;
     //x=Math.pow(x, 4);
     poweredcorrMat.setQuick(i, j, x);
     }
     }
    
     //I found this function is less error-prone  than the  EigenDecompositionImpl 2.0 and slightly faster
     //System.out.println(poweredcorrMat.toString());
     EigenvalueDecomposition ed = new EigenvalueDecomposition(poweredcorrMat);
    
     DoubleMatrix1D eVR = ed.getRealEigenvalues();
    
     //DoubleMatrix1D eVI = ed.getImagEigenvalues();
     // System.out.println(eVR.toString());
     // System.out.println(eVI.toString());
     //double effectSampleSize = newSampleSize;
     double effectSampleSize = 0;
    
     for (int i = 0; i < newSampleSize; i++) {
     if (Double.isNaN(eVR.get(i))) {
     System.err.println("NaN error for eigen values!");
     }
     if (eVR.getQuick(i) > 1) {
     effectSampleSize += (1);//(eVR.getQuick(i));
     } else {
     effectSampleSize += eVR.getQuick(i);
     }
     }
     return (effectSampleSize);
     }
     */
    public static double calculateEffectSampleSizeColtMatrixMyMethodByPCov(DoubleMatrix2D corrMat, Set<Integer> selectedSampleIndex) throws Exception {
        //http://gump.qimr.edu.au/general/daleN/SNPSpD/KeavneyResultsNew.pdf
        DoubleMatrix2D poweredcorrMat = corrMat.copy();
        int originalSampleSize = poweredcorrMat.columns();
        // DoubleMatrix2D corrMat = ColtMatrixBasic.readMatrixFromFile("test.txt", originalMatrixSize, originalMatrixSize);
        int newSampleSize = originalSampleSize;

        if (selectedSampleIndex != null && selectedSampleIndex.size() > 0) {
            // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
            newSampleSize = selectedSampleIndex.size();

            DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
            int incRow = 0;
            int incCol = 0;
            for (int i = 0; i < originalSampleSize; i++) {
                if (!selectedSampleIndex.contains(i)) {
                    continue;
                }
                incCol = 0;
                for (int j = 0; j < originalSampleSize; j++) {
                    if (!selectedSampleIndex.contains(j)) {
                        continue;
                    }
                    tmpCorMat.setQuick(incRow, incCol, poweredcorrMat.getQuick(i, j));
                    incCol++;
                }
                incRow++;
            }
            poweredcorrMat = tmpCorMat;
            // System.out.println(corrMat.toString());
        } else if (selectedSampleIndex != null && selectedSampleIndex.isEmpty()) {
            return 0;
        }

        originalSampleSize = poweredcorrMat.columns();
        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
        double maxCorr = 1;
        for (int i = 0; i < originalSampleSize; i++) {
            for (int j = i + 1; j < originalSampleSize; j++) {
                if (Math.abs(poweredcorrMat.getQuick(i, j)) >= maxCorr) {
                    if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
                        highlyCorrIndexes.add(j);
                        // System.out.println(i + " <-> " + j);
                    }
                }
            }
        }

        if (highlyCorrIndexes.size() > 0) {
            // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
            newSampleSize = originalSampleSize - highlyCorrIndexes.size();

            DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
            int incRow = 0;
            int incCol = 0;
            for (int i = 0; i < originalSampleSize; i++) {
                if (highlyCorrIndexes.contains(i)) {
                    continue;
                }
                incCol = 0;
                for (int j = 0; j < originalSampleSize; j++) {
                    if (highlyCorrIndexes.contains(j)) {
                        continue;
                    }
                    tmpCorMat.setQuick(incRow, incCol, poweredcorrMat.getQuick(i, j));
                    incCol++;
                }
                incRow++;
            }
            poweredcorrMat = tmpCorMat;
            // System.out.println(corrMat.toString());
        }
        if (newSampleSize == 1) {
            return 1;
        }

        //I found this function is less error-prone  than the  EigenDecompositionImpl 2.0 and slightly faster
        //System.out.println(poweredcorrMat.toString());
        EigenvalueDecomposition ed = new EigenvalueDecomposition(poweredcorrMat);

        DoubleMatrix1D eVR = ed.getRealEigenvalues();

        //DoubleMatrix1D eVI = ed.getImagEigenvalues();
        // System.out.println(eVR.toString());
        // System.out.println(eVI.toString());
        //double effectSampleSize = newSampleSize;
        double effectSampleSize = newSampleSize;

        for (int i = 0; i < newSampleSize; i++) {
            if (Double.isNaN(eVR.get(i))) {
                System.err.println("NaN error for eigen values!");
            }

            /*
             if (eVR.getQuick(i) < 0) {
             System.out.println(eVR.toString());
             }
             * 
             */
            if (eVR.getQuick(i) > 1) {
                effectSampleSize -= (eVR.getQuick(i) - 1);//(eVR.getQuick(j));
            }

            /*
             if (eVR.getQuick(i) > 1) {
             effectSampleSize += (1);//(eVR.getQuick(i));
             } else {
             effectSampleSize += eVR.getQuick(i);
             }
             */
        }
        if (effectSampleSize < 1) {
            effectSampleSize = 1;
        }

        return (effectSampleSize);
    }

    public static DoubleMatrix2D removeRedundantItems(DoubleMatrix2D corrMat, double maxCorr) {
        int originalSampleSize = corrMat.columns();
        int newSampleSize = originalSampleSize;
        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();

        for (int i = 0; i < originalSampleSize; i++) {
            for (int j = i + 1; j < originalSampleSize; j++) {
                if (Math.abs(corrMat.getQuick(i, j)) >= maxCorr) {
                    if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
                        highlyCorrIndexes.add(j);
                        //  System.out.println(i + " <-> " + j);
                    }
                }
            }
        }

        if (highlyCorrIndexes.size() > 0) {
            // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
            newSampleSize = originalSampleSize - highlyCorrIndexes.size();

            DoubleMatrix2D poweredCorrMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
            int incRow = 0;
            int incCol = 0;
            for (int i = 0; i < originalSampleSize; i++) {
                if (highlyCorrIndexes.contains(i)) {
                    continue;
                }
                incCol = 0;
                for (int j = 0; j < originalSampleSize; j++) {
                    if (highlyCorrIndexes.contains(j)) {
                        continue;
                    }
                    poweredCorrMat.setQuick(incRow, incCol, corrMat.getQuick(i, j));
                    incCol++;
                }
                incRow++;
            }

            // System.out.println(corrMat.toString());
            return poweredCorrMat;
        } else {
            return corrMat.copy();
        }
    }

    public static Set<Integer> markNonRedundantItems(DoubleMatrix2D corrMat, double maxCorr) {
        int originalMatrixSize = corrMat.columns();

        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();

        for (int i = 0; i < originalMatrixSize; i++) {
            for (int j = i + 1; j < originalMatrixSize; j++) {
                if (Math.abs(corrMat.getQuick(i, j)) >= maxCorr) {
                    if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
                        highlyCorrIndexes.add(j);
                        //  System.out.println(i + " <-> " + j);
                    }
                }
            }
        }
        Set<Integer> nonRedundantIndexes = new HashSet<Integer>();
        if (highlyCorrIndexes.size() > 0) {
            for (int i = 0; i < originalMatrixSize; i++) {
                if (highlyCorrIndexes.contains(i)) {
                    continue;
                }
                nonRedundantIndexes.add(i);
            }
            // System.out.println(corrMat.toString());
            return nonRedundantIndexes;
        } else {
            for (int i = 0; i < originalMatrixSize; i++) {
                nonRedundantIndexes.add(i);
            }

            return nonRedundantIndexes;
        }
    }

    public static double fastCalculateEffectSampleSizeLDSparseMatrixMyMethodByPCov(LDSparseMatrix ldRsMatrix,
            List<PValueWeight> pvalueWeightList, int maxBlockLen, double weakCorrelationThreshold,
            int maxCheckingNum, int maxCheckingDistance) throws Exception {
        int totalObservedSize = pvalueWeightList.size();
        if (totalObservedSize == 0) {
            return 0;
        } else if (totalObservedSize == 1) {
            return 1;
        }

        int[] snpPositionArray = new int[totalObservedSize];
        int count = 0;
        for (; count < totalObservedSize;) {
            snpPositionArray[count] = pvalueWeightList.get(count).physicalPos;
            count++;
        }

        Arrays.sort(snpPositionArray);
        IntArrayList indexInBlock = new IntArrayList();

        int originalSampleSize = snpPositionArray.length;
        double effectNumSum = 0;

        // System.out.println(poweredCorrMat.toString());
        int inBlockFirst = 0;
        int inBlockLast = 0;
        int outBlockFirst = 1;

        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        boolean allAreLess = false;

        boolean removeRedundant = true;
        double redundanctLD = 0.9988;
        // StringBuilder info = new StringBuilder();
        boolean debug = false;
        double partEffectiveNum;
        int movedRow;

        if (totalObservedSize <= maxBlockLen) {
            for (int i = inBlockFirst; i < originalSampleSize; i++) {
                indexInBlock.add(snpPositionArray[i]);
            }
            DoubleMatrix2D par = ldRsMatrix.subDenseLDMatrix(indexInBlock);
            ldRsMatrix.releaseLDData();
            indexInBlock.clear();
            if (removeRedundant) {
                par = EffectiveNumberEstimator.removeRedundantItems(par, redundanctLD);
            }
            partEffectiveNum = calculateEffectSampleSizeColtMatrixMyMethodByPCov(par, null);
            //System.out.println(par.toString());
            return partEffectiveNum;
        }

        while (inBlockLast <= originalSampleSize) {
            //find a site whose LD beteen inBlockFirst start to be less than weakCorrelationThreshold
            while ((outBlockFirst < originalSampleSize) && (outBlockFirst - inBlockFirst < maxBlockLen) && (ldRsMatrix.getLDAt(snpPositionArray[inBlockFirst], snpPositionArray[outBlockFirst]) >= weakCorrelationThreshold)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= originalSampleSize) {
                if (debug) {
                    System.out.print(snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[originalSampleSize - 1] + "\t" + (originalSampleSize - inBlockFirst) + "\t");
                }
                for (int i = inBlockFirst; i < originalSampleSize; i++) {
                    indexInBlock.add(snpPositionArray[i]);
                }
                DoubleMatrix2D par = ldRsMatrix.subDenseLDMatrix(indexInBlock);
                ldRsMatrix.releaseLDData();
                indexInBlock.clear();
                if (removeRedundant) {
                    par = EffectiveNumberEstimator.removeRedundantItems(par, redundanctLD);
                }
                partEffectiveNum = calculateEffectSampleSizeColtMatrixMyMethodByPCov(par, null);
                //System.out.println(par.toString());
                effectNumSum += partEffectiveNum;
                //  info.append(snpPositionArray[inBlockFirst]).append("\t").append(snpPositionArray[originalMatrixSize - 1]).append("\t").append(originalMatrixSize - inBlockFirst).append("\t").append(partEffectiveNum).append("\n");
                if (debug) {
                    System.out.println(partEffectiveNum);
                }
                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.print(snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[inBlockLast] + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }

                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    indexInBlock.add(snpPositionArray[i]);
                }
                DoubleMatrix2D par = ldRsMatrix.subDenseLDMatrix(indexInBlock);
                ldRsMatrix.releaseLDData();
                indexInBlock.clear();
                if (removeRedundant) {
                    par = EffectiveNumberEstimator.removeRedundantItems(par, redundanctLD);
                }

                partEffectiveNum = calculateEffectSampleSizeColtMatrixMyMethodByPCov(par, null);
                //  info.append("\t").append(snpPositionArray[inBlockFirst]).append("\t").append(snpPositionArray[inBlockLast]).append("\t").append(outBlockFirst - inBlockFirst).append("\t").append(partEffectiveNum).append("\n");
                if (debug) {
                    System.out.println(partEffectiveNum);
                }
                effectNumSum += partEffectiveNum;

                inBlockFirst = outBlockFirst;
                continue;
            }

            movedRow = inBlockLast;
            //check LD  beteen inBlockLast and checkingIndex
            while (movedRow >= inBlockFirst) {
                allAreLess = false;
                checkingIndex = outBlockFirst;
                checkingLen = 1;

                while ((checkingIndex < originalSampleSize)
                        && (ldRsMatrix.getLDAt(snpPositionArray[movedRow], snpPositionArray[checkingIndex]) < weakCorrelationThreshold)) {

                    if ((checkingLen >= maxCheckingNum && (snpPositionArray[checkingIndex] - snpPositionArray[movedRow]) >= maxCheckingDistance)) {
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
                if (debug) {
                    System.out.print(snpPositionArray[inBlockFirst] + "\t" + snpPositionArray[inBlockLast] + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }

                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    indexInBlock.add(snpPositionArray[i]);
                }
                DoubleMatrix2D par = ldRsMatrix.subDenseLDMatrix(indexInBlock);
                ldRsMatrix.releaseLDData();
                indexInBlock.clear();
                if (removeRedundant) {
                    par = EffectiveNumberEstimator.removeRedundantItems(par, redundanctLD);
                }
                partEffectiveNum = calculateEffectSampleSizeColtMatrixMyMethodByPCov(par, null);
                //System.out.println(par.toString());
                effectNumSum += partEffectiveNum;

                // info.append("\t").append(snpPositionArray[inBlockFirst]).append("\t").append(snpPositionArray[inBlockLast]).append("\t").append(outBlockFirst - inBlockFirst).append("\t").append(partEffectiveNum).append("\n");
                if (debug) {
                    System.out.println(partEffectiveNum);
                }
                inBlockFirst = outBlockFirst;

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

        // GlobalManager.logger.info(info); 
        return effectNumSum;
    }

    public static double calculateEffectSampleSizeColtMatrixLiJi(DoubleMatrix2D corrMat, Set<Integer> selectedSampleIndex) throws Exception {
        //http://gump.qimr.edu.au/general/daleN/SNPSpD/KeavneyResultsNew.pdf
        int originalSampleSize = corrMat.columns();
        // DoubleMatrix2D corrMat = ColtMatrixBasic.readMatrixFromFile("test.txt", originalMatrixSize, originalMatrixSize);
        int newSampleSize = originalSampleSize;
        if (selectedSampleIndex != null && selectedSampleIndex.size() > 0) {
            // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
            newSampleSize = selectedSampleIndex.size();

            DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
            int incRow = 0;
            int incCol = 0;
            for (int i = 0; i < originalSampleSize; i++) {
                if (!selectedSampleIndex.contains(i)) {
                    continue;
                }
                incCol = 0;
                for (int j = 0; j < originalSampleSize; j++) {
                    if (!selectedSampleIndex.contains(j)) {
                        continue;
                    }
                    tmpCorMat.setQuick(incRow, incCol, corrMat.getQuick(i, j));
                    incCol++;
                }
                incRow++;
            }
            corrMat = tmpCorMat;
            // System.out.println(corrMat.toString());
        } else if (selectedSampleIndex != null && selectedSampleIndex.isEmpty()) {
            return 0;
        }


        /*
         originalMatrixSize = corrMat.columns();
         Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
         double maxCorr = 1;
         for (int i = 0; i < originalMatrixSize; i++) {
         for (int j = i + 1; j < originalMatrixSize; j++) {
         if (Math.abs(corrMat.getQuick(i, j)) >= maxCorr) {
         if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
         highlyCorrIndexes.add(j);
         System.out.println(i + " <-> " + j);
         }
         }
         }
         }
         if (highlyCorrIndexes.size() > 0) {
         System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
         newSampleSize = originalMatrixSize - highlyCorrIndexes.size();
        
         DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(newSampleSize, newSampleSize);
         int incRow = 0;
         int incCol = 0;
         for (int i = 0; i < originalMatrixSize; i++) {
         if (highlyCorrIndexes.contains(i)) {
         continue;
         }
         incCol = 0;
         for (int j = 0; j < originalMatrixSize; j++) {
         if (highlyCorrIndexes.contains(j)) {
         continue;
         }
         tmpCorMat.setQuick(incRow, incCol, corrMat.getQuick(i, j));
         incCol++;
         }
         incRow++;
         }
         corrMat = tmpCorMat;
         // System.out.println(corrMat.toString());
         }
         */
        EigenvalueDecomposition ed = new EigenvalueDecomposition(corrMat);
        DoubleMatrix1D eVR = ed.getRealEigenvalues();
        DoubleMatrix1D eVI = ed.getImagEigenvalues();
        // System.out.println(eVR.toString());
        // System.out.println(eVI.toString());
        //double effectSampleSize = newSampleSize;
        double effectSampleSize = 0;
        double abs = 0;

        for (int i = 0; i < newSampleSize; i++) {
            abs = Math.abs(eVR.getQuick(i));
            if (abs >= 1) {
                effectSampleSize += (1);//(eVR.getQuick(i));
            }

            effectSampleSize += abs - Math.floor(abs);
        }
        return (effectSampleSize);


        /*
         DoubleArrayList eigenValueList = new DoubleArrayList(newSampleSize);
         for (int i = 0; i < newSampleSize; i++) {
         eigenValueList.add(eVR.getQuick(i));
         }
         double var = Descriptive.sampleVariance(eigenValueList, Descriptive.mean(eigenValueList));
         // System.out.println(var);
         effectSampleSize = 1 + (newSampleSize - 1) * (1 - var / newSampleSize);
         //System.out.println(effectSampleSize);
         return effectSampleSize;
         */
    }

    public static void main(String[] args) {
        try {
            EffectiveNumberEstimator sf = new EffectiveNumberEstimator();
            //sf.calculateEffectSampleSizeApacheMatrix();
            // sf.calculateEffectSampleSizeColtMatrixMyMethod();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
