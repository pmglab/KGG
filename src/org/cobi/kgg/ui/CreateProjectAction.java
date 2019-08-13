/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.cobi.kgg.ui.dialog.CreateProjectDialog;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File",
        id = "org.cobi.kgg.ui.CreateProjectAction")
@ActionRegistration(iconBase = "org/cobi/kgg/ui/png/16x16/Key.png", displayName = "#CTL_CreateProjectAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1300)
})
@Messages("CTL_CreateProjectAction=Create Project")
public final class CreateProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // createLoginDialog();
        //createLoginDialog1();

        //main.setIconImage(ImageUtilities.loadImage("org/cobi/kgg/ui/png/16x16/logo1.png"));
        CreateProjectDialog cpDialog = new CreateProjectDialog(GlobalManager.mainFrame, true);
        cpDialog.setLocationRelativeTo(GlobalManager.mainFrame);
        cpDialog.setVisible(true);


    }

    /*
     private void createLoginDialog1() {
     JButton ok = new JButton();
     ok.setText("OK");
     JButton cancel = new JButton();
     cancel.setText("Cancel");

     cancel.addActionListener(new ActionListener() {

     @Override
     public void actionPerformed(ActionEvent arg0) {
     cpPanel.setVisible(false);
     }
     });

     ok.addActionListener(new ActionListener() {

     @Override
     public void actionPerformed(ActionEvent arg0) {
     //authenicate username and password
     }
     });

     DialogDescriptor dd = new DialogDescriptor(cpPanel, "Create Project");
     dd.setOptions(new Object[]{ok, cancel});
     dd.addPropertyChangeListener(new PropertyChangeListener() {

     @Override
     public void propertyChange(PropertyChangeEvent evt) {
     if (NotifyDescriptor.CLOSED_OPTION.equals(evt.getNewValue())) {
     cpPanel.setVisible(false);
     }
     }
     });
     Dialog createDialog = DialogDisplayer.getDefault().createDialog(dd);
     createDialog.setVisible(true);
         
     }

     private void createLoginDialog() {
     JButton ok = new JButton();
     ok.setText("OK");
     JButton cancel = new JButton();
     cancel.setText("Cancel");

     cancel.addActionListener(new ActionListener() {

     @Override
     public void actionPerformed(ActionEvent arg0) {
     cpPanel.setVisible(false);
     }
     });

     ok.addActionListener(new ActionListener() {

     @Override
     public void actionPerformed(ActionEvent arg0) {
     //authenicate username and password
     }
     });

     NotifyDescriptor nd = new NotifyDescriptor.Confirmation(cpPanel, "Create Project");
     nd.setOptions(new Object[]{ok, cancel});
     DialogDisplayer.getDefault().notifyLater(nd);
     nd.addPropertyChangeListener(new PropertyChangeListener() {

     @Override
     public void propertyChange(PropertyChangeEvent evt) {
     if (NotifyDescriptor.CLOSED_OPTION.equals(evt.getNewValue())) {
     cpPanel.setVisible(false);
     }
     }
     });
     }
     */
}
