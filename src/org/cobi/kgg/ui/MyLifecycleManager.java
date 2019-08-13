/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.util.Collection;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * http://netbeans.dzone.com/nb-custom-lifecyclemanager
 *
 * @author mxli
 */
@ServiceProvider(service = LifecycleManager.class, position = 1)
public class MyLifecycleManager extends LifecycleManager {

    @Override
    public void saveAll() {
    }

    @Override
    public void exit() {
        String mssg = "Going to exit the KGG system?";
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(mssg, "Exit Confirmation", NotifyDescriptor.YES_NO_CANCEL_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        try {
            if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                GlobalManager.writeKGGSettings();
                if (GlobalManager.currentProject != null) {
                    GlobalManager.currentProject.writeProjectVariables();
                }
                /*
                 Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
                 Iterator<TopComponent> it = tcs.iterator();
                 while (it.hasNext()) {
                 TopComponent tc = it.next();
                 tc.close();
                 }
                 */
                Lookup.Template<LifecycleManager> template = new Lookup.Template<LifecycleManager>(LifecycleManager.class);
                Collection<? extends LifecycleManager> c = Lookup.getDefault().lookup(template).allInstances();

                for (LifecycleManager lm : c) {
                    if (lm != this) {
                        lm.exit();
                    }
                }

                System.out.println("KGG Exit Successfully!");
            } else if (nd.getValue().equals(NotifyDescriptor.CANCEL_OPTION)) {
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }

    }
}
