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

import java.io.Serializable;

/**
 *
 * @author mxli
 */
public class SparseLDMatrixElement implements Serializable {
private static final long serialVersionUID = 10L;
    public int index1;
    public int index2;
    public float ld;
    public boolean original = true;

    public SparseLDMatrixElement(int index1, int index2, float ld) {
        this.index1 = index1;
        this.index2 = index2;
        this.ld = ld;
    }

}
