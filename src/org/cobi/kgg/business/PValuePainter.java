// (c) 2009-2011 Miaoxin Li
// This file is distributed as part of the KGG source code package
// and may not be redistributed in any form, without prior written
// permission from the author. Permission is granted for you to
// modify this file for your own personal use, but modified versions
// must retain this copyright notice and must not be distributed.
// Permission is granted for you to use this file to compile IGG.
// All computer programs have bugs. Use this file at your own risk.
// Tuesday, March 01, 2011
package org.cobi.kgg.business;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.cobi.util.text.Util;
import umontreal.iro.lecuyer.probdist.BetaDist;

/**
 *
 * @author mxli
 */
public class PValuePainter {
    //unit pixel

    private int figureWidth = 650;
    private int figureHeight = 400;
    private int dataPlottingOffsetLeft = 50;
    private int dataPlottingOffsetTop = 10;
    private int dataPlottingOffsetBottom = 40;
    private int dataPlottingOffsetRight = 20;
    //area to present the data point
    private Rectangle dataPlottingArea;
    private Color plotBackgroundColor = Color.WHITE;
    private Color axesColor = Color.DARK_GRAY;
    private Color nonplotBackgroundColor = Color.LIGHT_GRAY;
    private Font titleFont = new Font("SansSerif", Font.BOLD, 16);
    private Font numFont = new Font("SansSerif", Font.BOLD, 12);
    private int titleLineHeight = 30;
    private int yScalDecimal = 2;

    /**
     *
     * @param width
     * @param height
     */
    public PValuePainter(int width, int height) {
        figureWidth = width;
        figureHeight = height;
    }
//fjkdfjdjfkdfjkdjfkldjkldjklfjdkldjljdklfjdlfjdl

    public void drawHistogramPlot(final DoubleArrayList valueList, double[][] range, String title, String outputPath) throws Exception {
        if (valueList == null || valueList.size() == 0) {
            System.err.println("Null p-value list");
            return;
        }
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }

        DoubleArrayList tmpValueList = valueList.copy();
        BufferedImage image = new BufferedImage(this.figureWidth, this.figureHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintPlotArea(g2d);
        yScalDecimal = 3;
        drawAxes(g2d, title, "MAF", "Proportion");
        int dataSize = tmpValueList.size();
        tmpValueList.quickSort();
        int rowNum = range.length;
        int startIndex = 0;
        int endIndex = dataSize - 1;
        DoubleArrayList pvalueCounts = new DoubleArrayList();
        for (int i = 0; i < rowNum; i++) {
            startIndex = tmpValueList.binarySearchFromTo(range[i][0], startIndex, dataSize - 1);
            if (startIndex < 0) {
                startIndex = -startIndex - 1;
            }
            endIndex = tmpValueList.binarySearchFromTo(range[i][1], startIndex, dataSize - 1);
            if (endIndex < 0) {
                endIndex = -endIndex - 1;
            }
            pvalueCounts.add((endIndex - startIndex + 1.0) / dataSize);
            startIndex = endIndex;
        }

        //max and min value in vertical
        double vMax = Descriptive.max(pvalueCounts) + 0.005;
        double vMin = 0;
        double hMin = range[0][0];
        double hMax = range[range.length - 1][1];

        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);

