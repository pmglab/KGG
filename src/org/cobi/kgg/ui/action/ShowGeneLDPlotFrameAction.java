/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.GlobalManager;
import org.cobi.kgg.ui.dialog.GeneLDPlotFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "org.cobi.kgg.ui.action.ShowGeneLDPlotFrameAction")
@ActionRegistration(
        iconBase = "org/cobi/kgg/ui/png/16x16/Anchor.png",
        displayName = "#CTL_ShowGeneLDPlotFrameAction")
@ActionReference(path = "Menu/Tools", position = 13333)
@Messages("CTL_ShowGeneLDPlotFrameAction=View LD Plot")
public final class ShowGeneLDPlotFrameAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GlobalManager.gldpDialog == null) {
            GlobalManager.gldpDialog = new GeneLDPlotFrame(GlobalManager.mainFrame, true);
        }
        GlobalManager.gldpDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        GlobalManager.gldpDialog.setVisible(true);
    }
}
