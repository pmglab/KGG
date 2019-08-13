/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.cobi.kgg.business.entity.PlinkDataset;
import org.cobi.kgg.ui.dialog.PowerSimulationFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author mxli
 */
public class Simulator extends SwingWorker<Void, UpdateUI> {

//    Population popu = null;
    int simulationTime = 1;
    int samplingTime = 1000;
    int intCounting = 0;
    double dblRatio;

    String genetModel = "MUL";
    int markerNum = 30;

    double ors = 2;
    double orsStart = ors;
    double orsEnd = ors;
    double orsStep = 0.2;

    double freq = 0;
    double freqStart = freq;
    double freqEnd = freq;
    double freqStep = 0.1;

    double dblLD = 0;
    double dblLDStart = dblLD;
    double dblLDEnd = dblLD;
    double dblLDStep = 0.1;

    int riskNum = 4;
    int riskNumStart = riskNum;
    int riskNumEnd = 10;
    int riskNumStep = 2;

//    double[] genePowerList = new double[2];
    double overallThreshold = 2.5E-6;

    PopuStatSummarizer pps = new PopuStatSummarizer();
    Sampler sampler = new Sampler();
    GenetAssociationAnalyzer analyer = new GenetAssociationAnalyzer();

//    double[][] rsqureMatrix = null;
//    double[][] rsqureMatrixAll = null;
//    double[] testedSNPPower = null;
    double snpThreshold = overallThreshold / markerNum;
//    double[] markerFreqs = null;

    int caseNum = 100;
    int controlNum = 100;

//    String ldRFilePath = null;
    boolean isIndependent = true;
    int intBlockNum = 2;
    int intRR=1;
    ArrayList<Integer> altRiskSNPPosition;

    PowerSimulationFrame psdOne = null;
    int intApplicationMark = -1;
    double dblDiseasePrevalence = 0.1;
//    int intGeneBasedMethod;
    int intMetaAnalysis;
    int intNumOfStudies = 1;
    boolean boolShuffle = false;

    PlinkDataset plinkSet;
    String strVCFFile;
    RealDataSimulationAssistant rdsaPioneer;
    SimulationParameter simpOne;
    int totalTaskAmount;

    DecimalFormat df1 = new DecimalFormat("0.00%");
    DecimalFormat df2 = new DecimalFormat("#.###");

    List<DrawPoint> lstPoint;
//    ArrayList<Population> altPopulation;
//    ArrayList<RischMultipLociModel> altDiseaseModel;
    ArrayList<ArrayList<Integer>> altRiskSNPPositions=new ArrayList<ArrayList<Integer>>();

    int intPopulation;
    boolean boolSKAT=false;
    SKAT skatOne=null;
    int intParallel=1;
    ExecutorService exec = null;

