/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

/**
 *
 * @author mxli
 */
public class MyTreeNode {
     private String value;
    private String name;
    private String link;

    public MyTreeNode(String name, String value, String link) {
        this.value = value;
        this.name = name;
        this.link = link;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String id) {
        this.value = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return value;
    }
}
