/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.CreateProjectDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.action.CreateProjectDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Euro.png",
        displayName = "#CTL_CreateProjectDialogAction")
@ActionReferences({
    @ActionReference(path = "Menu/Project", position = 1100),
    @ActionReference(path = "Toolbars/Edit", position = 0)
})
@Messages("CTL_CreateProjectDialogAction=Create Project")
public final class CreateProjectDialogAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
       //main.setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        if (GlobalManager.createProjectDialog == null) {
            GlobalManager.createProjectDialog = new CreateProjectDialog(GlobalManager.mainFrame, true);
        }
        GlobalManager.createProjectDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.createProjectDialog.setVisible(true);
    }
}
