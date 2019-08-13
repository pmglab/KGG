/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.PowerSimulationFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Power",
        id = "org.cobi.kgg.ui.action.ShowPowerSimulationDialogAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Calculator.png",
        displayName = "#CTL_ShowPowerSimulationDialogAction")
@ActionReference(path = "Menu/Power", position = 33)
@Messages("CTL_ShowPowerSimulationDialogAction=Calculator")
public final class ShowPowerSimulationDialogAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean openDialog = true;
        if (openDialog) {
            if (GlobalManager.psDialog == null) {
                GlobalManager.psDialog = new PowerSimulationFrame(GlobalManager.mainFrame, true);
            }
            //GlobalManager.psDialog.setHasOpenWidnow(true);
            GlobalManager.psDialog.setLocationRelativeTo(GlobalManager.mainFrame);
            GlobalManager.psDialog.setVisible(true);
        } else {
            String infor = "Sorry this function is under development!!";
            JOptionPane.showMessageDialog(GlobalManager.mainFrame, infor);
        }
    }
}
