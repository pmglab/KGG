/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.DefineCandidateGeneDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = "Edit",
        id = "org.cobi.kgg.ui.action.DefineSeedGeneDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Bee.png",
        displayName = "#CTL_DefineSeedGeneDialogAction")
@ActionReferences({
    @ActionReference(path = "Menu/Data", position = 2500),
    @ActionReference(path="Toolbars/Edit",position=2)
})

@Messages("CTL_DefineSeedGeneDialogAction=Define Seed Gene")
public final class DefineSeedGeneDialogAction extends CallableSystemAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GlobalManager.defineCandidateGeneDialog == null) {
            GlobalManager.defineCandidateGeneDialog = new DefineCandidateGeneDialog(GlobalManager.mainFrame, true);
        }
        GlobalManager.defineCandidateGeneDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.defineCandidateGeneDialog.setVisible(true);
    }
    
    
     @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Define Seed Genes(optional)";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
