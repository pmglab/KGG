/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.dialog.DriverTissueTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
    category = "Gene",
    id = "org.cobi.kgg.ui.action.DriverTissueExplore"
)
@ActionRegistration(
    iconBase = "org/cobi/kgg/ui/png/16x16/Taxi.png",
    displayName = "#CTL_DriverTissueExplore"
)
@ActionReference(path = "Menu/Gene", position = 700, separatorBefore = 650)
@Messages("CTL_DriverTissueExplore=Driver Tissue (DESE)")
public final class DriverTissueExplore implements ActionListener {

  DriverTissueTopComponent driverTissueTopComponent;

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO implement action body
    driverTissueTopComponent = (DriverTissueTopComponent) WindowManager.getDefault().findTopComponent("DriverTissueTopComponent");
    driverTissueTopComponent.open();   
    driverTissueTopComponent.requestActive();
  }
}
