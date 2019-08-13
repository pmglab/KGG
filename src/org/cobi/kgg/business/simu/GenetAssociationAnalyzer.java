/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import cern.jet.stat.Probability;
import java.util.Arrays;
import java.util.List;
import org.cobi.kgg.business.entity.StatusGtySet;
import org.cobi.util.math.ContingencyTable;

/**
 *
 * @author mxli
 */
public class GenetAssociationAnalyzer {
     public double[] allelicAssociationTest(List<Individual> indivList) throws Exception {
        if (indivList == null || indivList.isEmpty()) {
            return null;
        }
        int snpNum = indivList.get(0).markerGtySet.paternalChrom.size();
        int indivSize = indivList.size();

        long[][] counts = new long[2][2];
        int rowNum = counts.length;
      
        double[] pValues = new double[snpNum];

        // table for contigency test
        //           0    1
        // control  counts[0][0]   counts[0][1]
        // case     counts[1][0]   counts[1][1]

        for (int i = 0; i < snpNum; i++) {
            for (int j = 0; j < rowNum; j++) {
                Arrays.fill(counts[j], 0);
            }
            for (int k = 0; k < indivSize; k++) {
                StatusGtySet gty = indivList.get(k).markerGtySet;
                if (indivList.get(k).getAffectedStatus() == 2) {
//                    if (gty.existence.getQuick(i)) {
                        if (gty.paternalChrom.getQuick(i)) {
                            counts[1][1] += 1;
                        } else {
                            counts[1][0] += 1;
                        }
                        if (gty.maternalChrom.getQuick(i)) {
                            counts[1][1] += 1;
                        } else {
                            counts[1][0] += 1;
                        }
//                    }
                } else if (indivList.get(k).getAffectedStatus() == 1) {
//                    if (gty.existence.getQuick(i)) {
                        if (gty.paternalChrom.getQuick(i)) {
                            counts[0][1] += 1;
                        } else {
                            counts[0][0] += 1;
                        }
                        if (gty.maternalChrom.getQuick(i)) {
                            counts[0][1] += 1;
                        } else {
                            counts[0][0] += 1;
                        }
//                    }
                }
            }
            pValues[i] = ContingencyTable.pearsonChiSquared22(counts);
            pValues[i]=Probability.chiSquareComplemented(1, pValues[i]);
            
            /*
            if (pValues[snpIndex] <= 0.000001) {
            System.out.println("SNP " + snpIndex + " :" + pValues[snpIndex] + " :" + (counts[0][1] * 1.0) / (counts[0][0] + counts[0][1]) + " :" + (counts[1][1] * 1.0) / (counts[1][0] + counts[1][1]));
            }
             * 
             */

        }
        return pValues;
    }
}
