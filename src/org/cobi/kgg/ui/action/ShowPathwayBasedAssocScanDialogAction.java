/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.PathwayBasedScanDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.ui.action.ShowPathwayBasedAssocScanDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Help.png",
        displayName = "#CTL_ShowPathwayBasedAssocScanDialogAction")
@ActionReferences({
    @ActionReference(path = "Menu/BioModule", position = 400),
    @ActionReference(path="Toolbars/Edit",position=6)
})
@Messages("CTL_ShowPathwayBasedAssocScanDialogAction=Gene-set-based Association Scan")
public final class ShowPathwayBasedAssocScanDialogAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //main.setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        if (GlobalManager.pathwayBasedScanDialog == null) {
            GlobalManager.pathwayBasedScanDialog = new PathwayBasedScanDialog(GlobalManager.mainFrame, true);
        }
        GlobalManager.pathwayBasedScanDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.pathwayBasedScanDialog.setVisible(true);
    }

    @Override
    public void performAction() {
    }

    @Override
    public String getName() {
        return "Geneset-based Association";
        
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;       
    }
    
}
