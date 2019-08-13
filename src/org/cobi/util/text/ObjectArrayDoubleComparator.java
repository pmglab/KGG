/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.text;

import java.util.Comparator;

/**
 *
 * @author user
 */
public class ObjectArrayDoubleComparator implements Comparator<Object[]> {

    private int index = 0;

    public ObjectArrayDoubleComparator(int ind) {
        this.index = ind;
    }

    public int compare(Object[] o1, Object[] o2) {
        return Double.compare(Double.parseDouble(o2[index].toString()), Double.parseDouble(o1[index].toString()));
    }
}