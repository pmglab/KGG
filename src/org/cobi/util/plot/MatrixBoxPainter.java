/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.plot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.cobi.util.text.Util;
//import org.tc33.jheatchart.HeatChart;

/**
 *
 * @author mxli
 */
public class MatrixBoxPainter extends BasicPainter {

    private double[][] matrix;
    private String[] columnNames;
    private int[][] matrixDataColorIndices;
    private int legendBoxHeight = 150;
    private int legendBoxWidth = 10;
    private boolean drawLegend = true;
    private BufferedImage image = new BufferedImage(canvasWidth,canvasHeight, BufferedImage.TYPE_INT_BGR);
    private Graphics2D g2d = image.createGraphics();
    Color[] gradientColors = new Color[]{Color.white, Color.red};
    Color[] colors = Gradient.createMultiGradient(gradientColors, 500);
    // Sets the value of a single preference for the rendering algorithms.
    double minValue = 0;
    double maxValue = 1;
    double elementWidth = 0;
    double elementHeight = 0;
    int adjFontSize = 0;
//    private BufferedImage ige=new BufferedImage(canvasWidth,canvasHeight, BufferedImage.TYPE_INT_BGR);
//    private Graphics2D g=ige.createGraphics();

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public MatrixBoxPainter(int width, int height) {
        super(width, height);
        // TODO Auto-generated constructor stub
    }

