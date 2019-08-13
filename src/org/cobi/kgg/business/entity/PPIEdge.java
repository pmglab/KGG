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
public class PPIEdge implements Serializable {

    private static final long serialVersionUID = 6L;
    double pValue; // should be private for good practice
    int id;
    double score;
    boolean isSignificant;

    public PPIEdge(int id, double pValue, double score) {
        this.pValue = pValue;
        this.id = id;
        this.score = score;
    }

    public PPIEdge(int id, double score) {
        this.id = id;
        this.score = score;
    }

    public boolean isIsSignificant() {
        return isSignificant;
    }

    public void setIsSignificant(boolean isSignificant) {
        this.isSignificant = isSignificant;
    }

    public double getpValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() { // Always good for debugging
        return "E" + id;
    }
}
