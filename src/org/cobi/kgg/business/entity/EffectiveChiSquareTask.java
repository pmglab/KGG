/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.stat.Gamma;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.cobi.kgg.business.SetBasedTest;
import static org.cobi.kgg.business.SetBasedTest.partitionEvenBlock;
import org.cobi.util.math.MultipleTestingMethod;
import org.cobi.util.thread.Task;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.ops.CommonOps;

/**
 *
 * @author mxli
 */
public class EffectiveChiSquareTask extends Task implements Callable<String> {

    List<Gene> genes;
    CorrelationBasedByteLDSparseMatrix ldCorr;
    boolean ignoreNoLDSNP;
    int snpPVTypeIndex;

    List<PValueGene> pValueGenes;
    DoubleArrayList snpPValueArray = new DoubleArrayList();

    public EffectiveChiSquareTask(List<Gene> genes, CorrelationBasedByteLDSparseMatrix ldCorr, boolean ignoreNoLDSNP, int snpPVTypeIndex) {
        this.pValueGenes = new ArrayList<PValueGene>();
        this.snpPValueArray = new DoubleArrayList();
        this.genes = genes;
        this.ldCorr = ldCorr;
        this.ignoreNoLDSNP = ignoreNoLDSNP;
        this.snpPVTypeIndex = snpPVTypeIndex;

    }

    public List<PValueGene> getpValueGenes() {
        return pValueGenes;
    }

    public DoubleArrayList getSnpPValueArray() {
        return snpPValueArray;
    }

    @Override
    public String call() throws Exception {
        double p1 = Double.NaN;
        for (Gene gene : genes) {
            PValueGene pValueGene = new PValueGene(gene.getSymbol());
            p1 = snpSetPValuebyMyScaledChisquareTestBlock(gene);
            pValueGene.setpValue(p1);
            pValueGenes.add(pValueGene);
        }
        fireTaskComplete();
        return "";
    }

    public String ldPruning(List<SNP> mainSnpMap, CorrelationBasedByteLDSparseMatrix ldCorr, double maxCorr, boolean ingoreNOGty) throws Exception {
        List<SNP> tmpSNPMap = new ArrayList<SNP>();
        tmpSNPMap.addAll(mainSnpMap);
        mainSnpMap.clear();
        int listSize = tmpSNPMap.size();
        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
        int windowSize = 50;
        int stepLen = 5;
        double r, c;
        int[] counts = new int[2];
        for (int s = 0; s < listSize; s += stepLen) {
            for (int i = s; (i - s <= windowSize) && (i < listSize); i++) {
                SNP snp1 = tmpSNPMap.get(i);
                if (ingoreNOGty) {
                    if (snp1.genotypeOrder < 0) {
                        highlyCorrIndexes.add(i);
                        continue;
                    }
                }
                if (highlyCorrIndexes.contains(i)) {
                    continue;
                }
                for (int j = i + 1; (j - i <= windowSize) && (j < listSize); j++) {
                    if (highlyCorrIndexes.contains(j)) {
                        continue;
                    }
                    SNP snp2 = tmpSNPMap.get(j);
                    if (ingoreNOGty) {
                        if (snp2.genotypeOrder < 0) {
                            highlyCorrIndexes.add(j);
                            continue;
                        }
                    }

                    r = ldCorr.getLDAt(snp1.physicalPosition, snp2.physicalPosition);
                    /*
                     r = Math.sqrt(ldRMatrix.getQuick(i, j));
                     //for R
                     c = (0.6065 * r - 1.033) * r + 1.7351;
                     if (c > 2) {
                     c = 2;
                     }
                     */

                    //R2 
                    //y = -35.741x6 + 111.16x5 - 128.42x4 + 66.906x3 - 14.641x2 + 0.6075x + 0.8596
                    //c = (((((-35.741 * r + 111.16) * r - 128.42) * r + 66.906) * r - 14.641) * r + 0.6075) * r + 0.8596;
                    //y = 0.2725x2 - 0.3759x + 0.8508
                    //c = (0.2725 * r - 0.3759) * r + 0.8508;
                    // y = 0.2814x2 - 0.4308x + 0.86
                    //c = (0.2814 * r - 0.4308) * r + 0.86;
                    //y = -0.155x + 0.8172
                    //c = -0.155 * r + 0.8172;
                    //c = 0.9;
                    //r = Math.pow(r, c);

                    if (r >= maxCorr) {
                        highlyCorrIndexes.add(j);
                    }
                }
            }
        }

        counts[0] = listSize - highlyCorrIndexes.size();
        counts[1] = listSize;
        String info = (listSize - (highlyCorrIndexes.size()) + " SNPs (out of " + listSize + ") passed LD pruning (r2>=" + maxCorr + ").");

        for (int s = 0; s < listSize; s++) {
            SNP snp1 = tmpSNPMap.get(s);
            if (!highlyCorrIndexes.contains(s)) {
                mainSnpMap.add(snp1);
            }
        }
        return info;
    }

