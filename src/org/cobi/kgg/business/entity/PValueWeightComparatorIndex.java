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
public class PValueWeightComparatorIndex implements Comparator<PValueWeight> {

    @Override
    public int compare(final PValueWeight arg0, final PValueWeight arg1) { 
        return Double.compare(arg0.physicalPos, arg1.physicalPos);
    }
}