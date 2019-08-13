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
public class PPISetPValueComparator implements Comparator<PPISet> {

    public int compare(PPISet arg0, PPISet arg1) {
        return Double.compare(arg0.associcationPValue, arg1.associcationPValue);
    }
}