/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.kgg.business.simu;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.cobi.util.thread.Task;
import org.openide.util.Exceptions;

/**
 *
 * @author mxli
 */
public class SimulationTask extends Task implements Callable<String> {

    //Instant parameter. 
    double ors;
    double freq;
    int riskNum;
    double dblLD;

    //Constant parameter. 
    Simulator simuOne;

    //Control parameter.  
    int testIndex;

    //Dependent parameter. 
    double[][] rsqureMatrix = null;
    double[][] rsqureMatrixAll = null;
    double[] testedSNPPower = null;
    double[] markerFreqs = null;
    double[] genePowerList = null;
    double skatPower;

    //Independent parameter. 
    int markerNum;
    int intNumOfStudies;
    int intApplicationMark;
    int intRR;
    int intBlockNum;
    boolean isIndependent;
    boolean boolSKAT;
    SKAT skatOne;
    DecimalFormat df2 = new DecimalFormat("#.###");
    ArrayList<Population> altPopulation;
    ArrayList<RischMultipLociModel> altDiseaseModel;

    SimulationTask(int intCounting, Map<String, Double> mapInstant, Simulator simuOne) {
        this.testIndex = intCounting;

        this.ors = mapInstant.get("ors");
        this.freq = mapInstant.get("freq");
        this.riskNum = mapInstant.get("riskNum").intValue();
        this.dblLD = mapInstant.get("dblLD");
        this.simuOne = simuOne;
    }

