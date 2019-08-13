/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

/**
 *
 * @author limx54
 */
public class EnrichTissue {

    public String name;
    public int id;
    public int allGeneNum;
    public int sigGeneNum;
    public double p;
    public String geneSymbls;
    public String detailValues;
    

    public EnrichTissue(String name, int id,int allGeneNum, int sigGeneNum, double p) {
        this.name = name;
        this.id=id;
        this.allGeneNum=allGeneNum;
        this.sigGeneNum = sigGeneNum;
        this.p = p;
    }

    public String getDetailValues() {
        return detailValues;
    }

    public void setDetailValues(String detailValues) {
        this.detailValues = detailValues;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSigGeneNum() {
        return sigGeneNum;
    }

    public void setSigGeneNum(int sigGeneNum) {
        this.sigGeneNum = sigGeneNum;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public String getGeneSymbls() {
        return geneSymbls;
    }

    public void setGeneSymbls(String geneSymbls) {
        this.geneSymbls = geneSymbls;
    }
    

}
