/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.kgg.business.simu;

import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.jet.stat.Probability;
import java.util.ArrayList;
import java.util.List;
import org.cobi.kgg.business.SetBasedTest;
import org.cobi.kgg.business.entity.PValueWeight;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiang Li
 */
public class MetaAnalysis {
    
    public List<Double> lstPValue;
    public double dblMetaPValue;
    
    public List<double[]> lstPValues;
    public double[] dblMetaPValues;
    
//    public int intGeneBasedMethod;
    public int intMetaAnalysis;
    public int intNumOfStudies;
    public int intPValueLength;
            
    public List<Individual> lstIndividual;
    public ArrayList<Integer> altCaseIndex;
    public ArrayList<Integer> altControlIndex;   
    public List<ArrayList<Integer>> lstGroupIndex;
    public List<IntArrayList> lstGroupCaseIndex;
    public List<IntArrayList> lstGroupControlIndex;
    public DoubleMatrix2D dblRSquareMatrix; 
  
    public PValueWeight[] pvwTemp;
    
//    GenetAssociationAnalyzer gaaOne = new GenetAssociationAnalyzer();
    
    public MetaAnalysis(int intMetaAnalysis,int intNumOfStudies, int intPvalueLength){
//        this.intGeneBasedMethod=intGeneBasedMethod;
        this.intMetaAnalysis=intMetaAnalysis;
        this.intNumOfStudies=intNumOfStudies;
        this.intPValueLength=intPvalueLength;
        
        this.lstGroupIndex=new ArrayList<ArrayList<Integer>>();
        this.lstGroupCaseIndex=new ArrayList<IntArrayList>();
        this.lstGroupControlIndex=new ArrayList<IntArrayList>();
        this.lstPValue=new ArrayList<Double>();
        this.lstPValues=new ArrayList<double[]>();         
        
        this.pvwTemp=new PValueWeight[this.intPValueLength];
        for(int i=0;i<pvwTemp.length;i++)    pvwTemp[i]=new PValueWeight();

    }
    
    public void calculateMetaPValue(){
        if(lstPValue.isEmpty())    return;
        double dblChiSquare=0;
        for(int i=0;i<lstPValue.size();i++)    dblChiSquare+=Math.log(lstPValue.get(i)); //The base is e or others? 
        dblChiSquare=dblChiSquare*(-2);
        dblMetaPValue=Probability.chiSquareComplemented(2*lstPValue.size(),dblChiSquare);    
    }
    
    public double calculateMetaPValue(ArrayList<Double> lstPValue){
        if(lstPValue.isEmpty())    return -1;
        double dblChiSquare=0;
        for(int i=0;i<lstPValue.size();i++)    dblChiSquare+=Math.log(lstPValue.get(i)); //The base is e or others? 
        dblChiSquare=dblChiSquare*(-2);
        dblMetaPValue=Probability.chiSquareComplemented(2*lstPValue.size(),dblChiSquare);    
        return dblMetaPValue;
    }    
    
//    public void calculateSNPBasedPValue(){
//        
//        if(lstPValues.isEmpty())    return;
//        int intSNPSize=lstPValues.get(0).length;
//        dblMetaPValues=new double[intSNPSize];
//        
//        for(int i=0;i<intSNPSize;i++){            
//            lstPValue.clear();
//            for(int j=0;j<lstPValues.size();j++)  lstPValue.add(lstPValues.get(j)[i]);
//            calculateMetaPValue();
//            dblMetaPValues[i]=dblMetaPValue;        
//        }       
//    }
    
