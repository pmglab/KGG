/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.simu;

import java.awt.Color;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Jiang Li
 * This class can draw a line chart based on the given point!
 */
public class DrawPowerChart {
    
    public List<DrawPoint> lstPoint;
    public XYSeriesCollection xyscDataSet;
    public JFreeChart jfcPicture=null;
    
    public int intIndexMAF;
    public int intIndexLD;
    
    public int intRiskNum;
    public int intRiskNumStart;
    public int intRiskNumStep;
    public int intRiskNumEnd;
    
    public double dblSelectedMAF;
    public double dblMAFStart;
    public double dblMAFEnd;
    public double dblMAFStep;    
    
    public double dblSelectedLD;
    public double dblLDStart;
    public double dblLDEnd;
    public double dblLDStep;    

   public DrawPowerChart(List<DrawPoint> lstPoint, int intIndexMAF,int intIndexLD) {
        this.lstPoint = lstPoint;
        this.intIndexMAF=intIndexMAF;
        this.intIndexLD=intIndexLD;
        
        this.xyscDataSet=new XYSeriesCollection();       
    }
   
   public DrawPowerChart(List<DrawPoint> lstPoint, double dblSelectedMAF, double dblSelectedLD){
       this.lstPoint=lstPoint;
       this.dblSelectedLD=dblSelectedLD;
       this.dblSelectedMAF=dblSelectedMAF;
       
       this.xyscDataSet=new XYSeriesCollection();
   }
    
    public void createDataSet(boolean flag){
        //Every elements may be initialized . 
        xyscDataSet.removeAllSeries();
        XYSeries[] xysGATES = new XYSeries[intRiskNum];
        XYSeries[] xysFISHER=new XYSeries[intRiskNum];
        XYSeries[] xysHYTS=new XYSeries[intRiskNum];
        XYSeries[] xysSKAT=new XYSeries[intRiskNum];
        for(int i=0;i<intRiskNum;i++){
            xysGATES[i]=new XYSeries("GATES-"+(intRiskNumStart+i*intRiskNumStep));
            xysFISHER[i]=new XYSeries("ScaChi-"+(intRiskNumStart+i*intRiskNumStep));
            xysHYTS[i]=new XYSeries("HYTS-"+(intRiskNumStart+i*intRiskNumStep));   
            xysSKAT[i]=new XYSeries("SKAT-"+(intRiskNumStart+i*intRiskNumStep));
        }
        
        for(int i=0;i<lstPoint.size();i++){
            DrawPoint dpOne=lstPoint.get(i);
            if(Math.abs(dpOne.dblFreq-dblSelectedMAF)<=1e-4 & Math.abs(dpOne.dblLD-dblSelectedLD)<=1e-4){
                xysGATES[dpOne.intRiskNum].add(dpOne.dblOdd, dpOne.dblGATESPower);
                xysFISHER[dpOne.intRiskNum].add(dpOne.dblOdd,dpOne.dblFISHERPower);
                xysHYTS[dpOne.intRiskNum].add(dpOne.dblOdd, dpOne.dblHYTSPower);
                if(flag)  xysSKAT[dpOne.intRiskNum].add(dpOne.dblOdd,dpOne.dblSKATPower);
            }
        }
        
        for(int i=0;i<intRiskNum;i++){
            xyscDataSet.addSeries(xysGATES[i]);
            xyscDataSet.addSeries(xysFISHER[i]);
            xyscDataSet.addSeries(xysHYTS[i]);
            if(flag)  xyscDataSet.addSeries(xysSKAT[i]);
        }        
    }
    
    public void createChart(boolean flag){
        //Create the chart. 
        jfcPicture= ChartFactory.createXYLineChart("", "Odds", "Power", xyscDataSet, PlotOrientation.VERTICAL, true, true, false);
        //Set the chart. 
        jfcPicture.setBackgroundPaint(Color.white);
        
        XYPlot xypDraw = (XYPlot)jfcPicture.getPlot();
        xypDraw.setBackgroundPaint(Color.lightGray);
        xypDraw.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        xypDraw.setDomainGridlinePaint(Color.white);
        xypDraw.setRangeGridlinePaint(Color.white);
        
        XYLineAndShapeRenderer xylasrDraw = (XYLineAndShapeRenderer)xypDraw.getRenderer();
        
        if(flag){
            for(int i=0;i<xyscDataSet.getSeriesCount();i+=4){
            xylasrDraw.setSeriesPaint(i,Color.RED);
            xylasrDraw.setSeriesPaint(i+1,Color.BLUE);
            xylasrDraw.setSeriesPaint(i+2,Color.GREEN);
            xylasrDraw.setSeriesPaint(i+3,Color.YELLOW);
            
            xylasrDraw.setSeriesShape(i+1, xylasrDraw.lookupSeriesShape(i));  
            xylasrDraw.setSeriesShape(i+2, xylasrDraw.lookupSeriesShape(i));
            xylasrDraw.setSeriesShape(i+3, xylasrDraw.lookupSeriesShape(i));
            }        
        }else{
            for(int i=0;i<xyscDataSet.getSeriesCount();i+=3){
            xylasrDraw.setSeriesPaint(i,Color.RED);
            xylasrDraw.setSeriesPaint(i+1,Color.BLUE);
            xylasrDraw.setSeriesPaint(i+2,Color.GREEN);
            
            xylasrDraw.setSeriesShape(i+1, xylasrDraw.lookupSeriesShape(i));  
            xylasrDraw.setSeriesShape(i+2, xylasrDraw.lookupSeriesShape(i));
            }        
        }

        
        xylasrDraw.setBaseShapesFilled(true);
        xylasrDraw.setBaseShapesVisible(true);
               
    }

    public JFreeChart getJfreechart() {
        return jfcPicture;
    }

    public void setRiskNumber(int intRiskNumStart,int intRiskNumEnd,int intRiskNumStep){
        this.intRiskNumStart=intRiskNumStart;
        this.intRiskNumEnd=intRiskNumEnd;
        this.intRiskNumStep=intRiskNumStep;
        this.intRiskNum=(intRiskNumEnd-intRiskNumStart)/intRiskNumStep+1;
    }

   public void setMAF(double freqStart, double freqEnd, double freqStep) {
        this.dblMAFStart=freqStart;
        this.dblMAFEnd=freqEnd;
        this.dblMAFStep=freqStep;
        this.dblSelectedMAF=freqStart+this.intIndexMAF*freqStep;
    }

   public void setLD(double dblLDStart, double dblLDEnd, double dblLDStep) {
        this.dblLDStart=dblLDStart;
        this.dblLDEnd=dblLDEnd;
        this.dblLDStep=dblLDStep;
        this.dblSelectedLD=dblLDStart+this.intIndexLD*dblLDStep;
    }

    public void setIntIndexMAF(int intIndexMAF) {
        this.intIndexMAF = intIndexMAF;
    }

    public void setIntIndexLD(int intIndexLD) {
        this.intIndexLD = intIndexLD;
    }
    
    public void setMAFandLD(double maf, double ld){
        this.dblSelectedMAF=maf;
        this.dblSelectedLD=ld;
    }
       
}
