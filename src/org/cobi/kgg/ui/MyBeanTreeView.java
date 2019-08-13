/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;

/**
 *
 * @author mxli
 */
public class MyBeanTreeView  extends BeanTreeView {
     public MyBeanTreeView() {
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //Find the ExplorerManager for this explorer view:
                ExplorerManager mgr = ExplorerManager.find(MyBeanTreeView.this);
                //Get the selected node from the ExplorerManager:
                String selectedNode = mgr.getSelectedNodes()[0].getDisplayName();
                //Get the pressed key from the event:
                String pressedKey = KeyEvent.getKeyText(e.getKeyCode());
                //Put a message in the status bar:
                StatusDisplayer.getDefault().setStatusText(selectedNode +
                        " is being pressed by the " + pressedKey + " key!");
            }
        });
    }
}
