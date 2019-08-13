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

import cern.colt.bitvector.BitVector;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.StatusGtySet;

/**
 *
 * @author mxli
 */
public class GenotypeSetUtil {

    /**
     * Correct zeor allele frequencies for linkage analysis
     */
    public void calculateFrequency(List<SNP> mainSnpMap, Map<String, StatusGtySet> indivGtyMap) throws Exception {
        double alleleAFreq = 0.0;
        int counter0, counter1;
        int pos;
        int listSize = mainSnpMap.size();
        SNP snp;
        //select heterozygous within a region
        for (int i = 0; i < listSize; i++) {
            snp = mainSnpMap.get(i);
            pos = snp.genotypeOrder;

            counter0 = counter1 = 0;

            //count the number of heterozygous
            for (Map.Entry<String, StatusGtySet> mIndiv : indivGtyMap.entrySet()) {
                if (mIndiv.getValue().existence.size() < pos) {
                    continue;
                }
                if (mIndiv.getValue().existence.getQuick(pos)) {
                    if (mIndiv.getValue().paternalChrom.getQuick(pos)) {
                        counter1++;
                    } else {
                        counter0++;
                    }
                    if (mIndiv.getValue().maternalChrom.getQuick(pos)) {
                        counter1++;
                    } else {
                        counter0++;
                    }
                }
            }
            alleleAFreq = counter0 * 1.0 / (counter1 + counter0);
        }
    }

    /**
     * Correct zeor allele frequencies for linkage analysis
     */
    public void correctFrequency(List<SNP> mainSnpMap, Map<String, StatusGtySet> indivGtyMap) throws Exception {
        double alleleAFreq = 0.0;
        int counter0, counter1;
        int pos;
        int listSize = mainSnpMap.size();
        SNP snp;
        //select heterozygous within a region
        for (int i = 0; i < listSize; i++) {
            snp = mainSnpMap.get(i);
            pos = snp.genotypeOrder;

            counter0 = counter1 = 0;

            //count the number of heterozygous
            for (Map.Entry<String, StatusGtySet> mIndiv : indivGtyMap.entrySet()) {
                if (mIndiv.getValue().existence.size() < pos) {
                    continue;
                }
                if (mIndiv.getValue().existence.getQuick(pos)) {
                    if (mIndiv.getValue().paternalChrom.getQuick(pos)) {
                        counter1++;
                    } else {
                        counter0++;
                    }
                    if (mIndiv.getValue().maternalChrom.getQuick(pos)) {
                        counter1++;
                    } else {
                        counter0++;
                    }
                }
            }
            alleleAFreq = counter0 * 1.0 / (counter1 + counter0);
        }
    }

