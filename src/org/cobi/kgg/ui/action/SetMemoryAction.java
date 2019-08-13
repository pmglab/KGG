/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.SetMemoryDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "org.cobi.kgg.ui.action.SetMemoryAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Repair.png",
        displayName = "#CTL_SetMemoryAction")
@ActionReference(path = "Menu/Tools", position = 3333)
@Messages("CTL_SetMemoryAction=Set System Memory")
public final class SetMemoryAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GlobalManager.smDialog == null) {
            GlobalManager.smDialog = new SetMemoryDialog(GlobalManager.mainFrame, true);
        }
        GlobalManager.smDialog.setHasOpenWidnow(true);
        GlobalManager.smDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.smDialog.setVisible(true);
    }
}
