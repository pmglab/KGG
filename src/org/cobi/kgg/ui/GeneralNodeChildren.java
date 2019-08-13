/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author mxli
 */
public class GeneralNodeChildren extends Children.Keys<GeneralLeaf> {

    List<GeneralLeaf> tmpList;

    public GeneralNodeChildren(List<GeneralLeaf> tmpList) {
        this.tmpList = tmpList;
        // obj1.addChangeListener(WeakListeners.change(this, obj1));
    }

    @Override
    protected void addNotify() {
        setKeys(tmpList);
    }

    @Override
    protected Node[] createNodes(GeneralLeaf key1) {
        //category id 0: String 1: File
        //if no need to catch the object pa and ppia, the two "else if " can be merged into "else". 
        if (key1.categoryID >1) {
            if(key1.gba!=null)   return new Node[]{new StringNode(key1.v1,key1.gba)};
            else if(key1.pa!=null) return new Node[]{new StringNode(key1.v1,key1.pa)};
            else if(key1.ppia!=null) return new Node[]{new StringNode(key1.v1,key1.ppia)};
            else return new Node[]{new StringNode(key1.v1)};
        } else  {
            return new Node[]{new FileNode(key1.categoryID,key1.v2)};
        }
       // return null;
    }
}