    static public void calculateGenotypeCorrelationSquare(List<SNP> mainSnpMap,
            List<StatusGtySet> chromGtys, CorrelationBasedByteLDSparseMatrix ldRsMatrix, double minR2InGene) throws Exception {
        int snpNum = mainSnpMap.size();

        DoubleArrayList list1 = new DoubleArrayList(snpNum);
        DoubleArrayList list2 = new DoubleArrayList(snpNum);

        int indiSize = chromGtys.size();
        double mean1, mean2, sd1, sd2, r;
        for (int j = 0; j < snpNum; j++) {
            SNP snp1 = mainSnpMap.get(j);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }
            for (int t = j + 1; t < snpNum; t++) {
                list1.clear();
                list2.clear();
                SNP snp2 = mainSnpMap.get(t);
                if (snp2.genotypeOrder < 0) {
                    continue;
                }

                for (int k = 0; k < indiSize; k++) {
                    StatusGtySet gty = chromGtys.get(k);
                    if (gty.existence.getQuick(snp1.genotypeOrder)
                            && gty.existence.getQuick(snp2.genotypeOrder)) {
                        if (!gty.paternalChrom.getQuick(snp1.genotypeOrder) && !gty.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            list1.add(0);
                        } else if (gty.paternalChrom.getQuick(snp1.genotypeOrder) && gty.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            list1.add(2);
                        } else {
                            list1.add(1);
                        }

                        if (!gty.paternalChrom.getQuick(snp2.genotypeOrder) && !gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            list2.add(0);
                        } else if (gty.paternalChrom.getQuick(snp2.genotypeOrder) && gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            list2.add(2);
                        } else {
                            list2.add(1);
                        }
                    }
                }
                if (list1.isEmpty() || list2.isEmpty()) {
                    continue;
                }
                mean1 = Descriptive.mean(list1);
                mean2 = Descriptive.mean(list2);
                sd1 = Descriptive.sampleVariance(list1, mean1);
                sd2 = Descriptive.sampleVariance(list2, mean2);
                r = Descriptive.correlation(list1, Math.sqrt(sd1), list2, Math.sqrt(sd2));
                if (Double.isNaN(r)) {
                    continue;
                }
                r = r * r;

                //note: for r the Math.abs is needed here
                if (Math.abs(r) >= minR2InGene) {
                    ldRsMatrix.addLDAt(snp1.physicalPosition, snp2.physicalPosition, (float) r);
                }
            }
        }
    }

    static public void calculateGenotypeCorrelationSquareFast(List<SNP> mainSnpMap,
            List<StatusGtySet> chromGtys, CorrelationBasedByteLDSparseMatrix ldRsMatrix, double minR2InGene) throws Exception {
        int snpNum = mainSnpMap.size();

        int indiSize = chromGtys.size();
        double mean1, mean2, sd1, sd2, r = 0;
        //prepare some basic variables       

        double nonNumIndiv = 0;

        double douSize = 0;
        for (int j = 0; j < snpNum; j++) {
            SNP snp1 = mainSnpMap.get(j);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }
            for (int t = j + 1; t < snpNum; t++) {

                SNP snp2 = mainSnpMap.get(t);
                if (snp2.genotypeOrder < 0) {
                    continue;
                }
                douSize = 0;

                mean1 = 0;
                mean2 = 0;
                sd1 = 0;
                sd2 = 0;
                r = 0;
                nonNumIndiv = 0;
                for (int k = 0; k < indiSize; k++) {
                    StatusGtySet gty = chromGtys.get(k);
                    if (gty.existence.getQuick(snp1.genotypeOrder)
                            && gty.existence.getQuick(snp2.genotypeOrder)) {
                        /*
                         * 00 00 0 0
                         * 10 00 1 0
                         * 01 00 1 0
                         * 00 10 0 1
                         * 00 01 0 1
                         * 11 00 2 0
                         * 10 10 1 1
                         * 10 01 1 1
                         * 01 10 1 1
                         * 01 01 1 1
                         * 00 11 0 2
                         * 11 10 2 1
                         * 11 01 2 1
                         * 10 11 1 2
                         * 01 11 1 2
                         * 11 11 2 2                     
                         */

                        if (gty.paternalChrom.getQuick(snp1.genotypeOrder) && gty.maternalChrom.getQuick(snp1.genotypeOrder) && gty.paternalChrom.getQuick(snp2.genotypeOrder) && gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            r += 4;
                        } else if ((gty.paternalChrom.getQuick(snp1.genotypeOrder) && gty.maternalChrom.getQuick(snp1.genotypeOrder)) && (gty.paternalChrom.getQuick(snp2.genotypeOrder) || gty.maternalChrom.getQuick(snp2.genotypeOrder))) {
                            r += 2;
                        } else if ((gty.paternalChrom.getQuick(snp1.genotypeOrder) || gty.maternalChrom.getQuick(snp1.genotypeOrder)) && (gty.paternalChrom.getQuick(snp2.genotypeOrder) && gty.maternalChrom.getQuick(snp2.genotypeOrder))) {
                            r += 2;
                        } else if ((gty.paternalChrom.getQuick(snp1.genotypeOrder) || gty.maternalChrom.getQuick(snp1.genotypeOrder)) && (gty.paternalChrom.getQuick(snp2.genotypeOrder) || gty.maternalChrom.getQuick(snp2.genotypeOrder))) {
                            r += 1;
                        }
                        douSize += 2;

                        if (gty.paternalChrom.getQuick(snp1.genotypeOrder) && gty.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            mean1 += 2;
                            sd1 += 4;
                        } else if (gty.paternalChrom.getQuick(snp1.genotypeOrder) || gty.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            mean1++;
                            sd1++;
                        }
                        if (gty.paternalChrom.getQuick(snp2.genotypeOrder) && gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            mean2 += 2;
                            sd2 += 4;
                        } else if (gty.paternalChrom.getQuick(snp2.genotypeOrder) || gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            mean2++;
                            sd2++;
                        }
                        nonNumIndiv++;

                    }
                }

                //Warning!!!not missing value will affect the coeff
                r = (r - mean1 * mean2 / nonNumIndiv) / Math.sqrt((sd1 - mean1 * mean1 / nonNumIndiv) * (sd2 - mean2 * mean2 / nonNumIndiv));
                r = r * r;
                //correction
                // r = (douSize * r - 1) / (douSize - 3);
                //johny's correction                
                r = 1 - (douSize - 3) / (douSize - 2) * (1 - r) * (1 + 2 * (1 - r) / (douSize - 3.3));

                //note: for r the Math.abs is needed here
                if (Math.abs(r) >= minR2InGene) {
                    ldRsMatrix.addLDAt(snp1.physicalPosition, snp2.physicalPosition, (float) r);
                }
            }
        }
    }

    static public boolean calculateGenotypeCorrelationSquareFastBit(List<SNP> mainSnpMap,
            List<StatusGtySet> chromGtys, CorrelationBasedByteLDSparseMatrix ldRsMatrix, double minR2InGene) throws Exception {
        int snpNum = mainSnpMap.size();

        int indivSize = chromGtys.size();
        double r = 0;
        //prepare some basic variables       

        int varSize = mainSnpMap.size();

        long[][] bits1 = new long[varSize][];
        long[][] bits2 = new long[varSize][];
        long[][] bits3 = new long[varSize][];
        long[][] bits4 = new long[varSize][];
        boolean[] hasMissingGty = new boolean[varSize];
        Arrays.fill(hasMissingGty, false);
        BitVector temp1BV = new BitVector(indivSize);
        BitVector temp2BV = new BitVector(indivSize);
        BitVector temp3BV = new BitVector(indivSize);
        BitVector temp4BV = new BitVector(indivSize);

        double[] sum1 = new double[varSize];
        Arrays.fill(sum1, 0);
        double[] sum12 = new double[varSize];
        Arrays.fill(sum12, 0);
        int unitNum = -1;
        boolean tmpBool1, tmpBool2;

        int nonNumIndiv = 0;

        int effectNum = 0;
        for (int j = 0; j < varSize; j++) {
            SNP snp1 = mainSnpMap.get(j);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }
            nonNumIndiv = 0;

            temp1BV.replaceFromToWith(0, indivSize - 1, false);
            temp2BV.replaceFromToWith(0, indivSize - 1, false);
            temp3BV.replaceFromToWith(0, indivSize - 1, false);
            temp4BV.replaceFromToWith(0, indivSize - 1, true);

            for (int k = 0; k < indivSize; k++) {
                StatusGtySet gty = chromGtys.get(k);
                if (gty.existence.getQuick(snp1.genotypeOrder)) {

                    tmpBool1 = gty.paternalChrom.get(snp1.genotypeOrder);
                    tmpBool2 = gty.maternalChrom.get(snp1.genotypeOrder);
                    if (tmpBool1 && tmpBool2) {
                        temp1BV.putQuick(k, true);
                        temp2BV.putQuick(k, true);
                        temp3BV.putQuick(k, false);
                        sum1[j] += 2;
                        sum12[j] += 4;
                    } else if (tmpBool1 || tmpBool2) {
                        temp1BV.putQuick(k, false);
                        temp2BV.putQuick(k, true);
                        temp3BV.putQuick(k, true);
                        sum1[j] += 1;
                        sum12[j] += 1;
                    } else {
                        temp1BV.putQuick(k, false);
                        temp2BV.putQuick(k, false);
                        temp3BV.putQuick(k, false);
                    }
                    nonNumIndiv++;
                } else {
                    temp4BV.putQuick(k, false);
                    hasMissingGty[j] = true;
                    //return false;
                }
            }
            effectNum++;
            sum1[j] /= Math.sqrt(nonNumIndiv);
            sum12[j] = Math.sqrt(sum12[j] - sum1[j] * sum1[j]);
            long[] tempLong = temp1BV.elements();
            bits1[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits1[j], 0, tempLong.length);
            tempLong = temp2BV.elements();
            bits2[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits2[j], 0, tempLong.length);
            tempLong = temp3BV.elements();
            bits3[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits3[j], 0, tempLong.length);
            tempLong = temp4BV.elements();
            bits4[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits4[j], 0, tempLong.length);
            if (unitNum < 0) {
                unitNum = tempLong.length;
            }
        }
        if (effectNum < 2) {
            return true;
        }

        long x;
        int count = 0;
        int sum1i = 0;
        int sum1j = 0;
        int sum12i = 0;
        int sum12j = 0;
        int nij = 0;
        long exsit;
        double adjustSize = indivSize;
        int tempInt;
        for (int i = 0; i < snpNum; i++) {
            SNP snp1 = mainSnpMap.get(i);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }
            for (int j = i + 1; j < snpNum; j++) {
                SNP snp2 = mainSnpMap.get(j);
                if (snp2.genotypeOrder < 0) {
                    continue;
                }

                count = 0;
                //later I found Long.bit Long.bitCount(x) is faster than self-implementation 
                for (int k = 0; k < unitNum; k++) {
                    x = bits1[i][k] & bits1[j][k];
                    count += (Long.bitCount(x) << 1);  //returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 
                    x = bits2[i][k] & bits2[j][k];
                    count += (Long.bitCount(x) << 1);
                    x = bits3[i][k] & bits3[j][k];
                    count -= (Long.bitCount(x));
                }

                if (hasMissingGty[i] || hasMissingGty[j]) {
                    sum1i = 0;
                    sum1j = 0;
                    sum12i = 0;
                    sum12j = 0;
                    nij = 0;

                    for (int k = 0; k < unitNum; k++) {
                        exsit = bits4[i][k] & bits4[j][k];
                        x = exsit;
                        nij += (Long.bitCount(x));  //returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 

                        x = bits1[i][k] & bits1[i][k] & exsit;
                        tempInt = Long.bitCount(x);
                        sum1i += (tempInt);  //returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 
                        sum12i += (tempInt << 1);  //returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 

                        x = bits2[i][k] & bits2[i][k] & exsit;
                        tempInt = Long.bitCount(x);
                        sum1i += (tempInt);
                        sum12i += (tempInt << 1);

                        x = bits3[i][k] & bits3[i][k] & exsit;
                        sum12i -= (Long.bitCount(x));

                        x = bits1[j][k] & bits1[j][k] & exsit;
                        tempInt = Long.bitCount(x);
                        sum1j += (tempInt);  //returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 
                        sum12j += (tempInt << 1);
                        x = bits2[j][k] & bits2[j][k] & exsit;
                        tempInt = Long.bitCount(x);
                        sum1j += (tempInt);
                        sum12j += (tempInt << 1);

                        x = bits3[j][k] & bits3[j][k] & exsit;
                        sum12j -= (Long.bitCount(x));
                    }
                    //Strange! this formula is even faster
                    r = (count - sum1i * sum1j / ((double) nij)) / Math.sqrt((sum12i - sum1i * sum1i / ((double) nij)) * (sum12j - sum1j * sum1j / ((double) nij)));
                    //r = (count - sum1[i] * sum1[i]) / (sum12[i] * sum12[i]);
                } else {
                    //Strange! this formula is even faster
                    r = (count - sum1[i] * sum1[j]) / (sum12[i] * sum12[j]);
                    //r = (count - sum1[i] * sum1[i]) / (sum12[i] * sum12[i]);
                }

                r = r * r;
                //correction
                // r = (douSize * r - 1) / (douSize - 3);
                //johny's correction                
                r = 1 - (adjustSize - 3.0) / (adjustSize - 2.0) * (1.0 - r) * (1.0 + 2 * (1 - r) / (adjustSize - 3.3));

                //note: for r the Math.abs is needed here
                if (Math.abs(r) >= minR2InGene) {
                    ldRsMatrix.addLDAt(snp1.physicalPosition, snp2.physicalPosition, r);
                }
            }
        }
        return true;
    }

    static public void calculateLDRSequareByHaplotype(List<SNP> mainSnpMap,
            List<StatusGtySet> chromGtys, CorrelationBasedByteLDSparseMatrix ldRsMatrix, double minR2InGene) throws Exception {
        int snpNum = mainSnpMap.size();
        int indiSize = chromGtys.size();

        double freqAB = 0, freqA = 0, freqB = 0, totalHaplo = 0;
        double r2 = 0;
        double douSize = 0;
        for (int j = 0; j < snpNum; j++) {
            SNP snp1 = mainSnpMap.get(j);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }

            for (int t = j + 1; t < snpNum; t++) {
                SNP snp2 = mainSnpMap.get(t);
                if (snp2.genotypeOrder < 0) {
                    continue;
                }

                freqAB = 0;
                freqA = 0;
                freqB = 0;
                totalHaplo = 0;
                for (int k = 0; k < indiSize; k++) {
                    StatusGtySet gty = chromGtys.get(k);
                    if (gty.existence.getQuick(snp1.genotypeOrder)
                            && gty.existence.getQuick(snp2.genotypeOrder)) {
                        totalHaplo += 2;
                        if (gty.paternalChrom.getQuick(snp1.genotypeOrder) && gty.paternalChrom.getQuick(snp2.genotypeOrder)) {
                            freqAB += 1;
                        }
                        if (gty.paternalChrom.getQuick(snp1.genotypeOrder)) {
                            freqA += 1;
                        }
                        if (gty.paternalChrom.getQuick(snp2.genotypeOrder)) {
                            freqB += 1;
                        }

                        if (gty.maternalChrom.getQuick(snp1.genotypeOrder) && gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            freqAB += 1;
                        }
                        if (gty.maternalChrom.getQuick(snp1.genotypeOrder)) {
                            freqA += 1;
                        }
                        if (gty.maternalChrom.getQuick(snp2.genotypeOrder)) {
                            freqB += 1;
                        }
                    }
                }
                if (totalHaplo < 10) {
                    continue;
                }
                freqAB = freqAB / totalHaplo;
                freqA = freqA / totalHaplo;
                freqB = freqB / totalHaplo;

                r2 = (freqAB - freqA * freqB);
                r2 = r2 * r2;
                r2 = r2 / (freqA * (1 - freqA) * freqB * (1 - freqB));
                douSize = totalHaplo;
                //correction
                // r = (douSize * r - 1) / (douSize - 3);
                //johny's correction                
                r2 = 1 - (douSize - 3.0) / (douSize - 2.0) * (1 - r2) * (1.0 + 2 * (1.0 - r2) / (douSize - 3.3));
                //note: for r the Math.abs is needed here             
                if (r2 >= minR2InGene) {
                    ldRsMatrix.addLDAt(snp1.physicalPosition, snp2.physicalPosition, r2);
                }
                // System.out.println(snp1.getRsID() + "-> " + snp2.getRsID() + " " + r2);
            }
        }
    }

    final static long m1 = 0x5555555555555555L; //binary: 0101...  
    final static long m2 = 0x3333333333333333L; //binary: 00110011..  
    final static long m4 = 0x0f0f0f0f0f0f0f0fL; //binary:  4 zeros,  4 ones ...  
    final static long m8 = 0x00ff00ff00ff00ffL; //binary:  8 zeros,  8 ones ...  
    final static long m16 = 0x0000ffff0000ffffL; //binary: 16 zeros, 16 ones ...  
    final static long m32 = 0x00000000ffffffffL; //binary: 32 zeros, 32 ones ...  
    final static long hff = 0xffffffffffffffffL; //binary: all ones  
    final static long h01 = 0x0101010101010101L; //the sum of 256 to the power of 0,1,2,3...  

    static public boolean calculateLDRSequareByHaplotypeFast(List<SNP> mainSnpMap,
            List<StatusGtySet> chromGtys, CorrelationBasedByteLDSparseMatrix ldRsMatrix, double minR2InGene) throws Exception {
        int snpNum = mainSnpMap.size();
        int indiSize = chromGtys.size();

        double r2 = 0;

        int unitNum = -1;
        long[][] bits1 = new long[snpNum][];
        long[][] bits2 = new long[snpNum][];
        long[][] bits3 = new long[snpNum][];

        BitVector temp1BV = new BitVector(indiSize);
        BitVector temp2BV = new BitVector(indiSize);
        BitVector temp3BV = new BitVector(indiSize);
        double[] freqA = new double[snpNum];
        Arrays.fill(freqA, 0);
        double[] freqCA = new double[snpNum];
        Arrays.fill(freqCA, 0);
        int totalHaplo = 0;
        boolean tmpBool;
        int effectNum = 0;
        boolean hasMissingGty = false;
        for (int j = 0; j < snpNum; j++) {
            SNP snp1 = mainSnpMap.get(j);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }
            temp1BV.replaceFromToWith(0, indiSize - 1, false);
            temp2BV.replaceFromToWith(0, indiSize - 1, false);
            temp3BV.replaceFromToWith(0, indiSize - 1, false);
            totalHaplo = 0;
            for (int k = 0; k < indiSize; k++) {
                StatusGtySet gty = chromGtys.get(k);
                if (gty.existence.getQuick(snp1.genotypeOrder)) {
                    tmpBool = gty.paternalChrom.get(snp1.genotypeOrder);
                    temp1BV.putQuick(k, tmpBool);
                    if (tmpBool) {
                        freqA[j] += 1;
                    }
                    tmpBool = gty.maternalChrom.get(snp1.genotypeOrder);
                    temp2BV.putQuick(k, tmpBool);
                    if (tmpBool) {
                        freqA[j] += 1;
                    }
                    totalHaplo += 2;
                } else {
                    temp3BV.putQuick(k, true);
                    hasMissingGty = true;
                    // return false;
                }
            }
            freqA[j] /= totalHaplo;
            freqCA[j] = (1 - freqA[j]) * freqA[j];
            long[] tempLong = temp1BV.elements();
            bits1[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits1[j], 0, tempLong.length);
            tempLong = temp2BV.elements();
            bits2[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits2[j], 0, tempLong.length);
            tempLong = temp3BV.elements();
            bits3[j] = new long[tempLong.length];
            System.arraycopy(tempLong, 0, bits3[j], 0, tempLong.length);
            if (unitNum < 0) {
                unitNum = tempLong.length;
            }
            effectNum++;
        }

        if (effectNum < 2) {
            return true;
        }

        long x = 0;

        int freqAB = 0;
        int missingNum = 0;

        totalHaplo = 2 * indiSize;
        double adjustSize = 2 * indiSize;
        for (int j = 0; j < snpNum; j++) {
            SNP snp1 = mainSnpMap.get(j);
            if (snp1.genotypeOrder < 0) {
                //System.out.println(snp1.rsID+"  "+snp1.physicalPosition);
                continue;
            }
            for (int t = j + 1; t < snpNum; t++) {
                SNP snp2 = mainSnpMap.get(t);
                if (snp2.genotypeOrder < 0) {
                    continue;
                }

                freqAB = 0;
                missingNum = 0;
                if (hasMissingGty) {
                    totalHaplo = 2 * indiSize;
                }
                //refecen http://blog.csdn.net/hitwhylz/article/details/10122617
                for (int k = 0; k < unitNum; k++) {
                    x = bits1[j][k] & bits1[t][k];
                    freqAB += (Long.bitCount(x));  //returns left 8 bits of x + (x<<8) + (x<<16) + (x<<24) + ... 
                    x = bits2[j][k] & bits2[t][k];
                    freqAB += (Long.bitCount(x));
                    if (hasMissingGty) {
                        x = bits3[j][k] | bits3[t][k];
                        missingNum += (Long.bitCount(x));
                    }
                }

                if (hasMissingGty) {
                    totalHaplo -= (missingNum * 2);
                }

                r2 = ((double) freqAB / totalHaplo - freqA[j] * freqA[t]);
                r2 = (r2 * r2) / (freqCA[j] * freqCA[t]);

                //correction
                // r = (douSize * r - 1) / (douSize - 3);
                //johny's correction                
                r2 = 1 - (adjustSize - 3.0) / (adjustSize - 2.0) * (1 - r2) * (1.0 + 2 * (1 - r2) / (adjustSize - 3.3));
                //note: for r the Math.abs is needed here             
                if (r2 >= minR2InGene) {
                    ldRsMatrix.addLDAt(snp1.physicalPosition, snp2.physicalPosition, r2);
                }
                // System.out.println(snp1.getRsID() + "-> " + snp2.getRsID() + " " + r2);
            }
        }
        return true;
    }

}