    public void getMatrixMinValue(double[][] matrix) {
        minValue = matrix[0][0];
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                minValue = Math.min(minValue, matrix[x][y]);
            }
        }

    }

    public void getMatrixMaxValue(double[][] matrix) {
        maxValue = matrix[0][0];
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                maxValue = Math.max(maxValue, matrix[x][y]);
            }
        }
    }

    private int drawLegend() {
        Font oldFont = g2d.getFont();
        int minGrid = (int) Math.min(elementWidth, elementHeight);
        adjFontSize = 0;
        int strLen = 0;
        do {
            adjFontSize++;
            Font testFont = new Font("SansSerif", Font.BOLD, adjFontSize);
            g2d.setFont(testFont);
            strLen = g2d.getFontMetrics().stringWidth("1.00+");
        } while (strLen <= legendBoxWidth);
        g2d.setFont(oldFont);

        Font legendFont = new Font("SansSerif", Font.BOLD, adjFontSize);
        g2d.setFont(legendFont);
        int strHei = g2d.getFontMetrics().getAscent();
        String minValueStr = String.valueOf(minValue);
        String maxValueStr = String.valueOf(maxValue) + "+";
        strLen = Math.max(g2d.getFontMetrics().stringWidth(minValueStr), g2d.getFontMetrics().stringWidth(maxValueStr));
        legendBoxWidth = strLen;

        int legentXOffset = canvasWidth - legendBoxWidth - dataPlottingOffsetRight;
        int lengentYOffset = canvasHeight / 2 - dataPlottingOffsetTop - legendBoxHeight / 2;

        g2d.drawRect(legentXOffset, lengentYOffset, legendBoxWidth, legendBoxHeight);
        for (int y = 0; y < legendBoxHeight; y++) {
            //System.out.println("legendLen: " + legendLen);	            	
            int yStart = lengentYOffset + legendBoxHeight - y;
            g2d.setColor(colors[(int) ((y / (double) legendBoxHeight) * (colors.length * 1.0))]);
            g2d.fillRect(legentXOffset, yStart, legendBoxWidth, 1);
            //System.out.println("yStart: " + yStart);
        }


        //legentXOffset = legentXOffset - strLen - 2;
        legentXOffset = canvasWidth - legendBoxWidth / 2 - strLen / 2 - dataPlottingOffsetRight;
        g2d.setColor(Color.black);
        g2d.drawString(maxValueStr, legentXOffset, lengentYOffset);
        g2d.drawString(minValueStr, legentXOffset, lengentYOffset + legendBoxHeight + strHei);
        return strLen;
    }

    private void drawColumnNames(boolean vertial) throws Exception {
        int dim1 = matrix.length;
        int dim2 = matrix[0].length;
        if (dim1 == dim2 && dim1 == columnNames.length) {
            g2d.setColor(Color.black);

            Font indexFont = new Font("SansSerif", Font.BOLD, adjFontSize);
            g2d.setFont(indexFont);
            FontMetrics fm = g2d.getFontMetrics();


            int ascent = fm.getMaxAscent();
            int descent = fm.getMaxDescent();
            int fontHeight = fm.getMaxAscent() + fm.getMaxDescent();
            //  int strHei = g2d.getFontMetrics().getAscent();         

            for (int x = 0; x < columnNames.length; x++) {
                String index = columnNames[x];
                int strWidth = g2d.getFontMetrics().stringWidth(index);
                if (vertial) {
                    int xhCor = (int) (dataPlottingArea.getX() + elementWidth * x + (elementWidth - strWidth) / 2);
                    int yhCor = (int) (dataPlottingArea.getY() - fontHeight / 2);
                    g2d.drawString(index, xhCor, yhCor);

                    g2d.rotate(-90.0 * Math.PI / 180.0);
                    g2d.drawString(index, (int) (-elementHeight * x - (elementHeight) / 2 - strWidth / 2 - dataPlottingArea.y), dataPlottingArea.x - fontHeight / 2);
                    g2d.rotate(90.0 * Math.PI / 180.0);
                } else {
                    int xhCor = (int) (dataPlottingArea.getX() - strWidth - 2);
                    int yhCor = (int) (dataPlottingArea.getY() + ascent + elementHeight * x + (elementHeight - fontHeight) / 2);


                    g2d.drawString(index, xhCor, yhCor);

                    g2d.rotate(-90.0 * Math.PI / 180.0);
                    //strWidth / 2
                    g2d.drawString(index, -(dataPlottingArea.y - 2), (int) (dataPlottingArea.getX() + elementWidth * x + ascent + (elementWidth - fontHeight) / 2));
                    g2d.rotate(90.0 * Math.PI / 180.0);
                }

            }
            // dataPlottingArea.y += (fontHeight + 3);
            //  dataPlottingArea.height -= (fontHeight + 3);
        } else {
            System.err.println("the length of indexes does not keep with input matrix's dimission");
        }

    }

    /**
     * This uses the current array of colors that make up the gradient, and
     * assigns a color index to each data point, stored in the dataColorIndices
     * array, which is used by the drawData() method to plot the points.
     */
    private void setDataColors() {
        //We need to find the range of the data values,
        // in order to assign proper colors.

        double range = maxValue - minValue;

        // dataColorIndices is the same size as the data array
        // It stores an int index into the color array
        matrixDataColorIndices = new int[matrix.length][matrix[0].length];

        //assign a Color to each data point
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                double norm = (matrix[x][y] - minValue) / range; // 0 < norm < 1
                int colorIndex = (int) Math.floor(norm * (colors.length - 1));
                matrixDataColorIndices[x][y] = colorIndex;
            }
        }
    }

    /**
     * public void updateData(double[][] matrix, boolean useGraphicsYAxis) {
     * matrix = new double[matrix.length][matrix[0].length]; for (int ix = 0; ix
     * < matrix.length; ix++) { for (int iy = 0; iy < matrix[0].length; iy++) {
     * // we use the graphics Y-axis internally if (useGraphicsYAxis) {
     * matrix[ix][iy] = matrix[ix][iy]; } else { matrix[ix][iy] =
     * matrix[ix][matrix[0].length - iy - 1]; } } }
     *
     * updateDataColors(); drawData(); }
     */
    private void drawData(boolean drawValue) {
        numFont = new Font("SansSerif", Font.BOLD, adjFontSize);
        g2d.setFont(numFont);
        int strHei = g2d.getFontMetrics().getAscent();

        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {

                if (matrixDataColorIndices[x][y] < 0) {
                    matrixDataColorIndices[x][y] = 0;
                } else if (matrixDataColorIndices[x][y] >= colors.length) {
                    matrixDataColorIndices[x][y] = colors.length - 1;
                }

                g2d.setColor(colors[matrixDataColorIndices[x][y]]);
                double xCor = (dataPlottingArea.getX() + elementWidth * x);
                double yCor = (dataPlottingArea.getY() + elementHeight * y);
                Rectangle2D rect = new Rectangle2D.Double(xCor, yCor, elementWidth, elementHeight);
                g2d.fill(rect);
                //  g2d.fillRect((int) xCor, (int) yCor, (int) elementWidth, (int) elementHeight);
                if (drawValue) {
                    g2d.setColor(Color.black);
                    String num = Util.doubleToString(matrix[x][y], 2);
                    int strWidth = g2d.getFontMetrics().stringWidth(num);
                    g2d.drawString(num, (int) (xCor + (elementWidth - strWidth) / 2), (int) (yCor + (elementHeight + strHei) / 2));
                }

            }
        }

    }

    /**
     * This function will draw matrix with color according to the element's
     * amount
     *
     * @throws Exception
     */
    public void drawMatrixBox(double[][] matrix, String[] names, String title, String outputPath) throws Exception {
        // calculate plotting area, consider two conditions: area with title,
        // area without title        
        if (matrix == null || matrix.length == 0) {
            System.err.println("It's an empty matrix.");
            return;
        }

        this.matrix = matrix;
        this.columnNames = names;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvasBackgroundColor = new Color(0xD3, 0xD3, 0xD3);
        // set BufferedImage, and constrFont oldFront = g2.getFont();uct
        // graphics
        paintCanvas(g2d);
        setDataColors();
        dataPlottingOffsetLeft = 50;
        dataPlottingOffsetTop = 50;
        dataPlottingOffsetBottom = 10;
        dataPlottingOffsetRight = 3;


        legendBoxHeight = canvasHeight / 3;
        legendBoxWidth = 35;


        int legentWordLen = drawLegend();

        if (title != null && title.length() > 0) {
            calculateDataPlottingArea(true, legentWordLen + legendBoxWidth + 3);
        } else {
            calculateDataPlottingArea(false, legentWordLen + legendBoxWidth + 3);
        }

        elementWidth = dataPlottingArea.getWidth() / matrix[0].length;
        elementHeight = dataPlottingArea.getHeight() / matrix.length;

        Font oldFont = g2d.getFont();
        int minGrid = (int) Math.min(elementWidth, elementHeight);
        adjFontSize = 0;
        int strHei = 0;
        do {
            adjFontSize++;
            Font testFont = new Font("SansSerif", Font.BOLD, adjFontSize);
            g2d.setFont(testFont);
            strHei = g2d.getFontMetrics().getAscent();
        } while (strHei <= minGrid / 4);
        g2d.setFont(oldFont);

        drawTitle(g2d, title);
        drawColumnNames(true);
        drawData(true);
        g2d.dispose();
        
//        g.drawImage(image, 0, 0, ige.getWidth(),ige.getHeight(), 0,0,image.getWidth(), image.getHeight(), null);
//        g.dispose();
        
        if (outputPath != null) {
            outputPNGFile(image, outputPath);
        }
    }
    
