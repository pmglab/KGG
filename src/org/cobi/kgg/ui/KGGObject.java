/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.util.Date;

/**
 *
 * @author mxli
 */
public final class KGGObject {

    private final Date createdDate = new Date();
    private final String name;
    private final String type;

    public KGGObject(String type, String name) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + " - " + name;
    }
}
