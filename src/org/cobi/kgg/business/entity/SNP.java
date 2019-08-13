// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.business.entity;

import cern.colt.list.DoubleArrayList;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mxli
 */
public class SNP implements Serializable {

    private static final long serialVersionUID = 9L;
    /**
     * @pdOid 80554c4c-116c-4275-a969-84fd0c6fab91
     */
    public String rsID;
    public String orgRsID;
    public List<String> correlatedRSID;
    /**
     * @pdOid cae25e17-8554-4a04-a688-779bc00c1142
     */
    private double[] pValues;
    /**
     * @pdOid 59ad0fd2-d475-4191-a89a-c853b412bf0d
     */
    public int physicalPosition;
    public int genotypeOrder = -1;
    public boolean hasAdjustedP = false;
    private double riskScore = 1;
   
   

    /**
     * @pdOid b7298ecc-0e7e-4da0-a35d-69f35d12320d
     */
    public int geneFeatureID = 15; //by default
    public String geneFeatureStr = null;
    /**
     * @pdOid f2f722dd-ca95-4cf0-a4bb-ff110f755075
     */
    private final DoubleArrayList featureValues = new DoubleArrayList(0);
    private boolean miRNABind = false;

    /**
     *
     * @param pValue
     * @param physicalPosition
     */
    String strChr = "";

    public SNP(double[] pValue, int physicalPosition) {
        this.pValues = pValue;
        this.physicalPosition = physicalPosition;
    }

    public SNP(int physicalPosition) {
        this.physicalPosition = physicalPosition;
    }

    /**
     *
     * @param pValues
     */
    public SNP(double[] pValues) {
        this.pValues = pValues;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public List<String> getCorrelatedRSID() {
        return correlatedRSID;
    }

    public void setCorrelatedRSID(List<String> correlatedRSID) {
        this.correlatedRSID = correlatedRSID;
    }

    /**
     *
     */
    public SNP() {
    }

    public SNP(String rsID) {
        this.rsID = rsID;
    }

    /**
     *
     * @return
     */
    public int getGeneFeature() {
        return geneFeatureID;
    }

    /**
     *
     * @param geneFeatureID
     */
    public void setGeneFeature(int geneFeature) {
        this.geneFeatureID = geneFeature;
    }

    /**
     *
     * @return
     */
    public boolean isMiRNABind() {
        return miRNABind;
    }

    /**
     *
     * @param miRNABind
     */
    public void setMiRNABind(boolean miRNABind) {
        this.miRNABind = miRNABind;
    }

    /**
     *
     * @return
     */
    public double[] getpValues() {
        return pValues;
    }

    /**
     *
     * @param pValues
     */
    public void setpValues(double[] pValues) {
        this.pValues = pValues;
    }

    /**
     *
     * @return
     */
    public int getPhysicalPosition() {
        return physicalPosition;
    }

    /**
     *
     * @param physicalPosition
     */
    public void setPhysicalPosition(int physicalPosition) {
        this.physicalPosition = physicalPosition;
    }

    /**
     *
     * @return
     */
    public String getRsID() {
        return rsID;
    }

    /**
     *
     * @param rsID
     */
    public void setRsID(String rsID) {
        this.rsID = rsID;
    }

    public void addFeatureValue(double a) {
        featureValues.add(a);
    }

    public double getAFeatureValue(int index) {
        return featureValues.getQuick(index);
    }

    public DoubleArrayList getAFeatureValue() {
        return featureValues;
    }

    public void setGenotypeOrder(int genotypeOrder) {
        this.genotypeOrder = genotypeOrder;
    }

    public int getGenotypeOrder() {
        return genotypeOrder;
    }

    public void setStrChr(String strChr) {
        this.strChr = strChr;
    }

}
