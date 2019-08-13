/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.io.File;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;

/**
 *
 * @author mxli
 */
public class GeneralLeaf{
    //category id 0: String 1: File

    protected int categoryID;
    protected String v1;
    protected File v2;
    protected GeneBasedAssociation gba;
    protected PPIBasedAssociation ppia;
    protected PathwayBasedAssociation pa;
    
    public GeneralLeaf(int categoryID, String v1) {
        this.categoryID = categoryID;
        this.v1 = v1;
    }

    public GeneralLeaf(int categoryID, String v1, File v2) {
        this.categoryID = categoryID;
        this.v1 = v1;
        this.v2 = v2;
    }

    public GeneralLeaf(int categoryID, String v1, GeneBasedAssociation v2) {
        this.categoryID = categoryID;
        this.v1 = v1;
        this.gba = v2;
    }

    public GeneralLeaf(int categoryID, String v1, PPIBasedAssociation v2) {
        this.categoryID = categoryID;
        this.v1 = v1;
        this.ppia = v2;
    }

    public GeneralLeaf(int categoryID, String v1, PathwayBasedAssociation v2) {
        this.categoryID = categoryID;
        this.v1 = v1;
        this.pa = v2;
    }
}
