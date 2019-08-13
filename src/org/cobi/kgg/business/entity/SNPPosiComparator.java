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

import java.util.Comparator;

/**
 *
 * @author Miaoxin Li
 */
public class SNPPosiComparator implements Comparator <SNP> {

    @Override
    public int compare(SNP arg0, SNP arg1) {
        return    (arg0.physicalPosition - arg1.physicalPosition);
    }
}
