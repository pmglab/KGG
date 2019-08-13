/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.simu;

import cern.jet.stat.Probability;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mxli
 */
public class MultiLocusLiabilityThresholdModel {
   
    double overallDiseasePrevalence;
    double overallThreshold;
    double accumulatedVariance = 0.0;
    List<LiabilityDiseaseSNP> liabiSNPs = new ArrayList<LiabilityDiseaseSNP>();
     

    public MultiLocusLiabilityThresholdModel(double overallDiseasePrevalence) throws Exception {
        this.overallDiseasePrevalence = overallDiseasePrevalence;
        
        overallThreshold = Probability.normalInverse(1 - overallDiseasePrevalence);
        accumulatedVariance = 0.0;
    }

    public void addLiabilitySNPLoci(double riskAlleleFreq, boolean riskAlleleLabel, double genotypicRelativeRisk1, double genotypicRelativeRisk2,
            int LDMarkerPosition, double markerAlleleFreq, double rSquare) throws Exception {
        LiabilityDiseaseSNP snp = new LiabilityDiseaseSNP(riskAlleleFreq,
                genotypicRelativeRisk1, genotypicRelativeRisk2, riskAlleleLabel, LDMarkerPosition, markerAlleleFreq, rSquare);
        snp.calculateLiabilities();
        liabiSNPs.add(snp);
        accumulatedVariance += snp.explainedVariance;
        if (accumulatedVariance >= 1) {
            throw new Exception("Error! The overal explianed variance is larger than 1!");
        }
    }

    public double getAccumulatedVariance() {
        return accumulatedVariance;
    }

    public void setAccumulatedVariance(double accumulatedVariance) {
        this.accumulatedVariance = accumulatedVariance;
    }

    public List<LiabilityDiseaseSNP> getLiabiSNPs() {
        return liabiSNPs;
    }

    public void setLiabiSNPs(List<LiabilityDiseaseSNP> liabiSNPs) {
        this.liabiSNPs = liabiSNPs;
    }

    public double getOverallThreshold() {
        return overallThreshold;
    }

    public void setOverallThreshold(double overallThreshold) {
        this.overallThreshold = overallThreshold;
    }

    
}
