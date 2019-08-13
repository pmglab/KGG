/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.GeneBasedScanDialogUnivariate;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.ui.action.ShowGeneBasedAssocScanDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Pinion.png",
        displayName = "#CTL_ShowGeneBasedAssocScanDialogAction")
@ActionReferences({
    @ActionReference(path = "Menu/Gene", position = 200),
    @ActionReference(path="Toolbars/Edit",position=4)
})
@Messages("CTL_ShowGeneBasedAssocScanDialogAction=Univariate Association")
public final class ShowGeneBasedAssocScanDialogAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //main.setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        if (GlobalManager.univarGeneBasedScanDialog == null) {
            GlobalManager.univarGeneBasedScanDialog = new GeneBasedScanDialogUnivariate(GlobalManager.mainFrame, true);
        }
        GlobalManager.univarGeneBasedScanDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.univarGeneBasedScanDialog.setVisible(true);
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public String getName() {
        return "Univariate Association";
    }
    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
