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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.cobi.kgg.business.entity.Chromosome;
import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.Gene;
import org.cobi.kgg.business.entity.Genome;
import org.cobi.kgg.business.entity.SNP;

import org.cobi.util.text.Util;

/**
 *
 * @author mxli
 */
public class ManhattanPlotPainter implements Constants {

    //unit pixel
    private int offsetLeft = 70;
    private int offsetTop = 10;
    private int offsetBottom = 40;
    private int offsetRight = 0;
    private Rectangle plotScope;
    private Dimension canvasScope = new Dimension(650, 400);
    private boolean hasTitleFlag = false;
    private Color plotBackgroundColor = Color.WHITE;
    private Color axesColor = Color.BLACK;
    private Color nonplotBackgroundColor = Color.LIGHT_GRAY;
    private Font titleFont = new Font("SansSerif", Font.BOLD, 16);
    private HashSet<Integer> selectiveChromIDs = new HashSet<Integer>();
    double manhattanPlotLabeGenePValue;
    double manhattanPlotLableSNPPValue;
    double manhattanPlotMinPValue;
    boolean manhanttanPlotSNPOutGene;
    //do not draw string at the ocupied lable postions
    List<int[]> ocupiedLabelPostions = new ArrayList<int[]>();
    private Font numFont = new Font("SansSerif", Font.BOLD, 12);

    public double getManhattanPlotLabeGenePValue() {
        return manhattanPlotLabeGenePValue;
    }

    private boolean isAvailableSpace(int x, int y, int width, int height) {
        for (int[] poss : ocupiedLabelPostions) {
            if (poss[0] > x && poss[1] > y) {
                if (poss[0] < x + width && poss[1] < y + height) {
                    return false;
                }
            } else if (poss[0] > x && poss[1] < y) {
                if (poss[0] < x + width) {
                    return false;
                }
            } else if (poss[0] < x && poss[1] > y) {
                if (poss[1] < y + height) {
                    return false;
                }
            } else if (poss[0] + poss[2] < x || poss[1] + poss[3] < y) {
            } else {
                return false;
            }
        }
        ocupiedLabelPostions.add(new int[]{x, y, width, height});
        return true;
    }

    public void setManhattanPlotLabeGenePValue(double manhattanPlotLabeGenePValue) {
        this.manhattanPlotLabeGenePValue = manhattanPlotLabeGenePValue;
    }

    public double getManhattanPlotLableSNPPValue() {
        return manhattanPlotLableSNPPValue;
    }

    public void setManhattanPlotLableSNPPValue(double manhattanPlotLableSNPPValue) {
        this.manhattanPlotLableSNPPValue = manhattanPlotLableSNPPValue;
    }

    public double getManhattanPlotMinPValue() {
        return manhattanPlotMinPValue;
    }

    public void setManhattanPlotMinPValue(double manhattanPlotMinPValue) {
        this.manhattanPlotMinPValue = manhattanPlotMinPValue;
    }

    public HashSet<Integer> getSelectiveChromIDs() {
        return selectiveChromIDs;
    }

    public void setSelectiveChromIDs(HashSet<Integer> selectiveChromIDs) {
        this.selectiveChromIDs = selectiveChromIDs;
    }

    /**
     *
     * @param width
     * @param height
     */
    public ManhattanPlotPainter(int width, int height) {
        canvasScope.width = width;
        canvasScope.height = height;
        calculatePlotArea();
    }

    private void calculatePlotArea() {
        if (hasTitleFlag) {
            plotScope = new Rectangle(offsetLeft, offsetTop + 60,
                    canvasScope.width - offsetRight - offsetLeft, canvasScope.height - offsetBottom - offsetTop - 60);
        } else {
            plotScope = new Rectangle(offsetLeft, offsetTop,
                    canvasScope.width - offsetRight - offsetLeft, canvasScope.height - offsetBottom - offsetTop);
        }

    }

    private void paintPlotArea(Graphics2D g2d) {
        g2d.clipRect(0, 0, canvasScope.width, canvasScope.height);
        g2d.setColor(plotBackgroundColor);
        Rectangle2D rc = new Rectangle2D.Double((double) 0,
                (double) 0, (double) canvasScope.width,
                (double) canvasScope.height);
        g2d.fill(rc);
        GradientPaint pat = new GradientPaint(0f, 0f, Color.WHITE, 100f, 45f, Color.GREEN);
        g2d.setPaint(pat);

    }

