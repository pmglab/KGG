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

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;

/**
 *
 * @author MX Li
 */
public abstract class LDSparseMatrix {

    public abstract float getLDAt(int index1, int index2) throws Exception;

    public abstract DoubleMatrix2D subDenseLDMatrix(IntArrayList indexes) throws Exception;


    public abstract void releaseLDData();
}