//    public void drawMatrixPicture(double[][] matrix, String[] names){  
//        
//        BufferedImage bfiPicture = new BufferedImage(canvasWidth,canvasHeight, BufferedImage.TYPE_INT_BGR);      
//        int intMargin=10;
//        int intCellLength=(canvasHeight-51-intMargin*2)/matrix.length;
//        Font font = new Font("sansserif", Font.PLAIN, 18); 
//                    
//        HeatChart hctCorrelation=new HeatChart(matrix);
//        hctCorrelation.setTitle("Correlation Matrix");
//        hctCorrelation.setAxisValuesFont(font);
//        hctCorrelation.setXValues(names);
//        hctCorrelation.setYValues(names);
//        hctCorrelation.setXValuesHorizontal(true);
//        hctCorrelation.setChartMargin(intMargin);
//        hctCorrelation.setCellSize(new Dimension(intCellLength,intCellLength));
//        hctCorrelation.setHighValueColour(Color.RED);
//        hctCorrelation.setLowValueColour(Color.WHITE);
//        hctCorrelation.setBackgroundColour(Color.GRAY);
//        
//        bfiPicture=(BufferedImage) hctCorrelation.getChartImage();    
//        
//        //g2d.drawImage(bfiPicture, canvasWidth, canvasHeight, null);        
//        Graphics2D g2=bfiPicture.createGraphics();
//        g2.setFont(font);
//        g2.setColor(Color.BLACK);
//        for(int i=0;i<matrix.length;i++){
//            for(int j=0;j<matrix.length;j++){
//                g2.drawString(String.valueOf(matrix[i][j]), (float)(intMargin+15+0.25*intCellLength*(4*i+1)), (float)(intMargin+25+0.5*intCellLength*(2*j+1)));
//            }
//        }
//        
////        g2d.drawImage(bfiPicture, 0, 0, canvasWidth, canvasHeight, null);
//        image=new BufferedImage(bfiPicture.getWidth()+50,bfiPicture.getHeight(),BufferedImage.TYPE_INT_BGR);
//        g2d=image.createGraphics();
//        g2d.setColor(Color.GRAY);
//        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
//        g2d.drawImage(bfiPicture, 0, 0, bfiPicture.getWidth(),bfiPicture.getHeight(), 0,0,bfiPicture.getWidth(), bfiPicture.getHeight(), null);
//        Paint paint=new GradientPaint(image.getWidth()-40, 50, Color.RED, image.getWidth()-10,150, Color.WHITE, false);
//        
////        for (int y = 0; y < 100; y++) {	            	
////            g2d.setColor(colors[(int) ((y / 100) * (colors.length * 1.0))]);
////            g2d.fillRect(image.getWidth()-50, 50+y, 30, 1);
////        }
//        
////        g2d.setColor(Color.GREEN);
//        g2d.setPaint(paint);
//        g2d.fillRect(image.getWidth()-40, 50, 30, 100);
//        g2d.setFont(font);
//        g2d.setColor(Color.BLACK);
//        g2d.drawString("1", image.getWidth()-50, 50);
//        g2d.drawString("0", image.getWidth()-50, 150);
//       
//
//        g2d.dispose();        
//        image.flush();
////        ige=image.getScaledInstance(canvasHeight+20, canvasHeight, Image.SCALE_SMOOTH);
////        paintCanvas(g2d);
////        setDataColors();
////        legendBoxHeight = canvasHeight / 3;
////        legendBoxWidth = 35;
////        int legentWordLen = drawLegend();          
//        
//    }

    public static void main(String[] args) throws Exception {
        MatrixBoxPainter test = new MatrixBoxPainter(800, 600);
        double[][] matrix = {{1, 0.179, 0.335},
            {0.179, 1, 0.458},
            {0.335, 0.458, 1,}};
        String[] myIndex = {"T1", "T2", "T3"};
        String outputPath = "./MatrixImageTest.png";
        test.setMaxValue(1);
        test.setMinValue(0);
        test.drawMatrixBox(matrix, myIndex, null, outputPath);
        /*
         double[][] matrix = {{1, 0.227, 0.246, 0.408, 0.278},
         {0.227, 1, 0.492, 0.224, 0.349},
         {0.246, 0.492, 1, 0.284, 0.423},
         {0.408, 0.224, 0.284, 1, 0.393},
         {0.278, 0.349, 0.423, 0.393, 1}};
         * 
         */


        /*
         double[][] matrix = {{1, 0.179, 0.335, 0.532, 0.361},
         {0.179, 1, 0.458, 0.06, 0.355},
         {0.335, 0.458, 1, 0.262, 0.483},
         {0.532, 0.06, 0.262, 1, 0.373},
         {0.361, 0.355, 0.483, 0.373, 1}};
         */

        /*
         String[] myIndex = {"PhyloP ", "SIFT ", "Polyphen2 ", "LRTScore ", "MutationTaster"};
         String title = "Matrix Image";
         String outputPath = "./MatrixImageTest.png";
         test.setMaxValue(1);
         test.setMinValue(0);
         //  test.drawMatrixPicture(matrix, myIndex, null, outputPath);

         //-----read matrix
         String fileName = "plink.genome";
         //String fileName = "all158_prune.genome";
         //String fileName = "gwas76_relatedness2.genome";


         String line = null;
         String[] cells = null;
         Set<String> subIDSet = new HashSet<String>();
         Map<String, Double> pairCorr = new HashMap<String, Double>();
         String finalDelmiliter = "\t";
         int colNum = -1;
         BufferedReader br = new BufferedReader(new FileReader(fileName));
         br.readLine();
         while ((line = br.readLine()) != null) {
         line = line.trim();
         if (line.trim().length() == 0) {
         continue;
         }
         StringTokenizer tokenizer = new StringTokenizer(line);
         if (colNum < 0) {
         colNum = tokenizer.countTokens();
         cells = new String[colNum];
         }
         colNum = 0;
         while (tokenizer.hasMoreTokens()) {
         cells[colNum] = tokenizer.nextToken().trim();
         colNum++;
         }

         if(cells[0].equals("149T")||cells[2].equals("149T")){
         continue;
         }

         if(cells[0].equals("229T")||cells[2].equals("229T")){
         continue;
         }

         if(cells[0].equals("234NT")||cells[2].equals("234NT")){
         continue;
         }

         subIDSet.add(cells[0]);
         subIDSet.add(cells[2]);
         pairCorr.put(cells[0] + "&" + cells[2], Double.parseDouble(cells[9]));
         if (Double.parseDouble(cells[9]) > 0.5) {
         System.out.println(line);
         }
         }
         br.close();
         String[] subIDList = subIDSet.toArray(new String[subIDSet.size()]);
         Arrays.sort(subIDList);
         // String[] subIDList=new String[]{"18","237","264","265"};
         //String[] subIDList=new String[]{"HUMwymXDYAAAPEI-7","HUMwymXCYDCAAPEI-12","HUMwymXBGDCAAPEI-9","HUMwymXCPDCAAPEI-1","HUMwymXAQDCAAPEI-3","HUMwymXAFDCAAPEI-5","HUMwymXDLDCAAPEI-9","HUMwymXBODCAAPEI-2","HUMwymXARDCAAPEI-2","HUMwymXAODCAAPEI-2","HUMwymXCODCABPEI-2","HUMwymXDMDCAAPEI-1","HUMwymXCDDCAAPEI-8","HUMwymXCEDCAAPEI-7","HUMwymXBSDCABPEI-3","HUMwymXBDDCAAPEI-2","HUMwymXDADCAAPEI-5","HUMwymXAHDCAAPEI-12","HUMwymXBUDCAAPEI-3","HUMwymXBEDCAAPEI-4","HUMwymXBCDCBAPEI-1","HUMwymXDRDCAAPEI-7","HUMwymXCXDCAAPEI-9","HUMwymXCLDCAAPEI-8","HUMwymXBBDCAAPEI-8","HUMwymXATDCAAPEI-2","HUMwymXDCDCAAPEI-12","HUMwymXCZDCAAPEI-4","HUMwymXDPDCAAPEI-3","HUMwymXDQDCAAPEI-7","HUMwymXCBDCAAPEI-1","HUMwymXDSDCAAPEI-10","HUMwymXCADCAAPEI-7","HUMwymXDNDCAAPEI-3","HUMwymXCCDCAAPEI-7","HUMwymXBPDCAAPEI-2","HUMwymXASDCAAPEI-2","HUMwymXCMDCAAPEI-9","HUMwymXBNDCAAPEI-2","HUMwymXBFDCAAPEI-8","HUMwymXAGDCAAPEI-9","HUMwymXDGDCAAPEI-8","HUMwymXDWDCAAPEI-7","HUMwymXANDCAAPEI-3","HUMwymXDFDCAAPEI-3","HUMwymXBVDCAAPEI-8","HUMwymXAWDCAAPEI-1","HUMwymXDUDCAAPEI-10","HUMwymXBWDCAAPEI-9","HUMwymXBMDCAAPEI-2","HUMwymXBADCAAPEI-2","HUMwymXCFDCAAPEI-3","HUMwymXAADCAAPEI-4","HUMwymXABDCAAPEI-5","HUMwymXCGDCAAPEI-4","HUMwymXCUDCAAPEI-12","HUMwymXCHDCAAPEI-8","HUMwymXCWDCAAPEI-5","HUMwymXALDCAAPEI-9","HUMwymXAJDCAAPEI-3","HUMwymXDEDCAAPEI-1","HUMwymXCVDCAAPEI-4","HUMwymXBJDCAAPEI-7","HUMwymXBLDCAAPEI-7","HUMwymXDHDCAAPEI-9","HUMwymXCTDCAAPEI-9","HUMwymXCRDCAAPEI-4","HUMwymXBHDCAAPEI-12","HUMwymXAYDCAAPEI-1","HUMwymXACDCAAPEI-9","HUMwymXDIDCAAPEI-1","HUMwymXBIDCAAPEI-7","HUMwymXCIDCAAPEI-9","HUMwymXDVDCAAPEI-10","HUMwymXCJDCAAPEI-2","HUMwymXAKDCAAPEI-8","HUMwymXADDCAAPEI-12","HUMwymXDTDCAAPEI-10","HUMwymXBXDCAAPEI-1","HUMwymXBTDCAAPEI-1","HUMwymXDDDCAAPEI-9","HUMwymXAMDCAAPEI-1","HUMwymXBYDCAAPEI-3","HUMwymXAZDCAAPEI-1","HUMwymXAUDCAAPEI-1","HUMwymXBZDCAAPEI-3","HUMwymXAIDCAAPEI-1","HUMwymXAXDCAAPEI-1","HUMwymXBKDCAAPEI-7","HUMwymXDXAAAPEI-1"};
         myIndex = new String[subIDList.length];
         matrix = new double[subIDList.length][subIDList.length];
         for (int i = 0; i < subIDList.length; i++) {
         myIndex[i] = subIDList[i];
         matrix[i][i] = 1;
         for (int j = i + 1; j < subIDList.length; j++) {
         Double val = pairCorr.get(subIDList[i] + "&" + subIDList[j]);
         if (val == null) {
         val = pairCorr.get(subIDList[j] + "&" + subIDList[i]);
         }
         if (val == null) {
         System.out.println("Warning, no value for " + subIDList[i] + " & " + subIDList[j]);
         }
         matrix[i][j] = val;
         matrix[j][i] = matrix[i][j];
         }
         }

         outputPath = "./MatrixImageRelat.png";
         test.setMaxValue(0.5);
         test.setMinValue(0);
         test.drawMatrixPicture(matrix, myIndex, null, outputPath);
         */
    }

//    public Image getIge() {
//        return ige;
//    }
    
}
