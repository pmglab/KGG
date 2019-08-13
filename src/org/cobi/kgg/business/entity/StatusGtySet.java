// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.business.entity;

import cern.colt.bitvector.BitVector;
import java.io.Serializable;

/**
 *
 * @author Miaoxin Li
 */
public class StatusGtySet implements Cloneable, Serializable {

    private static final long serialVersionUID = 14L;
    public BitVector paternalChrom;
    public BitVector maternalChrom;
    //0 indicating missing
    public BitVector existence;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        StatusGtySet o = (StatusGtySet) super.clone();
        return o;
    }
}
