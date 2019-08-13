/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

/**
 *
 * @author mxli
 */
public class PPI {

    /**
     *
     */
    private String geneSymbA;
    /**
     *
     */
    private String geneSymbB;
    /**
     *
     */
    private String database;
    /**
     *
     */
    private String method;
    /**
     *
     */
    private String pubMed;
    public double associcationPValue;
    double confidenceScore;

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public double getAssocicationPValue() {
        return associcationPValue;
    }

    public void setAssocicationPValue(double associcationPValue) {
        this.associcationPValue = associcationPValue;
    }

    public PPI(String geneSymbA, String geneSymbB, double associcationPValue) {
        this.geneSymbA = geneSymbA;
        this.geneSymbB = geneSymbB;
        this.associcationPValue = associcationPValue;
    }

    /**
     * Get the value of pubMed
     *
     * @return the value of pubMed
     */
    public String getPubMed() {
        return pubMed;
    }

    /**
     * Set the value of pubMed
     *
     * @param pubMed new value of pubMed
     */
    public void setPubMed(String pubMed) {
        this.pubMed = pubMed;
    }

    /**
     * Get the value of method
     *
     * @return the value of method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Set the value of method
     *
     * @param method new value of method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Get the value of database
     *
     * @return the value of database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Set the value of database
     *
     * @param database new value of database
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Get the value of geneSymbB
     *
     * @return the value of geneSymbB
     */
    public String getGeneSymbB() {
        return geneSymbB;
    }

    /**
     * Set the value of geneSymbB
     *
     * @param geneSymbB new value of geneSymbB
     */
    public void setGeneSymbB(String geneSymbB) {
        this.geneSymbB = geneSymbB;
    }

    /**
     * Get the value of geneSymbA
     *
     * @return the value of geneSymbA
     */
    public String getGeneSymbA() {
        return geneSymbA;
    }

    /**
     * Set the value of geneSymbA
     *
     * @param geneSymbA new value of geneSymbA
     */
    public void setGeneSymbA(String geneSymbA) {
        this.geneSymbA = geneSymbA;
    }
}
