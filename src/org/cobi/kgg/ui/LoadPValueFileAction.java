/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.openide.ErrorManager;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "File",
        id = "org.cobi.kgg.ui.LoadPValueFileAction")
@ActionRegistration(iconBase = "org/cobi/kgg/ui/png/16x16/Database.png",
        displayName = "#CTL_LoadPValueFileAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1600)
})
@Messages("CTL_LoadPValueFileAction=Load P-Value File")
public final class LoadPValueFileAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fDialog = new KGGJFileChooser();
        fDialog.setDialogTitle("Load P Value File");
        fDialog.setMultiSelectionEnabled(true);
        try {
            int result = fDialog.showOpenDialog(GlobalManager.mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                GlobalManager.lastAccessedPath = fDialog.getSelectedFile().getParent();
                File[] files = fDialog.getSelectedFiles();
                if (files != null) {
                    for (File file : files) {
                        GlobalManager.currentProject.addPValueFile(file);
                        GlobalManager.originalAssociationFilesModel.addElement(new FileTextNode(file.getCanonicalPath()));
                    }
                    ProjectTopComponent projTopComp = (ProjectTopComponent) WindowManager.getDefault().findTopComponent("ProjectTopComponent");
                    projTopComp.showProject(GlobalManager.currentProject);
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}
