/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.cobi.kgg.ui.FileTextNode;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.KGGJFileChooser;
import org.cobi.kgg.ui.dialog.ProjectTopComponent;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.ui.action.LoadPValueAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Newdocument.png",
        displayName = "#CTL_LoadPValueAction")
@ActionReferences({
        @ActionReference(path = "Menu/Data", position = 1900),
        @ActionReference(path = "Toolbars/Edit", position = 1)
})
@Messages("CTL_LoadPValueAction=Load PValue File")
public final class LoadPValueAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fDialog = new KGGJFileChooser();
        fDialog.setDialogTitle("Load P Value File");
        fDialog.setMultiSelectionEnabled(true);
        int result = fDialog.showOpenDialog(GlobalManager.mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
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
                CallableSystemAction.get(ShowBuildAnalysisGenomeDialogAction.class).setEnabled(true);
                CallableSystemAction.get(DefineSeedGeneDialogAction.class).setEnabled(true);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Load P Value File";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
