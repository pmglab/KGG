/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

/**
 *
 * @author mxli
 */
public class RelativeRiskConverter {

    //http://onlinelibrary.wiley.com.eproxy2.lib.hku.hk/doi/10.1002/gepi.20579/full
    public static void oddsRation2RelativeRisk(double refAlleleFreq, double orHet, double orHom, double prev) {
        double minDiff = 1E-06;
        double inc = minDiff;
        double refRisk1 = 0.001;
        double hetRRisk = 1;
        double homRRisk = 1;
        double refRisk2 = 1;
        do {
            hetRRisk = orHet / (1 + refRisk1 * (orHet - 1));
            homRRisk = orHom / (1 + refRisk1 * (orHom - 1));
            refRisk2 = prev / ((1 - refAlleleFreq) * (1 - refAlleleFreq) + 2 * refAlleleFreq * (1 - refAlleleFreq) * hetRRisk + refAlleleFreq * refAlleleFreq * homRRisk);
            if (Math.abs(refRisk1 - refRisk2) < minDiff) {
                break;
            }
            if (refRisk2 < refRisk1) {
                refRisk1 -= inc;
            } else {
                refRisk1 += inc;
            }
            
        } while (true);
        System.out.println("The absolut risk of reference homozygous: " + refRisk2 + "\nRelative riks of Heterozygous: " + hetRRisk + " and Homozygous: " + homRRisk);
        System.out.println(2 * refAlleleFreq * (1 - refAlleleFreq) * (1-hetRRisk) + refAlleleFreq * refAlleleFreq * (1-homRRisk));
    }
}
