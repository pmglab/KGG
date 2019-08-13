/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.plot;

import cern.colt.list.DoubleArrayList;

import cern.colt.list.FloatArrayList;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.stat.Descriptive;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


/**
 *
 * @author mxli
 */
public class HistogramPainter extends BasicPainter {

    public HistogramPainter(int width, int height) {
        super(width, height);
    }
 
    public void drawHistogramPlot(final FloatArrayList valueList, double[][] range, String title, String xTitle, String outputPath) throws Exception {
        if (valueList == null || valueList.size() == 0) {
            System.err.println("Null p-value list");
            return;
        }
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }

        FloatArrayList tmpValueList = valueList.copy();
        BufferedImage image = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintCanvas(g2d);
        yScalDecimal = 3;

        int dataSize = tmpValueList.size();
        tmpValueList.quickSort();
        int rowNum = range.length;
        int varIndex = 0;
        DoubleArrayList pvalueCounts = new DoubleArrayList();
        double sum = 0;
        double count = 0;

        for (int i = 0; i < rowNum; i++) {
            //note: binarySearch cannot handle equal MAF in the list
            //the end is not included [0,0.01)
            count = 0;
            while (varIndex < dataSize) {
                if (tmpValueList.getQuick(varIndex) >= range[i][0] && tmpValueList.getQuick(varIndex) < range[i][1]) {
                    count += 1;
                } else {
                    break;
                }
                varIndex++;
            }
            //   System.out.println(varIndex + " " + count + " " + range[i][0] + " " + range[i][1]);
            if (i == rowNum - 1) {
                //include the largest one
                count += (dataSize - varIndex);
            }
            pvalueCounts.add(count / dataSize);
            sum += pvalueCounts.getQuick(i);
        }
        //System.out.println("Sum: " + sum);

