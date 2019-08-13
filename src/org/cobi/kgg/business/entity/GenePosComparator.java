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
public class GenePosComparator implements Comparator<Gene> {

    @Override
    public int compare(Gene arg0, Gene arg1) {
        return arg0.start - arg1.start;
    }
}