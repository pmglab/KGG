/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mxli
 */
public class PPISet implements Serializable {
   private static final long serialVersionUID = 11L;
    /**
     *
     */
    private List<String> geneSymbs;
    /**
     *
     */
    public double associcationPValue;
    public double heterogeneityPValue = Double.NaN;
    public double heterogeneityI2 = Double.NaN;
    double confidenceScore;

    public PPISet() {
    }

    public List<String> getGeneSymbs() {
        return geneSymbs;
    }

    public void setGeneSymbs(List<String> geneSymbs) {
        this.geneSymbs = geneSymbs;
    }

    public double getHeterogeneityI2() {
        return heterogeneityI2;
    }

    public void setHeterogeneityI2(double heterogeneityI2) {
        this.heterogeneityI2 = heterogeneityI2;
    }

    public PPISet(double associcationPValue) {
        this.associcationPValue = associcationPValue;
    }

    public double getAssocicationPValue() {
        return associcationPValue;
    }

    public double getHeterogeneityPValue() {
        return heterogeneityPValue;
    }

    public void setHeterogeneityPValue(double heterogeneityPValue) {
        this.heterogeneityPValue = heterogeneityPValue;
    }

    public void setAssocicationPValue(double associcationPValue) {
        this.associcationPValue = associcationPValue;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