        //max and min value in vertical
        double vMax = Descriptive.max(pvalueCounts) + 0.005;
        double vMin = 0;
        double hMin = range[0][0];
        double hMax = range[range.length - 1][1];


        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);

        int maxYScaleLen = drawAxesScale(g2d, transformer);
        drawAxes(g2d, title, xTitle, "Proportion", maxYScaleLen);
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

    public void drawColorfulHistogramPlot(final FloatArrayList valueList, double[][] range, String title, String xTitle, String outputPath) throws Exception {
        if (valueList == null || valueList.size() == 0) {
            System.err.println("Null p-value list");
            return;
        }
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }

        FloatArrayList tmpValueList = valueList.copy();
        BufferedImage image = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintCanvas(g2d);
        yScalDecimal = 3;

        int dataSize = tmpValueList.size();
        tmpValueList.quickSort();
        int rowNum = range.length;
        int varIndex = 0;
        DoubleArrayList pvalueCounts = new DoubleArrayList();
        double sum = 0;
        double count = 0;

        for (int i = 0; i < rowNum; i++) {
            //note: binarySearch cannot handle equal MAF in the list
            //the end is not included [0,0.01)
            count = 0;
            while (varIndex < dataSize) {
                if (tmpValueList.getQuick(varIndex) >= range[i][0] && tmpValueList.getQuick(varIndex) < range[i][1]) {
                    count += 1;
                } else {
                    break;
                }
                varIndex++;
            }
            //   System.out.println(varIndex + " " + count + " " + range[i][0] + " " + range[i][1]);
            if (i == rowNum - 1) {
                //include the largest one
                count += (dataSize - varIndex);
            }
            pvalueCounts.add(count / dataSize);
            sum += pvalueCounts.getQuick(i);
        }
        //System.out.println("Sum: " + sum);

        //max and min value in vertical
        double vMax = Descriptive.max(pvalueCounts) + 0.005;
        double vMin = 0;
        double hMin = range[0][0];
        double hMax = range[range.length - 1][1];


        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);

        int maxYScaleLen = drawAxesScale(g2d, transformer);
        drawAxes(g2d, title, xTitle, "Proportion", maxYScaleLen);
        dataSize = pvalueCounts.size();
        dataSize = pvalueCounts.size();
        Color[] gradientColors = new Color[]{Color.GREEN, Color.RED};
        Color[] colors = Gradient.createMultiGradient(gradientColors, dataSize);

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

            g2d.setColor(Color.BLACK);
            g2d.drawRect(point1.x + 1, point1.y, point2.x - point1.x, point2.y - point1.y);
            g2d.setColor(colors[i]);
             g2d.fillRect(point1.x + 2, point1.y + 1, point2.x - point1.x - 1, point2.y - point1.y - 1);
        }
        g2d.setColor(Color.BLACK);
        g2d.dispose();
        outputPNGFile(image, outputPath);
    }

    public void drawHistogramPlot(final DoubleArrayList valueList, int grids, String title, String xLabel, String outputPath) throws Exception {
        if (valueList == null || valueList.size() == 0) {
            System.err.println("Null p-value list");
            return;
        }
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }
        double[][] range = new double[grids][2];
        DoubleArrayList tmpValueList = valueList.copy();
        BufferedImage image = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintCanvas(g2d);
        yScalDecimal = 3;

        int dataSize = tmpValueList.size();
        tmpValueList.quickSort();
        double hMin = Descriptive.min(tmpValueList);
        double hMax = Descriptive.max(tmpValueList);
        double inc = (hMax - hMin) / grids;
        range[0][0] = hMin;
        //it looks runduant.but otherwise it cannot distinct between 0.15 and 0.15000002
        grids--;
        for (int i = 0; i < grids; i++) {
            range[i][1] = hMin + inc * (i + 1);
            range[i + 1][0] = range[i][1];
        }
        range[grids][1] = hMax;
        grids++;
        int rowNum = range.length;
        int varIndex = 0;
        DoubleArrayList pvalueCounts = new DoubleArrayList();
        double sum = 0;
        double count = 0;
        //  System.out.println("\n\n\n" + xLabel);
        for (int i = 0; i < rowNum; i++) {
            //note: binarySearch cannot handle equal MAF in the list
            //the end is not included [0,0.01)
            count = 0;
            while (varIndex < dataSize) {
                if (tmpValueList.getQuick(varIndex) >= range[i][0] && tmpValueList.getQuick(varIndex) < range[i][1]) {
                    count += 1;
                } else {
                    break;
                }
                varIndex++;
            }
            //   System.out.println(varIndex + " " + count + " " + range[i][0] + " " + range[i][1]);
            if (i == rowNum - 1) {
                //include the largest one
                count += (dataSize - varIndex);
            }
            pvalueCounts.add(count / dataSize);
            sum += pvalueCounts.getQuick(i);
        }
        // System.out.println("Sum: " + sum);

        //max and min value in vertical
        double vMax = Descriptive.max(pvalueCounts);
        double vMin = 0;


        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);

        int maxYScaleLen = drawAxesScale(g2d, transformer);
        drawAxes(g2d, title, xLabel, "Proportion", maxYScaleLen);
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
            transformer.data2ScreenPoint(point2, tmpDouble1, vMin);
            g2d.drawRect(point1.x + 1, point1.y, point2.x - point1.x, point2.y - point1.y);
        }
        g2d.dispose();
        outputPNGFile(image, outputPath);
    }

    public void drawColorfulHistogramPlot(final DoubleArrayList valueList, int grids, String title,
            String xLabel, boolean scificScaleX, boolean isProportionY, String outputPath) throws Exception {
        if (valueList == null || valueList.size() == 0) {
            System.err.println("Null p-value list");
            return;
        }
        dataPlottingOffsetRight = 30;
        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true);
        } else {
            calculateDataPlottingArea(false);
        }
        double[][] range = new double[grids][2];
        DoubleArrayList tmpValueList = valueList.copy();
        BufferedImage image = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintCanvas(g2d);


        int dataSize = tmpValueList.size();
        tmpValueList.quickSort();
        double hMin = Descriptive.min(tmpValueList);
        double hMax = Descriptive.max(tmpValueList);
        double inc = (hMax - hMin) / grids;
        range[0][0] = hMin;
        //it looks runduant.but otherwise it cannot distinct between 0.15 and 0.15000002
        grids--;
        for (int i = 0; i < grids; i++) {
            range[i][1] = hMin + inc * (i + 1);
            range[i + 1][0] = range[i][1];
        }
        range[grids][1] = hMax;
        grids++;
        int rowNum = range.length;
        int varIndex = 0;
        DoubleArrayList pvalueCounts = new DoubleArrayList();
        double sum = 0;
        double count = 0;
        //  System.out.println("\n\n\n" + xLabel);
        for (int i = 0; i < rowNum; i++) {
            //note: binarySearch cannot handle equal MAF in the list
            //the end is not included [0,0.01)
            count = 0;
            while (varIndex < dataSize) {
                if (tmpValueList.getQuick(varIndex) >= range[i][0] && tmpValueList.getQuick(varIndex) < range[i][1]) {
                    count += 1;
                } else {
                    break;
                }
                varIndex++;
            }

            if (i == rowNum - 1) {
                //include the largest one
                count += (dataSize - varIndex);
            }
            if (isProportionY) {
                pvalueCounts.add(count / dataSize);
            } else {
                pvalueCounts.add(count);
            }
            // sum += pvalueCounts.getQuick(i);
            System.out.println(range[i][0] + "-" + range[i][1] + ":" + count);
        }
        // System.out.println("Sum: " + sum);

        //max and min value in vertical
        double vMax = Descriptive.max(pvalueCounts);
        double vMin = 0;
        if (isProportionY) {
            yScalDecimal = 3;
        } else {
            yScalDecimal = 0;
        }
        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(dataPlottingArea.x, dataPlottingArea.x + dataPlottingArea.width,
                dataPlottingArea.y, dataPlottingArea.y + dataPlottingArea.height, hMin, hMax, vMin, vMax);

        int maxYScalLen = 0;
        if (scificScaleX) {
            maxYScalLen = drawAxesSciScale(g2d, transformer, true, false);
        } else {
            maxYScalLen = drawAxesScale(g2d, transformer);
        }
        if (isProportionY) {
            drawAxes(g2d, title, xLabel, "Proportion", maxYScalLen);
        } else {
            drawAxes(g2d, title, xLabel, "Count", maxYScalLen);
        }

        dataSize = pvalueCounts.size();
        Color[] gradientColors = new Color[]{Color.GREEN, Color.red};
        Color[] colors = Gradient.createMultiGradient(gradientColors, dataSize);
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
            transformer.data2ScreenPoint(point2, tmpDouble1, vMin);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(point1.x + 1, point1.y, point2.x - point1.x, point2.y - point1.y);
            g2d.setColor(colors[i]);
            g2d.fillRect(point1.x + 2, point1.y + 1, point2.x - point1.x - 1, point2.y - point1.y - 1);
        }
        g2d.setColor(Color.BLACK);
        g2d.dispose();
        outputPNGFile(image, outputPath);
    }

    public static void main(String[] args) {
        try {

           
            Uniform twister = new Uniform(new MersenneTwister(new java.util.Date()));
           
            DoubleArrayList rands = new DoubleArrayList();
            int num = 10000;
            for (int i = 0; i < num; i++) {
                rands.add(twister.nextDouble());
            }
            HistogramPainter plot = new HistogramPainter(800, 600);
            plot.drawColorfulHistogramPlot(rands, 10, null, "Proportion", true, true, "test.png");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
