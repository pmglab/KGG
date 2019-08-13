/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.util.Comparator;

/**
 *
 * @author MX Li
 */
public class SeqSegmentEndComparator implements Comparator<SeqSegment> {

    @Override
    public int compare(SeqSegment arg0, SeqSegment arg1) {
        return arg0.end - arg1.end;
    }
}
