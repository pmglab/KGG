/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui.action;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Jiang Li
 */
public class LoadSNPInfoAction implements ChartMouseListener {

    @Override
    public void chartMouseClicked(ChartMouseEvent cme) {
        JFreeChart jfcChart=cme.getChart();
        System.out.println("Test whether the event is caught!");
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent cme) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
