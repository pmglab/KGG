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
public class PathwayRankComparator implements Comparator<Pathway> {

    @Override
    public int compare(final Pathway arg0, final Pathway arg1) {
        return arg0.getRank() - arg1.getRank();
    }
}
