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
import cern.colt.list.IntArrayList;
import java.io.Serializable;

/**
 *
 * @author mxli
 */
public class PValueGene implements Serializable {

    private static final long serialVersionUID = 7L;
    /**
     * @pdOid 7b547fa2-f62a-4956-8205-feab25119ab4
     */
    private String symbol;
    /**
     * @pdOid c6e4e242-7cf2-4239-ba90-fda89546e178
     */
    public double pValue;
    public IntArrayList keySNPPositions;
    public DoubleArrayList blockPValues;
    public DoubleArrayList keySNPPValues;
    public DoubleArrayList blockWeights;
    public int keyBlockIndex=0;
    public double[] pValues;
    
    /**
     *
     * @param symbol
     */
    public PValueGene(String symbol) {
        this.symbol = symbol;
        keySNPPositions = new IntArrayList(0);
        blockPValues = new DoubleArrayList(0);
        blockWeights = new DoubleArrayList(0);
        keySNPPValues = new DoubleArrayList(0);
    }

    public void setpValues(double[] pValues) {
        this.pValues = pValues;
    }

    
    /**
     *
     * @return
     */
    public double getpValue() {
        return pValue;
    }

    /**
     *
     * @param pValue
     */
    public void setpValue(double pValue) {
        this.pValue = pValue;
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
