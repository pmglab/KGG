// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.ui;

import java.io.File;

/**
 *
 * @author Miaoxin Li
 */
// a class which can change the node name and icon label
public class FileTextNode extends File {

    public FileTextNode(String pathname) {
        super(pathname);
    }

    @Override
    public String toString() {
        return super.getName();
    }
}
