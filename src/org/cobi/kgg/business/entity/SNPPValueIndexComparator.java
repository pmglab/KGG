/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.entity;

import java.util.Comparator;

/**
 *
 * @author mxli
 */
public class SNPPValueIndexComparator implements Comparator<SNP> {

    int indexID = 0;

    public SNPPValueIndexComparator(int index) {
        indexID = index;
    }

    public int compare(SNP arg0, SNP arg1) {
        double[] p1s = arg0.getpValues();
        double[] p2s = arg1.getpValues();
        if (p1s == null || p2s == null) {
            return 0;
        }
        return Double.compare(p1s[indexID], p2s[indexID]);
    }
}
