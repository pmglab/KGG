/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.Serializable;

/**
 *
 * @author mxli
 */
public class GeneVertex implements Serializable {

    private static final long serialVersionUID = 4L;
    String symbol;
    String entrezID;
    String annotation;
    boolean isSignificant;
    boolean isSeed;

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return this.symbol.equals(((GeneVertex) obj).symbol);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
        return hash;
    }

    public GeneVertex(String symbole) {
        this.symbol = symbole;
    }

    public boolean isIsSeed() {
        return isSeed;
    }

    public void setIsSeed(boolean isSeed) {
        this.isSeed = isSeed;
    }

    public boolean isIsSignificant() {
        return isSignificant;
    }

    public void setIsSignificant(boolean isSignificant) {
        this.isSignificant = isSignificant;
    }

    @Override
    public String toString() { // Always a good idea for debuging
        return symbol; // JUNG2 makes good use of these.
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getEntrezID() {
        return entrezID;
    }

    public void setEntrezID(String entrezID) {
        this.entrezID = entrezID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbole) {
        this.symbol = symbole;
    }
}
