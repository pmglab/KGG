/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

/**
 *
 * @author mxli
 */
public class PValueFileSetting {

    private int chromIndexInFile;
    private int positionIndexInFile;
    private int markerIndexInFile;
    private int imputInfoIndexInFile = -1;
    private int[] sNPPValueIndexes;
    private int testNameIndex;
    private String missingLabel;
    private String posGenomeVersion;
    //Single test per column
//Multiple tests per column
    private int pvalueColType;
    //p-values
    //z-scores
    //chi-square
    private String testInputType;
    //this is for chi-square
    private int chiSquareDf = 1;
    private int testNameIndexInFile;
    boolean hasTitleRow = true;
    boolean isImputInfoLarger = true;
    float imputInfoCutoff = 0.8f;

    public PValueFileSetting(int chromIndexInFile, int positionIndexInFile, int[] sNPPValueIndexes) {
        this.chromIndexInFile = chromIndexInFile;
        this.positionIndexInFile = positionIndexInFile;
        this.sNPPValueIndexes = sNPPValueIndexes;
    }

    public PValueFileSetting(int chromIndexInFile, int positionIndexInFile, int markerIndexInFile, int imputInfoIndexInFile, boolean isImputInfoLarger, float imputInfoCutoff, int[] sNPPValueIndexes) {
        this.chromIndexInFile = chromIndexInFile;
        this.positionIndexInFile = positionIndexInFile;
        this.markerIndexInFile = markerIndexInFile;
        this.sNPPValueIndexes = sNPPValueIndexes;
        this.imputInfoIndexInFile = imputInfoIndexInFile;
        this.isImputInfoLarger = isImputInfoLarger;
        this.imputInfoCutoff = imputInfoCutoff;
    }

    public int getImputInfoIndexInFile() {
        return imputInfoIndexInFile;
    }

    public boolean isIsImputInfoLarger() {
        return isImputInfoLarger;
    }

    public float getImputInfoCutoff() {
        return imputInfoCutoff;
    }
 
    public int getPvalueColType() {
        return pvalueColType;
    }

    public String getPosGenomeVersion() {
        return posGenomeVersion;
    }

    public void setPosGenomeVersion(String posGenomeVersion) {
        this.posGenomeVersion = posGenomeVersion;
    }

    public void setPvalueColType(int pvalueColType) {
        this.pvalueColType = pvalueColType;
    }

    public boolean isHasTitleRow() {
        return hasTitleRow;
    }

    public void setHasTitleRow(boolean hasTitleRow) {
        this.hasTitleRow = hasTitleRow;
    }

    public int getChiSquareDf() {
        return chiSquareDf;
    }

    public void setChiSquareDf(int chiSquareDf) {
        this.chiSquareDf = chiSquareDf;
    }

    public int getChromIndexInFile() {
        return chromIndexInFile;
    }

    public void setChromIndexInFile(int chromIndexInFile) {
        this.chromIndexInFile = chromIndexInFile;
    }

    public int getMarkerIndexInFile() {
        return markerIndexInFile;
    }

    public void setMarkerIndexInFile(int markerIndexInFile) {
        this.markerIndexInFile = markerIndexInFile;
    }

    public int getPositionIndexInFile() {
        return positionIndexInFile;
    }

    public void setPositionIndexInFile(int positionIndexInFile) {
        this.positionIndexInFile = positionIndexInFile;
    }

    public int[] getsNPPValueIndexes() {
        return sNPPValueIndexes;
    }

    public String getTestInputType() {
        return testInputType;
    }

    public void setTestInputType(String testInputType) {
        this.testInputType = testInputType;
    }

    public void setsNPPValueIndexes(int[] sNPPValueIndexes) {
        this.sNPPValueIndexes = sNPPValueIndexes;
    }

    public String getMissingLabel() {
        return missingLabel;
    }

    public void setMissingLabel(String missingLabel) {
        this.missingLabel = missingLabel;
    }

    public int getTestNameIndexInFile() {
        return testNameIndexInFile;
    }

    public void setTestNameIndexInFile(int testNameIndexInFile) {
        this.testNameIndexInFile = testNameIndexInFile;
    }

    public int getTestNameIndex() {
        return testNameIndex;
    }

    public void setTestNameIndex(int testNameIndex) {
        this.testNameIndex = testNameIndex;
    }
}
