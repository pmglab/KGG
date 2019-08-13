/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author mxli
 */
public class KGGJFileChooser extends JFileChooser {

    public KGGJFileChooser() {
        if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
            this.setCurrentDirectory(new File(GlobalManager.lastAccessedPath));
        }
    }

    public KGGJFileChooser(String extend, String descrp) {
        if ((GlobalManager.lastAccessedPath != null) && (GlobalManager.lastAccessedPath.trim().length() > 0)) {
            this.setCurrentDirectory(new File(GlobalManager.lastAccessedPath));
        }
        FileFilter filter = new MyFileFilter(extend, descrp);
        this.setFileFilter(filter);
    }
}