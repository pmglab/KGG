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
public class PathwayEnrichedPValueComparator implements Comparator<Pathway> {

    @Override
    public int compare(final Pathway arg0, final Pathway arg1) { 
        return Double.compare(arg0.getEnrichedPValue(), arg1.getEnrichedPValue());
    }
}