    @Override
    public String call() throws Exception {
        try {
            System.out.println("Test " + testIndex);
            //Whether to stop. 
            if (simuOne.isCancelled()) {
                simuOne.exec.shutdown();
                return "The program is stopped. ";
            }

            //Initial some variables. 
            markerNum = simuOne.markerNum;
            intNumOfStudies = simuOne.intNumOfStudies;
            intApplicationMark = simuOne.intApplicationMark;
            intRR = simuOne.intRR;
            intBlockNum = simuOne.intBlockNum;
            isIndependent = simuOne.isIndependent;
            markerFreqs = new double[markerNum];
            rsqureMatrixAll = new double[markerNum * intRR][markerNum * intRR];
            testedSNPPower = new double[markerNum * intRR];
            genePowerList = new double[3];            
            boolSKAT=simuOne.boolSKAT;
            skatOne=simuOne.skatOne;
            skatPower=0.0;

            altPopulation = new ArrayList<Population>();
            for (int i = 0; i < intNumOfStudies; i++) {
                altPopulation.add(new Population(simuOne.intPopulation));
            }

            altDiseaseModel = new ArrayList<RischMultipLociModel>();
            for (int i = 0; i < intNumOfStudies; i++) {
                altDiseaseModel.add(new RischMultipLociModel(simuOne.dblDiseasePrevalence));
            }

            //Get LD matrix depend on different situation. 
            if (intApplicationMark == 0) {

                //Calculate Matrix. 
                if (isIndependent) {
                    rsqureMatrix = new double[markerNum][markerNum];
                    for (int i = 0; i < markerNum; i++) {
                        Arrays.fill(rsqureMatrix[i], 0);
                        rsqureMatrix[i][i] = freq;
                        markerFreqs[i] = freq;
                    }
                } else {
                    //Design indicator vector
                    int[] intTeamIndex = new int[markerNum];
                    int intTeamNumber = markerNum / intBlockNum;
                    Arrays.fill(intTeamIndex, intBlockNum - 1);
                    int intTeam = 0;
                    for (int i = 1; i <= intTeamNumber * (intBlockNum - 1); i++) {
                        intTeamIndex[i - 1] = intTeam;
                        if (i % intTeamNumber == 0) {
                            intTeam++;
                        }
                    }

                    //Fill r-squared matrix. 
                    rsqureMatrix = new double[markerNum][markerNum];
                    for (int i = 0; i < markerNum; i++) {
                        for (int j = 0; j < markerNum; j++) {
                            if (intTeamIndex[i] == intTeamIndex[j]) {
                                rsqureMatrix[i][j] = dblLD;
                            } else {
                                rsqureMatrix[i][j] = 0;
                            }
                        }
                        rsqureMatrix[i][i] = freq;
                    }

                    DoubleMatrix2D dm2JP = simuOne.pps.convertLDR2JointProbability(rsqureMatrix);

                    for (int w = 0; w < intNumOfStudies; w++) {
                        altPopulation.get(w).calculateGenotypeCovarianceMatrix(dm2JP);
                    }
                }
            } else if (simuOne.intApplicationMark == 1) {

                //Get allelic frequence. 
                List<Double> altMAF = simuOne.rdsaPioneer.getLstMAF();

                //Construct r-square matrix. 
                if (simuOne.isIndependent) {
                    rsqureMatrix = new double[markerNum][markerNum];
                    for (int i = 0; i < markerNum; i++) {
                        Arrays.fill(rsqureMatrix[i], 0);
                        rsqureMatrix[i][i] = altMAF.get(i);
                        markerFreqs[i] = altMAF.get(i);
                    }
                } else {
                    rsqureMatrix = simuOne.rdsaPioneer.getDblLD();
                    for (int i = 0; i < markerNum; i++) {
                        rsqureMatrix[i][i] = altMAF.get(i);
                    }
                    DoubleMatrix2D dm2JP = simuOne.pps.convertLDR2JointProbability(rsqureMatrix);
                    for (int w = 0; w < intNumOfStudies; w++) {
                        altPopulation.get(w).calculateGenotypeCovarianceMatrix(dm2JP);
                    }
                }
            } else {

                //Get allelic frequence. 
                List<Double> altMAF = simuOne.rdsaPioneer.getLstMAF();

                if (simuOne.isIndependent) {
                    rsqureMatrix = new double[markerNum][markerNum];
                    for (int i = 0; i < markerNum; i++) {
                        Arrays.fill(rsqureMatrix[i], 0);
                        rsqureMatrix[i][i] = altMAF.get(i);
                        markerFreqs[i] = altMAF.get(i);
                    }
                } else {
                    rsqureMatrix = simuOne.rdsaPioneer.getDblLD();
                    for (int i = 0; i < markerNum; i++) {
                        rsqureMatrix[i][i] = altMAF.get(i);
                    }
                    DoubleMatrix2D dm2JP = simuOne.pps.convertLDR2JointProbability(rsqureMatrix);//The upper triangle matrix is used. 
                    for (int w = 0; w < intNumOfStudies; w++) {
                        altPopulation.get(w).calculateGenotypeCovarianceMatrix(dm2JP);
                    }
                }
            }

            //Construct a whole matrix when Repeated Region is larger than one. 
            for (int p = 0; p < rsqureMatrixAll.length; p++) {
                for (int q = 0; q < rsqureMatrixAll.length; q++) {
                    if (p / markerNum == q / markerNum) {
                        rsqureMatrixAll[p][q] = rsqureMatrix[p % markerNum][q % markerNum] * rsqureMatrix[p % markerNum][q % markerNum];
                    } else {
                        rsqureMatrixAll[p][q] = 0;
                    }
                    if (p == q) {
                        rsqureMatrixAll[p][q] = 1;
                    }
                }
            }

            //Set susceptible loci. 
            ArrayList<int[]> altSuscepLoci = new ArrayList();
            for (int w = 0; w < intNumOfStudies; w++) {
                int[] suscepLoci = new int[riskNum * intRR];
                for (int j = 0; j < riskNum; j++) {
                    for (int k = 0; k < intRR; k++) {
                        suscepLoci[j + k * riskNum] = simuOne.altRiskSNPPositions.get(w).get(j) - 1 + k * markerNum;
                    }
                }
                altSuscepLoci.add(suscepLoci);
            }

            //Set disease model. 
            for (int w = 0; w < intNumOfStudies; w++) {
                RischMultipLociModel rischModel = altDiseaseModel.get(w);
                rischModel.cleanDiseaseSNPLoci();
                for (int j = 0; j < altSuscepLoci.get(w).length; j++) {
                    int[] suscepLoci = altSuscepLoci.get(w);
                    rischModel.addDiseaseSNPLoci(rsqureMatrix[suscepLoci[j] % riskNum][suscepLoci[j] % riskNum], true, suscepLoci[j]);
                }
                if (simuOne.genetModel.equals("ADD")) {
                    rischModel.calculateAdditiveModelParams(ors);
                } else if (simuOne.genetModel.equals("MUL")) {
                    rischModel.calculateMultiplicativeModelParams(ors);
                } else {
                    System.err.println("Unrecognisable disease model: " + simuOne.genetModel);
                }
            }

            //Initialize genotype. 
            for (int w = 0; w < intNumOfStudies; w++) {
                altPopulation.get(w).allocateMarkerTraitGenotypeSpaces(markerNum, altSuscepLoci.get(w).length, intRR);
            }

            //Start simulation.            
            for (int k = 0; k < simuOne.simulationTime; k++) {
                Arrays.fill(testedSNPPower, 0.0);
                Arrays.fill(genePowerList, 0.0);
                if(boolSKAT)    skatPower=0.0;

                //Simulate Genotype. 
                if (isIndependent) {
                    for (int w = 0; w < intNumOfStudies; w++) {
                        altPopulation.get(w).simulateIndependentMarkerGenotypes(markerFreqs, intRR);
                    }
                } else {
                    for (int w = 0; w < intNumOfStudies; w++) {
                        altPopulation.get(w).simulateDependentGenotypes(intRR);
                    }
                }

                //Simulate Phenotype. 
                for (int w = 0; w < intNumOfStudies; w++) {
                    altPopulation.get(w).simulatePhenotypRischMultipLociModel(altDiseaseModel.get(w));
                }

                //Divide the population into several part. 
                MetaAnalysis mtaOne = new MetaAnalysis(simuOne.intMetaAnalysis, intNumOfStudies, testedSNPPower.length);
                mtaOne.setRSquareMatrix(rsqureMatrixAll);
                IntArrayList[] caseIndexes = new IntArrayList[intNumOfStudies];
                IntArrayList[] controlIndexes = new IntArrayList[intNumOfStudies];
                for (int j = 0; j < intNumOfStudies; j++) {
                    caseIndexes[j] = new IntArrayList();
                    controlIndexes[j] = new IntArrayList();
                    simuOne.pps.summarizePhenotypeProperty1(altPopulation.get(j).getAllIndiv(), altDiseaseModel.get(j).getDiseaseSNPs(), caseIndexes[j], controlIndexes[j]);

                }
                //Repeat sampling.         
                ArrayList<ArrayList<int[][]>> altSKATs=new ArrayList();
                for (int i = 0; i < simuOne.samplingTime; i++) {
                    ArrayList<double[]> altPValues = new ArrayList();
                    ArrayList<int[][]> altSKAT=new ArrayList();                   
                    for (int j = 0; j < intNumOfStudies; j++) {
                        List<Individual> selectedIndivs = simuOne.sampler.sampleWithoutReplacement(altPopulation.get(j).getAllIndiv(), simuOne.caseNum, simuOne.controlNum, caseIndexes[j], controlIndexes[j]);
                        //Calculate case-control p value. 
                        double[] pValues = simuOne.analyer.allelicAssociationTest(selectedIndivs);
                        altPValues.add(pValues);
                        if(boolSKAT){
                            int[][] Z=simuOne.skatOne.getGenotype(selectedIndivs);
                            altSKAT.add(Z);
                        }
                    }
                    altSKATs.add(altSKAT);
                    
                    //Calculate SKAT p value. 
                    if(boolSKAT){                            
                        if((i+1)%simuOne.intParallel==0 | (i+1)==simuOne.samplingTime){
                            ArrayList<Double> skatP=simuOne.skatOne.getPValueParallel(altSKATs,altSKATs.size(),intNumOfStudies);
                            if(simuOne.intMetaAnalysis==0){
                                for(int j=0;j<skatP.size();j++){
                                    if(skatP.get(j)<=simuOne.overallThreshold)    skatPower+=1;
                                }
                            }else{                             
                                for(int j=0;j<intNumOfStudies;j++){
                                    ArrayList<Double> altTemp=new ArrayList();
                                    for(int s=0;s<altSKATs.size();s++){
                                        altTemp.add(skatP.get(s*altSKATs.size()+j));
                                    }
                                    double dblSKATPValue=mtaOne.calculateMetaPValue(altTemp);
                                    if(dblSKATPValue<=simuOne.overallThreshold)    skatPower+=1;
                                }                             
                            }
                            altSKATs.clear();
                        }                        
                    }

                    //Calculate p values in different situation.                                    
                    if (simuOne.intMetaAnalysis == 0) {
                        //Count snp-based p values. 
                        double[] dblPValues = new double[testedSNPPower.length];
                        dblPValues = altPValues.get(0);
                        for (int z = 0; z < dblPValues.length; z++) {
                            if (dblPValues[z] < simuOne.snpThreshold) {
                                testedSNPPower[z]++;
                            }
                        }

                        //Count gene-based p values. 
                        double[] dblGATESandFISHERandHYTS = mtaOne.calculateGeneBasedPValue(dblPValues);
                        if (dblGATESandFISHERandHYTS[0] <= simuOne.overallThreshold) {
                            genePowerList[0] += 1;
                        }
                        if (dblGATESandFISHERandHYTS[1] <= simuOne.overallThreshold) {
                            genePowerList[1] += 1;
                        }
                        if (dblGATESandFISHERandHYTS[2] <= simuOne.overallThreshold) {
                            genePowerList[2] += 1;
                        }
                            
                    } else if (simuOne.intMetaAnalysis == 1) {
                        //Meta analysis at variants. 

                        //Count snp-based p values.
                        double[] dblPValues = new double[testedSNPPower.length];
                        for (int u = 0; u < dblPValues.length; u++) {
                            ArrayList<Double> altTempPValue = new ArrayList();
                            for (int v = 0; v < altPValues.size(); v++) {
                                altTempPValue.add(altPValues.get(v)[u]);
                            }
                            dblPValues[u] = mtaOne.calculateMetaPValue(altTempPValue);
                        }
                        for (int z = 0; z < dblPValues.length; z++) {
                            if (dblPValues[z] < simuOne.snpThreshold) {
                                testedSNPPower[z]++;
                            }
                        }

                        //Count gene-based p values.
                        double[] dblGATESandFISHERandHYTS = mtaOne.calculateGeneBasedPValue(dblPValues);
                        if (dblGATESandFISHERandHYTS[0] <= simuOne.overallThreshold) {
                            genePowerList[0] += 1;
                        }
                        if (dblGATESandFISHERandHYTS[1] <= simuOne.overallThreshold) {
                            genePowerList[1] += 1;
                        }
                        if (dblGATESandFISHERandHYTS[2] <= simuOne.overallThreshold) {
                            genePowerList[2] += 1;
                        }

                    } else {
                         //Meta analysis at genes.    

                        //Count snp-based p values.
                        double[] dblPValues = new double[testedSNPPower.length];
                        for (int u = 0; u < dblPValues.length; u++) {
                            ArrayList<Double> altTempPValue = new ArrayList();
                            for (int v = 0; v < altPValues.size(); v++) {
                                altTempPValue.add(altPValues.get(v)[u]);
                            }
                            dblPValues[u] = mtaOne.calculateMetaPValue(altTempPValue);
                        }
                        for (int z = 0; z < dblPValues.length; z++) {
                            if (dblPValues[z] < simuOne.snpThreshold) {
                                testedSNPPower[z]++;
                            }
                        }

                        //Count gene-based p values.
                        ArrayList<Double> altGATESPValues = new ArrayList();
                        ArrayList<Double> altFISHERValues = new ArrayList();
                        ArrayList<Double> altHYTSPValues = new ArrayList();

                        for (int u = 0; u < altPValues.size(); u++) {
                            double dblPvalue[] = altPValues.get(u);
                            double[] dblGATESandFISHERandHYTS = mtaOne.calculateGeneBasedPValue(dblPvalue);
                            altGATESPValues.add(dblGATESandFISHERandHYTS[0]);
                            altFISHERValues.add(dblGATESandFISHERandHYTS[1]);
                            altHYTSPValues.add(dblGATESandFISHERandHYTS[2]);
                        }
                        double dblGATESPValue = mtaOne.calculateMetaPValue(altGATESPValues);
                        double dblFISHERValue = mtaOne.calculateMetaPValue(altFISHERValues);
                        double dblHYTSPValue = mtaOne.calculateMetaPValue(altHYTSPValues);

                        if (dblGATESPValue <= simuOne.overallThreshold) {
                            genePowerList[0] += 1;
                        }
                        if (dblFISHERValue <= simuOne.overallThreshold) {
                            genePowerList[1] += 1;
                        }
                        if (dblHYTSPValue <= simuOne.overallThreshold) {
                            genePowerList[2] += 1;
                        }                                         
                    }
                }//End sampling Time. 

                //Set Output when finised one situation. 
                //Calculate snp-based power. 
                String[] strTemp = new String[5 + markerNum * intRR];
                strTemp[0] = Integer.toString(testIndex);
                strTemp[1] = df2.format(ors);
                if (intApplicationMark == 0) {
                    strTemp[2] = df2.format(freq);
                } else {
                    strTemp[2] = "Read from file.";
                }
                strTemp[3] = String.valueOf(riskNum);
                if (intApplicationMark == 0) {
                    strTemp[4] = df2.format(dblLD);
                } else {
                    strTemp[4] = "Calculate from file.";
                }

                for (int j = 0; j < markerNum * intRR; j++) {  //calculate snp-based power!
                    testedSNPPower[j]=(testedSNPPower[j]+1)/(simuOne.samplingTime+1);
                    strTemp[5 + j] = String.valueOf(testedSNPPower[j]);
                }

                simuOne.Output(new UpdateUI(1, strTemp, testIndex));  //0:othter  1:snp-table  2:gene-table

                //Calculate gene-based power. 
                strTemp = new String[8+1];
                strTemp[0] = Integer.toString(testIndex);
                strTemp[1] = df2.format(ors);
                if (intApplicationMark == 0) {
                    strTemp[2] = df2.format(freq);
                } else {
                    strTemp[2] = "Read from file.";
                }
                strTemp[3] = String.valueOf(riskNum);
                if (intApplicationMark == 0) {
                    strTemp[4] = df2.format(dblLD);
                } else {
                    strTemp[4] = "Calculate from file.";
                }
                for (int i = 0; i < genePowerList.length; i++) { //calculate gene-based power!
                    genePowerList[i] = (genePowerList[i]+1)/(simuOne.samplingTime+1);
                }
                strTemp[5] = String.valueOf(genePowerList[0]);
                strTemp[6] = String.valueOf(genePowerList[1]);
                strTemp[7] = String.valueOf(genePowerList[2]);
                
                if(boolSKAT){
                    skatPower=(skatPower+1)/(simuOne.samplingTime+1);
                    strTemp[8]=String.valueOf(skatPower);
                }
                else strTemp[8]="NA";

                simuOne.Output(new UpdateUI(2, strTemp, testIndex));  //0:othter  1:snp-table  2:gene-table

            }//simulationTime finished!            

            //Add Ponts.
            //Please take care of the situation when visiting it together.!!!!!!!!
            simuOne.lstPoint.add(new DrawPoint(ors, freq, (riskNum - simuOne.riskNumStart) / simuOne.riskNumStep, dblLD, genePowerList[0], genePowerList[1], genePowerList[2],skatPower));

            //Set ProgressBar.
            simuOne.intCounting++;
            double dblProgress = simuOne.intCounting * simuOne.dblRatio;
            simuOne.Output(new UpdateUI(3, dblProgress));
            altPopulation.clear();
            System.gc();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return "The program is finished!";
    }
}
