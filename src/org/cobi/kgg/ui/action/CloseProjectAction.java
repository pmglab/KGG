/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "org.cobi.kgg.ui.action.CloseProjectAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Cancel.png",
        displayName = "#CTL_CloseProjectAction")
@ActionReference(path = "Menu/Project", position = 2000, separatorBefore = 1700)
@Messages("CTL_CloseProjectAction=Close Project")
public final class CloseProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (GlobalManager.currentProject != null) {
                GlobalManager.currentProject.writeProjectVariables();
            }
            GlobalManager.currentProject = null;
            GlobalManager.originalAssociationFilesModel.removeAllElements();
            GlobalManager.candiGeneFilesModel.removeAllElements();
            GlobalManager.genomeSetModel.removeAllElements();
            GlobalManager.geneAssocSetModel.removeAllElements();
            GlobalManager.ppiAssocSetModel.removeAllElements();
            GlobalManager.pathwayAssocSetModel.removeAllElements();
            ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
            projTopComp.showProject(GlobalManager.currentProject);

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(CloseProjectAction.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }
}
