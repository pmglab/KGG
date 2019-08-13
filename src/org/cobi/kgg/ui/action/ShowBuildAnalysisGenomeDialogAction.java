/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.BuildAnalysisGenomeByPositionDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.ui.action.ShowBuildAnalysisGenomeDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Earth.png",
        displayName = "#CTL_ShowBuildAnalysisGenomeDialogAction")
@ActionReferences({
    @ActionReference(path = "Menu/Data", position = 4000, separatorBefore = 3183),
    @ActionReference(path="Toolbars/Edit",position=3)
})
@Messages("CTL_ShowBuildAnalysisGenomeDialogAction=Build Analysis Genome")
public final class ShowBuildAnalysisGenomeDialogAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
         //main.setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        if (GlobalManager.buildAnalysisGenomeByPositionDialog == null) {
            GlobalManager.buildAnalysisGenomeByPositionDialog = new BuildAnalysisGenomeByPositionDialog(GlobalManager.mainFrame, true);
        }
        GlobalManager.buildAnalysisGenomeByPositionDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.buildAnalysisGenomeByPositionDialog.setVisible(true);
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Build Analysis Genome";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