    public Simulator(SimulationParameter simpOne) {

        this.simpOne = simpOne;
        intApplicationMark = simpOne.intApplicationFlag;

        //Self-define
        if (intApplicationMark == 0) {
            markerNum = simpOne.intTotalSNP;
            intBlockNum = simpOne.intBlockNum;
            intRR=simpOne.intRR1;
            freqStart = simpOne.dblMAFStart;
            freqEnd = simpOne.dblMAFEnd;
            freqStep = simpOne.dblMAFStep;
            isIndependent = simpOne.boolSNPIndependenceFlag1;
            if (!isIndependent) {
                dblLDStart = simpOne.dblLDStart;
                dblLDEnd = simpOne.dblLDEnd;
                dblLDStep = simpOne.dblLDStep;
            }
        } else if (intApplicationMark == 1) {
            //RealData-Plink
            plinkSet = new PlinkDataset(simpOne.strFamilyFile, simpOne.strMapFile, simpOne.strBEDFile);
            markerNum = simpOne.intVariant2;
            intRR = simpOne.intRR2;
            isIndependent = simpOne.boolSNPIndependenceFlag2;
            freqStart = freqEnd;
            dblLDStart = dblLDEnd;
        } else {
            //RealData-VCF
            strVCFFile = simpOne.strVCFFile;
            markerNum = simpOne.intVariant3;
            intRR = simpOne.intRR3;
            isIndependent = simpOne.boolSNPIndependenceFlag3;
            freqStart = freqEnd;
            dblLDStart = dblLDEnd;
        }

        //Simulation & Test
        simulationTime = simpOne.intSimulationTimes;
        samplingTime = simpOne.intSamplingTimes;

        intMetaAnalysis = simpOne.intMetaAnalysis;
        intNumOfStudies = simpOne.intNumOfStudies;
        boolShuffle = simpOne.boolShuffle;

        //Risk Variants
        riskNumStart = simpOne.intRiskSNPStart;
        riskNumEnd = simpOne.intRiskSNPEnd;
        riskNumStep = simpOne.intRiskSNPStep;

        orsStart = simpOne.dblOddsStart;
        orsEnd = simpOne.dblOddsEnd;
        orsStep = simpOne.dblOddsStep;

        dblDiseasePrevalence = simpOne.dblDiseasePrevalence;
        if (simpOne.intGeneticModel == 0) {
            genetModel = "ADD";
        } else {
            genetModel = "MUL";
        }
        
//        //Set Risk Allele Positions. 
//        altRiskSNPPosition = simpOne.altRiskSNPPosition;
//        altRiskSNPPositions = new ArrayList<ArrayList<Integer>>();
//        if (boolShuffle) {
//            for (int i = 0; i < intNumOfStudies; i++) {
//                ArrayList<Integer> altRandom=new ArrayList(riskNumEnd);
//                altRandom=this.getRandomNumber(markerNum, intBlockNum, riskNumEnd);
//                altRiskSNPPositions.add(altRandom);
//            }
//        } else {
//            for (int i = 0; i < intNumOfStudies; i++) {
//                altRiskSNPPositions.add(altRiskSNPPosition);
//            }
//        }

//        altDiseaseModel = new ArrayList<RischMultipLociModel>();
//        for (int i = 0; i < intNumOfStudies; i++) {
//            altDiseaseModel.add(new RischMultipLociModel(dblDiseasePrevalence));
//        }
        //Population & Sample 
        intPopulation = simpOne.intPopulation;
        caseNum = simpOne.intCase;
        controlNum = simpOne.intControl;

//        altPopulation = new ArrayList<Population>();
//        for (int i = 0; i < intNumOfStudies; i++) {
//            altPopulation.add(new Population(intPopulation));
//        }
        
        //SKAT
        boolSKAT = simpOne.boolSKAT;
        skatOne=simpOne.skatOne;
        intParallel=simpOne.intParallel;
        
        //Others
//        genePowerList = new double[2];
        overallThreshold = simpOne.dblPValueThreshold;
        pps = new PopuStatSummarizer();
        sampler = new Sampler();
        analyer = new GenetAssociationAnalyzer();

        snpThreshold = simpOne.dblPValueThreshold / markerNum;
//        rsqureMatrix = null;
//        rsqureMatrixAll = new double[markerNum * intBlockNum][markerNum * intBlockNum];
//        testedSNPPower = new double[markerNum * intBlockNum];
//        markerFreqs = new double[markerNum];
        lstPoint = new ArrayList<DrawPoint>();
        psdOne = simpOne.jdOne;
//        RealDataSimulationAssistant rdsaPioneer = null;
    }

