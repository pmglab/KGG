/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.plot;

import java.awt.Font;
import java.util.Iterator;
import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.kgg.business.entity.Gene;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.cobi.kgg.business.entity.SNP;
import org.cobi.util.text.Util;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author Jiang Li
 */
public class ShowSNPChart implements Constants {

    private double[][] data = null;
    private DefaultXYZDataset dataset;
    private XYItemRenderer render;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private XYPlot xyPlot;
    private JFreeChart chart;
    public static Gene gene;

    public double[][] getData() {
        return data;
    }

    public JFreeChart getChart() {
        return chart;
    }

    public ShowSNPChart(Gene gene, CorrelationBasedByteLDSparseMatrix ldCorr, Double dblPV) throws Exception {
        ShowSNPChart.gene = gene;
        Iterator<SNP> itrSNP = gene.snps.iterator();
        int intCount = 0;

        data = new double[3][gene.snps.size()];
        double dblMaxY = 0;
        SNP snpMax = null;
        int intMax = 0;
        while (itrSNP.hasNext()) {
            SNP snpTerm = itrSNP.next();
            double dblY = Math.log10(snpTerm.getpValues()[0]) * (-1);
            if (dblY >= dblMaxY) {
                dblMaxY = dblY;
                snpMax = snpTerm;
                intMax = intCount;
            }
            intCount++;
        }
        itrSNP = gene.snps.iterator();
        intCount = 0;
        while (itrSNP.hasNext()) {

            SNP snpTerm = itrSNP.next();
            double dblX = snpTerm.getPhysicalPosition();
            double dblY = -Math.log10(snpTerm.getpValues()[0]);
            double dblShape = snpTerm.getGeneFeature();
            double dblZ1;
            if (dblShape >= 0 && dblShape <= 5) {
                dblZ1 = 0;
            } else if (dblShape >= 6 && dblShape <= 7) {
                dblZ1 = 1;
            } else if (dblShape >= 8 && dblShape <= 10) {
                dblZ1 = 2;
            } else if (dblShape >= 11 && dblShape <= 12) {
                dblZ1 = 3;
            } else if (dblShape == 13) {
                dblZ1 = 4;
            } else {
                dblZ1 = 5;
            }
            double r = 0;
            if (ldCorr != null) {
                r = ldCorr.getLDAt(snpMax.getPhysicalPosition(), snpTerm.getPhysicalPosition());
            }
            double dblZ2;
            if (r >= 0 && r < 0.2) {
                dblZ2 = 0;
            } else if (r >= 0.2 && r < 0.4) {
                dblZ2 = 1;
            } else if (r >= 0.4 && r < 0.6) {
                dblZ2 = 2;
            } else if (r >= 0.6 && r < 0.8) {
                dblZ2 = 3;
            } else if (r >= 0.8 && r < 1.0) {
                dblZ2 = 4;
            } else {
                if (intCount == intMax) {
                    dblZ2 = 6;
                } else {
                    dblZ2 = 5;
                }
            }
            double dblZ = dblZ1 * 10 + dblZ2;
            data[0][intCount] = dblX;
            data[1][intCount] = dblY;
            data[2][intCount] = dblZ;
            intCount++;
        }
        dataset = new DefaultXYZDataset();
        dataset.addSeries(gene.getSymbol(), data);
        render = new XYZShapeRenderer();

        xAxis = new NumberAxis("position" + "(" + gene.getStart() + "-" + gene.getEnd() + ")");
        yAxis = new NumberAxis("-log10(p-value)");
        xAxis.setLowerBound(gene.getStart() - 6000);//The extend distance should be discussed. 
        xAxis.setUpperBound(gene.getEnd() + 6000);//The extend distance should be discussed. 
        yAxis.setUpperBound(dblMaxY * 1.1);
        xyPlot = new XYPlot(dataset, xAxis, yAxis, render);

        LegendTitle ldtColor = new LegendTitle(new ColorLegendItemSource());
        LegendTitle ldtShape = new LegendTitle(new ShapeLegendItemSource());
        BlockContainer container = new BlockContainer(new BorderArrangement());
        container.add(ldtColor, RectangleEdge.LEFT);
        container.add(ldtShape, RectangleEdge.RIGHT);
        container.add(new EmptyBlock(200, 0));
        CompositeTitle ctlLegend = new CompositeTitle(container);
        ctlLegend.setPosition(RectangleEdge.TOP);

        chart = new JFreeChart("Gene: " + gene.getSymbol() + "(" + Util.formatPValue(dblPV == null ? 1 : dblPV) + ")", new Font("Tahoma", 0, 18), xyPlot, false);
        chart.addSubtitle(ctlLegend);
    }
}
