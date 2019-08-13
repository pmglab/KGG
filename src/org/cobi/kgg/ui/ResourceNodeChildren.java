/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author mxli
 */
public class ResourceNodeChildren extends Children.Keys<GeneralNodeEntity> implements ChangeListener {

    private List<GeneralNodeEntity> tmpList = new ArrayList<GeneralNodeEntity>();

    public ResourceNodeChildren(List<GeneralNodeEntity> tmpList) {
        this.tmpList = tmpList;
        // obj1.addChangeListener(WeakListeners.change(this, obj1));
    }

    @Override
    protected void addNotify() {
        setKeys(tmpList);
    }

    @Override
    protected Node[] createNodes(GeneralNodeEntity key) {
        List<File> genomeList = (List<File>) key.value;
        if (genomeList.isEmpty()) {
            return null;
        }
        List<GeneralLeaf> leafs = new ArrayList<GeneralLeaf>();
        for (File genme : genomeList) {
            leafs.add(new GeneralLeaf(1, null, genme));
        }
        return new Node[]{new GeneralNode(key.getTypeName(), "", leafs)};

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        setKeys(tmpList);
    }
}
