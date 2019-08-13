/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.border.CompoundBorder;
import org.cobi.kgg.business.entity.CorrelationBasedLDSparseMatrix;

import org.cobi.kgg.business.entity.SNP;
import org.cobi.kgg.business.entity.SNPPosiComparator;
import org.cobi.kgg.business.entity.Constants;
import org.cobi.kgg.business.entity.CorrelationBasedByteLDSparseMatrix;
import org.cobi.util.plot.Gradient;
import org.cobi.util.text.Util;

/**
 *
 * @author MX Li
 */
public class LDPlotPainter extends JComponent implements MouseListener, MouseMotionListener, Constants {

    int scale = 1;
    private static final int H_BORDER = 30;
    private static final int V_BORDER = 40;
    private static final int TEXT_GAP = 3;
    private static final int GBROWSE_MARGIN = 25;
    private static final int MAX_GBROWSE_WIDTH = 30000;
    private static final int LAST_SELECTION_LEFT = 7;
    private static final int LAST_SELECTION_TOP = 18;
    private static final int BOX_SIZES[] = {50, 24, 12};
    private static final int BOX_RADII[] = {24, 11, 6};
    private static final int TICK_HEIGHT = 8;
    private static final int TICK_BOTTOM = 50;
    private static final int TRACK_BUMPER = 3;
    private static final int TRACK_PALETTE = 50;
    private static final int TRACK_HEIGHT = TRACK_PALETTE + TRACK_BUMPER * 2;
    private static final int TRACK_GAP = 5;
    private int widestMarkerName = 30; //default size
    private int blockDispHeight = 0, infoHeight = 0;
    private int boxSize = BOX_SIZES[scale];
    private int boxRadius = BOX_RADII[scale];
    private int lowX, highX, lowY, highY;
    private int left = H_BORDER;
    private int top = V_BORDER;
    private int clickXShift, clickYShift;
    private Vector displayStrings;
    private final int popupLeftMargin = 12;
    private final Color BG_GREY = new Color(212, 208, 200);
    private BufferedImage gBrowseImage = null;
    BasicStroke thickerStroke = new BasicStroke(1);
    BasicStroke thinnerStroke = new BasicStroke(0.35f);
    BasicStroke fatStroke = new BasicStroke(2.5f);
    float dash1[] = {5.0f};
    BasicStroke dashedFatStroke = new BasicStroke(2.5f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            5.0f, dash1, 0.0f);
    BasicStroke dashedThinStroke = new BasicStroke(0.35f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            5.0f, dash1, 0.0f);
    private Font boxFont = new Font("SansSerif", Font.PLAIN, 12);
    private Font boxSmallFont = new Font("SansSerif", Font.PLAIN, 9);
    private Font markerNumFont = new Font("SansSerif", Font.BOLD, 12);
    private Font markerNameFont = new Font("Default", Font.PLAIN, 12);
    private Font boldMarkerNameFont = new Font("Default", Font.BOLD, 12);
    private Font popupFont = new Font("Monospaced", Font.PLAIN, 12);
    private boolean printMarkerNames = true;
    private boolean forExport = false;
    private int exportStart, exportStop;
    private boolean showWM = false;
    private int zoomLevel = 0;
    private boolean noImage = true;
    private Rectangle wmInteriorRect = new Rectangle();
    private Rectangle wmResizeCorner = new Rectangle(0, 0, -1, -1);
    private Rectangle resizeWMRect = null;
    private Rectangle popupDrawRect = null;
    private BufferedImage worldmap;
    private Dimension chartSize = null;
    private int wmMaxWidth = 0;
    private Rectangle blockRect = null;
    private int blockStartX = 0;
    private double[] alignedPositions;
    private String currentSelection;
    private String lastSelection = new String("");
    List<SNP> snpList;
    //CorrelationBasedLDSparseMatrix ldCorr;
    CorrelationBasedByteLDSparseMatrix ldCorr;
    //gBrowser
    private static double missingThreshold = 0.5;
    private static double spacingThreshold = 0.0;
    private static long gBrowseLeft = 0;
    private static long gBrowseRight = 0;
    static final String[] GB_TYPES = {"gtsh", "mRNA", "recomb", "NT", "DNA"};
    static final String[] GB_OPTS = {"gtsh%201", "mRNA%203", "", "", ""};
    static final String[] GB_OPTS_NAMES = {"HapMap SNPs", "Entrez Genes", "Recombination Rate", "NT Contigs", "DNA/GC Content"};
    static final String GB_DEFAULT_OPTS = GB_OPTS[0] + "+" + GB_OPTS[1];
    static final String GB_DEFAULT_TYPES = GB_TYPES[0] + "+" + GB_TYPES[1];
    public static final String USER_AGENT = "Haploview/" + "4.0" + " Java/" + JAVA_VERSION;
    //public static final String USER_AGENT="";
    String gChrom = "chr1";
    int pvalueIndex = 0;
    String geneAnnotationLabel = "";
    Color[] gradientColors = new Color[]{Color.WHITE, Color.RED};
    Color[] colors = Gradient.createMultiGradient(gradientColors, 101);
    long geneStartPosition;
    long geneEndPosition;
    boolean plotGene = false;

    public boolean isPlotGene() {
        return plotGene;
    }

    public void setPlotGene(boolean plotGene) {
        this.plotGene = plotGene;
    }

