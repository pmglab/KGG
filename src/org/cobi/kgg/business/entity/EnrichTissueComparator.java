/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.util.Comparator;

/**
 *
 * @author limx54
 */
public class EnrichTissueComparator implements Comparator<EnrichTissue> {

    @Override
    public int compare(EnrichTissue arg1, EnrichTissue arg0) {
        if (arg1.p == arg0.p) {
            return Double.compare(arg0.sigGeneNum, arg1.sigGeneNum);
        } else {
            return Double.compare(arg1.p, arg0.p);
        }
    }
}