    private double snpSetPValuebyMyScaledChisquareTestBlock(Gene gene) throws Exception {

        List<SNP> snpList = gene.snps;

        //remove redundant SNPs according to LD
        double maxCorr = 0.98;
        Collections.sort(snpList, new SNPPosiComparator());
        ldPruning(snpList, ldCorr, maxCorr, ignoreNoLDSNP);

        //because it is very time consumming, when SNP number is over 200. I try to spl
        double p = Double.NaN;
        int totaSNPSize = snpList.size();
        //System.out.println(gene.getSymbol() + " " + snpNum);
        for (int k = 0; k < totaSNPSize; k++) {
            SNP snp = gene.snps.get(k);
            double[] pValues = snp.getpValues();
            if (pValues == null) {
                continue;
            }
            if (!Double.isNaN(pValues[snpPVTypeIndex])) //
            {
                snpPValueArray.add(pValues[snpPVTypeIndex]);
            }
        }

        double[] tmpResults = new double[2];
        double[] totalResults = new double[2];
        Arrays.fill(totalResults, 0);
        //because it is very time consumming, when SNP number is over 200. I try to spl
        int minBlockLen = 1;

        int maxBlockLen = 500;
        int maxCheckingNum = 50;
        int maxCheckingDistance = 1000000;

        // check number of snps for rs genome and pos genome
        double minWthinBlockR2 = 0.1;

        int[] keySNPIndex = new int[1];
        keySNPIndex[0] = -1;
        if (totaSNPSize <= maxBlockLen) {
            p = snpSetPValuebyMyChiSquareApproxEJML(snpList, ldCorr, snpPVTypeIndex, false, tmpResults);

            totalResults[0] += tmpResults[0];
            totalResults[1] += tmpResults[1];

            //System.out.println(rLDMatrix.toString());
            return p;
        }

        //automatically splict a large gene into multiple blocks and then combine the block-wise p1-values by scaled chi-square test
        // which are more powerful gene with large number of SNPs   
        // System.out.println(poweredCorrMat.toString()); 
        boolean allAreLess = false;
        boolean debug = false;
        int movedRow;

        int inBlockFirst = 0;
        int outBlockFirst = minBlockLen;
        if (outBlockFirst > totaSNPSize) {
            outBlockFirst = totaSNPSize;
        }
        int inBlockLast = outBlockFirst - 1;
        int checkingIndex = outBlockFirst;
        int checkingLen = 1;
        List<SNP> tmpSNPList = new ArrayList<SNP>();

        while (inBlockLast <= totaSNPSize) {
            //find a site whose LD beteen inBlockFirst start to be less than minWthinBlockR2
            while ((outBlockFirst < totaSNPSize)
                    && (outBlockFirst - inBlockFirst < maxBlockLen)
                    && (ldCorr.getLDAt(snpList.get(inBlockFirst).physicalPosition, snpList.get(outBlockFirst).physicalPosition) >= minWthinBlockR2)) {
                outBlockFirst++;
            }
            inBlockLast = outBlockFirst - 1;

            if (outBlockFirst >= totaSNPSize) {
                if (debug) {
                    System.out.print(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(totaSNPSize - 1).physicalPosition + "\t" + (totaSNPSize - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < totaSNPSize; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

                break;
            }

            if (outBlockFirst - inBlockFirst >= maxBlockLen) {
                if (debug) {
                    System.out.println(snpList.get(inBlockFirst).physicalPosition
                            + "\t" + snpList.get(inBlockLast).physicalPosition + "\t"
                            + (outBlockFirst - inBlockFirst) + "\t");
                }

                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);
                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

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
                        && (ldCorr.getLDAt(snpList.get(movedRow).physicalPosition, snpList.get(checkingIndex).physicalPosition) < minWthinBlockR2)) {
                    if ((checkingLen >= maxCheckingNum && (snpList.get(checkingIndex).physicalPosition
                            - snpList.get(movedRow).physicalPosition) >= maxCheckingDistance)) {
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
                    System.out.println(snpList.get(inBlockFirst).physicalPosition + "\t" + snpList.get(outBlockFirst).physicalPosition + "\t" + (outBlockFirst - inBlockFirst) + "\t");
                }
                tmpSNPList.clear();
                for (int i = inBlockFirst; i < outBlockFirst; i++) {
                    tmpSNPList.add(snpList.get(i));
                }
                keySNPIndex[0] = -1;
                p = snpSetPValuebyMyChiSquareApproxEJML(tmpSNPList, ldCorr, snpPVTypeIndex, false, tmpResults);

                totalResults[0] += tmpResults[0];
                totalResults[1] += tmpResults[1];

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

        p = Gamma.incompleteGammaComplement(totalResults[0] / 2, totalResults[1] / 2);
        return p;
        //Note it is usually r2, wwe have to transform it
        // return combinePValuebyVEGAS(pvalueWeightArray, subLDCorr);
    }

    //use a faster matrix invers function DenseMatrix64F
    private double snpSetPValuebyMyChiSquareApproxEJML(List<SNP> snpList, CorrelationBasedByteLDSparseMatrix ldCorr, int snpPVTypeIndex,
            boolean needWeight, double[] results) throws Exception {
        int snpNum = snpList.size();
        // System.out.println("Size " + snpNum);
        //because it is very time consumming, when SNP number is over 200. I try to spl

        double p1 = Double.NaN;

        int[] keySNPPosition = new int[1];
        keySNPPosition[0] = -1;
        List<PValueWeight> pvalueWeightList = new ArrayList<PValueWeight>();

        //here I think the only uesfulness of the pvalueWeightList is to filter out the null p1-value SNPs
        for (int k = 0; k < snpNum; k++) {
            SNP snp = snpList.get(k);
            if (ignoreNoLDSNP) {
                if (snp.genotypeOrder < 0) {
                    continue;
                }
            }
            double[] pValues = snp.getpValues();
            if (pValues == null) {
                continue;
            }

            if (!Double.isNaN(pValues[snpPVTypeIndex])) //
            {
                PValueWeight pv = new PValueWeight();
                pv.physicalPos = snp.physicalPosition;
                pv.pValue = pValues[snpPVTypeIndex];
                pv.chiSquare = pv.pValue / 2;
                pv.chiSquare = MultipleTestingMethod.zScore(pv.chiSquare);
                pv.chiSquare = pv.chiSquare * pv.chiSquare;

                pv.weight = 1;
                pvalueWeightList.add(pv);
            }
        }

        snpNum = pvalueWeightList.size();

        if (snpNum == 0) {
            keySNPPosition[0] = -1;
            results[0] = 0;
            results[1] = 0;
            return 1;
        } else if (snpNum == 1) {
            keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
            results[0] = 1;
            results[1] = pvalueWeightList.get(0).chiSquare;
            return pvalueWeightList.get(0).pValue;
        }

        DoubleMatrix2D ldRMatrix = new DenseDoubleMatrix2D(snpNum, snpNum);
        double r, c;
        for (int i = 0; i < snpNum; i++) {
            PValueWeight pv = pvalueWeightList.get(i);
            ldRMatrix.setQuick(i, i, pv.weight * pv.weight);
            for (int j = i + 1; j < snpNum; j++) {
                //Math.sqrt(snpPValueArray[t].var * snpPValueArray[j].var) 
                ldRMatrix.setQuick(i, j, pv.weight * pvalueWeightList.get(j).weight * ldCorr.getLDAt(pv.physicalPos, pvalueWeightList.get(j).physicalPos));
                if (ldRMatrix.getQuick(i, j) > 0) {
                    r = ldRMatrix.getQuick(i, j);
                    /*
                     r = Math.sqrt(r);
                     //for R
                     c = (0.6065 * r - 1.033) * r + 1.7351;
                     if (c > 2) {
                     c = 2;
                     }
                     */

                    //R2
                    //R2
                    //y = -35.741x6 + 111.16x5 - 128.42x4 + 66.906x3 - 14.641x2 + 0.6075x + 0.8596
                    //c = (((((-35.741 * r + 111.16) * r - 128.42) * r + 66.906) * r - 14.641) * r + 0.6075) * r + 0.8596;
                    //y = 0.2725x2 - 0.3759x + 0.8508
                    //c = (0.2725 * r - 0.3759) * r + 0.8508;
                    // y = 0.2814x2 - 0.4308x + 0.86
                    //c = (0.2814 * r - 0.4308) * r + 0.86;
                    //y = -0.155x + 0.8172
                    //c = -0.155 * r + 0.8172;
                   // c = 0.9;
                    //r = Math.pow(r, c);
                    ldRMatrix.setQuick(i, j, r);
                } else {
                    r = 0;
                    ldRMatrix.setQuick(i, j, r);
                }
                ldRMatrix.setQuick(j, i, r);
            }
        }

        //remove redundant SNPs according to LD
        int originalSampleSize = snpNum;
        Set<Integer> highlyCorrIndexes = new HashSet<Integer>();
        double maxCorr = 0.98;
        for (int i = 0; i < originalSampleSize; i++) {
            for (int j = i + 1; j < originalSampleSize; j++) {
                if (Math.abs(ldRMatrix.getQuick(i, j)) >= maxCorr) {
                    if (!highlyCorrIndexes.contains(j) && !highlyCorrIndexes.contains(i)) {
                        highlyCorrIndexes.add(j);
                        // System.out.println(t + " <-> " + j);
                    }
                }
            }
        }

        if (highlyCorrIndexes.size() > 0) {
            // System.out.println("Removed columns and rows: " + highlyCorrIndexes.toString());
            snpNum = originalSampleSize - highlyCorrIndexes.size();
            List<PValueWeight> tmpPvalueWeightList = new ArrayList<PValueWeight>(pvalueWeightList);
            pvalueWeightList.clear();
            DoubleMatrix2D tmpCorMat = new DenseDoubleMatrix2D(snpNum, snpNum);
            int incRow = 0;
            int incCol = 0;
            for (int i = 0; i < originalSampleSize; i++) {
                if (highlyCorrIndexes.contains(i)) {
                    continue;
                }
                pvalueWeightList.add(tmpPvalueWeightList.get(i));
                incCol = 0;
                for (int j = 0; j < originalSampleSize; j++) {
                    if (highlyCorrIndexes.contains(j)) {
                        continue;
                    }
                    tmpCorMat.setQuick(incRow, incCol, ldRMatrix.getQuick(i, j));
                    incCol++;
                }
                incRow++;
            }
            ldRMatrix = tmpCorMat;
            tmpPvalueWeightList.clear();
            // System.out.println(corrMat.toString());
        }

        if (snpNum == 1) {
            keySNPPosition[0] = pvalueWeightList.get(0).physicalPos;
            results[0] = 1;
            results[1] = pvalueWeightList.get(0).chiSquare;
            return pvalueWeightList.get(0).pValue;
        }

        double df = 0;
        double Y = 0;
        DenseMatrix64F A = new DenseMatrix64F(snpNum, snpNum);
        DenseMatrix64F b1 = new DenseMatrix64F(snpNum, 1);
        for (int i = 0; i < snpNum; i++) {
            A.set(i, i, 1);
            for (int j = i + 1; j < snpNum; j++) {
                A.set(i, j, ldRMatrix.getQuick(i, j));
                A.set(j, i, ldRMatrix.getQuick(i, j));
            }
        }

        for (int k = 0; k < snpNum; k++) {
            b1.set(k, 0, pvalueWeightList.get(k).chiSquare);
        }

        DenseMatrix64F b2 = new DenseMatrix64F(snpNum, 1);
        for (int i = 0; i < snpNum; i++) {
            b2.set(i, 0, 1);
        }

        /*
         DenseMatrix64F x1 = new DenseMatrix64F(snpNum, 1);
         DenseMatrix64F x2 = new DenseMatrix64F(snpNum, 1);
         SetBasedTest.mySudoSVDSolverEJML(A, b1, b2, x1, x2);
         // SetBasedTest.myInverseUJMP(A, b1, b2, x1, x2);
         df = 0;
         Y = 0;
         for (int i = 0; i < snpNum; i++) {
         PValueWeight pv = pvalueWeightList.get(i);
         Y += (pv.weight * x1.get(i, 0));
         //  Y1 += chisquareArray[t][0];
         //   snpPValueArray[t].var *
         df += (pv.weight * x2.get(i, 0));
         }
         if (Y <= 0) {
         Y = 0;
         }
         if (df <= 0) {
         df = 0.1;
         }

         results[0] = df;
         results[1] = Y;
         */
        SetBasedTest sbt = new SetBasedTest();
        sbt.mySudoSVDSolverOverlappedWindow(A, b1, b2, results);
        //It has some inflation at the tail
        //SetBasedTest.combinePValuebyCorrectedChiFisherCombinationTestMXLiS(A, b1, results);

        p1 = Gamma.incompleteGammaComplement(results[0] / 2, results[1] / 2);
        return p1;
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
                df = 0.001;
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
        for (int i = 0; i < blockNum; i += 2) {
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
        }

        bounds.clear();
        blockNum = bigBlockIndexes.length - 2;
        //calculate overlapped regions
        for (int i = 1; i < blockNum; i += 2) {
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
        }

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
        double diffCutoff = 0.01 * A.numRows;
        double diff1, diff2;
        double minDiff1 = Integer.MAX_VALUE, minDiff2 = Integer.MAX_VALUE;
        DenseMatrix64F x1 = new DenseMatrix64F(A.numRows, 1);
        DenseMatrix64F x2 = new DenseMatrix64F(A.numRows, 1);
        maxB1 = maxB1 * 3;
        maxB2 = maxB2 * 3;
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

    }
}
