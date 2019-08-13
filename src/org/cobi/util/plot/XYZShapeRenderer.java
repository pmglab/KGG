/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author Jiang Li
 */
 class XYZShapeRenderer extends AbstractXYItemRenderer {

//Currently, just provide 6 kinds of shapes and 6 kinds of colors.     
        private Shape[] shapes = new Shape[]{new RoundRectangle2D.Double(-4, -4, 8, 8, 5, 5),new Rectangle2D.Double(-4, -4, 8, 8),new Ellipse2D.Double(-4, -4, 8, 8),ShapeUtilities.createDiamond(5),ShapeUtilities.createUpTriangle(5),ShapeUtilities.createDownTriangle(5)};
        private Color[] colors = new Color[]{new Color(0,255,255), new Color(0, 255, 0), new Color(255,255,0), new Color(255, 128, 0), new Color(255, 0, 0), new Color(160,32,240),new Color(0,0,0)};

        @Override
        public void drawItem(Graphics2D gd, XYItemRendererState xyirs, Rectangle2D rd, PlotRenderingInfo pri, XYPlot xyplot, ValueAxis va, ValueAxis va1, XYDataset xyd, int i, int i1, CrosshairState cs, int i2) {

            Shape hotspot = null;
            EntityCollection entities = null;
            if (pri != null) {
                entities = pri.getOwner().getEntityCollection();
            }

            double x = xyd.getXValue(i, i1);
            double y = xyd.getYValue(i, i1);
            if (Double.isNaN(x) || Double.isNaN(y)) {
                // can't draw anything
                return;
            }

            double transX = va.valueToJava2D(x, rd,
                    xyplot.getDomainAxisEdge());
            double transY = va1.valueToJava2D(y, rd,
                    xyplot.getRangeAxisEdge());

            PlotOrientation orientation = xyplot.getOrientation();
            // boolean useDefaultShape = true;
            Shape shape = null;
            Color color = null;
            if (xyd instanceof XYZDataset) {
                XYZDataset xyz = (XYZDataset) xyd;
                double z = xyz.getZValue(i, i1);
                
                shape = shapes[(int) Math.floor(z / 10 )];
                color = colors[(int) z % 10];

                if (orientation == PlotOrientation.HORIZONTAL) {
                    shape = ShapeUtilities.createTranslatedShape(shape, transY, transX);

                } else if (orientation == PlotOrientation.VERTICAL) {
                    shape = ShapeUtilities.createTranslatedShape(shape, transX, transY);
                }
                hotspot = shape;

                if (shape.intersects(rd)) {
                    gd.setPaint(color);
                    gd.fill(shape);
                    gd.setPaint(getItemOutlinePaint(i, i1));
                    gd.setStroke(getItemOutlineStroke(i, i1));
                    gd.draw(shape);
                }

                // add an entity for the item...
                if (entities != null) {
                    addEntity(entities, hotspot, xyd, i, i1, transX, transY);
                }
            }
        }
    }
