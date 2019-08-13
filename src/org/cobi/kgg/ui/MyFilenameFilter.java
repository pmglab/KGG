/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author mxli
 */
public class MyFilenameFilter implements FilenameFilter {

    String ext;

    public MyFilenameFilter(String ext) {
        this.ext = "." + ext;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(ext);
    }
}