    public long calculateChromsomeLength(Genome genome, Map<Integer, long[]> chromPositions) throws Exception {
        long totalChromLen = 0;
        int geneNum = 0;
        int seperator = 10;
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (!selectiveChromIDs.contains(chromIndex)) {
                continue;
            }
            Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
            if (chromosome == null || chromosome.genes.isEmpty()) {
                continue;
            }

            //startPos and length;
            long[] chromPosition = new long[3];
            chromPosition[0] = 0;
            chromPosition[1] = 0;

            List<Gene> genes = chromosome.genes;
            geneNum = genes.size();
            long startPos = 0;
            long endPos = 0;

            //no need assume the genes have been sortted according to their physical position
            if (genes != null && geneNum > 0) {
                startPos = Integer.MAX_VALUE;
                endPos = Integer.MIN_VALUE;
                for (int i = 0; i < geneNum; i++) {
                    if (Math.min(genes.get(i).start, genes.get(i).end) > 0 && startPos > Math.min(genes.get(i).start, genes.get(i).end)) {
                        startPos = Math.min(genes.get(i).start, genes.get(i).end);
                    }

                    if (Math.max(genes.get(i).start, genes.get(i).end) > 0 && endPos < Math.max(genes.get(i).start, genes.get(i).end)) {
                        endPos = Math.max(genes.get(i).start, genes.get(i).end);
                    }
                }
            }
            //no need assume the SNPs have been sortted according to their physical position
            if (manhanttanPlotSNPOutGene) {
                List<SNP> snps = chromosome.snpsOutGenes;
                int snpNum = snps.size();
                for (int i = 0; i < snpNum; i++) {
                    if (startPos > snps.get(i).physicalPosition) {
                        startPos = snps.get(i).physicalPosition;
                    }
                    if (endPos < snps.get(i).physicalPosition) {
                        endPos = snps.get(i).physicalPosition;
                    }
                }
            }
            //start position in the figure
            chromPosition[0] = totalChromLen;
            //end position in the figure
            chromPosition[1] = endPos - startPos + totalChromLen + seperator;
            //offset at this chromsome
            chromPosition[2] = startPos;
            totalChromLen = chromPosition[1];
            chromPositions.put(chromIndex, chromPosition);
        }
        return totalChromLen;
    }

    /**
     *
     * @param geneList
     * @param adjustedPValue
     * @param addedName
     * @return
     * @throws Exception
     */
    public String plotGenePValues(Map<String, Double> pValueGeneMap, Genome genome, double vMax, String outputPath) throws Exception {
        if (pValueGeneMap == null || pValueGeneMap.isEmpty()) {
            System.err.println("Null p-value list");
            return "Null p-value list";
        }
        BufferedImage image = new BufferedImage(this.canvasScope.width, this.canvasScope.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintPlotArea(g2d);

        Map<Integer, long[]> chromPositions = new HashMap<Integer, long[]>();

        //max and min value in vertical
        double vMin = 0;
        double hMin = 0;
        double hMax = calculateChromsomeLength(genome, chromPositions);

        double rectangleHieght = 3;
        double tmpX1;
        double tmpX2;
        double tmpY;

        //sometimes the p values are too large
        if (vMax > -Math.log10(manhattanPlotMinPValue)) {
            vMax = -Math.log10(manhattanPlotMinPValue);
        }
        double labeLogP = -Math.log10(manhattanPlotLabeGenePValue);
        double amplifiedScale = 8;
        CoordinateTransformer transformer = new CoordinateTransformer();
        transformer.setupBasicScope(plotScope.x, plotScope.x + plotScope.width,
                plotScope.y, plotScope.y + plotScope.height, hMin, hMax, vMin, vMax);

        drawAxes(g2d, null, "Chromosomes", "-Log10(P)");
        renderPlotBackGroundHorizontalScale(g2d, chromPositions, transformer);

        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();

        //Color[] colors = {Color.BLUE, Color.RED};
        Color[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.PINK, Color.RED,
            Color.BLACK, Color.MAGENTA, Color.YELLOW, Color.CYAN};

        int validGeneNum = 0;
        int str_hei = g2d.getFontMetrics().getAscent();

        int validChromNum = 0;
        ocupiedLabelPostions.clear();
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (!selectiveChromIDs.contains(chromIndex)) {
                continue;
            }
            Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
            if (chromosome == null || chromosome.genes.isEmpty()) {
                continue;
            }
            long[] postions = chromPositions.get(chromIndex);
            transformer.setxOffset(postions[0]);
            transformer.setLocalMaxX(postions[1]);
            transformer.setChromosomeOffset(postions[2]);
            Point2D pointMin = transformer.generalData2ScreenPoint(postions[0], 0);
            Point2D pointMax = transformer.generalData2ScreenPoint(postions[1], 0);
            int geneNum = chromosome.genes.size();
            g2d.setColor(colors[validChromNum % colors.length]);
            double recX, recY;
            for (int i = 0; i < geneNum; i++) {
                Gene gene = chromosome.genes.get(i);
                String geneSymbol = gene.getSymbol();
                validGeneNum++;
                Double p = pValueGeneMap.get(geneSymbol);
                if (p == null) {
                    continue;
                }
                tmpY = -Math.log10(p);
                tmpX1 = gene.start;
                tmpX2 = gene.end;
                if (tmpX1 < 0 || tmpX2 < 0) {
                    continue;
                }

                //Point2D testPoint=transformer.chromosomePoint2ScreenPoint(postions[2], tmpY);
                Point2D point1 = transformer.chromosomePoint2ScreenPoint(tmpX1, tmpY);
                Point2D point2 = transformer.chromosomePoint2ScreenPoint(tmpX2, tmpY);

                double width = point2.getX() - point1.getX();
                width *= amplifiedScale;
                if (tmpY > 3) {
                    width *= 3;
                }
                recX = point1.getX() - width / 2;
                recY = point1.getY() - width / 2;
                if (recX < pointMin.getX()) {
                    recX = pointMin.getX();
                } else if (recX + width > pointMax.getX()) {
                    recX = pointMax.getX() - width;
                }

                recY = point1.getY() - rectangleHieght / 2;
                if (recY < plotScope.y) {
                    recY = plotScope.y;
                } else if (recY + rectangleHieght > plotScope.y + plotScope.height) {
                    recY = plotScope.y + plotScope.height - rectangleHieght;
                }
                Rectangle2D.Double f = new Rectangle2D.Double(recX, recY, width, rectangleHieght);

                g2d.fill(f);
                g2d.draw(f);
                if (tmpY >= labeLogP) {
                    int str_len = g2d.getFontMetrics().stringWidth(geneSymbol);

                    recX = (float) (point1.getX() + point2.getX() - str_len) / 2;
                    if (recX < plotScope.x) {
                        recX = plotScope.x;
                    } else if (recX + str_len > plotScope.x + plotScope.width) {
                        recX = plotScope.x + plotScope.width - str_len;
                    }
                    if (this.isAvailableSpace((int) recX, (int) (point1.getY() + str_hei), (str_len), (str_hei))) {
                        g2d.setColor(Color.black);
                        g2d.drawString(geneSymbol, (float) recX, (float) (point1.getY() + str_hei));
                        g2d.setColor(colors[validChromNum % colors.length]);
                    }
                }
            }
            validChromNum++;
        }

        genome.releaseMemory();
        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);
        drawAxesOnly(g2d, chromPositions, transformer);
        g2d.dispose();
        //PValuePainter.outputJPEGFile(image, outputPath);
        PValuePainter.outputPNGFile(image, outputPath);
        return "";
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

    public Color[] pick(int num) {
        Color[] colors = new Color[num];
        if (num < 2) {
            colors[0] = Color.BLACK;
            return colors;
        }
        float dx = 1.0f / (float) (num - 1);
        for (int i = 0; i < num; i++) {
            colors[i] = (get(i * dx));
        }
        return colors;
    }

    /**
     *
     * @param geneList
     * @param adjustedPValue
     * @param addedName
     * @return
     * @throws Exception
     */
    public String plotSNPPValues(Genome genome, double vMax, int[] selectedSNPIndex, String outputPath) throws Exception {
        BufferedImage image = new BufferedImage(this.canvasScope.width, this.canvasScope.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintPlotArea(g2d);

        int geneNum = 0;

        Map<Integer, long[]> chromPositions = new HashMap<Integer, long[]>();

        //max and min value in vertical
        double vMin = 0;
        double hMin = 0;
        double hMax = calculateChromsomeLength(genome, chromPositions);

        double rectangleSize = 3;
        double tmpX1;
        double tmpX2;
        double tmpY;

        //sometimes the p values are too large
        if (vMax > -Math.log10(manhattanPlotMinPValue)) {
            vMax = -Math.log10(manhattanPlotMinPValue);
        }
        double labeLogP = -Math.log10(manhattanPlotLableSNPPValue);

        CoordinateTransformer transformer = new CoordinateTransformer();

        //set plot area excluding axes
        transformer.setupBasicScope(plotScope.x, plotScope.x + plotScope.width,
                plotScope.y, plotScope.y + plotScope.height, hMin, hMax, vMin, vMax);
        drawAxes(g2d, null, "Chromosomes", "-Log10(P)");
        renderPlotBackGroundHorizontalScale(g2d, chromPositions, transformer);

        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        // Color[] colors = pick(23);
        // Color[] colors = {Color.BLUE, Color.RED};

        Color[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.PINK, Color.RED,
            Color.BLACK, Color.MAGENTA, Color.YELLOW, Color.CYAN};

        int str_hei = g2d.getFontMetrics().getAscent();
        int slectedSNPNum = selectedSNPIndex.length;
        int validChromNum = 0;
        ocupiedLabelPostions.clear();
        for (int chromIndex = 0; chromIndex < CHROM_NAMES.length; chromIndex++) {
            if (!selectiveChromIDs.contains(chromIndex)) {
                continue;
            }
            Chromosome chromosome = genome.readChromosomefromDisk(chromIndex);
            if (chromosome == null || chromosome.genes.isEmpty()) {
                continue;
            }
            long[] postions = chromPositions.get(chromIndex);
            transformer.setxOffset(postions[0]);
            transformer.setLocalMaxX(postions[1]);
            transformer.setChromosomeOffset(postions[2]);

            List<Gene> genes = chromosome.genes;
            geneNum = genes.size();
            g2d.setColor(colors[validChromNum % colors.length]);
            double recX, recY;
            Point2D pointMin = transformer.generalData2ScreenPoint(postions[0], 0);
            Point2D pointMax = transformer.generalData2ScreenPoint(postions[1], 0);

            for (int j = 0; j < geneNum; j++) {
                List<SNP> snps = genes.get(j).snps;
                int snpNum = snps.size();
                for (int k = 0; k < snpNum; k++) {
                    double[] pValues = snps.get(k).getpValues();
                    if (pValues == null) {
                        continue;
                    }
                    tmpX1 = snps.get(k).physicalPosition;
                    for (int t = 0; t < slectedSNPNum; t++) {
                        tmpY = -Math.log10(pValues[selectedSNPIndex[t]]);
                        if (tmpY > vMax) {
                            tmpY = vMax;
                        }
                        Point2D point1 = transformer.chromosomePoint2ScreenPoint(tmpX1, tmpY);
                        if (tmpY < 3) {
                            rectangleSize = 1.5;
                        } else {
                            rectangleSize = 4;
                        }
                        recX = point1.getX() - rectangleSize / 2;
                        recY = point1.getY() - rectangleSize / 2;
                        if (recX < pointMin.getX()) {
                            recX = pointMin.getX();
                        } else if (recX + rectangleSize > pointMax.getX()) {
                            recX = pointMax.getX() - rectangleSize;
                        }

                        if (recY < plotScope.y) {
                            recY = plotScope.y;
                        } else if (recY + rectangleSize > plotScope.y + plotScope.height) {
                            recY = plotScope.y + plotScope.height - rectangleSize;
                        }

                        Rectangle2D.Double f = new Rectangle2D.Double(recX,
                                recY, rectangleSize, rectangleSize);
                        g2d.fill(f);
                        g2d.draw(f);
                        if (tmpY >= labeLogP) {
                            String rsID = snps.get(k).rsID;
                            int str_len = g2d.getFontMetrics().stringWidth(rsID);

                            recX = (float) point1.getX() - str_len / 2;
                            if (recX < plotScope.x) {
                                recX = plotScope.x;
                            } else if (recX + str_len > plotScope.x + plotScope.width) {
                                recX = plotScope.x + plotScope.width - str_len;
                            }
                            if (this.isAvailableSpace((int) recX, (int) (point1.getY() + str_hei), (str_len), (str_hei))) {
                                g2d.setColor(Color.black);
                                g2d.drawString(rsID, (float) recX,
                                        (float) (point1.getY() + str_hei));
                                g2d.setColor(colors[validChromNum % colors.length]);
                            }

                        }
                    }
                }
            }
            if (manhanttanPlotSNPOutGene) {
                List<SNP> snps = chromosome.snpsOutGenes;
                int snpNum = snps.size();
                for (int k = 0; k < snpNum; k++) {
                    double[] pValues = snps.get(k).getpValues();
                    if (pValues == null) {
                        continue;
                    }
                    tmpX1 = snps.get(k).physicalPosition;
                    for (int t = 0; t < slectedSNPNum; t++) {
                        tmpY = -Math.log10(pValues[selectedSNPIndex[t]]);
                        if (tmpY > vMax) {
                            tmpY = vMax;
                        }
                        Point2D point1 = transformer.chromosomePoint2ScreenPoint(tmpX1, tmpY);
                        Rectangle2D.Double f = new Rectangle2D.Double(point1.getX() - rectangleSize / 2,
                                point1.getY() - rectangleSize / 2, rectangleSize, rectangleSize);
                        g2d.fill(f);
                        g2d.draw(f);
                        if (tmpY >= labeLogP) {
                            String rsID = snps.get(k).rsID;
                            int str_len = g2d.getFontMetrics().stringWidth(rsID);

                            recX = (float) point1.getX() - str_len / 2;
                            if (recX < plotScope.x) {
                                recX = plotScope.x;
                            } else if (recX + str_len > plotScope.x + plotScope.width) {
                                recX = plotScope.x + plotScope.width - str_len;
                            }
                            if (this.isAvailableSpace((int) recX, (int) (point1.getY() + str_hei), (str_len), (str_hei))) {
                                g2d.setColor(Color.black);
                                g2d.drawString(rsID, (float) recX,
                                        (float) (point1.getY() + str_hei));
                                g2d.setColor(colors[validChromNum % colors.length]);
                            }

                        }
                    }
                }
            }
            validChromNum++;
        }
        g2d.setStroke(oldStroke);
        g2d.setColor(oldColor);

        drawAxesOnly(g2d, chromPositions, transformer);

        g2d.dispose();
        //PValuePainter.outputJPEGFile(image, outputPath);
        PValuePainter.outputPNGFile(image, outputPath);
        return "";
    }

    /**
     *
     * @param g2
     */
    private void drawAxes(Graphics2D g2, String title, String XLabel, String yLabel) {
        g2.setColor(axesColor.darker());
        //vertical axis
        Line2D.Double zz = new Line2D.Double(plotScope.getX(),
                (plotScope.getY()), plotScope.getX(), (plotScope.getY() + plotScope.getHeight()));

        g2.draw(zz);
        //no arrow
        // g2.drawLine(plotScope.x, plotScope.y, plotScope.x - 3, plotScope.y + 7);
        //g2.drawLine(plotScope.x, plotScope.y, plotScope.x + 3, plotScope.y + 7);
        Font oldFront = g2.getFont();
        g2.setFont(numFont);

        int str_len = g2.getFontMetrics().stringWidth(yLabel);
        int str_hei = g2.getFontMetrics().getAscent();
        if (str_len > offsetLeft) {
            str_len = offsetLeft;
        }
        //g2.drawString(yLabel, plotScope.x - str_len, plotScope.y + str_hei + 5);

        g2.rotate(-90.0 * Math.PI / 180.0);
        // g2.drawString(yLabel, -(plotScope.height - str_len) / 2-plotScope.y,  str_hei );
        g2.drawString(yLabel, -(plotScope.height + str_len) / 2 - plotScope.y, plotScope.x - str_hei - 15);
        g2.rotate(90.0 * Math.PI / 180.0);
        //horizontal axis
        zz = new Line2D.Double(plotScope.getX(),
                (plotScope.getY() + plotScope.getHeight()),
                (plotScope.getX() + plotScope.getWidth()),
                (plotScope.getY() + plotScope.getHeight()));
        g2.draw(zz);
        //no arrow
        // g2.drawLine(plotScope.x + plotScope.width, plotScope.y + plotScope.height, plotScope.x + plotScope.width - 7, plotScope.y + plotScope.height - 3);
        // g2.drawLine(plotScope.x + plotScope.width, plotScope.y + plotScope.height, plotScope.x + plotScope.width - 7, plotScope.y + plotScope.height + 3);

        str_len = g2.getFontMetrics().stringWidth(XLabel);
        g2.drawString(XLabel, plotScope.x + plotScope.width / 2 - str_len / 2,
                (float) (plotScope.getY() + plotScope.getHeight() + offsetBottom * 2 / 3));
        g2.setFont(oldFront);
        if (title != null) {
            oldFront = g2.getFont();
            g2.setFont(titleFont);
            str_len = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, plotScope.x + plotScope.width / 2 - str_len / 2,
                    (float) (plotScope.getY() + plotScope.getHeight() / 6));
            g2.setFont(oldFront);
        }
    }

    /**
     *
     */
    public class CoordinateTransformer {

        public int screenHorizontalMin, screenVerticalMin, screenHorizontalMax, screenVerticalMax;
        public double dataHorizontalMin, dataVerticalMin, dataHorizontalMax, dataVerticalMax;
        double horizontalResolution, verticalResolution;
        long xOffset = 0;
        long localMaxX = 0;
        long chromosomeOffset = 0;

        public long getChromosomeOffset() {
            return chromosomeOffset;
        }

        public void setChromosomeOffset(long chromosomeOffset) {
            this.chromosomeOffset = chromosomeOffset;
        }

        public long getLocalMaxX() {
            return localMaxX;
        }

        public void setLocalMaxX(long localMaxX) {
            this.localMaxX = localMaxX;
        }

        public long getxOffset() {
            return xOffset;
        }

        public void setxOffset(long xOffset) {
            this.xOffset = xOffset;
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
        // redesign the ploting
        public Point2D chromosomePoint2ScreenPoint(double Xa, double Ya) {
            Point2D myout = new Point2D.Double();
            Xa = xOffset + (Xa - chromosomeOffset);
            /*
             if (Xa > localMaxX)
             {
             Xa = localMaxX;
             }
             *
             */

            myout.setLocation(((Xa - dataHorizontalMin) / horizontalResolution + screenHorizontalMin),
                    (screenVerticalMax - (Ya - dataVerticalMin) / verticalResolution));
            return myout;
        }

        // redesign the ploting
        public Point2D generalData2ScreenPoint(double Xa, double Ya) {
            Point2D myout = new Point2D.Double();
            myout.setLocation(((Xa - dataHorizontalMin) / horizontalResolution + screenHorizontalMin),
                    (screenVerticalMax - (Ya - dataVerticalMin) / verticalResolution));
            return myout;
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

    private void renderPlotBackGroundHorizontalScale(Graphics2D g2, Map<Integer, long[]> chromPositions, CoordinateTransformer transformer) {
        g2.setColor(axesColor.darker());
        String str;
        int scaleLen = 3;
        int verticalScale = 10;
        double vMin = transformer.dataVerticalMin;
        double vMax = transformer.dataVerticalMax;
        double hMin = transformer.dataHorizontalMin;
        double gridLen = (vMax - vMin) / verticalScale;
        int str_hei = g2.getFontMetrics().getAscent();
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        float[] dashes = {3.f};
        BasicStroke dashedStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0);
        Font oldFront = g2.getFont();
        g2.setFont(numFont);

        int counter = 0;
        for (int i = 0; i < CHROM_NAMES.length; i++) {
            long[] postions = chromPositions.get(i);
            if (postions == null || postions[1] == 0) {
                continue;
            }

            Point2D point1 = transformer.generalData2ScreenPoint(postions[0], vMin);
            Point2D point2 = transformer.generalData2ScreenPoint(postions[1], vMax);

            // Point2D point3 = new Point2D.Double(point2.getX(), point2.getY() - scaleLen);
            // Line2D.Double zz = new Line2D.Double(point2, point3);
            // g2.draw(zz);
            str = CHROM_NAMES[i];
            //System.out.println(str + " " + point2.toString());
            counter++;
            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.setColor(Color.BLACK);
            g2.drawString(str, (float) (point1.getX() + point2.getX() - str_len) / 2, (float) (point1.getY() + str_hei));
            // g2.drawString(str, (float) (point2.getX()), (float) (point2.getY() + str_hei));
            if (counter % 2 == 1) {
                g2.setColor(new Color(225, 225, 225));
            } else {
                g2.setColor(Color.WHITE);
            }

            Rectangle2D.Double f = new Rectangle2D.Double(point1.getX(), point2.getY(),
                    point2.getX() - point1.getX(), point1.getY() - point2.getY());
            g2.fill(f);
            g2.draw(f);
        }
        for (int i = 1; i <= verticalScale; i++) {
            g2.setStroke(dashedStroke);
            g2.setColor(new Color(192, 192, 192));
            Point2D point1 = transformer.generalData2ScreenPoint(hMin, vMin + i * gridLen);
            Point2D point2 = transformer.generalData2ScreenPoint(transformer.dataHorizontalMax, vMin + i * gridLen);
            Line2D.Double zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            g2.setStroke(oldStroke);
            g2.setColor(oldColor);
            point2 = new Point2D.Double(point1.getX() + scaleLen, point1.getY());
            zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            str = Util.doubleToString(vMin + i * gridLen, 2);

            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.drawString(str, (float) point1.getX() - str_len,
                    (float) (point1.getY() + str_hei / 2));
        }


        /*
         //draw reference line
         Point2D point1 = transformer.data2ScreenPoint(hMin, baseLinePoint);
         Point2D point2 = transformer.data2ScreenPoint(transformer.dataHorizontalMax, baseLinePoint);
         Line2D.Double zz = new Line2D.Double(point1, point2);
         g2.setColor(Color.GREEN);
         float[] dashes = {3.f};
         g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0));
         g2.draw(zz);
         *
         */
        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
        g2.setFont(oldFront);
    }

    private void drawAxesOnly(Graphics2D g2, Map<Integer, long[]> chromPositions, CoordinateTransformer transformer) {
        g2.setColor(axesColor.darker());
        String str;
        int scaleLen = 3;
        int verticalScale = 10;
        double vMin = transformer.dataVerticalMin;
        double vMax = transformer.dataVerticalMax;
        double hMin = transformer.dataHorizontalMin;
        double gridLen = (vMax - vMin) / verticalScale;
        Font oldFront = g2.getFont();
        g2.setFont(numFont);

        int str_hei = g2.getFontMetrics().getAscent();
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        float[] dashes = {3.f};
        BasicStroke dashedStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0);

        //vertical axis
        Line2D.Double zz = new Line2D.Double(plotScope.getX(),
                (plotScope.getY()), plotScope.getX(), (plotScope.getY() + plotScope.getHeight()));

        g2.draw(zz);
        //horizontal axis
        zz = new Line2D.Double(plotScope.getX(),
                (plotScope.getY() + plotScope.getHeight()),
                (plotScope.getX() + plotScope.getWidth()),
                (plotScope.getY() + plotScope.getHeight()));
        g2.draw(zz);

        for (int i = 0; i < CHROM_NAMES.length; i++) {
            long[] postions = chromPositions.get(i);
            if (postions == null || postions[1] == 0) {
                continue;
            }

            Point2D point1 = transformer.generalData2ScreenPoint(postions[0], vMin);
            Point2D point2 = new Point2D.Double(point1.getX(), point1.getY() + 3);
            zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
        }

        for (int i = 1; i <= verticalScale; i++) {
            Point2D point1 = transformer.generalData2ScreenPoint(hMin, vMin + i * gridLen);
            g2.setStroke(oldStroke);
            g2.setColor(oldColor);
            Point2D point2 = new Point2D.Double(point1.getX() + scaleLen, point1.getY());
            zz = new Line2D.Double(point1, point2);
            g2.draw(zz);
            str = Util.doubleToString(vMin + i * gridLen, 2);

            int str_len = g2.getFontMetrics().stringWidth(str);
            g2.drawString(str, (float) point1.getX() - str_len,
                    (float) (point1.getY() + str_hei / 2));
        }

        /*
         //draw reference line
         Point2D point1 = transformer.data2ScreenPoint(hMin, baseLinePoint);
         Point2D point2 = transformer.data2ScreenPoint(transformer.dataHorizontalMax, baseLinePoint);
         Line2D.Double zz = new Line2D.Double(point1, point2);
         g2.setColor(Color.GREEN);
         float[] dashes = {3.f};
         g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0));
         g2.draw(zz);
         *
         */
        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
        g2.setFont(oldFront);
    }
}
