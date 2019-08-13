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
import java.util.List;

/**
 *
 * @author mxli
 */
public class Gene implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * @pdOid 843ffde5-7135-45d0-b1f3-e692498a8118
     */
    private String symbol;
    /**
     * @pdOid 843f9159-edb9-41dd-ba07-a46af29ecb44
     */
    private int entrezID;
    /**
     * @pdOid b2cf85c5-b3ea-4430-86d2-e0d5b4b62c8c
     */
    private String officialName;
    /**
     * @pdOid 3fd44dc1-d935-43df-8603-eb4aa96d99e3
     */
    private String omimid;
    /**
     * @pdOid ef3cf8ab-4570-4b20-8c6c-5f7dd8eacf76
     */
    //Note the start and end positions are defined by users with their interested extension
    public int start = -1;
    /**
     * @pdOid b0f43497-16f3-44e1-bb93-81845be5755c
     */
    public int end = -1;
    /**
     * @pdRoleInfo migr=no name=Snp assc=association10 mult=0..*
     * type=Aggregation
     */
    public List<SNP> snps;
    //unknown by defualt
    private byte geneGroupID = 5;
 
    public byte getGeneGroupID() {
        return geneGroupID;
    }

    public void setGeneGroupID(byte geneGroupID) {
        this.geneGroupID = geneGroupID;
    }

    @Override
    public String toString() {
        if (symbol != null) {
            return symbol;
        } else {
            return "No Symbol";
        }
    }

    /**
     *
     */
    public Gene() {
        snps = new ArrayList<SNP>();
    }

    /**
     *
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     *
     * @param end
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     *
     * @return
     */
    public int getEntrezID() {
        return entrezID;
    }

    /**
     *
     * @param entrezID
     */
    public void setEntrezID(int entrezID) {
        this.entrezID = entrezID;
    }

    /**
     *
     * @return
     */
    public String getOfficialName() {
        return officialName;
    }

    /**
     *
     * @param officialName
     */
    public void setOfficialName(String officialName) {
        this.officialName = officialName;
    }

    /**
     *
     * @return
     */
    public String getOmimid() {
        return omimid;
    }

    /**
     *
     * @param omimid
     */
    public void setOmimid(String omimid) {
        this.omimid = omimid;
    }

    /**
     *
     * @param s
     */
    public void addSNP(SNP s) {
        snps.add(s);
    }

    /**
     *
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     *
     * @param start
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     *
     * @return
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     *
     * @param symbol
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
