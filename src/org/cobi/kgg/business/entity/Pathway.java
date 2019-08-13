/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mxli
 */
public class Pathway implements Serializable {

    private static final long serialVersionUID = 12L;
    /**
     *
     */
    private String ID;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private String URL;
    /**
     *
     */
    private Map<String, Float> geneSymbolWeightMap;
    /**
     *
     */
    private double hystPValue = Double.NaN;
    private double enrichedPValue = Double.NaN;
    private double wilcoxonPValue = Double.NaN;
    private int rank = -1;
    private int totalGeneNum;

    public double getHystPValue() {
        return hystPValue;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setHystPValue(double hystPValue) {
        this.hystPValue = hystPValue;
    }

    public double getWilcoxonPValue() {
        return wilcoxonPValue;
    }

    public void setWilcoxonPValue(double wilcoxonPValue) {
        this.wilcoxonPValue = wilcoxonPValue;
    }

    public int getTotalGeneNum() {
        return totalGeneNum;
    }

    public void setTotalGeneNum(int totalGeneNum) {
        this.totalGeneNum = totalGeneNum;
    }

    /**
     * Get the value of enrichedPValue
     *
     * @return the value of enrichedPValue
     */
    public double getEnrichedPValue() {
        return enrichedPValue;
    }

    /**
     * Set the value of enrichedPValue
     *
     * @param enrichedPValue new value of enrichedPValue
     */
    public void setEnrichedPValue(double enrichedPValue) {
        this.enrichedPValue = enrichedPValue;
    }

    public void setGeneSymbolWeightMap(Map<String, Float> geneSymbolWeightMap) {
        this.geneSymbolWeightMap = geneSymbolWeightMap;
    }

    public Map<String, Float> getGeneSymbolWeightMap() {
        return geneSymbolWeightMap;
    }

    public Pathway(String ID, String name, String URL) {
        this.ID = ID;
        this.name = name;
        this.URL = URL;
        geneSymbolWeightMap = new HashMap<String, Float>();
    }

    /**
     *
     * @param geneSyb
     */
    public void addGeneSymbolWeight(String geneSyb, float weight) {
        geneSymbolWeightMap.put(geneSyb, weight);
    }

    /**
     *
     * @return
     */
    public Set<String> getGeneSymbols() {
        return geneSymbolWeightMap.keySet();
    }

    /**
     *
     * @return
     */
    public String getGeneSymbolString() {
        StringBuilder strbuf = new StringBuilder();
        strbuf.append('(');
        for (Map.Entry<String, Float> mGene : geneSymbolWeightMap.entrySet()) {
            strbuf.append(mGene.getKey());
            strbuf.append(", ");
        }
        strbuf.delete(strbuf.length() - 2, strbuf.length());
        strbuf.append(')');
        return strbuf.toString();
    }

    /**
     * Get the value of URL
     *
     * @return the value of URL
     */
    public String getURL() {
        return URL;
    }

    /**
     * Set the value of URL
     *
     * @param URL new value of URL
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of ID
     *
     * @return the value of ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Set the value of ID
     *
     * @param ID new value of ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }
}