    @Override
    protected Void doInBackground() throws Exception {

        try {

            //Set table titles
            String[] strTitleNew = new String[5 + markerNum * intRR];
            strTitleNew[0] = "ID";
            strTitleNew[1] = "Odds Ratio";
            strTitleNew[2] = "Frequence";
            strTitleNew[3] = "Number of Risk Allele";
            strTitleNew[4] = "LD";
            
            ArrayList<Integer> altRiskSNPPosition=null;
            if (intApplicationMark == 0) {
                //Set the risk snp positions. 
                double[][] temp=new double[markerNum][markerNum];
                for(int i=0;i<markerNum;i++)    Arrays.fill(temp[i],0);
                altRiskSNPPosition=getRandomNumber(markerNum,riskNumEnd,temp);                
                //altRiskSNPPosition = getRandomNumber(markerNum, intBlockNum, riskNumEnd);
                if (boolShuffle) {
                    for (int i = 0; i < intNumOfStudies; i++) {
                        ArrayList<Integer> altRandom=new ArrayList(riskNumEnd);
                        //altRandom=this.getRandomNumber(markerNum, intBlockNum, riskNumEnd);
                        altRandom=getRandomNumber(markerNum,riskNumEnd,temp); 
                        altRiskSNPPositions.add(altRandom);
                    }
                } else {
                    for (int i = 0; i < intNumOfStudies; i++) {
                        altRiskSNPPositions.add(altRiskSNPPosition);
                    }
                    String strOutput = "";
                    for (int i = 0; i < altRiskSNPPosition.size(); i++) strOutput+=altRiskSNPPosition.get(i)+" ";
                    simpOne.jdOne.getjTextField20().setText(strOutput);
                }                

                //Make the remaining title. 
                for (int i = 1; i <= markerNum; i++) {
                    for (int j = 0; j < intRR; j++) {
                        strTitleNew[i + j * markerNum + 4] = "P-" + Integer.toString(i + j * markerNum);
                        for (int w = 0; w < altRiskSNPPositions.size(); w++) {
                            if (altRiskSNPPositions.get(w).contains(i)) {
                                strTitleNew[i + j * markerNum + 4] += "*";
                            }
                        }
                    }
                }
                publish(new UpdateUI(0, strTitleNew));
            } else if (intApplicationMark == 1) {
                //Read files and calculate basic information. 
                rdsaPioneer = new RealDataSimulationAssistant(plinkSet, markerNum);
                rdsaPioneer.readSNPFromBimFileByPositions();
                rdsaPioneer.readIndividualFromFamFile();
                rdsaPioneer.readGenotypeFromBedFile();
                rdsaPioneer.calculateMAF();
                if (!isIndependent) {
                    rdsaPioneer.calculateRSquareMatrix();
                    altRiskSNPPosition=getRandomNumber(markerNum,riskNumEnd,rdsaPioneer.getDblLD());
                    if (boolShuffle) {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            ArrayList<Integer> altRandom=new ArrayList(riskNumEnd);
                            altRandom=this.getRandomNumber(markerNum,riskNumEnd,rdsaPioneer.getDblLD());
                            altRiskSNPPositions.add(altRandom);
                        }
                    } else {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            altRiskSNPPositions.add(altRiskSNPPosition);
                        }
                        String strOutput = "";
                        for (int i = 0; i < altRiskSNPPosition.size(); i++) strOutput+=altRiskSNPPosition.get(i)+" ";
                        simpOne.jdOne.getjTextField20().setText(strOutput);
                    }                    
                }else{
                    double[][] temp=new double[markerNum][markerNum];
                    for(int i=0;i<markerNum;i++)    Arrays.fill(temp[i],0);
                    altRiskSNPPosition=getRandomNumber(markerNum,riskNumEnd,temp);
                    if (boolShuffle) {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            ArrayList<Integer> altRandom=new ArrayList(riskNumEnd);
                            altRandom=this.getRandomNumber(markerNum,riskNumEnd,temp);
                            altRiskSNPPositions.add(altRandom);
                        }
                    } else {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            altRiskSNPPositions.add(altRiskSNPPosition);
                        }
                        String strOutput = "";
                        for (int i = 0; i < altRiskSNPPosition.size(); i++) strOutput+=altRiskSNPPosition.get(i)+" ";
                        simpOne.jdOne.getjTextField20().setText(strOutput);
                    }                    
                }

                //Make the remaining title.
                String strNames[] = rdsaPioneer.getSNPNameSet();
                for (int i = 1; i <= markerNum; i++) {
                    for (int j = 0; j < intRR; j++) {
                        strTitleNew[i + j * markerNum + 4] = strNames[i - 1] + "_" + Integer.toString(j);
                        for (int w = 0; w < altRiskSNPPositions.size(); w++) {
                            if (altRiskSNPPositions.get(w).contains(i)) {
                                strTitleNew[i + j * markerNum + 4] += "*";
                            }
                        }
                    }
                }
                publish(new UpdateUI(0, strTitleNew));
            } else {
                //Read files and calculate basic information. 
                rdsaPioneer = new RealDataSimulationAssistant(strVCFFile, markerNum);
                rdsaPioneer.readEverythingFromVcfFile();
                rdsaPioneer.calculateMAF();
                if (!isIndependent) {
                    rdsaPioneer.calculateRSquareMatrix();
                    altRiskSNPPosition=getRandomNumber(markerNum,riskNumEnd,rdsaPioneer.getDblLD());
                    if (boolShuffle) {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            ArrayList<Integer> altRandom=new ArrayList(riskNumEnd);
                            altRandom=this.getRandomNumber(markerNum,riskNumEnd,rdsaPioneer.getDblLD());
                            altRiskSNPPositions.add(altRandom);
                        }
                    } else {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            altRiskSNPPositions.add(altRiskSNPPosition);
                        }
                        String strOutput = "";
                        for (int i = 0; i < altRiskSNPPosition.size(); i++) strOutput+=altRiskSNPPosition.get(i)+" ";
                        simpOne.jdOne.getjTextField20().setText(strOutput);
                    }                    
                }else{
                    double[][] temp=new double[markerNum][markerNum];
                    for(int i=0;i<markerNum;i++)    Arrays.fill(temp[i],0);
                    altRiskSNPPosition=getRandomNumber(markerNum,riskNumEnd,temp);
                    if (boolShuffle) {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            ArrayList<Integer> altRandom=new ArrayList(riskNumEnd);
                            altRandom=this.getRandomNumber(markerNum,riskNumEnd,temp);
                            altRiskSNPPositions.add(altRandom);
                        }
                    } else {
                        for (int i = 0; i < intNumOfStudies; i++) {
                            altRiskSNPPositions.add(altRiskSNPPosition);
                        }
                        String strOutput = "";
                        for (int i = 0; i < altRiskSNPPosition.size(); i++) strOutput+=altRiskSNPPosition.get(i)+" ";
                        simpOne.jdOne.getjTextField20().setText(strOutput);
                    }                    
                }

                //Make the remaining title.
                String strNames[] = rdsaPioneer.getSNPNameSet();
                for (int i = 1; i <= markerNum; i++) {
                    for (int j = 0; j < intRR; j++) {
                        strTitleNew[i + j * markerNum + 4] = strNames[i - 1] + "_" + Integer.toString(j);
                        for (int w = 0; w < altRiskSNPPositions.size(); w++) {
                            if (altRiskSNPPositions.get(w).contains(i)) {
                                strTitleNew[i + j * markerNum + 4] += "*";
                            }
                        }
                    }
                }
                publish(new UpdateUI(0, strTitleNew));
            }

            //Set Progress bar. 
            int n1 = (int) Math.round((orsEnd - orsStart) / orsStep + 1);
            int n2 = (int) Math.round((freqEnd - freqStart) / freqStep + 1);
            int n3 =   ((riskNumEnd - riskNumStart) / riskNumStep + 1);
            int n4 = (int) Math.round((dblLDEnd - dblLDStart) / dblLDStep + 1);
            totalTaskAmount = n1 * n2 * n3 * n4;
            dblRatio = 100.0 / totalTaskAmount;
            publish(new UpdateUI(3, 0));

            //Set thread. 
            exec = Executors.newFixedThreadPool(simpOne.parellelRunNum);
            CompletionService serv = new ExecutorCompletionService(exec);
            int inc = 0;

            //The main procedure tested in different conditions.      
            for (ors = orsStart; ors - orsEnd <= 1e-3; ors += orsStep) {
                for (freq = freqStart; freq - freqEnd <= 1e-3; freq += freqStep) {
                    for (riskNum = riskNumStart; riskNum <= riskNumEnd; riskNum += riskNumStep) {
                        for (dblLD = dblLDStart; dblLD - dblLDEnd <= 1e-3; dblLD += dblLDStep) {

                            //Prepare instant parameters. 
                            Map<String, Double> mapInstant = new HashMap<String, Double>();
                            mapInstant.put("ors", ors);
                            mapInstant.put("freq", freq);
                            mapInstant.put("riskNum", (double) riskNum);
                            mapInstant.put("dblLD", dblLD);

                            SimulationTask taks = new SimulationTask(inc, mapInstant, this);
                            inc++;
                            serv.submit(taks);

                        }//End LD
                    }//End RiskNum
                }//End MAF
            }//End Odds ratio

            for (int index = 0; index < inc; index++) {
                Future task = serv.take();
            }
            exec.shutdown();

            //Draw two picture.    
            psdOne.dpcOne = new DrawPowerChart(lstPoint, psdOne.getDcbmMAF().getElementAt(0), psdOne.getDcbmLD().getElementAt(0));
            psdOne.dpcOne.setRiskNumber(riskNumStart, riskNumEnd, riskNumStep);
            psdOne.dpcOne.createDataSet(boolSKAT);
            psdOne.dpcOne.createChart(boolSKAT);

            JFreeChart jfcPlot = psdOne.dpcOne.getJfreechart();
            ChartPanel cplCanvas = new ChartPanel(jfcPlot);
            psdOne.getjPanel1().removeAll();
            psdOne.getjPanel1().setLayout(new java.awt.BorderLayout());
            psdOne.getjPanel1().add(cplCanvas, BorderLayout.CENTER);
            psdOne.getjPanel1().validate();

            if (intApplicationMark == 0) {
                psdOne.getjComboBox2().setEnabled(true);
                psdOne.getjComboBox3().setEnabled(true);
            }

            //Set Progress Bar. 
            psdOne.getjProgressBar1().setValue(100);
            psdOne.getjProgressBar1().setString(df1.format(1));

            //Update the talbe
            psdOne.aomSNPModel.fireTableDataChanged();
            psdOne.getjTable1().scrollRectToVisible(psdOne.getjTable1().getCellRect(psdOne.getjTable1().getRowCount() - 1, 0, true));
            psdOne.aomGeneModel.fireTableDataChanged();
            psdOne.getjTable2().scrollRectToVisible(psdOne.getjTable2().getCellRect(psdOne.getjTable2().getRowCount() - 1, 0, true));
            
            //Close snow link. 
            if(boolSKAT){
                skatOne.closeRServe();
            }

        } catch (InterruptedException e) {
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    protected void process(List<UpdateUI> chunks) {
        //0:title  1:snp-table  2:gene-table 3:progressbar
        for (UpdateUI message : chunks) {
            if (message.intIndex == 0) {
                psdOne.aomSNPModel.setTitle(message.strItems);
            } else if (message.intIndex == 1) {
                synchronized (psdOne.lstSNPPower) {
                    if (psdOne.lstSNPPower.isEmpty()) {
                        psdOne.lstSNPPower.add(message.strItems);
                    } else {
                        if (message.intTableItemIndex > Integer.parseInt(psdOne.lstSNPPower.get(psdOne.lstSNPPower.size() - 1)[0])) {
                            psdOne.lstSNPPower.add(message.strItems);
                           // continue;
                        }
                        int i = 0;
                        for (; i < psdOne.lstSNPPower.size(); i++) {
                            int num = Integer.parseInt(psdOne.lstSNPPower.get(i)[0]);
                            if (message.intTableItemIndex < num) {
                                psdOne.lstSNPPower.add(i, message.strItems);
                                break;
                            }
                        }
                    }

                    psdOne.aomSNPModel.fireTableStructureChanged();
                    psdOne.aomSNPModel.fireTableDataChanged();
                    psdOne.getjTable1().scrollRectToVisible(psdOne.getjTable1().getCellRect(psdOne.getjTable1().getRowCount() - 1, 0, true));
                    psdOne.getjTable1().updateUI();
                }
            } else if (message.intIndex == 2) {
                synchronized (psdOne.lstGenePower) {
                    if (psdOne.lstGenePower.isEmpty()) {
                        psdOne.lstGenePower.add(message.strItems);
                    } else {
                        if (message.intTableItemIndex > Integer.parseInt(psdOne.lstGenePower.get(psdOne.lstGenePower.size() - 1)[0])) {
                            psdOne.lstGenePower.add(message.strItems);
                         //   continue;
                        }
                        int i = 0;
                        for (; i < psdOne.lstGenePower.size(); i++) {
                            int num = Integer.parseInt(psdOne.lstGenePower.get(i)[0]);
                            if (message.intTableItemIndex < num) {
                                psdOne.lstGenePower.add(i, message.strItems);
                                break;
                            }
                        }
                    }

                    psdOne.aomGeneModel.fireTableDataChanged();

                    psdOne.getjTable2().scrollRectToVisible(psdOne.getjTable2().getCellRect(psdOne.getjTable2().getRowCount() - 1, 0, true));
                    psdOne.getjTable2().updateUI();
                }
            } else {
                psdOne.getjProgressBar1().setValue((int) message.dblProgress);
                double prop = message.dblProgress / 100;
                int count = (int) (prop * totalTaskAmount);
                psdOne.getjProgressBar1().setString(count + "/" + totalTaskAmount + " (" + df1.format(prop) + ")");
            }
        }
    }

    protected void done() {
        if (isCancelled()) {
            JOptionPane.showMessageDialog(null, "The simulation has been stopped!", "Message", JOptionPane.INFORMATION_MESSAGE);
        }else{
             JOptionPane.showMessageDialog(null, "The simulation has been finished!!", "Message", JOptionPane.INFORMATION_MESSAGE);
             Toolkit.getDefaultToolkit().beep();
        }
    }

    public void Output(UpdateUI uui) {
        this.publish(uui);
    }
    
    public ArrayList<Integer> getRandomNumber(int intNum, int intBlock, int intSelect) {
        ArrayList<Integer> altRandom = new ArrayList(intSelect);
        Random rdmNumber = new Random();
        int intItem;
        int intBlockRegion = intNum / intBlock;
        for (int i = 0; i < intSelect; i++) {
            do {
                intItem = rdmNumber.nextInt(intBlockRegion) + 1 + (i % intBlock) * intBlockRegion;
            } while (altRandom.contains(intItem));
            altRandom.add(intItem);
        }
        return altRandom;
    }
    
    public ArrayList<Integer> getRandomNumber(int intNum, int intSelect, double[][] LD){
        ArrayList<Integer> altRandom = new ArrayList(intSelect);
        double rStart=0.01;
        double rStep=0.05;
        Random rdmNumber=new Random();
        boolean[] boolFlag=new boolean[intNum];
        
        for(int i=0;i<boolFlag.length;i++)   boolFlag[i]=false;
        for(int i=0;i<intSelect;i++){
            boolean[] boolFlag2=Arrays.copyOf(boolFlag, boolFlag.length);
            int intSum=i;
            while(true){                      
                if(intSum==intNum){
                    rStart+=rStep;
                    boolFlag2=Arrays.copyOf(boolFlag, boolFlag.length);
                    intSum=i;
                }
                
                int intItem=rdmNumber.nextInt(intNum);
                if(boolFlag2[intItem])   continue;
                else{
                    boolean boolMarker=false;
                    for(int j=0;j<intNum;j++){
                        if(j==intItem)  continue;
                        if(!boolFlag[j]) continue;
                        if(LD[intItem][j]>Math.sqrt(rStart))    boolMarker=true;
                    }
                    if(boolMarker){
                        boolFlag2[intItem]=true;
                        intSum++;
                        continue;
                    }else{
                        altRandom.add(intItem+1);
                        boolFlag[intItem]=true;                        
                        break;
                    }
                }                              
            }
        }
        return altRandom;
    }
}