    public double[] calculateGeneBasedPValue(double p[]){
        double[] pvalue={0,0,0}; //changed

        try {      
            this.refreshPValueWeitght(p);          
            pvalue[0]=SetBasedTest.combineGATESPValuebyNoWeightSimeCombinationTestMyMe(pvwTemp,dblRSquareMatrix.copy(),new int[1]);           
            
            this.refreshPValueWeitght(p);
            pvalue[1]=SetBasedTest.combinePValuebyScaleedFisherCombinationTestCovLogP(pvwTemp,dblRSquareMatrix.copy());   
            
            this.refreshPValueWeitght(p);
            pvalue[2]=SetBasedTest.setPValuebyHystBlock(pvwTemp,dblRSquareMatrix.copy());     
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return pvalue;
    }
    
    public void divideGroup(){
        
        int intCaseSize=altCaseIndex.size()/intNumOfStudies;
        int intControlSize=altControlIndex.size()/intNumOfStudies;
        //LinkedList<Integer> lktMarker;
        
        for(int i=0;i<intNumOfStudies;i++){
            
            ArrayList<Integer> altStudy=new ArrayList();
            IntArrayList ialCaseIndex=new IntArrayList();
            IntArrayList ialControlIndex=new IntArrayList();
            
            for(int j=0;j<intCaseSize;j++){
                int intIndex=altCaseIndex.get(i*intCaseSize+j);
                altStudy.add(intIndex);
                ialCaseIndex.add(intIndex);
            }
            
            for(int j=0;j<intControlSize;j++){ 
                int intIndex=altControlIndex.get(i*intControlSize+j);
                altStudy.add(intIndex);
                ialControlIndex.add(intIndex);
            }
            
            lstGroupIndex.add(altStudy);
            lstGroupCaseIndex.add(ialCaseIndex);
            lstGroupControlIndex.add(ialControlIndex);
            
//            HashSet<Integer> hstMapCase=new HashSet();
//            HashSet<Integer> hstMapControl=new HashSet();
//            generateRandomNumber(0,ialCaseIndex.size()-1,intCaseSize,hstMapCase);
//            generateRandomNumber(0,ialControlIndex.size()-1,intControlSize,hstMapControl);
//            
//            ArrayList<Integer> altStudy=new ArrayList();
//            ArrayList<Integer> altIndex=new ArrayList();
//            //lktMarker=new LinkedList(ialCaseIndex);
//            for(Iterator<Integer> itr=hstMapCase.iterator();itr.hasNext();){
//                int intTemp=itr.next();   
//                altIndex.add(intTemp);
//                altStudy.add(ialCaseIndex.get(intTemp));
//                //lktMarker.remove(intTemp);
//            }
//            //altCaseIndex=new ArrayList(lktMarker);
//            for(int j=0;j<altIndex.size();j++){
//                int temp=altIndex.get(j);
//                ialCaseIndex.remove(temp);               
//            }
//            altIndex.clear();
//            
//            for(Iterator<Integer> itr=hstMapControl.iterator();itr.hasNext();){
//                int intTemp=itr.next();
//                altIndex.add(intTemp);
//                altStudy.add(ialControlIndex.get(intTemp));
//            }
//            for(int j=0;j<altIndex.size();j++)  ialControlIndex.remove(altIndex.get(j));
//            altIndex.clear();           
//            lstGroupIndex.add(altStudy);    
        }
    }
  
//    public void generateRandomNumber(int intMin, int intMax,int intNum, HashSet<Integer> hstMap){
//        if(intNum>(intMax-intMin+1) || intMax<intMin)   return;
//        for(int i=0;i<intNum;i++){
//            int intRandom=(int) (Math.random()*(intMax-intMin))+intMin;
//            hstMap.add(intRandom);
//        }
//        int intHashSize=hstMap.size();
//        if(intHashSize<intNum)  generateRandomNumber(intMin,intMax,intNum-intHashSize,hstMap);//This may be danguous when intNum is smaller. 
//    }
    
//    public void performMetaAnalysis() throws Exception{
//        divideGroup();
//        if(intMetaAnalysis==1){
//            //snp-based situation
//            for(int i=0;i<lstGroupIndex.size();i++){
//                ArrayList<Integer> altIndex=lstGroupIndex.get(i);
//                List<Individual> lstGroupedIndividual=new ArrayList<Individual>();
//                for(int j=0;j<altIndex.size();j++)  lstGroupedIndividual.add(lstIndividual.get(altIndex.get(j)));
//                double[] dblSNPPValue=gaaOne.allelicAssociationTest(lstGroupedIndividual);
//                lstPValues.add(dblSNPPValue);
//            }
//            calculateSNPBasedPValue();
//        }else{
//            //gene-based situation
//            for(int i=0;i<lstGroupIndex.size();i++){
//                ArrayList<Integer> altIndex=lstGroupIndex.get(i);
//                List<Individual> lstGroupedIndividual = new ArrayList<Individual>();
//                for(int j=0;j<altIndex.size();j++)  lstGroupedIndividual.add(lstIndividual.get(altIndex.get(j)));
//                double[] dblSNPPValue=gaaOne.allelicAssociationTest(lstGroupedIndividual);
//                double dblGenePValue=calculateGeneBasedPValue(dblSNPPValue);
//                lstPValue.add(dblGenePValue);               
//            }
//            calculateMetaPValue();
//        }
//    }
    
    public void setIndividual(List<Individual> lstIndividual) {
        this.lstIndividual = lstIndividual;
    }

    public void setCaseIndex(IntArrayList ialCaseIndex) {
        this.altCaseIndex = ialCaseIndex.toList();
    }

    public void setControlIndex(IntArrayList ialControlIndex) {
        this.altControlIndex = ialControlIndex.toList();
        
    }    
//    
//    public double getMetaPValue() {
//        return dblMetaPValue;
//    }  
//
//    public double[] getMetaPValues() {
//        return dblMetaPValues;
//    }
//
    public void setRSquareMatrix(double[][] rsqureMatrix) {
        this.dblRSquareMatrix=new DenseDoubleMatrix2D(rsqureMatrix);
    // System.out.println(dblRSquareMatrix.toString());
    }
//    
//    public ArrayList<Integer> getStudyIndex(int i){
//        return lstGroupIndex.get(i);
//    }
//    
    public ArrayList<Individual> getStudyIndividual(int i){
        ArrayList<Individual> altIndividual=new ArrayList();
        ArrayList<Integer> altIndex=lstGroupIndex.get(i);
        for(int j=0;j<altIndex.size();j++)    altIndividual.add(lstIndividual.get(altIndex.get(j)));
        return altIndividual;    
    }
    
    public IntArrayList getStudyCaseIndex(int i){
        return this.lstGroupCaseIndex.get(i);
    }
    
    public IntArrayList getStudyControlIndex(int i){
        return this.lstGroupControlIndex.get(i);
    }
    
    public void refreshPValueWeitght(double[] p){
        for(int i=0;i<pvwTemp.length;i++){
            pvwTemp[i].pValue=p[i];
            pvwTemp[i].physicalPos=i;
            pvwTemp[i].weight=1;
        }     
    }
}