    public LDPlotPainter() {
        this.setDoubleBuffered(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        this.setAutoscrolls(true);
        this.setToolTipText("Right-click to show detailed information!");
    }

    public void updateSNPAndLDInfor(List<SNP> snpList, int pVIndex,
            String chrName, CorrelationBasedByteLDSparseMatrix ldCorr,
            String geneAnaLabel, long start, long end) throws Exception {
        this.pvalueIndex = pVIndex;
        this.snpList = snpList;
        this.ldCorr = ldCorr;
        geneStartPosition = start;
        geneEndPosition = end;

        gChrom = "chr" + chrName;
        geneAnnotationLabel = geneAnaLabel;
        this.computePreferredSize();
    }

    public void changeScale(int cl) throws Exception {
        scale = cl;
        boxSize = BOX_SIZES[scale];
        boxRadius = BOX_RADII[scale];
        this.computePreferredSize();
    }

    public void changeColor(Color cl) throws Exception {
        gradientColors = new Color[]{Color.WHITE, cl};
        colors = Gradient.createMultiGradient(gradientColors, 101);
        // this.computePreferredSize();
    }

    private Color makeColor(double rsq) {

        int r, g, b;
        // r = g = b = (int) (255.0 * (1.0 - rsq));



        /*
        //originally it is design to display the dprim in HaploView
        double dprime = rsq;
        
        if (dprime < 0.2) {
        r = 0;
        g = 0;
        b = 127 + (int) ((dprime / 0.2) * 127);
        } else if (dprime < 0.4) {
        r = 0;
        g = (int) (((dprime - 0.2) / 0.2) * 255);
        b = 255;
        } else if (dprime < 0.6) {
        r = 0;
        g = 127 + (int) (((dprime - 0.4) / 0.2) * 127);
        b = 0;
        } else if (dprime < 0.8) {
        r = (int) (((dprime - 0.6) / 0.2) * 255);
        g = 255;
        b = 0;
        } else {
        r = 255;
        g = (int) (((1 - dprime) / 0.2) * 255);
        b = 0;
        }
         */
        // Color boxColor = new Color(r, g, b);
        Color boxColor = colors[ (int) (rsq * 100)];
        return boxColor;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (chartSize == null) {
            return;
        }
        Rectangle visRect = getVisibleRect();

        try {
            //deal with zooming
            if (chartSize.getWidth() > (3 * visRect.width)) {
                showWM = true;
            } else {
                showWM = false;
            }

            boolean printValues = true;

            if (zoomLevel != 0) {
                printValues = false;
            }

            if (scale == 2) {
                printValues = false;
            }
            Graphics2D g2 = (Graphics2D) g;
            Dimension size = getSize();
            Dimension pref = getPreferredSize();
            g2.setColor(BG_GREY);

            //if it's a big dataset, resize properly, if it's small make sure to fill whole background
            if (size.height < pref.height) {
                g2.fillRect(0, 0, pref.width, pref.height);
                setSize(pref);
            } else {
                g2.fillRect(0, 0, size.width, size.height);
            }
            if (snpList == null || snpList.isEmpty() || ldCorr == null || ldCorr.isEmpty()) {
                return;
            }

            if (snpList.size() < 2) {
                //if there zero or only one valid marker
                return;
            }
            g2.setColor(Color.black);


            //okay so this dumb if block is to prevent the ugly repainting
            //bug when loading markers after the data are already being displayed,
            //results in a little off-centering for small datasets, but not too bad.
            if (!forExport) {
                /*
                if (!theData.infoKnown){
                g2.translate((size.width - pref.width) / 2,
                (size.height - pref.height) / 2);
                } else {
                g2.translate((size.width - pref.width) / 2,   0);
                }
                 *
                 */
                g2.translate((size.width - pref.width) / 2, 0);
            }

            FontMetrics boxFontMetrics = g2.getFontMetrics(boxFont);
            FontMetrics boxSmallFontMetrics = g2.getFontMetrics(boxSmallFont);
            int diamondX[] = new int[4];
            int diamondY[] = new int[4];
            Polygon diamond;


            double lineSpan = alignedPositions[alignedPositions.length - 1] - alignedPositions[0];
            long minpos = snpList.get(0).getPhysicalPosition();
            long maxpos = snpList.get(snpList.size() - 1).getPhysicalPosition();
            double spanpos = maxpos - minpos;


            //See http://www.hapmap.org/cgi-perl/gbrowse/gbrowse_img
            //for more info on GBrowse img.
            int imgHeight = 0;
            if (gBrowseImage != null) {
                g2.drawImage(gBrowseImage, H_BORDER - GBROWSE_MARGIN, V_BORDER, this);
                imgHeight = gBrowseImage.getHeight(this) + TRACK_GAP; // get height so we can shift everything down
            }
            left = H_BORDER;
            top = V_BORDER + imgHeight; // push the haplotype display down to make room for gbrowse image.


            if (forExport) {
                left -= exportStart * boxSize;
            }

            FontMetrics metrics;
            int ascent;

            g2.setFont(boldMarkerNameFont);
            metrics = g2.getFontMetrics();
            ascent = metrics.getAscent();

            //the following values are the bounds on the boxes we want to
            //display given that the current window is 'visRect'
            lowX = getBoundaryMarker(visRect.x - clickXShift - (visRect.y + visRect.height - clickYShift)) - 1;
            highX = getBoundaryMarker(visRect.x + visRect.width);
            lowY = getBoundaryMarker((visRect.x - clickXShift) + (visRect.y - clickYShift)) - 1;
            highY = getBoundaryMarker((visRect.x - clickXShift + visRect.width) + (visRect.y - clickYShift + visRect.height));
            if (lowX < 0) {
                lowX = 0;
            }
            if (highX > snpList.size() - 1) {
                highX = snpList.size() - 1;
            }
            if (lowY < lowX + 1) {
                lowY = lowX + 1;
            }
            if (highY > snpList.size()) {
                highY = snpList.size();
            }

            if (forExport) {
                lowX = exportStart;
                lowY = exportStart;
                highX = exportStop;
                highY = exportStop + 1;
            }
            Color green = new Color(0, 170, 0);

            int startWhiteBlock = 5;
            int strLen = metrics.stringWidth(geneAnnotationLabel);
            int strHei = metrics.getHeight();


            int geneBlockHeight = 10;
            //plot gene region
            int relativeGeneLeft = (int) ((minpos - geneStartPosition) / spanpos * lineSpan);
            int relativeGeneRight = (int) ((maxpos - geneEndPosition) / spanpos * lineSpan);

            int annotLeft = left;
            int annotWidth = (int) lineSpan;

            if (plotGene) {
                if (relativeGeneLeft < 0) {
                    annotLeft = left + relativeGeneLeft;
                    annotWidth -= relativeGeneLeft;
                }
                if (relativeGeneRight < 0) {
                    annotWidth -= relativeGeneRight;
                }
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.white);
            g2.fillRect(annotLeft, startWhiteBlock, (int) (annotWidth), strHei + 1);



            // g2.drawString(geneAnnotationLabel, (float) myLeft - strLen / 2, (float) ((top - strHei) / 2));
            g2.setColor(Color.BLACK);
            g2.setStroke(thickerStroke);
            g2.drawString(geneAnnotationLabel, (float) (annotLeft + (annotWidth - strLen) / 2), (float) startWhiteBlock + strHei * 3 / 4);


            if (plotGene) {
                //plot gene region
                g2.drawRect((int) (left + relativeGeneLeft), strHei + startWhiteBlock + 1 + 7, (int) (lineSpan - relativeGeneLeft - relativeGeneRight), geneBlockHeight);
                g2.drawLine(annotLeft, strHei + startWhiteBlock + 1 + 7 + geneBlockHeight / 2, left + relativeGeneLeft, strHei + startWhiteBlock + 1 + 7 + geneBlockHeight / 2);
                g2.drawLine(left + (int) lineSpan - relativeGeneRight, strHei + startWhiteBlock + 1 + 7 + geneBlockHeight / 2, annotLeft + annotWidth, strHei + startWhiteBlock + 1 + 7 + geneBlockHeight / 2);
            }

            top += strHei + 1;
            boolean manhattanPlot = true;
            int manhattanPlotHeight = 100;
            if (manhattanPlot) {
                g2.setColor(Color.BLACK);
                g2.setStroke(thinnerStroke);

                double rectangleSize = 3;
                double minLogP = 1;
                for (int i = 0; i < snpList.size(); i++) {
                    if (minLogP > snpList.get(i).getpValues()[pvalueIndex]) {
                        minLogP = snpList.get(i).getpValues()[pvalueIndex];
                    }
                }
                minLogP = -Math.log10(minLogP);
                if (minLogP < 5) {
                    minLogP = 5;
                }
                double pixPerLogP = manhattanPlotHeight / minLogP;

                //plot anxies
                g2.setStroke(thickerStroke);
                g2.draw(new Line2D.Double(annotLeft, 0, annotLeft, top + manhattanPlotHeight));
                g2.draw(new Line2D.Double(annotLeft + annotWidth, 0, annotLeft + annotWidth, top + manhattanPlotHeight));


                int grid = 5;
                float[] dashes = {3.f};
                BasicStroke dashedStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, dashes, 0);
                Stroke oldStroke = g2.getStroke();
                Color oldColor = g2.getColor();

                int longestStrLen = 0;
                for (int i = 0; i <= grid; i++) {
                    g2.setStroke(dashedStroke);
                    g2.setColor(new Color(192, 192, 192));
                    double yy = (grid - i) * manhattanPlotHeight / grid + top;
                    g2.draw(new Line2D.Double(annotLeft, yy, annotLeft + annotWidth, yy));

                    g2.setStroke(oldStroke);
                    g2.setColor(oldColor);

                    String str = Util.doubleToString(i * minLogP / grid, 2);
                    strLen = metrics.stringWidth(str);
                    if (longestStrLen < strLen) {
                        longestStrLen = strLen;
                    }
                    g2.drawString(str, (float) annotLeft - strLen, (float) (yy + strHei / 2));
                }

                g2.rotate(-90.0 * Math.PI / 180.0);
                strLen = metrics.stringWidth("-Log10(P)");
                // g2.drawString(yLabel, -(plotScope.height - str_len) / 2-plotScope.y,  str_hei );
                g2.drawString("-Log10(P)", -top - (manhattanPlotHeight + strLen) / 2, annotLeft - longestStrLen - 2);
                g2.rotate(90.0 * Math.PI / 180.0);

                for (int i = 0; i < snpList.size(); i++) {
                    double pos = (snpList.get(i).physicalPosition - minpos) / spanpos;
                    double xx = left + lineSpan * pos;
                    // if we're zoomed, use the line color to indicate whether there is extra data available
                    // (since the marker names are not displayed when zoomed)

                    double yy = top + manhattanPlotHeight + Math.log10(snpList.get(i).getpValues()[pvalueIndex]) * pixPerLogP;
                    Rectangle2D.Double f = new Rectangle2D.Double(xx - rectangleSize / 2,
                            yy - rectangleSize / 2, rectangleSize, rectangleSize);
                    g2.fill(f);
                }
                top += manhattanPlotHeight;
            }

            //  top += TRACK_HEIGHT + TRACK_GAP;

            //// draw the marker locations
            g2.setStroke(thinnerStroke);
            g2.setColor(Color.white);
            g2.fill(new Rectangle2D.Double(left + 1, top + 1, lineSpan - 1, TICK_HEIGHT - 1));
            g2.setColor(Color.black);
            g2.draw(new Rectangle2D.Double(left, top, lineSpan, TICK_HEIGHT));



            for (int i = 0; i < snpList.size(); i++) {
                double pos = (snpList.get(i).physicalPosition - minpos) / spanpos;

                double xx = left + lineSpan * pos;
                // if we're zoomed, use the line color to indicate whether there is extra data available
                // (since the marker names are not displayed when zoomed)

                //   if (Chromosome.getMarker(i).getExtra() != null) g2.setColor(green);

                //draw tick
                g2.setStroke(thickerStroke);
                g2.draw(new Line2D.Double(xx, top, xx, top + TICK_HEIGHT));

                //  if (Chromosome.getMarker(i).getExtra() != null) g2.setStroke(thickerStroke);
                // else g2.setStroke(thinnerStroke);
                g2.setStroke(thinnerStroke);
                //draw connecting line
                g2.draw(new Line2D.Double(xx, top + TICK_HEIGHT, left + alignedPositions[i], top + TICK_BOTTOM));
                /*
                if (Chromosome.getMarker(i).getDisplayName().equals(theHV.getChosenMarker())){
                float cornerx = (float)xx-10;
                float cornery = top;
                float xpoints[] = {cornerx,cornerx+20,cornerx+10};
                float ypoints[] = {cornery,cornery,cornery-10};
                GeneralPath triangle = new GeneralPath(GeneralPath.WIND_NON_ZERO,xpoints.length);
                triangle.moveTo(xpoints[0],ypoints[0]);
                for (int index = 1; index < xpoints.length; index++){
                triangle.lineTo(xpoints[index],ypoints[index]);
                }
                triangle.closePath();
                g2.fill(triangle);
                }
                 */
            }


            g2.setColor(Color.black);
            //// draw manhanton plots at marker locations
            for (int i = 0; i < snpList.size(); i++) {
                double pos = (snpList.get(i).physicalPosition - minpos) / spanpos;

                double xx = left + lineSpan * pos;

                // if we're zoomed, use the line color to indicate whether there is extra data available
                // (since the marker names are not displayed when zoomed)

                //   if (Chromosome.getMarker(i).getExtra() != null) g2.setColor(green);

                //draw tick
                g2.setStroke(thickerStroke);
                g2.draw(new Line2D.Double(xx, top, xx, top + TICK_HEIGHT));

                //  if (Chromosome.getMarker(i).getExtra() != null) g2.setStroke(thickerStroke);
                // else g2.setStroke(thinnerStroke);
                g2.setStroke(thinnerStroke);
                //draw connecting line
                g2.draw(new Line2D.Double(xx, top + TICK_HEIGHT, left + alignedPositions[i], top + TICK_BOTTOM));
                g2.setColor(Color.black);
            }

            top += TICK_BOTTOM + TICK_HEIGHT;

            //// draw the marker names
            if (printMarkerNames) {
                widestMarkerName = metrics.stringWidth(snpList.get(0).getRsID());
                for (int x = 1; x < snpList.size(); x++) {
                    int thiswide = metrics.stringWidth(snpList.get(x).getRsID());
                    if (thiswide > widestMarkerName) {
                        widestMarkerName = thiswide;
                    }
                }

                g2.translate(left, top + widestMarkerName);
                g2.rotate(-Math.PI / 2.0);
                for (int x = 0; x < snpList.size(); x++) {
                    g2.setFont(markerNameFont);
                    /*
                    if (theHV != null)
                    {
                    if (snpList.get(x).getRsID().equals(theHV.getChosenMarker())){
                    g2.setColor(Color.white);
                    g2.fillRect(TEXT_GAP,(int)alignedPositions[x] - ascent/2,
                    metrics.stringWidth(snpList.get(x).getRsID()),ascent);
                    g2.setColor(green);
                    g2.drawRect(TEXT_GAP-1,(int)alignedPositions[x] - ascent/2 - 1,
                    metrics.stringWidth(snpList.get(x).getRsID())+1,ascent+1);
                    }
                    }
                     */
                    // if (Chromosome.getMarker(x).getExtra() != null) g2.setColor(green);

                    SNP snp = snpList.get(x);
                    g2.drawString(snp.getRsID(), (float) TEXT_GAP, (float) alignedPositions[x] + ascent / 3);

                    //  g2.drawString(snp.getRsID() + "(" + Util.formatPValue(snp.getpValues()[pvalueIndex]) + ")", (float) TEXT_GAP, (float) alignedPositions[x] + ascent / 3);
                    g2.setColor(Color.black);
                }

                g2.rotate(Math.PI / 2.0);
                g2.translate(-left, -(top + widestMarkerName));

                // move everybody down
                top += (widestMarkerName + TEXT_GAP);
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);


            top += blockDispHeight;
            //// draw the marker numbers
            if (printMarkerNames) {
                g2.setFont(markerNumFont);
                metrics = g2.getFontMetrics();
                ascent = metrics.getAscent();
                /*
                for (int x = 0; x <  snpList.size(); x++) {
                String mark = String.valueOf(Chromosome.realIndex[x] + 1);
                g2.drawString(mark,
                (float)(myLeft + alignedPositions[x] - metrics.stringWidth(mark)/2),
                (float)(top + ascent));
                }*/

                top += boxRadius / 2; // give a little space between numbers and boxes
            }

            //clickxshift and clickyshift are used later to translate from x,y coords
            //to the pair of markers comparison at those coords
            clickXShift = left + (size.width - pref.width) / 2;
            clickYShift = top;
            /*
            if (!(theData.infoKnown))
            {
            clickXShift = left + (size.width-pref.width)/2;
            clickYShift = top + (size.height - pref.height)/2;
            } else {
            clickXShift = left + (size.width-pref.width)/2;
            clickYShift = top;
            }
             */


            // draw table column by column
            for (int x = lowX; x < highX; x++) {
                //always draw the fewest possible boxes
                if (lowY < x + 1) {
                    lowY = x + 1;
                }

                for (int y = lowY; y < highY; y++) {
                    double r = ldCorr.getLDAt(snpList.get(x).physicalPosition, snpList.get(y).physicalPosition);
                    //double l = dPrimeTable.getLDStats(x,y).getLOD();
                    Color boxColor = makeColor(r);

                    // draw markers above
                    int xx = left + (int) ((alignedPositions[x] + alignedPositions[y]) / 2);
                    int yy = top + (int) ((alignedPositions[y] - alignedPositions[x]) / 2);

                    diamondX[0] = xx;
                    diamondY[0] = yy - boxRadius;
                    diamondX[1] = xx + boxRadius;
                    diamondY[1] = yy;
                    diamondX[2] = xx;
                    diamondY[2] = yy + boxRadius;
                    diamondX[3] = xx - boxRadius;
                    diamondY[3] = yy;

                    diamond = new Polygon(diamondX, diamondY, 4);
                    g2.setColor(boxColor);
                    g2.fillPolygon(diamond);

                    if (printValues) {
                        g2.setFont(boxFont);
                        ascent = boxFontMetrics.getAscent();
                        int val = (int) (r * 100);

                        if (boxColor.getGreen() < 175 && boxColor.getBlue() < 175 && boxColor.getRed() < 175) {
                            g2.setColor(Color.white);
                        } else {
                            g2.setColor((val < 50) ? Color.gray : Color.black);
                        }
                        if (val != 100) {
                            String valu = String.valueOf(val);
                            int widf = boxFontMetrics.stringWidth(valu);
                            g.drawString(valu, xx - widf / 2, yy + ascent / 2);
                        } else {
                            g2.setFont(boxSmallFont);
                            String valu = String.valueOf(val);

                            int widf = boxSmallFontMetrics.stringWidth(valu);
                            g.drawString(valu, xx - widf / 2, yy + ascent / 2);
                            g2.setFont(boxFont);
                        }
                    }
                }
            }

            //highlight blocks
            g2.setFont(markerNameFont);
            ascent = g2.getFontMetrics().getAscent();
            //g.setColor(new Color(153,255,153));
            g2.setColor(Color.black);
            //g.setColor(new Color(51,153,51));

            g2.setStroke(thickerStroke);

            if (showWM && !forExport) {
                //dataset is big enough to require worldmap
                if (wmMaxWidth == 0) {
                    wmMaxWidth = visRect.width / 3;
                }
                double scalefactor;
                scalefactor = (double) (chartSize.width) / wmMaxWidth;
                double prefBoxSize = boxSize / (scalefactor * ((double) wmMaxWidth / (double) (wmMaxWidth)));

                //stick WM_BD in the middle of the blank space at the top of the worldmap
                final int WM_BD_GAP = (int) (infoHeight / (scalefactor * 2));
                final int WM_BD_HEIGHT = 2;
                CompoundBorder wmBorder = new CompoundBorder(BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createLoweredBevelBorder());

                if (noImage) {
                    //first time through draw a worldmap if dataset is big:
                    worldmap = new BufferedImage((int) (chartSize.width / scalefactor) + wmBorder.getBorderInsets(this).left * 2,
                            (int) (chartSize.height / scalefactor) + wmBorder.getBorderInsets(this).top * 2,
                            BufferedImage.TYPE_3BYTE_BGR);

                    Graphics gw = worldmap.getGraphics();
                    Graphics2D gw2 = (Graphics2D) (gw);
                    gw2.setColor(BG_GREY);
                    gw2.fillRect(1, 1, worldmap.getWidth() - 1, worldmap.getHeight() - 1);
                    //make a pretty border
                    gw2.setColor(Color.black);

                    wmBorder.paintBorder(this, gw2, 0, 0, worldmap.getWidth(), worldmap.getHeight());
                    wmInteriorRect = wmBorder.getInteriorRectangle(this, 0, 0, worldmap.getWidth(), worldmap.getHeight());

                    float[] smallDiamondX = new float[4];
                    float[] smallDiamondY = new float[4];
                    GeneralPath gp;
                    for (int x = 0; x < snpList.size() - 1; x++) {
                        for (int y = x + 1; y < snpList.size(); y++) {

                            double rsq = ldCorr.getLDAt(snpList.get(x).physicalPosition, snpList.get(y).physicalPosition);
                            double xx = ((alignedPositions[y] + alignedPositions[x]) / (scalefactor * 2))
                                    + wmBorder.getBorderInsets(this).left;
                            double yy = ((alignedPositions[y] - alignedPositions[x] + infoHeight * 2) / (scalefactor * 2))
                                    + wmBorder.getBorderInsets(this).top;

                            smallDiamondX[0] = (float) xx;
                            smallDiamondY[0] = (float) (yy - prefBoxSize / 2);
                            smallDiamondX[1] = (float) (xx + prefBoxSize / 2);
                            smallDiamondY[1] = (float) yy;
                            smallDiamondX[2] = (float) xx;
                            smallDiamondY[2] = (float) (yy + prefBoxSize / 2);
                            smallDiamondX[3] = (float) (xx - prefBoxSize / 2);
                            smallDiamondY[3] = (float) yy;

                            gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD, smallDiamondX.length);
                            gp.moveTo(smallDiamondX[0], smallDiamondY[0]);
                            for (int i = 1; i < smallDiamondX.length; i++) {
                                gp.lineTo(smallDiamondX[i], smallDiamondY[i]);
                            }
                            gp.closePath();

                            gw2.setColor(makeColor(rsq));
                            gw2.fill(gp);

                        }
                    }
                    noImage = false;
                }

                //draw block display in worldmap
                Graphics gw = worldmap.getGraphics();
                Graphics2D gw2 = (Graphics2D) (gw);
                gw2.setColor(BG_GREY);
                gw2.fillRect(wmBorder.getBorderInsets(this).left,
                        wmBorder.getBorderInsets(this).top + WM_BD_GAP,
                        wmInteriorRect.width,
                        WM_BD_HEIGHT);
                gw2.setColor(Color.black);
                boolean even = true;

                wmResizeCorner = new Rectangle(visRect.x + worldmap.getWidth() - (worldmap.getWidth() - wmInteriorRect.width) / 2,
                        visRect.y + visRect.height - worldmap.getHeight(),
                        (worldmap.getWidth() - wmInteriorRect.width) / 2,
                        (worldmap.getHeight() - wmInteriorRect.height) / 2);

                g2.drawImage(worldmap, visRect.x,
                        visRect.y + visRect.height - worldmap.getHeight(),
                        this);
                wmInteriorRect.x = visRect.x + (worldmap.getWidth() - wmInteriorRect.width) / 2;
                wmInteriorRect.y = visRect.y + visRect.height - worldmap.getHeight()
                        + (worldmap.getHeight() - wmInteriorRect.height) / 2;

                //draw the outline of the viewport
                g2.setColor(Color.black);
                double hRatio = wmInteriorRect.getWidth() / pref.getWidth();
                double vRatio = wmInteriorRect.getHeight() / pref.getHeight();
                int hBump = worldmap.getWidth() - wmInteriorRect.width;
                int vBump = worldmap.getHeight() - wmInteriorRect.height;
                //bump a few pixels to avoid drawing on the border
                g2.drawRect((int) (visRect.x * hRatio) + hBump / 2 + visRect.x,
                        (int) (visRect.y * vRatio) + vBump / 2 + (visRect.y + visRect.height - worldmap.getHeight()),
                        (int) (visRect.width * hRatio),
                        (int) (visRect.height * vRatio));
            }


            //see if the user has myRight-clicked to popup some marker info
            if (popupDrawRect != null) {

                //dumb bug where little datasets popup the box in the wrong place
                int smallDatasetSlopH = 0;
                int smallDatasetSlopV = 0;
                if (pref.getHeight() < visRect.height) {
                    smallDatasetSlopV = (int) (visRect.height - pref.getHeight()) / 2;
                }
                if (pref.getWidth() < visRect.width) {
                    smallDatasetSlopH = (int) (visRect.width - pref.getWidth()) / 2;
                }

                g2.setColor(Color.white);
                g2.fillRect(popupDrawRect.x + 1 - smallDatasetSlopH,
                        popupDrawRect.y + 1 - smallDatasetSlopV,
                        popupDrawRect.width - 1,
                        popupDrawRect.height - 1);
                g2.setColor(Color.black);
                g2.drawRect(popupDrawRect.x - smallDatasetSlopH,
                        popupDrawRect.y - smallDatasetSlopV,
                        popupDrawRect.width,
                        popupDrawRect.height);

                g.setFont(popupFont);
                for (int x = 0; x < displayStrings.size(); x++) {
                    g.drawString((String) displayStrings.elementAt(x), popupDrawRect.x + popupLeftMargin - smallDatasetSlopH,
                            popupDrawRect.y + ((x + 1) * metrics.getHeight()) - smallDatasetSlopV);
                }
            }


            // draw the cached last myRight-click selection
            // The purpose of testing for empty string is just to avoid an 2-unit empty white box
            if (lastSelection != null) {
                if ((zoomLevel == 0) && (!lastSelection.equals("")) && (!forExport)) {
                    g2.setFont(boxFont);
                    // a bit extra on all side
                    int last_descent = g2.getFontMetrics().getDescent();
                    int last_box_x = (visRect.x + LAST_SELECTION_LEFT) - 2;
                    int last_box_y = (visRect.y - g2.getFontMetrics().getHeight() + LAST_SELECTION_TOP + last_descent) - 1;
                    int last_box_width = g2.getFontMetrics().stringWidth(lastSelection) + 4;
                    int last_box_height = g2.getFontMetrics().getHeight() + 2;
                    g2.setColor(Color.white);
                    g2.fillRect(last_box_x, last_box_y, last_box_width, last_box_height);
                    g2.setColor(Color.black);
                    g2.drawRect(last_box_x, last_box_y, last_box_width, last_box_height);
                    g2.drawString(lastSelection, LAST_SELECTION_LEFT + visRect.x, LAST_SELECTION_TOP + visRect.y);
                }
            }


            //see if we're drawing a worldmap resize rect
            if (resizeWMRect != null) {
                g2.setColor(Color.black);
                g2.drawRect(resizeWMRect.x,
                        resizeWMRect.y,
                        resizeWMRect.width,
                        resizeWMRect.height);
            }

            //see if we're drawing a block selector rect
            if (blockRect != null) {
                g2.setColor(Color.black);
                g2.setStroke(dashedThinStroke);
                g2.drawRect(blockRect.x, blockRect.y,
                        blockRect.width, blockRect.height);
            }
        } catch (Exception ex) {
        }
    }

    public int getBoundaryMarker(double pos) {
        //if pos is in the array the binarysearch returns the positive index
        //otherwise it returns the negative "insertion index" - 1
        int where = Arrays.binarySearch(alignedPositions, pos);
        if (where >= 0) {
            return where;
        } else {
            return -where - 1;
        }
    }

    public void computePreferredSize() throws Exception {
        this.computePreferredSize(this.getGraphics());
    }

    public void computePreferredSize(Graphics g) throws Exception {
        if (snpList.isEmpty()) {
            //no valid markers so return an empty size
            setPreferredSize(new Dimension(0, 0));
            return;
        }

        Collections.sort(snpList, new SNPPosiComparator());
        //setup marker positions
        //force it to run through the aligner once by setting this val as negative
        double aligned = -1;
        long minpos = snpList.get(0).physicalPosition;
        long maxpos = snpList.get(snpList.size() - 1).physicalPosition;
        double spanpos = maxpos - minpos;
        double[] initialPositions = new double[snpList.size()];
        alignedPositions = new double[snpList.size()];
        double lineSpan = (snpList.size() - 1) * boxSize;

        //keep trying until we've got at least a certain fraction of the markers aligned
        while (aligned < spacingThreshold) {
            double numAligned = 0;
            for (int i = 0; i < initialPositions.length; i++) {
                initialPositions[i] = (lineSpan * ((snpList.get(i).physicalPosition - minpos) / spanpos));
            }
            alignedPositions = doMarkerLayout(initialPositions, lineSpan);
            for (int i = 0; i < initialPositions.length; i++) {
                //if the alignedPos is less than two pixels from the intitialpos we
                //decide that's "close enough" to being aligned
                if (initialPositions[i] == alignedPositions[i]) {
                    numAligned++;
                }
            }
            aligned = numAligned / initialPositions.length;
            //if we haven't finished yet we want to try again with a longer line...
            lineSpan += 0.05 * lineSpan;
        }
        double gblineSpan = alignedPositions[alignedPositions.length - 1] - alignedPositions[0];
        int gbImageHeight = 0;


        //loop through table to find deepest non-null comparison
        int upLim, loLim;
        if (forExport) {
            loLim = exportStart;
            upLim = exportStop;
        } else {
            loLim = 0;
            upLim = snpList.size() - 1;
        }
        double sep = 0;
        for (int x = loLim; x < upLim; x++) {
            for (int y = x + 1; y <= upLim; y++) {
                double r = ldCorr.getLDAt(snpList.get(x).physicalPosition, snpList.get(y).physicalPosition);
                if (r != 0) {
                    if (sep < alignedPositions[y] - alignedPositions[x]) {
                        sep = alignedPositions[y] - alignedPositions[x];
                    }
                }
            }
        }
        //add one so we don't clip bottom box
        sep += boxSize;


        if (g != null) {
            g.setFont(markerNameFont);
            FontMetrics fm = g.getFontMetrics();
            if (printMarkerNames) {
                blockDispHeight = boxSize / 3 + fm.getHeight();
            } else {
                blockDispHeight = boxSize / 3;
            }
        }

        //"high" represents the total height of the panel. "infoheight" is the total height of the
        //header info above the LD plot. When we draw the worldmap we want to nudge the LD plot down
        //by a scaled factor of infoheight so that clicking lines it up properly.
        int high = (int) (sep / 2) + V_BORDER * 2 + blockDispHeight;
        /*
        if (theData.infoKnown)
        {
        infoHeight = TICK_HEIGHT + TICK_BOTTOM + widestMarkerName + TEXT_GAP + gbImageHeight;
        }else{
        infoHeight = 0;
        }
         *
         */
        infoHeight = TICK_HEIGHT + TICK_BOTTOM + widestMarkerName + TEXT_GAP + gbImageHeight;
        // if (theData.trackExists)
        {
            //make room for analysis track at top
            infoHeight += TRACK_HEIGHT + TRACK_GAP;
        }
        high += infoHeight;

        int wide = 2 * H_BORDER + (int) (alignedPositions[upLim] - alignedPositions[loLim]);
        //this dimension is just the area taken up by the dprime chart
        //it is used in drawing the worldmap
        //for other elements add their heights in the next code hunk!
        chartSize = new Dimension(wide, high);


        Rectangle visRect = getVisibleRect();
        //big datasets often scroll way offscreen in zoom-out mode
        //but aren't the full height of the viewport
        if (high < visRect.height && showWM && !forExport) {
            high = visRect.height;
        }
        if (!getPreferredSize().equals(new Dimension(wide, high))) {
            noImage = true;

        }
        setPreferredSize(new Dimension(wide, high));
        if (getParent() != null) {
            JViewport par = (JViewport) getParent();
            //OK, if the resizing involves a dataset which is larger than the visible Rect we need to prod the
            //Viewport into resizing itself. if the resizing is all within the visrect, we don't want to do this
            //because it makes the screen flicker with a double-repaint.
            if (par.getVisibleRect().width < par.getViewSize().width
                    || par.getVisibleRect().height < par.getViewSize().height) {
                par.setViewSize(getPreferredSize());
            }
        }
    }

    public double[] doMarkerLayout(double[] snpPositions, double goalSpan) {
        //create an array for the projected positions, initialized to starting positions
        double spp[] = new double[snpPositions.length];
        System.arraycopy(snpPositions, 0, spp, 0, spp.length);

        /*
        Create some simple structures to keep track of which snps are bumping into each other (and whose
        positions are dependent on each other)
         */
        BitSet[] conflicts = new BitSet[snpPositions.length];
        for (int i = 0; i < conflicts.length; ++i) {
            conflicts[i] = new BitSet();
            conflicts[i].set(i);
        }

        while (true) {
            boolean trouble = false;
            for (int i = 0; i < spp.length - 1; ++i) {

                //if two SNPs are overlapping (i.e. centers are < boxSize apart)
                if (spp[i + 1] - spp[i] < boxSize - .0001) {
                    trouble = true;

                    //update the bump structures .. these two snps now bump (and have positions that are
                    //dependent on each other) .. indicate that in the bump structure
                    int ip = i + 1;
                    conflicts[i].set(ip);
                    conflicts[ip].set(i);

                    //Come up with the full set all snps that are involved in a bump/dependency with either
                    //of these two snps
                    BitSet full = new BitSet();
                    for (int j = 0; j < conflicts[i].size(); ++j) {
                        if (conflicts[i].get(j)) {
                            full.set(j);
                        }
                    }
                    for (int j = 0; j < conflicts[ip].size(); ++j) {
                        if (conflicts[ip].get(j)) {
                            full.set(j);
                        }
                    }

                    /*
                    decide on the bounds of this full set of snps for which a bump problem exists
                    each snp inherits this full set of snps for its bump/dependency structure
                     */
                    int li = -1;
                    int hi = -1;
                    int conflict_count = 0;
                    for (int j = 0; j < full.size(); ++j) {
                        if (full.get(j)) {
                            conflicts[j] = (BitSet) full.clone();
                            if (li == -1) {
                                li = j;
                            }
                            hi = j;
                            conflict_count++;
                        }
                    }

                    //reposition the projected positions of the bumping snps, centered over
                    //the non-projected snp range of that set of snps .. with boundary conditions
                    double total_space_to_be_spanned = boxSize * (conflict_count - 1);
                    double low_point = snpPositions[li];
                    double high_point = snpPositions[hi];
                    double first_snp_proj_pos = low_point - (total_space_to_be_spanned - (high_point - low_point)) / 2;
                    if (first_snp_proj_pos < 0.0) {
                        first_snp_proj_pos = 0.0;
                    }
                    if (first_snp_proj_pos + total_space_to_be_spanned > goalSpan) {
                        first_snp_proj_pos = goalSpan - total_space_to_be_spanned;
                    }
                    for (int j = li; j <= hi; ++j) {
                        spp[j] = first_snp_proj_pos + boxSize * (j - li);
                    }
                    break;
                }
            }
            if (!trouble) {
                break;
            }
        }
        return spp;
    }

    public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
            int clickX = e.getX();
            int clickY = e.getY();
            if (showWM && wmInteriorRect.contains(clickX, clickY)) {
                //convert a click on the worldmap to a point on the big picture
                int bigClickX = (((clickX - getVisibleRect().x - (worldmap.getWidth() - wmInteriorRect.width) / 2)
                        * chartSize.width)
                        / wmInteriorRect.width) - getVisibleRect().width / 2;
                int bigClickY = (((clickY - getVisibleRect().y
                        - (worldmap.getHeight() - wmInteriorRect.height) / 2
                        - (getVisibleRect().height - worldmap.getHeight()))
                        * chartSize.height) / wmInteriorRect.height)
                        - getVisibleRect().height / 2;

                //if the clicks are near the edges, correct values
                if (bigClickX > chartSize.width - getVisibleRect().width) {
                    bigClickX = chartSize.width - getVisibleRect().width;
                }
                if (bigClickX < 0) {
                    bigClickX = 0;
                }
                if (bigClickY > chartSize.height - getVisibleRect().height) {
                    bigClickY = chartSize.height - getVisibleRect().height;
                }
                if (bigClickY < 0) {
                    bigClickY = 0;
                }

                ((JViewport) getParent()).setViewPosition(new Point(bigClickX, bigClickY));
            } else {

                Rectangle2D blockselector = new Rectangle2D.Double(clickXShift - boxRadius, clickYShift - boxRadius,
                        alignedPositions[alignedPositions.length - 1] + boxSize, boxSize);
                if (blockselector.contains(clickX, clickY)) {
                }
            }
        }
    }

    public int getPreciseMarkerAt(double pos) {
        int where = Arrays.binarySearch(alignedPositions, pos);
        if (where >= 0) {
            return where;
        } else {
            int myLeft = -where - 2;
            int myRight = -where - 1;
            if (myLeft < 0) {
                myLeft = 0;
                myRight = 1;
            }
            if (myRight >= alignedPositions.length) {
                myRight = alignedPositions.length - 1;
                myLeft = alignedPositions.length - 1;
            }
            if (Math.abs(alignedPositions[myRight] - pos) < boxRadius) {
                return myRight;
            } else if (Math.abs(pos - alignedPositions[myLeft]) < boxRadius) {
                return myLeft;
            } else {
                //out of bounds
                return -1;
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        Rectangle blockselector = new Rectangle(clickXShift - boxRadius, clickYShift - boxRadius,
                (int) alignedPositions[alignedPositions.length - 1] + boxSize, boxSize);

        //if users myRight clicks & holds, pop up the info
        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            Graphics g = getGraphics();
            g.setFont(popupFont);
            FontMetrics metrics = g.getFontMetrics();

            final int clickX = e.getX();
            final int clickY = e.getY();
            final int boxX, boxY;
            boxX = getPreciseMarkerAt(clickX - clickXShift - (clickY - clickYShift));
            boxY = getPreciseMarkerAt(clickX - clickXShift + (clickY - clickYShift));
            displayStrings = null;

            if ((boxX >= lowX && boxX <= highX)
                    && (boxY > boxX && boxY < highY)
                    && !(wmInteriorRect.contains(clickX, clickY))) {
                displayStrings = new Vector();
                currentSelection = new String("Last Selection: ("); // update the cached value

                displayStrings.add(new String("(" + snpList.get(boxX).getRsID()
                        + ", " + snpList.get(boxY).getRsID() + ")"));
                double sep = (int) ((snpList.get(boxY).getPhysicalPosition()
                        - snpList.get(boxX).getPhysicalPosition()) / 100);
                sep /= 10;
                displayStrings.add(new Double(sep).toString() + " kb");
                currentSelection += snpList.get(boxX).getRsID()
                        + ", " + snpList.get(boxY).getRsID();
                try {
                    displayStrings.add(new String("r-squared: " + ldCorr.getLDAt(snpList.get(boxX).physicalPosition, snpList.get(boxY).physicalPosition)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (blockselector.contains(clickX, clickY)) {
            }
            if (displayStrings != null) {
                int strlen = 0;
                for (int x = 0; x < displayStrings.size(); x++) {
                    if (strlen < metrics.stringWidth((String) displayStrings.elementAt(x))) {
                        strlen = metrics.stringWidth((String) displayStrings.elementAt(x));
                    }
                }
                //edge shifts prevent window from popping up partially offscreen
                int visRightBound = (int) (getVisibleRect().getWidth() + getVisibleRect().getX());
                int visBotBound = (int) (getVisibleRect().getHeight() + getVisibleRect().getY());
                int rightEdgeShift = 0;
                if (clickX + strlen + popupLeftMargin + 5 > visRightBound) {
                    rightEdgeShift = clickX + strlen + popupLeftMargin + 10 - visRightBound;
                }
                int botEdgeShift = 0;
                if (clickY + displayStrings.size() * metrics.getHeight() + 10 > visBotBound) {
                    botEdgeShift = clickY + displayStrings.size() * metrics.getHeight() + 15 - visBotBound;
                }
                int smallDataVertSlop = 0;

                popupDrawRect = new Rectangle(clickX - rightEdgeShift,
                        clickY - botEdgeShift + smallDataVertSlop,
                        strlen + popupLeftMargin + 5,
                        displayStrings.size() * metrics.getHeight() + 10);
                repaint();
            }
        } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK)
                == InputEvent.BUTTON1_MASK) {
            // clear the last selection if the mouse is myLeft clicked
            lastSelection = new String("");

            int x = e.getX();
            int y = e.getY();
            if (blockselector.contains(x, y)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                blockStartX = x;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON3_MASK)
                == InputEvent.BUTTON3_MASK) {
            //remove popped up window
            popupDrawRect = null;

            //cache last selection.
            lastSelection = currentSelection;
            currentSelection = null;

            repaint();
        } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK)
                == InputEvent.BUTTON1_MASK) {
            //resize window once user has ceased dragging
            if (getCursor() == Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)) {
                noImage = true;
                if (resizeWMRect.width > 20) {
                    wmMaxWidth = resizeWMRect.width;
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                resizeWMRect = null;
                repaint();
            }
            if (getCursor() == Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (blockRect != null) {
                    //don't add the block if the dragging was really short, as it was probably just a twitch while clicking
                    if (Math.abs(e.getX() - blockStartX) > boxRadius / 2) {
                        int firstMarker = getPreciseMarkerAt(blockStartX - clickXShift);
                        int lastMarker = getPreciseMarkerAt(e.getX() - clickXShift);
                        //we're moving myLeft to myRight
                        if (blockStartX > e.getX()) {
                            int temp = firstMarker;
                            firstMarker = lastMarker;
                            lastMarker = temp;
                        }
                        //negative results represent starting or stopping the drag in "no-man's land"
                        //so we adjust depending on which side we're on
                        if (firstMarker < 0) {
                            firstMarker = -firstMarker + 1;
                        }
                        if (lastMarker < 0) {
                            lastMarker = -lastMarker;
                        }

                    }
                    blockRect = null;
                    repaint();
                }
            }

        }
    }

    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK)
                == InputEvent.BUTTON1_MASK) {
            //conveniently, we can tell what do do with the drag event
            //based on what the cursor is
            if (getCursor() == Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)) {
                int width = e.getX() - wmInteriorRect.x;
                double ratio = (double) width / (double) worldmap.getWidth();
                int height = (int) (ratio * worldmap.getHeight());

                resizeWMRect = new Rectangle(wmInteriorRect.x + 1,
                        wmInteriorRect.y + wmInteriorRect.height - height,
                        width,
                        height - 1);
                repaint();
            } else if (getCursor() == Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)) {
                Rectangle r = getVisibleRect();

                int xcorner, width;
                if (e.getX() < blockStartX) {
                    if (e.getX() < r.x + 2) {
                        scrollRectToVisible(new Rectangle(r.x - 25, r.y, r.width, 1));
                    }
                    //we're dragging myRight to myLeft, so flip it.
                    xcorner = e.getX() - clickXShift + left;
                    width = blockStartX - e.getX();
                } else {
                    if (e.getX() > r.x + r.width - 2) {
                        scrollRectToVisible(new Rectangle(r.x + 25, r.y, r.width, 1));
                    }
                    xcorner = blockStartX - clickXShift + left;
                    width = e.getX() - blockStartX;
                }
                blockRect = new Rectangle(xcorner, top - boxRadius / 2 - TEXT_GAP,
                        width, boxRadius);
                repaint();
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        //when the user mouses over the corner of the worldmap, change the cursor
        //to the resize cursor
        if (getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
            if (wmResizeCorner.contains(e.getPoint())) {
                setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
            }
        } else if (getCursor() == Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)) {
            if (!(wmResizeCorner.contains(e.getPoint()))) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
