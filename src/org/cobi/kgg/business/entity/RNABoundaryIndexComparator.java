/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.util.Comparator;

/**
 *
 * @author MXLi
 */
public class RNABoundaryIndexComparator implements Comparator<RNABoundaryIndex> {

    @Override
    public int compare(RNABoundaryIndex arg0, RNABoundaryIndex arg1) {
        
        return arg0.position - arg1.position;
    }
}