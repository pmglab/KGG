/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.plot;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;

/**
 *
 * @author Jiang Li
 */
public class ColorLegendItemSource implements LegendItemSource {

    @Override
    public LegendItemCollection getLegendItems() {
        String[] strDisplays={"[0.0-0.2)","[0.2-0.4)","[0.4-0.6)","[0.6-0.8)","[0.8-1.0)","1.0","ref"};
        Color[] colors = new Color[]{new Color(0,255,255), new Color(0, 255, 0), new Color(255,255,0), new Color(255, 128, 0), new Color(255, 0, 0), new Color(160,32,240),new Color(0,0,0)};
        //Paint paint = new Color(0,0,255);
        Shape shape =  new Ellipse2D.Double(-4, -4, 8, 8);
        LegendItemCollection itemCollection = new LegendItemCollection();      
        for(int i=0;i<=6;i++){
            LegendItem item =new LegendItem(strDisplays[i],"","","",shape,colors[i]);
            itemCollection.add(item);
        }
       return itemCollection; 
    }   
}
