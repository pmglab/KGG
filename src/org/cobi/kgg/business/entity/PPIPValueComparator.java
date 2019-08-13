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
public class PPIPValueComparator implements Comparator<PPI> {

    public int compare(PPI arg0, PPI arg1) {
        return Double.compare(arg0.associcationPValue, arg1.associcationPValue);
    }
}
