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

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Miaoxin Li
 */
public class Individual implements Cloneable, Serializable {
private static final long serialVersionUID = 5L;
    private String familyID;
    private String individualID;
    private String momID;
    private String dadID;
    private int gender;
    private int affectedStatus;
    private int liability; //optional

    public StatusGtySet gtySet;
    private String labelInChip;
    private ArrayList<String> traitValues;

    public Individual() {
        traitValues = new ArrayList<String>();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Individual o = null;
        o = (Individual) super.clone();
        o.traitValues = new ArrayList<String>();
        o.traitValues.addAll(this.traitValues);
        return o;
    }

    /**
     * Get the value of traitValues
     *
     * @return the value of traitValues
     */
    public ArrayList<String> getTraits() {
        return traitValues;
    }

    /**
     * Set the value of traitValues
     *
     * @param traitValues new value of traitValues
     */
    public void setTraits(ArrayList<String> traits) {
        this.traitValues = traits;
    }

    /**
     * Set the value of a trait 
     *
     * @param traitValues new value of trait 
     */
    public void addTrait(String trait) {
        this.traitValues.add(trait);
    }

    /**
     * Get the value of labelInChip
     *
     * @return the value of labelInChip
     */
    public String getLabelInChip() {
        return labelInChip;
    }

    /**
     * Set the value of labelInChip
     *
     * @param labelInChip new value of labelInChip
     */
    public void setLabelInChip(String labelInChip) {
        this.labelInChip = labelInChip;
    }

    public int getAffectedStatus() {
        return affectedStatus;
    }

    public void setAffectedStatus(int affectedStatus) {
        this.affectedStatus = affectedStatus;
    }

    public String getDadID() {
        return dadID;
    }

    public void setDadID(String dadID) {
        this.dadID = dadID;
    }

    public String getFamilyID() {
        return familyID;
    }

    public void setFamilyID(String familyID) {
        this.familyID = familyID;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getIndividualID() {
        return individualID;
    }

    public void setIndividualID(String individualID) {
        this.individualID = individualID;
    }

    public int getLiability() {
        return liability;
    }

    public void setLiability(int liability) {
        this.liability = liability;
    }

    public String getMomID() {
        return momID;
    }

    public void setMomID(String momID) {
        this.momID = momID;
    }
}
