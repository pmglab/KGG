/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.util.Comparator;

/**
 *
 * @author mxli
 */
public class GenePValueComparator implements Comparator <PValueGene>{

    @Override
    public int compare(PValueGene arg0, PValueGene arg1) {         
        return Double.compare(arg0.pValue, arg1.pValue);
    }
}