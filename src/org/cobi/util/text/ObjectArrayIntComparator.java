/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.text;

import java.util.Comparator;

/**
 *
 * @author mxli
 */
public class ObjectArrayIntComparator implements Comparator<Object[]> {

    private int index = 0;

    public ObjectArrayIntComparator(int ind) {
        this.index = ind;
    }

    public int compare(Object[] o1, Object[] o2) {
        return Integer.compare(Integer.parseInt(o1[index].toString()), Integer.parseInt(o2[index].toString()));
    }
}
