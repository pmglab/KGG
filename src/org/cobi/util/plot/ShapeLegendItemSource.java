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
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author Jiang Li
 */
public class ShapeLegendItemSource implements LegendItemSource{

    @Override
    public LegendItemCollection getLegendItems() {
        
        Shape[] shapes = new Shape[]{new Rectangle2D.Double(-4, -4, 8, 8),new Ellipse2D.Double(-4, -4, 8, 8),ShapeUtilities.createDiamond(5),ShapeUtilities.createUpTriangle(5),ShapeUtilities.createDownTriangle(5)};
        String[] strDisplays={"Exon","Intron-UTR-UpDownStream","ncRNA","InterGene","Others"};  
        Paint paint = new Color(0,0,255);
        LegendItemCollection itemCollection = new LegendItemCollection();
        for(int i=0;i<5;i++){
            LegendItem item =new LegendItem(strDisplays[i],"","","",shapes[i],paint);
            itemCollection.add(item);
        }
       return itemCollection; 
    }
}
    
