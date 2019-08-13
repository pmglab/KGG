/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.util.text.LocalString;

/**
 *
 * @author mxli
 */
public class PopuStatSummarizer {

    public void summarizePhenotypeProperty1(List<Individual> allIndiv, List<DiseaseSNP> snpList) throws Exception {
        int suscepNum = snpList.size();

        int[] suscepLoci = new int[suscepNum];
        boolean[] riskAlleles = new boolean[suscepNum];
        int indivSize = allIndiv.size();

        for (int j = 0; j < suscepNum; j++) {
            suscepLoci[j] = snpList.get(j).getLDMarkerPosition();
            riskAlleles[j] = snpList.get(j).isRiskAlleleLable();
        }

        double prevalence = 0.0;
        double[][] penerances = new double[suscepNum][3];
        int[][] accounts = new int[suscepNum][3];
        for (int i = 0; i < suscepNum; i++) {
            Arrays.fill(penerances[i], 0);
            Arrays.fill(accounts[i], 0);
        }

        int affected = 0;
        for (int k = 0; k < indivSize; k++) {
            Individual indiv = allIndiv.get(k);
            affected = indiv.getAffectedStatus();
            if (affected == 2) {
                prevalence += 1.0;
            }

            StatusGtySet gty = indiv.markerGtySet;
            for (int i = 0; i < suscepNum; i++) {
//                if (gty.existence.getQuick(suscepLoci[i])) {
                    //ensure 2 denote 2 risk alleles and 1 1 risk allele and 0 no risk allele
                    if (gty.paternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i] && gty.maternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i]) {
                        accounts[i][2] += 1;
                        if (affected == 2) {
                            penerances[i][2] += 1.0;
                        }
                    } else if (gty.paternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i] || gty.maternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i]) {
                        accounts[i][1] += 1;
                        if (affected == 2) {
                            penerances[i][1] += 1.0;
                        }
                    } else {
                        accounts[i][0] += 1;
                        if (affected == 2) {
                            penerances[i][0] += 1.0;
                        }
                    }
