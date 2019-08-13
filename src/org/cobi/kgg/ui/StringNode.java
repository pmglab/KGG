/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mxli
 */
public class StringNode extends AbstractNode {

    String namevalue;
    Object obj;

    public StringNode(String namevalue, Object obj) {
        super(Children.LEAF, Lookups.singleton(obj));
        this.namevalue = namevalue;
    }

    public StringNode(String namevalue) {
        super(Children.LEAF, Lookups.singleton(namevalue));
        this.namevalue = namevalue;
    }

    @Override
    public String getHtmlDisplayName() {
        String[] items = namevalue.split("@%@");
        if (items.length > 1) {
            return "<font color='#0000FF'> <strong>" + items[0] + " : " + "</strong>" + items[1] + "</font>";
        } else {
             return "<font color='#0000FF'> <strong>" +items[0] + " : " + "</strong> null</font>";
        }
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/Green pin.png");
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}
