/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.dialog;

import java.util.Collection;
import org.cobi.kgg.business.entity.GeneBasedAssociation;
import org.cobi.kgg.business.entity.PPIBasedAssociation;
import org.cobi.kgg.business.entity.PathwayBasedAssociation;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiang Li
 */
public class TopComponentFactory implements LookupListener {
    
    public static Lookup.Result<GeneBasedAssociation> lprDetectGBA=null;
    public ShowGeneResultTopComponent sgrTC=null;
    public static Lookup.Result<PathwayBasedAssociation> lprDetectPBA=null;
    public PathwayInfoTopComponent piTC=null;
    public static Lookup.Result<PPIBasedAssociation> lprDetectPPI=null;
    public PPIResultViewerTopComponent ppiTC=null;

    public TopComponentFactory() {
        lprDetectGBA=Utilities.actionsGlobalContext().lookupResult(GeneBasedAssociation.class);
        lprDetectGBA.addLookupListener(this);
        lprDetectPBA=Utilities.actionsGlobalContext().lookupResult(PathwayBasedAssociation.class);
        lprDetectPBA.addLookupListener(this);
        lprDetectPPI=Utilities.actionsGlobalContext().lookupResult(PPIBasedAssociation.class);
        lprDetectPPI.addLookupListener(this);
        
    }
   
    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends GeneBasedAssociation> clnGBA = lprDetectGBA.allInstances();
        Collection<? extends PathwayBasedAssociation> clnPBA = lprDetectPBA.allInstances();
        Collection<? extends PPIBasedAssociation> clnPPI = lprDetectPPI.allInstances();
        
        if(!clnGBA.isEmpty()){
            sgrTC=(ShowGeneResultTopComponent)WindowManager.getDefault().findTopComponent("ShowGeneResultTopComponent");
            sgrTC.open();
            sgrTC.resultChanged(le);
        }
        if(!clnPBA.isEmpty()){
            piTC=(PathwayInfoTopComponent)WindowManager.getDefault().findTopComponent("PathwayInfoTopComponent");
            piTC.open();
            piTC.resultChanged(le);
        }
        if(!clnPPI.isEmpty()){
            ppiTC=(PPIResultViewerTopComponent)WindowManager.getDefault().findTopComponent("PPIResultViewerTopComponent");
            ppiTC.open();
            ppiTC.resultChanged(le);
        } 
    }
}