        drawAxesScale(g2d, transformer);
        dataSize = pvalueCounts.size();
        Point point1 = transformer.data2ScreenPoint(hMin, vMin);
        Point point2 = transformer.data2ScreenPoint(hMin, vMin);
        double tmpDouble2;
        double tmpDouble1;
        double xUnit = (hMax - hMin) / dataSize;
        for (int i = 0; i < dataSize; i++) {
            tmpDouble1 = hMin + i * xUnit;
            tmpDouble2 = pvalueCounts.getQuick(i);
            transformer.data2ScreenPoint(point1, tmpDouble1, tmpDouble2);
            tmpDouble1 = hMin + (i + 1) * xUnit;
            transformer.data2ScreenPoint(point2, tmpDouble1, hMin);
            g2d.drawRect(point1.x + 1, point1.y, point2.x - point1.x, point2.y - point1.y);
        }
        g2d.dispose();
        outputPNGFile(image, outputPath);
    }

    /**
     *
     * @param valueList
     * @param title
     * @param outputPath
     * @param pValueTolerationLevle
     * @return
     * @throws Exception
     */
    public int drawQQPlot(final DoubleArrayList valueList, String title, String outputPath, double pValueTolerationLevle) throws Exception {
        if (valueList == null || valueList.size() == 0) {
            System.err.println("Null p-value list");
            return -1;
        }
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }
        DoubleArrayList tmpValueList = valueList.copy();
        BufferedImage image = new BufferedImage(this.figureWidth, this.figureHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintPlotArea(g2d);
        drawAxes(g2d, title, "Expected [-log10(P)]", "Observed [-log10(P)]");

        int dataSize = tmpValueList.size();
        tmpValueList.quickSort();
        //max and min value in vertical
        double vMax = -Math.log10(tmpValueList.getQuick(0));
        // double vMin = -Math.log10(tmpValueList.getQuick(dataSize - 1));
        double vMin = 0;
        double hMin = 0;
        double hMax = -Math.log10((1.0 / (dataSize + 1)));

        //sometimes the p values are too large
        if (vMax > -Math.log10(pValueTolerationLevle)) {
            vMax = -Math.log10(pValueTolerationLevle);
        }

        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);
        drawAxesScale(g2d, transformer);

        //draw the standard line
        Point point1 = transformer.data2ScreenPoint(0, 0);
        Point point2 = transformer.data2ScreenPoint(Math.min(hMax, vMax), Math.min(hMax, vMax));
        Line2D.Double zz = new Line2D.Double(point1, point2);
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(zz);
        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);

        int rectangleSize = 2;
        double maxDiff = 0.05;
        int maxDiffIndex = -1;
        double tmpDouble2;
        double tmpDouble1;
        double triangleLen = 4;
        int dataSizeMore = dataSize + 1;
        for (int i = 0; i < dataSize; i++) {
            tmpDouble1 = -Math.log10((double) (i + 1) / (dataSizeMore));
            tmpDouble2 = -Math.log10(tmpValueList.getQuick(i));
            if (tmpDouble2 <= vMax) {
                transformer.data2ScreenPoint(point1, tmpDouble1, tmpDouble2);
                g2d.drawRect(point1.x - rectangleSize / 2, point1.y - rectangleSize / 2, rectangleSize, rectangleSize);
                //Rectangle2D.Double f = new Rectangle2D.Double(point1.getX() - rectangleSize / 2,  point1.getY() - rectangleSize / 2, rectangleSize, rectangleSize);
                // g2d.draw(f);
            } else {
                tmpDouble2 = vMax;
                transformer.data2ScreenPoint(point1, tmpDouble1, tmpDouble2);
                double x = point1.getX();
                double y = point1.getY();

                g2d.setColor(Color.red);
                plotTriangle(g2d, (int) x, (int) y, (int) triangleLen);
                g2d.setColor(oldColor);
            }


            /*
             if (maxDiffIndex < 0 && (Math.abs(expectedList.getQuick(i) - tmpValueList.getQuick(i)) > maxDiff)) {
             maxDiffIndex = i;
             }
             *
             */
        }

        /*
         float[] dashes = {3.f};
         g2d.setColor(Color.RED);
         g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0));
        
         //draw large difference
         point1 = transformer.data2ScreenPoint(expectedList.getQuick(maxDiffIndex), tmpValueList.getQuick(maxDiffIndex));
         point2 = transformer.data2ScreenPoint(hMax, vMax);
         double vRectangleSize = point1.getY() - point2.getY();
         double hRectangleSize = point2.getX() - point1.getX();
         //zz = new Line2D.Double(point1, point2);
         // g2d.draw(zz);
         Rectangle2D.Double f = new Rectangle2D.Double(point1.getX(),
         point1.getY() - vRectangleSize, hRectangleSize, vRectangleSize);
         g2d.draw(f);
        
         g2d.setStroke(oldStroke);
         g2d.setColor(oldColor);
         */

        g2d.dispose();
        outputPNGFile(image, outputPath);
        return maxDiffIndex;
    }

    /**
     * Do QQ plot by JFreeChart. But it is very very slow for large dataset.
     *
     * @param valueList
     * @param title
     * @param outputPath
     * @param pValueTolerationLevle
     * @return
     * @throws Exception
     */
    /*
     public int drawQQPlotJFreeChart(final DoubleArrayList valueList, String title, String outputPath, double pValueTolerationLevle) throws Exception {
     if (valueList == null || valueList.size() == 0) {
     System.err.println("Null p-value list");
     return -1;
     }
     XYSeriesCollection dataset = new XYSeriesCollection();
     XYSeries series = new XYSeries("");
    
     DoubleArrayList tmpValueList = valueList.copy();
     int dataSize = tmpValueList.size();
     tmpValueList.quickSort();
     //max and min value in vertical
     double vMax = -Math.log10(tmpValueList.getQuick(0));
     double vMin = -Math.log10(tmpValueList.getQuick(dataSize - 1));
     double hMin = 0;
     double hMax = -Math.log10((1.0 / (dataSize + 1)));
     //sometimes the p values are too large
     if (vMax > -Math.log10(pValueTolerationLevle)) {
     vMax = -Math.log10(pValueTolerationLevle);
     }
     double tmpDouble2;
     double tmpDouble1;
     int dataSizeMore = dataSize + 1;
     int dataSizeLess = dataSize - 1;
     for (int i = 0; i < dataSizeLess; i++) {
     tmpDouble1 = -Math.log10((double) (i + 1) / (dataSizeMore));
     tmpDouble2 = -Math.log10(tmpValueList.getQuick(i));
     if (tmpDouble2 <= vMax) {
     series.add(tmpDouble1, tmpDouble2, false);
     } else {
     tmpDouble2 = vMax;
     series.add(tmpDouble1, tmpDouble2, false);
     }
     }
     tmpDouble1 = -Math.log10((double) (dataSizeLess + 1) / (dataSizeMore));
     tmpDouble2 = -Math.log10(tmpValueList.getQuick(dataSizeLess));
     if (tmpDouble2 <= vMax) {
     series.add(tmpDouble1, tmpDouble2);
     } else {
     tmpDouble2 = vMax;
     series.add(tmpDouble1, tmpDouble2);
     }
     dataset.addSeries(series);
     JFreeChart chart = ChartFactory.createXYLineChart(title, "Expected (-log(10)P)", "Observed (-log(10)P)", dataset, PlotOrientation.VERTICAL, false, false, false);
    
     BufferedImage image = chart.createBufferedImage(this.figureWidth, this.figureHeight);
    
    
     Graphics2D g2d = image.createGraphics();
     CoordinateTransformer transformer = new CoordinateTransformer();
     transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
     dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);
    
     //draw the standard line
     Point2D point1 = transformer.data2ScreenPoint(0, 0);
     Point2D point2 = transformer.data2ScreenPoint(Math.max(hMax, vMax), Math.max(hMax, vMax));
     Line2D.Double zz = new Line2D.Double(point1, point2);
     Stroke oldStroke = g2d.getStroke();
     Color oldColor = g2d.getColor();
     g2d.setColor(Color.GREEN);
     g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
     g2d.draw(zz);
     g2d.setStroke(oldStroke);
     g2d.setColor(oldColor);
    
     g2d.dispose();
     //outputJPEGFile(image, outputPath);
     outputPNGFile(image, outputPath);
     return 0;
     }
     */
    /**
     *
     * @param valueLists
     * @param legends
     * @param title
     * @param outputPath
     * @param pValueTolerationLevle
     * @return
     * @throws Exception
     */
    public void drawMultipleQQPlot(final List<DoubleArrayList> valueLists, List<String> legends,
            String title, String outputPath, double pValueTolerationLevle) throws Exception {
        if (valueLists == null || valueLists.isEmpty()) {
            System.err.println("Null p-value list");
            return;
        }

        int listNum = valueLists.size();
        for (int i = listNum - 1; i >= 0; i--) {
            if (valueLists.get(i) == null || valueLists.get(i).size() == 0) {
                System.err.println("Null p-value list");
                valueLists.remove(i);
                legends.remove(i);
            }
        }
        listNum = valueLists.size();
        if (valueLists.isEmpty()) {
            System.err.println("Null p-value list");
            return;
        }

        int dataSize = valueLists.get(0).size();
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }
        //max and min value in vertical
        double vMax = (valueLists.get(0).getQuick(0));
        double vMin = vMax;
        double hMin = 0;
        double hMax = (1.0 / dataSize);
        int maxDataPointSize = dataSize;

        for (int i = 0; i < listNum; i++) {
            DoubleArrayList tmpValueList = valueLists.get(i);
            tmpValueList.quickSort();
            dataSize = tmpValueList.size();
            for (int j = 0; j < dataSize; j++) {
                if (vMax > tmpValueList.getQuick(j)) {
                    vMax = tmpValueList.getQuick(j);
                } else if (vMin < tmpValueList.getQuick(j)) {
                    vMin = tmpValueList.getQuick(j);
                }
            }
            if (hMax > (1.0 / (dataSize + 1))) {
                hMax = (1.0 / (dataSize + 1));
            }
            if (maxDataPointSize < dataSize) {
                maxDataPointSize = dataSize;
            }
        }

        //convert them into the data to be used
        // vMin = -Math.log10(vMin);
        vMin = 0;
        vMax = -Math.log10(vMax);
        hMax = -Math.log10(hMax);

        //sometimes the p values are too large
        if (vMax > -Math.log10(pValueTolerationLevle)) {
            vMax = -Math.log10(pValueTolerationLevle);
        }

        BufferedImage image = new BufferedImage(figureWidth, figureHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        Font oldFront = g2d.getFont();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintPlotArea(g2d);
        drawAxes(g2d, title, "Expected [-log10(P)]", "Observed [-log10(P)]");
        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);
        drawAxesScale(g2d, transformer);
        //draw the standard line
        Point point1 = transformer.data2ScreenPoint(0, 0);
        Point point2 = transformer.data2ScreenPoint(Math.min(hMax, vMax), Math.min(hMax, vMax));

        Line2D.Double zz = new Line2D.Double(point1, point2);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[]{3.f}, 0));
        //g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(zz);

        int rectangleSize = 2;
        int harlfRectangleSize = rectangleSize / 2;
        int triangleLen = 4;
        /*95 CI
         *  k = 1:len
         n = length(data)
         x = qbeta(.5, k, n+1-k )
         f.min = qbeta(.025, k, n+1-k )
         f.max = qbeta(.975, k, n+1-k )
         lines( -log10(x), -log10(f.min),pch=22, lty=2)
         lines( -log10(x), -log10(f.max),pch=22, lty=2)
         * 
         */


        double maxDiff = 0.05;
        int lineNum = legends.size();
        if (lineNum <= 1) {
            lineNum = 2;
        }
        List<Color> colors = pick(lineNum);
        // Color[] colors = {Color.BLUE, Color.RED, Color.BLACK, Color.ORANGE, Color.MAGENTA, Color.YELLOW};

        //Color[] colors = {new Color(00, 00, 0xff), new Color(0xff, 00, 00), new Color(00, 0x88, 0xff),    new Color(0xff, 0x88, 00), new Color(0x88, 00, 0xff), new Color(00, 00, 00)};
        int strHei = g2d.getFontMetrics().getHeight();
        double vRectangleSize = 6;
        double hRectangleSize = 6;
        double tmpDouble1;
        double tmpDouble2;
        g2d.setStroke(oldStroke);


        g2d.setFont(numFont);
        int maxLegentWidth = g2d.getFontMetrics().stringWidth(legends.get(listNum - 1));
        for (int i = listNum - 2; i >= 0; i--) {
            int strLen = g2d.getFontMetrics().stringWidth(legends.get(i));
            if (maxLegentWidth < strLen) {
                maxLegentWidth = strLen;
            }
        }

        /*
         *  k = 1:len
         n = length(data)
         * double x = qbeta(.5, k, n+1-k )
         f.min = qbeta(.025, k, n+1-k )
         f.max = qbeta(.975, k, n+1-k )
         */
        double x, fmin, fmax;
        BetaDist betaDist = null;

        Point minP1 = new Point(), minP2 = new Point();
        Point maxP1 = new Point(), maxP2 = new Point();

        betaDist = new BetaDist(1, maxDataPointSize);
        x = betaDist.inverseF(0.5);
        x = -Math.log10(x);
        fmin = betaDist.inverseF(.025);
        fmin = -Math.log10(fmin);
        transformer.data2ScreenPoint(minP1, x, fmin);

        fmax = betaDist.inverseF(.975);
        fmax = -Math.log10(fmax);
        transformer.data2ScreenPoint(maxP1, x, fmax);

        g2d.setColor(Color.BLACK);
        float[] dashes = {3.f};
        g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0));
        // maxDataPointSize
        for (int k = 2; k <= maxDataPointSize;) {
            betaDist = new BetaDist(k, maxDataPointSize + 1 - k);
            x = betaDist.inverseF(0.5);
            x = -Math.log10(x);
            fmin = betaDist.inverseF(.025);
            fmin = -Math.log10(fmin);

            transformer.data2ScreenPoint(minP2, x, fmin);
            g2d.drawLine(minP1.x, minP1.y, minP2.x, minP2.y);
            minP1.x = minP2.x;
            minP1.y = minP2.y;
            fmax = betaDist.inverseF(.975);
            fmax = -Math.log10(fmax);

            transformer.data2ScreenPoint(maxP2, x, fmax);
            g2d.drawLine(maxP1.x, maxP1.y, maxP2.x, maxP2.y);

            maxP1.x = maxP2.x;
            maxP1.y = maxP2.y;
            if (k <= 2000) {
                k++;
            } else {
                k += k;
            }
        }

        g2d.setStroke(oldStroke);
        for (int i = listNum - 1; i >= 0; i--) {
            DoubleArrayList tmpValueList = valueLists.get(i);
            dataSize = tmpValueList.size();
            g2d.setColor(colors.get(i));
            int dataSizeMore = dataSize + 1;
            for (int j = 0; j < dataSize; j++) {
                tmpDouble1 = -Math.log10((double) (j + 1) / (dataSizeMore));
                tmpDouble2 = -Math.log10(tmpValueList.getQuick(j));
                if (tmpDouble2 < vMin) {
                    tmpDouble2 = vMin;
                }
                if (tmpDouble2 <= vMax) {
                    transformer.data2ScreenPoint(point1, tmpDouble1, tmpDouble2);
                    g2d.drawRect(point1.x - harlfRectangleSize, point1.y - harlfRectangleSize, rectangleSize, rectangleSize);
                    //Rectangle2D.Double f = new Rectangle2D.Double(point1.getX() - rectangleSize / 2,  point1.getY() - rectangleSize / 2, rectangleSize, rectangleSize);
                    // g2d.draw(f);
                } else {
                    tmpDouble2 = vMax;
                    transformer.data2ScreenPoint(point1, tmpDouble1, tmpDouble2);
                    x = point1.getX();
                    double y = point1.getY();
                    plotTriangle(g2d, (int) x, (int) y, triangleLen);
                }
            }
            //plot legends
            // float[] dashes = {3.f};

            //g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0));
            g2d.setStroke(new BasicStroke(1.5f));
            double xOffset = dataPlottingArea.getWidth() / 8;
            double yOffset = dataPlottingArea.getHeight() / 8;
            g2d.drawString(legends.get(i), (float) (dataPlottingArea.x + xOffset), (float) (dataPlottingArea.getY() + yOffset) + strHei * i);
            Rectangle2D.Double f = new Rectangle2D.Double(dataPlottingArea.x + xOffset + maxLegentWidth + hRectangleSize,
                    dataPlottingArea.getY() + yOffset + strHei * i - vRectangleSize, hRectangleSize, vRectangleSize);

            // g2d.fill(f);
            g2d.draw(f);
            g2d.setStroke(oldStroke);
            //tmpValueList.clear();
        }


        g2d.setFont(oldFront);
        g2d.setColor(oldColor);
        g2d.dispose();
        //outputJPEGFile(image, outputPath);
        outputPNGFile(image, outputPath);

    }

    public static void main(String[] args) {
        try {
            BetaDist betaDist = new BetaDist(383700, 1);
            double x = betaDist.cdf(0.5);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Color> pick(int num) {
        List<Color> colors = new ArrayList<Color>();
        if (num < 2) {
            return colors;
        }
        float dx = 1.0f / (float) (num - 1);
        for (int i = 0; i < num; i++) {
            colors.add(get(i * dx));
        }
        return colors;
    }

    public Color get(float x) {
        float r = 0.0f;
        float g = 0.0f;
        float b = 1.0f;
        if (x >= 0.0f && x < 0.2f) {
            x = x / 0.2f;
            r = 0.0f;
            g = x;
            b = 1.0f;
        } else if (x >= 0.2f && x < 0.4f) {
            x = (x - 0.2f) / 0.2f;
            r = 0.0f;
            g = 1.0f;
            b = 1.0f - x;
        } else if (x >= 0.4f && x < 0.6f) {
            x = (x - 0.4f) / 0.2f;
            r = x;
            g = 1.0f;
            b = 0.0f;
        } else if (x >= 0.6f && x < 0.8f) {
            x = (x - 0.6f) / 0.2f;
            r = 1.0f;
            g = 1.0f - x;
            b = 0.0f;
        } else if (x >= 0.8f && x <= 1.0f) {
            x = (x - 0.8f) / 0.2f;
            r = 1.0f;
            g = 0.0f;
            b = x;
        }
        return new Color(r, g, b);
    }

    private void plotTriangle(Graphics2D g, int positionX, int postionY, int triangleLen) {
        Point p1 = new Point(positionX, postionY);
        Point p2 = new Point(positionX - triangleLen / 2, postionY + triangleLen);
        Point p3 = new Point(positionX + triangleLen / 2, postionY + triangleLen);

        Polygon filledPolygon = new Polygon();
        filledPolygon.addPoint(p1.x, p1.y);
        filledPolygon.addPoint(p2.x, p2.y);
        filledPolygon.addPoint(p3.x, p3.y);

        g.fillPolygon(filledPolygon);
    }

    private void calculateDataPlottingArea(boolean drawTitle) throws Exception {
        if (drawTitle) {
            dataPlottingArea = new Rectangle(dataPlottingOffsetLeft, dataPlottingOffsetTop,
                    figureWidth - dataPlottingOffsetRight - dataPlottingOffsetLeft, figureHeight - dataPlottingOffsetBottom - dataPlottingOffsetTop - titleLineHeight);
        } else {
            dataPlottingArea = new Rectangle(dataPlottingOffsetLeft, dataPlottingOffsetTop,
                    figureWidth - dataPlottingOffsetRight - dataPlottingOffsetLeft, figureHeight - dataPlottingOffsetBottom - dataPlottingOffsetTop);
        }

    }

    /**
     *
     * @param image
     * @param outputPath
     * @throws Exception
     */
    public static void outputJPEGFile(BufferedImage image, String outputPath) throws Exception {
        ImageWriter writer = null;
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
        Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "jpg");
        if (iter.hasNext()) {
            writer = iter.next();
        }
        if (writer == null) {
            return;
        }
        IIOImage iioImage = new IIOImage(image, null, null);
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality((float) 1.0);
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(new File(outputPath));
        writer.setOutput(outputStream);
        writer.write(null, iioImage, param);
        outputStream.flush();
        outputStream.close();
        writer.dispose();
    }

    /**
     *
     * @param image
     * @param outputPath
     * @throws Exception
     */
    public static void outputPNGFile(BufferedImage image, String outputPath) throws Exception {
        ImageWriter writer = null;
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
        Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "png");
        if (iter.hasNext()) {
            writer = iter.next();
        }
        if (writer == null) {
            return;
        }
        IIOImage iioImage = new IIOImage(image, null, null);
        ImageWriteParam param = writer.getDefaultWriteParam();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(new File(outputPath));
        writer.setOutput(outputStream);
        writer.write(null, iioImage, param);
        outputStream.flush();
        outputStream.close();
        writer.dispose();
    }

    private void paintPlotArea(Graphics2D g2d) {
        g2d.clipRect(0, 0, figureWidth, figureHeight);
        g2d.setColor(plotBackgroundColor);
        Rectangle2D rc = new Rectangle2D.Double((double) 0, (double) 0, (double) figureWidth, (double) figureHeight);
        g2d.fill(rc);
        GradientPaint pat = new GradientPaint(0f, 0f, Color.WHITE, 100f, 45f, Color.GREEN);
        g2d.setPaint(pat);
        /*
         //		fill background
         g2d.setColor(plotBackgroundColor);
         rc = new Rectangle2D.Double((double) dataPlottingArea.getX(),
         (double) dataPlottingArea.getY(), (double) dataPlottingArea.getWidth(),
         (double) dataPlottingArea.getHeight());
         g2d.fill(rc);
         *
         */
    }

    /**
     *
     * @param g2
     */
    private void drawAxes(Graphics2D g2, String title, String XLabel, String yLabel) {
        g2.setColor(axesColor.darker());
        //vertical axis
        Line2D.Double zz = new Line2D.Double(dataPlottingArea.getX(),
                (dataPlottingArea.getY()), dataPlottingArea.getX(), (dataPlottingArea.getY() + dataPlottingArea.getHeight()));

        g2.draw(zz);
        Font oldFront = g2.getFont();
        g2.setFont(numFont);

        int strLen = g2.getFontMetrics().stringWidth(yLabel);
        int strHei = g2.getFontMetrics().getAscent();
        if (strLen > dataPlottingOffsetLeft) {
            strLen = dataPlottingOffsetLeft;
        }

        g2.rotate(-90.0 * Math.PI / 180.0);
        g2.drawString(yLabel, -(dataPlottingArea.height + strLen) / 2 - dataPlottingArea.y, dataPlottingArea.x - strHei - yScalDecimal * 9);
        g2.rotate(90.0 * Math.PI / 180.0);

        //horizontal axis
        zz = new Line2D.Double(dataPlottingArea.getX(),
                (dataPlottingArea.getY() + dataPlottingArea.getHeight()),
                (dataPlottingArea.getX() + dataPlottingArea.getWidth()),
                (dataPlottingArea.getY() + dataPlottingArea.getHeight()));
        g2.draw(zz);


        strLen = g2.getFontMetrics().stringWidth(XLabel);
        g2.drawString(XLabel, dataPlottingArea.x + dataPlottingArea.width / 2 - strLen / 2,
                (float) (dataPlottingArea.getY() + dataPlottingArea.getHeight() + dataPlottingOffsetBottom * 2 / 3));

        if (title != null && title.length() > 0) {
            g2.setFont(titleFont);
            strLen = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, dataPlottingArea.x + dataPlottingArea.width / 2 - strLen / 2,
                    (float) (dataPlottingArea.getY() + dataPlottingArea.getHeight() + dataPlottingOffsetBottom + titleLineHeight / 2));
        }
        g2.setFont(oldFront);
    }

    private void drawAxesSciScale(Graphics2D g2, CoordinateTransformer transformer) {
        g2.setColor(axesColor.darker());
        String str;
        int scaleLen = 3;
        int verticalScale = 10;
        double vMin = transformer.getDataVerticalMin();
        double vMax = transformer.getDataVerticalMax();
        double hMin = transformer.getDataHorizontalMin();
        double hMax = transformer.getDataHorizontalMax();
        double gridLen = (vMax - vMin) / verticalScale;
        Font oldFront = g2.getFont();
        g2.setFont(numFont);

        int str_hei = g2.getFontMetrics().getAscent();
        for (int i = 1; i <= verticalScale; i++) {
            Point2D point1 = transformer.data2ScreenPoint(hMin, vMin + i * gridLen);
            Point2D point2 = new Point2D.Double(point1.getX() + scaleLen, point1.getY());
            Line2D.Double zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            str = Util.formatPValue(vMin + i * gridLen);

            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.drawString(str, (float) point1.getX() - str_len,
                    (float) (point1.getY() + str_hei / 2));
        }

        int horizontalScale = 10;
        gridLen = (hMax - hMin) / horizontalScale;
        for (int i = 1; i <= horizontalScale; i++) {
            Point2D point1 = transformer.data2ScreenPoint(hMin + i * gridLen, vMin);
            Point2D point2 = new Point2D.Double(point1.getX(), point1.getY() - scaleLen);
            Line2D.Double zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            str = Util.formatPValue(hMin + i * gridLen);
            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.drawString(str, (float) point1.getX() - str_len / 2,
                    (float) (point1.getY() + str_hei));
        }
        g2.setFont(oldFront);
    }

    private void drawAxesScale(Graphics2D g2, CoordinateTransformer transformer) {
        g2.setColor(axesColor.darker());
        String str;
        int scaleLen = 3;
        int verticalScale = 10;
        double vMin = transformer.getDataVerticalMin();
        double vMax = transformer.getDataVerticalMax();
        double hMin = transformer.getDataHorizontalMin();
        double hMax = transformer.getDataHorizontalMax();
        double gridLen = (vMax - vMin) / verticalScale;

        Font oldFront = g2.getFont();
        g2.setFont(numFont);

        int str_hei = g2.getFontMetrics().getAscent();
        for (int i = 1; i <= verticalScale; i++) {
            Point2D point1 = transformer.data2ScreenPoint(hMin, vMin + i * gridLen);
            Point2D point2 = new Point2D.Double(point1.getX() + scaleLen, point1.getY());
            Line2D.Double zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            str = Util.doubleToString(vMin + i * gridLen, 5, yScalDecimal);

            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.drawString(str, (float) point1.getX() - str_len,
                    (float) (point1.getY() + str_hei / 2));
        }

        int horizontalScale = 10;
        gridLen = (hMax - hMin) / horizontalScale;
        for (int i = 1; i <= horizontalScale; i++) {
            Point2D point1 = transformer.data2ScreenPoint(hMin + i * gridLen, vMin);
            Point2D point2 = new Point2D.Double(point1.getX(), point1.getY() - scaleLen);
            Line2D.Double zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            str = Util.doubleToString(hMin + i * gridLen, 5, 2);
            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.drawString(str, (float) point1.getX() - str_len / 2,
                    (float) (point1.getY() + str_hei));
        }
        g2.setFont(oldFront);
    }

    /**
     *
     */
    public class CoordinateTransformer {

        private int screenHorizontalMin, screenVerticalMin, screenHorizontalMax, screenVerticalMax;
        private double dataHorizontalMin, dataVerticalMin, dataHorizontalMax, dataVerticalMax;
        double horizontalResolution, verticalResolution;

        /**
         *
         * @return
         */
        public double getDataHorizontalMax() {
            return dataHorizontalMax;
        }

        /**
         *
         * @param dataHorizontalMax
         */
        public void setDataHorizontalMax(double dataHorizontalMax) {
            this.dataHorizontalMax = dataHorizontalMax;
        }

        /**
         *
         * @return
         */
        public double getDataHorizontalMin() {
            return dataHorizontalMin;
        }

        /**
         *
         * @param dataHorizontalMin
         */
        public void setDataHorizontalMin(double dataHorizontalMin) {
            this.dataHorizontalMin = dataHorizontalMin;
        }

        /**
         *
         * @return
         */
        public double getDataVerticalMax() {
            return dataVerticalMax;
        }

        /**
         *
         * @param dataVerticalMax
         */
        public void setDataVerticalMax(double dataVerticalMax) {
            this.dataVerticalMax = dataVerticalMax;
        }

        /**
         *
         * @return
         */
        public double getDataVerticalMin() {
            return dataVerticalMin;
        }

        /**
         *
         * @param dataVerticalMin
         */
        public void setDataVerticalMin(double dataVerticalMin) {
            this.dataVerticalMin = dataVerticalMin;
        }

        /**
         *
         * @return
         */
        public double getHorizontalResolution() {
            return horizontalResolution;
        }

        /**
         *
         * @param horizontalResolution
         */
        public void setHorizontalResolution(double horizontalResolution) {
            this.horizontalResolution = horizontalResolution;
        }

        /**
         *
         * @return
         */
        public int getScreenHorizontalMax() {
            return screenHorizontalMax;
        }

        /**
         *
         * @param screenHorizontalMax
         */
        public void setScreenHorizontalMax(int screenHorizontalMax) {
            this.screenHorizontalMax = screenHorizontalMax;
        }

        /**
         *
         * @return
         */
        public int getScreenHorizontalMin() {
            return screenHorizontalMin;
        }

        /**
         *
         * @param screenHorizontalMin
         */
        public void setScreenHorizontalMin(int screenHorizontalMin) {
            this.screenHorizontalMin = screenHorizontalMin;
        }

        /**
         *
         * @return
         */
        public int getScreenVerticalMax() {
            return screenVerticalMax;
        }

        /**
         *
         * @param screenVerticalMax
         */
        public void setScreenVerticalMax(int screenVerticalMax) {
            this.screenVerticalMax = screenVerticalMax;
        }

        /**
         *
         * @return
         */
        public int getScreenVerticalMin() {
            return screenVerticalMin;
        }

        /**
         *
         * @param screenVerticalMin
         */
        public void setScreenVerticalMin(int screenVerticalMin) {
            this.screenVerticalMin = screenVerticalMin;
        }

        /**
         *
         * @return
         */
        public double getVerticalResolution() {
            return verticalResolution;
        }

        /**
         *
         * @param verticalResolution
         */
        public void setVerticalResolution(double verticalResolution) {
            this.verticalResolution = verticalResolution;
        }

        /**
         *
         * @param screenHorizontalMin
         * @param screenHorizontalMax
         * @param screenVerticalMin
         * @param screenVerticalMax
         * @param dataHorizontalMin
         * @param dataHorizontalMax
         * @param dataVerticalMin
         * @param dataVerticalMax
         */
        public void setupBasicScope(int screenHorizontalMin, int screenHorizontalMax, int screenVerticalMin, int screenVerticalMax,
                double dataHorizontalMin, double dataHorizontalMax, double dataVerticalMin, double dataVerticalMax) {
            this.screenHorizontalMin = screenHorizontalMin;
            this.screenHorizontalMax = screenHorizontalMax;
            this.screenVerticalMin = screenVerticalMin;
            this.screenVerticalMax = screenVerticalMax;

            this.dataHorizontalMin = dataHorizontalMin;
            this.dataHorizontalMax = dataHorizontalMax;
            this.dataVerticalMin = dataVerticalMin;
            this.dataVerticalMax = dataVerticalMax;

            this.horizontalResolution = (dataHorizontalMax - dataHorizontalMin) / (screenHorizontalMax - screenHorizontalMin);
            this.verticalResolution = (dataVerticalMax - dataVerticalMin) / (screenVerticalMax - screenVerticalMin);

        }
// note the vertical coordinates has been converted as we see usually

        /**
         *
         * @param Xa
         * @param Ya
         * @return
         */
        public double[] data2Screen(double Xa, double Ya) {
            double[] myout = new double[2];
            myout[0] = ((Xa - dataHorizontalMin) / horizontalResolution + screenHorizontalMin);
            myout[1] = (screenVerticalMax - (Ya - dataVerticalMin) / verticalResolution);
            return myout;
        }

        /**
         *
         * @param Xa
         * @param Ya
         * @return
         */
        public Point data2ScreenPoint(double Xa, double Ya) {
            Point myout = new Point();
            myout.setLocation(((Xa - dataHorizontalMin) / horizontalResolution + screenHorizontalMin),
                    (screenVerticalMax - (Ya - dataVerticalMin) / verticalResolution));
            return myout;
        }

        public void data2ScreenPoint(Point myout, double Xa, double Ya) {
            myout.setLocation(((Xa - dataHorizontalMin) / horizontalResolution + screenHorizontalMin),
                    (screenVerticalMax - (Ya - dataVerticalMin) / verticalResolution));
        }

        /**
         *
         * @param X6
         * @param Y6
         * @return
         */
        public double[] screen2Data(int X6, int Y6) {
            double[] myout = new double[2];
            myout[0] = (X6 - screenHorizontalMin) * horizontalResolution + dataHorizontalMin;
            myout[1] = (screenVerticalMax - Y6) * verticalResolution + dataVerticalMin;
            return myout;
        }

        /**
         *
         * @param len
         * @return
         */
        public double horizontalSegmentData2Screen(double len) {
            return len / horizontalResolution;
        }

        /**
         *
         * @param len
         * @return
         */
        public double verticalSegmentData2Screen(double len) {
            return len / verticalResolution;
        }
    }

    /**
     *
     * @param xList
     * @param yList
     * @param title
     * @param outputPath
     * @throws Exception
     */
    public void drawScatterPlot(DoubleArrayList xList, DoubleArrayList yList, String title, String outputPath) throws Exception {
        BufferedImage image = new BufferedImage(this.figureWidth, this.figureHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintPlotArea(g2d);
        drawAxes(g2d, title, "Relative Pyshical Position (bp)", "-Log10(p)");

        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pointSize = xList.size();
        //max and min value in vertical
        double vMax = Descriptive.max(yList);
        double vMin = Descriptive.min(yList);
        double hMin = Descriptive.min(xList);
        double hMax = Descriptive.max(xList);
        double thresholdForColor = 0.05;
        //sometimes there is only one point
        if (vMin - vMax == 0) {
            vMin = vMax - 1;
        }
        if (hMin - hMax == 0) {
            hMin = hMax - 1;
        }
        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);
        drawAxesSciScale(g2d, transformer);
        double rectangleSize = 4;
        for (int i = 0; i < pointSize; i++) {
            if (yList.getQuick(i) > (-Math.log10(thresholdForColor))) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLACK);
            }
            Point2D point1 = transformer.data2ScreenPoint(xList.getQuick(i), yList.getQuick(i));
            Rectangle2D.Double f = new Rectangle2D.Double(point1.getX() - rectangleSize / 2,
                    point1.getY() - rectangleSize / 2, rectangleSize, rectangleSize);
            g2d.draw(f);
            g2d.fill(f);
        }
        g2d.dispose();
        //outputJPEGFile(image, outputPath);
        outputPNGFile(image, outputPath);
    }
}