//                }
            }
        }
        /*
         int count = 0;
         StatusGtySet gty1 = indiv.traitGtySet;
         for (int i = 0; i < suscepNum; i++) {
         if (gty1.paternalChrom.getQuick(i) != gty.paternalChrom.getQuick(i)) {
         count++;
         }
         if (gty1.maternalChrom.getQuick(i) != gty.maternalChrom.getQuick(i)) {
         count++;
         }
         }
         System.out.println(count);
         */
        DecimalFormat df = new DecimalFormat("0.0000000", new DecimalFormatSymbols(Locale.US));
        StringBuffer infor = new StringBuffer();
        infor.append("Prevalence: ");
        infor.append(df.format(prevalence / indivSize));
        infor.append("\n");
        infor.append("riskRatio1\triskRatio2\n");
        BufferedWriter genePBw = new BufferedWriter(new FileWriter("geneticRisk.txt", true));
        for (int i = 0; i < suscepNum; i++) {
            for (int j = 0; j < 3; j++) {
                penerances[i][j] /= accounts[i][j];
            }
            infor.append(df.format(penerances[i][1] / penerances[i][0]));
            infor.append("\t");
            infor.append(df.format(penerances[i][2] / penerances[i][0]));
            infor.append("\n");
            //save in  hard disk
            genePBw.write(df.format(penerances[i][1] / penerances[i][0]));
            genePBw.write("\t");
            genePBw.write(df.format(penerances[i][1] / penerances[i][0]));
            genePBw.write("\t");
        }
        genePBw.newLine();
        genePBw.close();

        System.out.println(infor);
    }
    
       public DoubleMatrix2D convertLDR2JointProbability(final double[][] ldR) throws Exception {
        int rowNum = ldR.length, colNum = ldR[0].length;
        int i, j;
        double tmp;
        DoubleMatrix2D probMatrix = new DenseDoubleMatrix2D(ldR);
        
        System.out.println("Joint probabilies:");
        double[] alleleFreq = new double[rowNum];
        for (i = 0; i < rowNum; i++) {
            alleleFreq[i] = ldR[i][i];
        }
        for (i = 0; i < rowNum; i++) {
            for (j = i + 1; j < colNum; j++) {
                tmp = ldR[i][j] * Math.sqrt(alleleFreq[i] * alleleFreq[j] * (1 - alleleFreq[i]) * (1 - alleleFreq[j]));
                tmp = tmp + alleleFreq[i] * alleleFreq[j];
                probMatrix.setQuick(i, j, tmp);
            }
        }
        LocalString.print2DRealMatrix(probMatrix);
        return probMatrix;
    }
    
    public DoubleMatrix2D convertLDR2JointProbability(final DoubleMatrix2D ldR, double[] alleleFreq) throws Exception {
        int rowNum = alleleFreq.length, colNum = alleleFreq.length;
        int i, j;
        double tmp;
        DoubleMatrix2D probMatrix = new DenseDoubleMatrix2D(rowNum, colNum);
        
        System.out.println("Joint probabilies:");
        
        for (i = 0; i < rowNum; i++) {
            probMatrix.setQuick(i, i, alleleFreq[i]);
            for (j = i + 1; j < colNum; j++) {
                tmp = ldR.getQuick(i, j) * Math.sqrt(alleleFreq[i] * alleleFreq[j] * (1 - alleleFreq[i]) * (1 - alleleFreq[j]));
                tmp = tmp + alleleFreq[i] * alleleFreq[j];
                if (tmp < 0) {
                    tmp = 0;
                }
                probMatrix.setQuick(i, j, tmp);
            }
        }
        LocalString.print2DRealMatrix(probMatrix);
        return probMatrix;
    }
    
    public void summarizePhenotypeProperty1(List<Individual> allIndiv, List<DiseaseSNP> snpList, IntArrayList caseIndexes, IntArrayList controlIndexes) throws Exception {
        int suscepNum = snpList.size();
        
        int[] suscepLoci = new int[suscepNum];
        boolean[] riskAlleles = new boolean[suscepNum];
        int indivSize = allIndiv.size();
        
        for (int j = 0; j < suscepNum; j++) {
            suscepLoci[j] = snpList.get(j).getLDMarkerPosition();
            riskAlleles[j] = snpList.get(j).isRiskAlleleLable();
        }
        
        
        double prevalence = 0.0;
        double[][] penerances = new double[suscepNum][3];
        int[][] accounts = new int[suscepNum][3];
        for (int i = 0; i < suscepNum; i++) {
            Arrays.fill(penerances[i], 0);
            Arrays.fill(accounts[i], 0);
        }
        
        int affected = 0;
        for (int k = 0; k < indivSize; k++) {
            Individual indiv = allIndiv.get(k);
            affected = indiv.getAffectedStatus();
            if (affected == 2) {
                prevalence += 1.0;
                caseIndexes.add(k);
            } else {
                controlIndexes.add(k);
            }
                       
            StatusGtySet gty = indiv.markerGtySet;
            for (int i = 0; i < suscepNum; i++) {
//                if (gty.existence.getQuick(suscepLoci[i])) {
                    //ensure 2 denote 2 risk alleles and 1 1 risk allele and 0 no risk allele
                    if (gty.paternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i] && gty.maternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i]) {
                        accounts[i][2] += 1;
                        if (affected == 2) {
                            penerances[i][2] += 1.0;
                        }
                    } else if (gty.paternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i] || gty.maternalChrom.getQuick(suscepLoci[i]) == riskAlleles[i]) {
                        accounts[i][1] += 1;
                        if (affected == 2) {
                            penerances[i][1] += 1.0;
                        }
                    } else {
                        accounts[i][0] += 1;
                        if (affected == 2) {
                            penerances[i][0] += 1.0;
                        }
                    }
//                }
            }
        }
        /*
        int count = 0;
        StatusGtySet gty1 = indiv.traitGtySet;
        for (int i = 0; i < suscepNum; i++) {
        if (gty1.paternalChrom.getQuick(i) != gty.paternalChrom.getQuick(i)) {
        count++;
        }
        if (gty1.maternalChrom.getQuick(i) != gty.maternalChrom.getQuick(i)) {
        count++;
        }
        }
        System.out.println(count);
         */
        DecimalFormat df = new DecimalFormat("0.0000000", new DecimalFormatSymbols(Locale.US));
        StringBuffer infor = new StringBuffer();
        infor.append("Prevalence: ");
        infor.append(df.format(prevalence / indivSize));
        infor.append("\n");
        infor.append("riskRatio1\triskRatio2\n");
        BufferedWriter genePBw = new BufferedWriter(new FileWriter("geneticRisk.txt", true));
        for (int i = 0; i < suscepNum; i++) {
            for (int j = 0; j < 3; j++) {
                penerances[i][j] /= accounts[i][j];
            }
            infor.append(df.format(penerances[i][1] / penerances[i][0]));
            infor.append("\t");
            infor.append(df.format(penerances[i][2] / penerances[i][0]));
            infor.append("\n");
            //save in  hard disk
            genePBw.write(df.format(penerances[i][1] / penerances[i][0]));
            genePBw.write("\t");
            genePBw.write(df.format(penerances[i][1] / penerances[i][0]));
            genePBw.write("\t");
        }
        genePBw.newLine();
        genePBw.close();
        
        System.out.println(infor);
    }
    
}
