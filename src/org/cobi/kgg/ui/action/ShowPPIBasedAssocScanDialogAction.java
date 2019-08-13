/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.PPIBasedScanDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.ui.action.ShowPPIBasedAssocScanDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Networkconnection.png",
        displayName = "#CTL_ShowPPIBasedAssocScanDialogAction")
@ActionReferences({
    @ActionReference(path = "Menu/BioModule", position = 33),
    @ActionReference(path="Toolbars/Edit",position=5)
})
@Messages("CTL_ShowPPIBasedAssocScanDialogAction=Interaction-based Association Scan")
public final class ShowPPIBasedAssocScanDialogAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
              //main.setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        if (GlobalManager.pPIBasedScanDialog == null) {
            GlobalManager.pPIBasedScanDialog = new PPIBasedScanDialog(GlobalManager.mainFrame, true);
        }
        GlobalManager.pPIBasedScanDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.pPIBasedScanDialog.setVisible(true);

    }

    @Override
    public String getName() {
        return "Gene-pair-based Association";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
