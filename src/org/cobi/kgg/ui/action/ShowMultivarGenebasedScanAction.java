/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.GeneBasedScanDialogMultivariate;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = "Gene",
        id = "org.cobi.kgg.ui.action.ShowMultivarGenebasedScanAction"
)
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Bubble.png",
        displayName = "#CTL_ShowMultivarGenebasedScanAction"
)
@ActionReference(path = "Menu/Gene", position = 210)
@Messages("CTL_ShowMultivarGenebasedScanAction=Multivariate Association")
public final class ShowMultivarGenebasedScanAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GlobalManager.multivarGeneBasedScanDialog == null) {
            GlobalManager.multivarGeneBasedScanDialog = new GeneBasedScanDialogMultivariate(GlobalManager.mainFrame, true);
        }
        GlobalManager.multivarGeneBasedScanDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.multivarGeneBasedScanDialog.setVisible(true);
    }
       @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    @Override
    public String getName() {
        return "Multivariate Association";
    }
    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
