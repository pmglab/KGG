/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.simu;

/**
 *
 * @author Jiang Li
 */
public class UpdateUI {
    public int intIndex=0;
    public String[] strItems=null;
    public double dblProgress=0;
    public int intTableItemIndex=0;

    public UpdateUI(int intTableIndex, String[] strItems,int intTableItemIndex) {
        this.intIndex=intTableIndex;
        this.strItems=strItems;
        this.intTableItemIndex=intTableItemIndex;
    }

    public UpdateUI(int intTableIndex, double dblProgress) {
        this.intIndex=intTableIndex;
        this.dblProgress=dblProgress;
    }

    UpdateUI(int intTableIndex, String[] strItems) {
        this.intIndex=intTableIndex;
        this.strItems=strItems;
    }
    
    
    
}
