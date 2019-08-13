/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.entity;

import java.io.File;

/**
 *
 * @author mxli
 */
public class FileString extends File {

    public FileString(String pathname) {
        super(pathname);
    }

    public String toString() {
      return getName();
    }
}
